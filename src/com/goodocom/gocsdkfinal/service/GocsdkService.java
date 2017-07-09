package com.goodocom.gocsdkfinal.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.goodocom.gocsdk.IGocsdkCallback;
import com.goodocom.gocsdk.IGocsdkServiceSimple;
import com.goodocom.gocsdk.SerialPort;
import com.goodocom.gocsdkfinal.Commands;
import com.goodocom.gocsdkfinal.Config;
import com.goodocom.gocsdkfinal.GocsdkSettings;
import com.goodocom.gocsdkfinal.Ringer;
import com.goodocom.gocsdkfinal.activity.CallActivity;
import com.goodocom.gocsdkfinal.activity.ChooseCallActivity;
import com.goodocom.gocsdkfinal.activity.InComingActivity;
import com.goodocom.gocsdkfinal.db.Database;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.telecom.TelecomManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.util.Log;

import android.text.TextUtils;
import android.database.sqlite.SQLiteDatabase;

public class GocsdkService extends Service {
	public static final String TAG = "hcj.serial";
	public static final int MSG_START_SERIAL = 1;//串口
	public static final int MSG_SERIAL_RECEIVED = 2; //接收到串口信息
	private static final int RESTART_DELAY = 2000; // ms
	//命令解析
	private CommandParser parser;
	//是否使用socket
	private final boolean use_socket = false;
	//串口线程
	private SerialThread serialThread = null;
	private volatile boolean running = true;
	private RemoteCallbackList<IGocsdkCallback> callbacks;
	
	public static boolean isBehind = false;

	private GocsdkSettings mSettings;
	private static GocsdkService mGocsdkService;
	
	private IGocsdkServiceSimple.Stub mIGocsdkServiceSimple = new IGocsdkServiceSimple.Stub(){
		@Override
		public void sendCommand(String cmd) throws RemoteException {
			Log.i(TAG, "sendCommand:"+cmd);
			write(cmd);
		}

		@Override
		public void registerCallback(IGocsdkCallback callback) throws RemoteException {
			GocsdkService.this.registerCallback(callback);
		}
		
		@Override
		public void unregisterCallback(IGocsdkCallback callback) throws RemoteException {
			GocsdkService.this.unregisterCallback(callback);
		}

		@Override
		public void setBtSwitch(boolean open) throws RemoteException {
			GocsdkService.this.setBtSwitch(open);
		}

		@Override
		public boolean isBtOpen() throws RemoteException {
			return GocsdkService.this.isOpened();
		}

		@Override
		public boolean isBtConnected() throws RemoteException {
			return GocsdkService.this.isConnected();
		}

		@Override
		public void dial(String number) throws RemoteException {
			GocsdkService.this.placeCall(number);
		}

		@Override
		public boolean isInCall() throws RemoteException {
			// TODO Auto-generated method stub
			return GocsdkService.this.isInCall();
		}

		@Override
		public void endCall() throws RemoteException {
			GocsdkService.this.endCall();
		}

		@Override
		public void acceptCall() throws RemoteException {
			GocsdkService.this.acceptCall();
		}		
	};
	
	public static GocsdkService getInstance(){
		return mGocsdkService;
	}

	@Override
	public void onCreate() {
		callbacks = new RemoteCallbackList<IGocsdkCallback>();
		hand = handler;
		parser = new CommandParser(callbacks,this);
		handler.sendEmptyMessage(MSG_START_SERIAL);
		super.onCreate();
		
		mSettings = GocsdkSettings.getInstance(this);
		mGocsdkService = this;
		Log.i("hcj.GocsdkExtService","onCreate mGocsdkService="+mGocsdkService);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//System.out.println("服务启动了");
		isBehind = true;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		running = false;
		callbacks.kill();
		super.onDestroy();
		
		mGocsdkService = null;
		Log.i("hcj.GocsdkExtService","onDestroy mGocsdkService="+mGocsdkService);
	}


