package com.goodocom.gocsdkfinal.service;

import com.goodocom.gocsdk.IGocsdkCallback;
import com.goodocom.gocsdk.IGocsdkExt;
import com.goodocom.gocsdk.IGocsdkService;
import com.goodocom.gocsdkfinal.GocsdkSettings;
import com.goodocom.gocsdkfinal.R;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

public class GocsdkExtService extends Service{
	public static final String TAG = "hcj.GocsdkExtService";
	private GocsdkSettings mSettings;
	//private Handler mHandler = new Handler();
	
	private MyConnection mMyConnection;
	private GocsdkService mGocsdkService;
	
	private class MyConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mGocsdkService = GocsdkService.getInstance();
			Log.i(TAG,"onServiceConnected mGocsdkService="+mGocsdkService);
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mGocsdkService = null;
		}
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
		Log.i(TAG, "onCreate");
		Intent service = new Intent(this,GocsdkService.class);
		mMyConnection = new MyConnection();
		this.bindService(service, mMyConnection, Context.BIND_AUTO_CREATE);
		
		mSettings = GocsdkSettings.getInstance(this);
		
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i(TAG, "onStartCommand");
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return mBinder;
	}
	
	private IGocsdkExt.Stub mBinder = new IGocsdkExt.Stub(){
		
		@Override
		public void setBtSwitch(boolean open) throws RemoteException {
			Log.i(TAG, "setBtSwitch mGocsdkService="+mGocsdkService);
			if(mGocsdkService != null){
				mGocsdkService.setBtSwitch(open);
			}
		}

		@Override
		public boolean isBtOpen() throws RemoteException {
			return (mGocsdkService != null) ? mGocsdkService.isOpened() : false;
			//return mSettings.isOpen();
		}

		@Override
		public boolean isBtConnected() throws RemoteException {
			return (mGocsdkService != null) ? mGocsdkService.isConnected() : false;
		}

		@Override
		public void dial(String number) throws RemoteException {
			if(mGocsdkService == null){
				return;
			}
			mGocsdkService.placeCall(number);
		}
		
		@Override
		public boolean isInCall() throws RemoteException {
			if(mGocsdkService == null){
				return false;
			}
			return mGocsdkService.isInCall();
		}
		
		@Override
		public void endCall() throws RemoteException {
			if(mGocsdkService == null){
				return;
			}
			mGocsdkService.endCall();
		}
		
		@Override
		public void acceptCall() throws RemoteException {
			if(mGocsdkService == null){
				return;
			}
			mGocsdkService.acceptCall();
		}

	};
}