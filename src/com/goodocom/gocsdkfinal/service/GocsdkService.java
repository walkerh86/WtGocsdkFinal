package com.goodocom.gocsdkfinal.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.goodocom.gocsdk.IGocsdkCallback;
import com.goodocom.gocsdk.SerialPort;
import com.goodocom.gocsdkfinal.Commands;
import com.goodocom.gocsdkfinal.Config;
import com.goodocom.gocsdkfinal.GocsdkSettings;
import com.goodocom.gocsdkfinal.Ringer;
import com.goodocom.gocsdkfinal.activity.CallActivity;
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
import android.telephony.PhoneNumberUtils;
import android.util.Log;

import android.text.TextUtils;
import android.database.sqlite.SQLiteDatabase;

public class GocsdkService extends Service {
	public static final String TAG = "GocsdkService";
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

	@Override
	public void onCreate() {
		callbacks = new RemoteCallbackList<IGocsdkCallback>();
		hand = handler;
		parser = new CommandParser(callbacks,this);
		handler.sendEmptyMessage(MSG_START_SERIAL);
		super.onCreate();
		
		mSettings = GocsdkSettings.getInstance(this);		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		System.out.println("服务启动了");
		isBehind = true;
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		running = false;
		callbacks.kill();
		super.onDestroy();
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
				
				initSerial();
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
				initSerialSettings();
			}
		};
	};

	@Override
	public IBinder onBind(Intent intent) {
		isBehind = false;
		return new GocsdkServiceImp(this);
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
	
	private void initSerial(){
		if(mSettings.isOpen()){
			//should try more times
			write(Commands.BT_OPEN);
		}
	}
	
	private void initSerialSettings(){
		Log.i("hcj.serial","initSerialSettings isOpen="+mSettings.isOpen());
		//if(mSettings.isOpen()){
			write(mSettings.isAutoConnect() ? Commands.SET_AUTO_CONNECT_ON_POWER : Commands.UNSET_AUTO_CONNECT_ON_POWER);
			write(mSettings.isAutoAnswer() ? Commands.SET_AUTO_ANSWER : Commands.UNSET_AUTO_ANSWER);
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
	public static final int MSG_IND_HFP_CONNECTED = 17;
	public static final int MSG_IND_HFP_DISCONNECTED = 17;
	
	public static String mLocalName = null;
	public static String mPinCode = null;

	private String mIncomingNumber;

	private void startInCallActivity(String phonenum){
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
	}

	private void startOutCallActivity(String phoneNumber2, boolean isConnect) {
		if(!isConnect){
			placeCall(phoneNumber2);
		}
		Intent intent = new Intent(this, CallActivity.class);
		intent.putExtra("callNumber", phoneNumber2);
		intent.putExtra("isConnect", isConnect);
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
		startActivity(intent);
	}

	private void placeCall(String mLastNumber) {
		if (mLastNumber.length() == 0)
			return;
		if (PhoneNumberUtils.isGlobalPhoneNumber(mLastNumber)) {
			// place the call if it is a valid number
			if (mLastNumber == null || !TextUtils.isGraphic(mLastNumber)) {
				// There is no number entered.
				return;
			}
			write(Commands.DIAL+mLastNumber);
		}
	}

	private void callhangUp(){
		Handler handler = InComingActivity.getHandler();
		if(handler != null){
			handler.sendEmptyMessage(InComingActivity.MSG_INCOMINNG_HANGUP);
		}
		handler = CallActivity.getHandler();
		if(handler != null){
			handler.sendEmptyMessage(CallActivity.MSG_INCOMING_HANGUP);
		}
	}

	private void callConnected(){
		Handler handler = InComingActivity.getHandler();
		if(handler != null){
			handler.sendEmptyMessage(InComingActivity.MSG_INCOMINNG_HANGUP);
			write(Commands.ACCEPT_INCOMMING);
			startOutCallActivity(mIncomingNumber, true);
			return;
		}
		handler = CallActivity.getHandler();
		if (handler != null) {
			handler.sendEmptyMessage(CallActivity.Msg_CONNECT);
		}
	}
	
}