	private static Handler hand = null;
	public static Handler getHandler(){
		return hand;
	}
	private Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == MSG_START_SERIAL) {
				serialThread = new SerialThread();
				serialThread.start();
				
				this.postDelayed(new Runnable(){
					@Override
					public void run() {
						queryBtState();
					}					
				},100);
			} else if (msg.what == MSG_SERIAL_RECEIVED) {
				byte[] data = (byte[]) msg.obj;
				parser.onBytes(data);
			}
			
			else if(msg.what == MSG_IND_INCOMING){
				startInCallActivity((String) msg.obj);
			}else if(msg.what == MSG_IND_HANG_UP){
				callhangUp();
			}else if(msg.what == MSG_IND_OUTGOING_TALKING_NUMBER){
				startOutCallActivity((String) msg.obj, false);
			}else if(msg.what == MSG_IND_TALKING){
				callConnected();
			}else if(msg.what == MSG_IND_CURRENT_DEVICE_NAME){
				mLocalName = (String) msg.obj;
			}else if(msg.what == MSG_IND_CURRENT_PIN_CODE){
				mPinCode = (String) msg.obj;
			}else if(msg.what == MSG_IND_INIT_SUCCEED){
				setBtState(BT_STATE_ON);
			}else if(msg.what == MSG_IND_STOP_DISCOVERY){
				setBtState(BT_STATE_OFF);
			}else if(msg.what == MSG_IND_HFP_CONNECTED){
				mConnected = true;
				notifyConnectState(mConnected);
			}else if(msg.what == MSG_IND_HFP_DISCONNECTED){
				mConnected = false;
				notifyConnectState(mConnected);
			}else if(msg.what == MSG_IND_HFP_STATUS){
				int hfpStatus = msg.arg1;
				Log.i("hcj.serial", "MSG_IND_HFP_STATUS hfpStatus="+hfpStatus);
				mConnected = (hfpStatus > 0);
				notifyConnectState(mConnected);
			}
		};
	};

	@Override
	public IBinder onBind(Intent intent) {
		isBehind = false;
		//return new GocsdkServiceImp(this);
		return mIGocsdkServiceSimple;
	}
	@Override
	public boolean onUnbind(Intent intent) {
		isBehind = true;		
		return super.onUnbind(intent);
	}
	class SerialThread extends Thread {
		private InputStream inputStream;
		private OutputStream outputStream = null;
		private byte[] buffer = new byte[1024];
		public void write(byte[] buf) {
			if (outputStream != null) {
				try {
					outputStream.write(buf);
				} catch (IOException e) {
					
				}
			}
		}
		
		public SerialThread() {
		
		}

		@Override
		public void run() {
			LocalSocket client = null;
			SerialPort serial = null;
			
			int n;
			try {
				if(use_socket){
					client = new LocalSocket();
					client.connect(new LocalSocketAddress(Config.SERIAL_SOCKET_NAME,LocalSocketAddress.Namespace.RESERVED));
					inputStream = client.getInputStream();
					outputStream = client.getOutputStream();
				}else{
					serial = new SerialPort(new File("/dev/BT_serial"),115200,0);
					inputStream = serial.getInputStream();
					outputStream = serial.getOutputStream();
				}
				while (running) {
					n = inputStream.read(buffer);
					if (n < 0) {
						if(use_socket ){
							if(client != null)client.close();
						}else{
							if(serial != null)serial.close();
						}
						throw new IOException("n==-1");
					}
					
					byte[] data = new byte[n];
					System.arraycopy(buffer, 0, data, 0, n);
					if(handler == null){
						//Log.i(TAG, "thread exit by handle null");
						//break;
					}
					handler.sendMessage(handler.obtainMessage(
							MSG_SERIAL_RECEIVED, data));
				}
			} catch (IOException e) {
				try {
					if(use_socket){
						if(client != null)client.close();
					}else{
						if(serial != null)serial.close();
					}
				} catch (IOException e1) {
					//e1.printStackTrace();
				}
				handler.sendEmptyMessageDelayed(MSG_START_SERIAL, RESTART_DELAY);
				return;
			}
			
			try {
				if(use_socket){
					if(client != null)client.close();
				}else{
					if(serial != null)serial.close();
				}
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}

	public void write(String str) {
		if (serialThread == null) return;
		Log.i("hcj.serial","write cmd:"+str);
		serialThread.write((Commands.COMMAND_HEAD + str + "\r\n").getBytes());
	}

	public void registerCallback(IGocsdkCallback callback) {
		//Log.d("goc", "GocsdkService registerCallback");
		callbacks.register(callback);
	}

	public void unregisterCallback(IGocsdkCallback callback) {
		//Log.d("goc", "GocsdkService unregisterCallback");
		callbacks.unregister(callback);
	}
	
	private void queryBtState(){
		Log.i("hcj.serial", "queryBtState");
		//if(mBtState == BT_STATE_UNKOWN){
			write("QS");
		//}
	}
	
	private void setBtState(int state){
		Log.i("hcj.serial", String.format("setBtState prev=%d,now=%d",mBtState,state));
		if(mBtState != state){
			mBtSwitching = false;
			
			if(mBtState == BT_STATE_UNKOWN){
				boolean isBtSettingsOn = mSettings.isOpen();
				if(isBtSettingsOn && (state == BT_STATE_OFF)){
					mBtState = state;
					Log.i("hcj.serial", "init open bt");
					write(Commands.BT_OPEN);
					return;
				}else if(!isBtSettingsOn && (state == BT_STATE_ON)){
					mBtState = state;
					Log.i("hcj.serial", "init close bt");
					write(Commands.BT_CLOSE);
					return;
				}
			}
			mBtState = state;
			notifyOpenState();
			if(isOpened()){
				initSerialSettings();
			}
		}
	}
		
	private void initSerialSettings(){
		Log.i("hcj.serial","initSerialSettings isOpen="+mSettings.isOpen());
		//if(mSettings.isOpen()){
			write(mSettings.isAutoConnect() ? Commands.SET_AUTO_CONNECT_ON_POWER : Commands.UNSET_AUTO_CONNECT_ON_POWER);
			write(mSettings.isAutoAnswer() ? Commands.SET_AUTO_ANSWER : Commands.UNSET_AUTO_ANSWER);
			write(Commands.INQUIRY_HFP_STATUS);
			write(Commands.MUSIC_UNMUTE);
			write(Commands.MODIFY_LOCAL_NAME);
			write(Commands.MODIFY_PIN_CODE);
		//}
	}

	public static final int MSG_IND_INCOMING = 10;
	public static final int MSG_IND_HANG_UP = 11;
	public static final int MSG_IND_OUTGOING_TALKING_NUMBER = 12;
	public static final int MSG_IND_TALKING = 13;
	public static final int MSG_IND_CURRENT_DEVICE_NAME = 14;
	public static final int MSG_IND_CURRENT_PIN_CODE = 15;
	public static final int MSG_IND_INIT_SUCCEED = 16;
	public static final int MSG_IND_STOP_DISCOVERY = 17;
	public static final int MSG_IND_HFP_CONNECTED = 18;
	public static final int MSG_IND_HFP_DISCONNECTED = 19;
	public static final int MSG_IND_HFP_STATUS = 20;
	
	public static String mLocalName = null;
	public static String mPinCode = null;

	private String mIncomingNumber;

	private void startInCallActivity(String phonenum){
		if(isPhoneInUse()){
			showChooseCall(phonenum);
			return;
		}
		
		mIncomingNumber = phonenum;
		
		String phonename = "";
		SQLiteDatabase mDbDataBase = Database.getSystemDb();
		Database.createTable(mDbDataBase,
				Database.Sql_create_phonebook_tab);
		phonename = Database.queryPhoneName(mDbDataBase,
				Database.PhoneBookTable, phonenum);// 根据号码查询联系人
		Intent intent = new Intent(this,InComingActivity.class);
		if (TextUtils.isEmpty(phonename)) {
			intent.putExtra("incomingNumber", phonenum);
		} else {
			intent.putExtra("incomingNumber", phonename);
		}
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
		
		mCallState = BT_CALL_STATE_INCOMING;
	}

	public void startOutCallActivity(String phoneNumber2, boolean isConnect) {
		if(!isConnect){
			placeCall(phoneNumber2);
		}
		Intent intent = new Intent(this, CallActivity.class);
		intent.putExtra("callNumber", phoneNumber2);
		intent.putExtra("isConnect", isConnect);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	public void placeCall(String mLastNumber) {
		Log.i("hcj.GocsdkExtService", "placeCall mLastNumber="+mLastNumber);
		if (mLastNumber.length() == 0)
			return;
		if (PhoneNumberUtils.isGlobalPhoneNumber(mLastNumber)) {
			// place the call if it is a valid number
			if (mLastNumber == null || !TextUtils.isGraphic(mLastNumber)) {
				// There is no number entered.
				return;
			}
			write(Commands.DIAL+mLastNumber);
			
			mCallState = BT_CALL_STATE_DIALING;
		}
	}

	private void callhangUp(){
		mCallState = BT_CALL_STATE_IDLE;
		
		Handler tmphandler = InComingActivity.getHandler();
		if(tmphandler != null){
			tmphandler.sendEmptyMessage(InComingActivity.MSG_INCOMINNG_HANGUP);
		}
		tmphandler = CallActivity.getHandler();
		if(tmphandler != null){
			tmphandler.sendEmptyMessage(CallActivity.MSG_INCOMING_HANGUP);
		}
	}

	private void callConnected(){
		mCallState = BT_CALL_STATE_INCALL;
		
		Handler tmphandler = InComingActivity.getHandler();
		if(tmphandler != null){
			tmphandler.sendEmptyMessage(InComingActivity.MSG_INCOMINNG_HANGUP);
			write(Commands.ACCEPT_INCOMMING);
			startOutCallActivity(mIncomingNumber, true);
			return;
		}
		tmphandler = CallActivity.getHandler();
		if (tmphandler != null) {
			tmphandler.sendEmptyMessage(CallActivity.Msg_CONNECT);
		}
	}
	
	private boolean mBtSwitching = false;
	public void setBtSwitch(boolean open){
		Log.i("hcj.serial", "setBtSwitch mBtSwitching="+mBtSwitching);
		if(mBtSwitching){
			return;
		}
		mBtSwitching = true;
		write(open ? Commands.BT_OPEN : Commands.BT_CLOSE);
	}

	private static final int BT_STATE_UNKOWN = -1;
	private static final int BT_STATE_OFF = 0;
	private static final int BT_STATE_ON = 1;
	private int mBtState = BT_STATE_UNKOWN;
	public boolean isOpened(){
		Log.i(GocsdkExtService.TAG, "isConnected mBtState="+mBtState);
		return mBtState == BT_STATE_ON;
	}

	private void notifyOpenState(){
		Log.i("hcj.serial", "notifyOpenState state="+mBtState);
		
		Intent intent = new Intent("com.goodocom.gocsdk.open_state");
		intent.putExtra("opened", isOpened());
		this.sendBroadcast(intent);
	}
	
	private boolean mConnected = false;
	public boolean isConnected(){
		Log.i(GocsdkExtService.TAG, "isConnected mConnected="+mConnected);
		return mConnected;
	}
	
	private void notifyConnectState(boolean connected){
		Log.i("hcj.serial", "notifyConnectState connected="+connected);
		Intent intent = new Intent("com.goodocom.gocsdk.connect_state");
		intent.putExtra("connected", connected);
		this.sendBroadcast(intent);
	}
	
	private static final int BT_CALL_STATE_IDLE = 0;
	private static final int BT_CALL_STATE_INCOMING = 1;
	private static final int BT_CALL_STATE_DIALING = 2;
	private static final int BT_CALL_STATE_INCALL = 3;
	private int mCallState = BT_CALL_STATE_IDLE;
	public boolean isInCall(){
		Log.i(GocsdkExtService.TAG, "isInCall mCallState="+mCallState);
		return mCallState == BT_CALL_STATE_INCOMING
				|| mCallState == BT_CALL_STATE_DIALING
                || mCallState == BT_CALL_STATE_INCALL;
	}
	
	public void endCall(){
		Log.i(GocsdkExtService.TAG, "endCall mCallState="+mCallState);
		if(mCallState == BT_CALL_STATE_INCOMING){
			Handler tmphandler = InComingActivity.getHandler();
			if(tmphandler != null){
				tmphandler.sendEmptyMessage(InComingActivity.MSG_INCOMINNG_HANGUP);
			}else{
				write(Commands.REJECT_INCOMMMING);
			}
		}else if(mCallState == BT_CALL_STATE_DIALING || mCallState == BT_CALL_STATE_INCALL){
			Handler tmphandler = CallActivity.getHandler();
			if(tmphandler != null){
				tmphandler.sendEmptyMessage(CallActivity.MSG_HANGUP);
			}else{
				write(Commands.REJECT_INCOMMMING);
			}
		}
	}
	
	public void acceptCall(){
		Log.i(GocsdkExtService.TAG, "acceptCall mCallState="+mCallState);
		if(mCallState == BT_CALL_STATE_INCOMING){
			Handler Handler  = InComingActivity.getHandler();
			if(Handler  != null){
				Handler .sendEmptyMessage(InComingActivity.MSG_INCOMING_ANSWER);
			}
		}
	}

//local interface @{	
	public String getInCallNumber(){
		return "15625255162";
	}
	
	public String getInCallName(){
		return "Hello";
	}
	
	public void rejectIncoming(){
		
	}
//local interface @}	
	
	private void showChooseCall(String number){
		Log.i(TAG, "showChooseCall");
		Intent intent = new Intent(this,ChooseCallActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		intent.putExtra("number", number);
		String name = getContactsName(number);
		if(name != null){
			intent.putExtra("name", name);
		}
		this.startActivity(intent);
	}
	
	private String getContactsName(String number){
		SQLiteDatabase mDbDataBase = Database.getSystemDb();
		Database.createTable(mDbDataBase,
				Database.Sql_create_phonebook_tab);
		String name = Database.queryPhoneName(mDbDataBase,
				Database.PhoneBookTable, number);
		return name;
	}
	
	private TelecomManager getTelecomManager() {
        return (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
    }
	
	private boolean isPhoneInUse() {
        return getTelecomManager().isInCall();
    }
	
	public void endLocalCall(){
		getTelecomManager().endCall();
	}
	
}
