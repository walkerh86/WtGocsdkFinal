package com.goodocom.gocsdkfinal.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.goodocom.gocsdk.IGocsdkCallback;
import com.goodocom.gocsdk.SerialPort;
import com.goodocom.gocsdkfinal.Commands;
import com.goodocom.gocsdkfinal.Config;
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

	private PowerManager.WakeLock mWakeLock;

	@Override
	public void onCreate() {
		callbacks = new RemoteCallbackList<IGocsdkCallback>();
		hand = handler;
		parser = new CommandParser(callbacks,this);
		handler.sendEmptyMessage(MSG_START_SERIAL);
		super.onCreate();

            PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
            mWakeLock = powerManager.newWakeLock(PowerManager.FULL_WAKE_LOCK |PowerManager.ON_AFTER_RELEASE, "incoming");
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
			} else if (msg.what == MSG_SERIAL_RECEIVED) {
				byte[] data = (byte[]) msg.obj;
				parser.onBytes(data);
			}else if(msg.what == MSG_CALL_INCOMING){
				startInCallActivity((String) msg.obj);
			}else if(msg.what == MSG_CALL_HUANGUP){
				
			}
		};
	};

	@Override
	public IBinder onBind(Intent intent) {
		System.out.println("服务绑定了");
		isBehind = false;
		return new GocsdkServiceImp(this);
	}
	@Override
	public boolean onUnbind(Intent intent) {
		isBehind = true;
		System.out.println("服务解绑了");
		
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


	public static final int MSG_CALL_INCOMING = 10;
	public static final int MSG_CALL_HUANGUP = 11;

	private void startInCallActivity(String phonenum){
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
		
		//mWakeLock.acquire();
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
		/*
		if(mWakeLock.isHeld()){
			mWakeLock.release();
		}*/
	}
	
}
