package com.goodocom.gocsdkfinal.activity;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.Ringer;
import com.goodocom.gocsdkfinal.domain.BlueToothPairedInfo;
import com.goodocom.gocsdkfinal.service.GocsdkServiceHelper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.RemoteException;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

public class InComingActivity extends Activity implements OnClickListener {
	public static final int MSG_INCOMINNG_HANGUP = 0;
	public static final int MSG_INCOMING_CONNECTION = 1;
	public static final int MSG_INCOMING_ANSWER = 2;
	
	private ImageView iv_connect;
	private ImageView iv_hangup;
	private TextView tv_incoming_phonenumber;
	private String incomingNumber;
	
	private PowerManager.WakeLock mWakeLock;
	private Ringer mRinger;
	
	private static Handler hand = null;
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_INCOMINNG_HANGUP:
				finish();
				break;
			case MSG_INCOMING_CONNECTION:
				if(incomingNumber!=null){
					System.out.println("来电了，你到底接没接");
					connectInComing(incomingNumber);
				}
				break;
			case MSG_INCOMING_ANSWER:
				//try {
				mGocsdkServiceHelper.phoneAnswer();
				//} catch (RemoteException e) {
					//e.printStackTrace();
				//}
				break;
			}
		};
	};

	public static Handler getHandler() {
		return hand;
	}

	private GocsdkServiceHelper mGocsdkServiceHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGocsdkServiceHelper = new GocsdkServiceHelper(null);
		mGocsdkServiceHelper.bindService(this);
		
		getWindow().addFlags(
		        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
		        | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
		        | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
		        | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		        
		setContentView(R.layout.activity_incoming);
		Intent intent = getIntent();
		incomingNumber = intent.getStringExtra("incomingNumber");
		tv_incoming_phonenumber = (TextView)findViewById(R.id.tv_incoming_phonenumber);
		tv_incoming_phonenumber.setText(incomingNumber);
		iv_connect = (ImageView) findViewById(R.id.iv_connect);
		iv_hangup = (ImageView) findViewById(R.id.iv_hangup);
		iv_connect.setOnClickListener(this);
		iv_hangup.setOnClickListener(this);
		hand = handler;
		
		mRinger = Ringer.init(this);
		if(!mRinger.isRinging()){
			mRinger.ring();
		}
	}
	
	@Override
    protected void onResume() {
		super.onResume();
		mIsForegroundActivity = true;
	}
	
	@Override
    protected void onPause() {
		super.onPause();

        mIsForegroundActivity = false;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		hand = null;
		if(mRinger.isRinging()){
			mRinger.stopRing();
		}
		mGocsdkServiceHelper.unbindService(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_connect:
			//try {
			mGocsdkServiceHelper.phoneAnswer();
			//} catch (RemoteException e) {
				//e.printStackTrace();
			//}
			break;
		case R.id.iv_hangup:
			hangupInComing();
			break;

		}
	}

	private void hangupInComing() {
		//try {
		mGocsdkServiceHelper.phoneHangUp();
		//} catch (RemoteException e) {
			//e.printStackTrace();
		//}
		finish();
	}

	private void connectInComing(String incomingNumber2) {
		//try {
		mGocsdkServiceHelper.phoneAnswer();
		//} catch (RemoteException e) {
			//e.printStackTrace();
		//}
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(
				"com.goodocom.gocsdkfinal",
				"com.goodocom.gocsdkfinal.activity.CallActivity"));
		intent.putExtra("incomingNumber", incomingNumber2);
		intent.putExtra("isConnect", true);
		startActivity(intent);
		finish();
	}
	
	private boolean mIsForegroundActivity;
    public boolean isForegroundActivity() {
    	return mIsForegroundActivity;
    }
}
