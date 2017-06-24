package com.goodocom.gocsdkfinal.activity;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.db.Database;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;


import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.SystemClock;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class CallActivity extends Activity implements OnClickListener {
	public static final int MSG_CALL_STATUS = 0;// 鎷ㄥ嚭鐢佃瘽
	public static final int Msg_CONNECT = 1;// 鎺ラ��
	public static final int MSG_INCOMING_HANGUP = 2;// 鎷掓帴
	private RelativeLayout rl_call_pager;
	private RelativeLayout rl_connect;
	private TextView tv_call_people_name;
	private Chronometer chronometer;
	private TextView tv_connection_info;
	private LinearLayout ll_number;
	private ImageView iv_number;
	private ImageView iv_guaduan;
	private ImageView iv_qieshengdao;
	private ImageView iv_bujingyin;

	// flag
	private boolean isShowNumber = false;
	//private boolean isPhoneConnection = false;
	//private boolean isInComing = false;
	private boolean volume_flag = false;
	private boolean isMute = false;
	private boolean isConnect = false;
	
	private String callNumber;//鍓嶅彴鎷ㄥ彿
	//private String calloutNumber;//鍚庡彴鎷ㄥ彿
	
	private static Handler hand = null;
	private Handler handler = new Handler() {
		private String phoneNumber;

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_CALL_STATUS:
				phoneNumber = (String) msg.obj;
				callOut(phoneNumber);
				break;
			case MSG_INCOMING_HANGUP:
				System.out.println("handler------");
				chronometer.stop();
				finish();
				break;
			case Msg_CONNECT:
				System.out.println("callNumber="+callNumber);
				if(callNumber!=null){
					//System.out.println("鎴戦兘鎷ㄥ嚭鍘讳簡锛屼綘鎺ヤ簡娌℃湁鍝�");
					callConnect(callNumber);
				}else{
					Handler handler2 = InComingActivity.getHandler();
					if(handler2!=null){
						handler2.sendEmptyMessage(InComingActivity.MSG_INCOMING_CONNECTION);
					}
				}
				break;
			}
		};
	};
	private String incomingNumber;
	private ImageView iv_one;
	private ImageView iv_two;
	private ImageView iv_three;
	private ImageView iv_four;
	private ImageView iv_five;
	private ImageView iv_six;
	private ImageView iv_seven;
	private ImageView iv_eight;
	private ImageView iv_nine;
	private ImageView iv_xinghao;
	private ImageView iv_zero;
	private ImageView iv_jinghao;
	public static Handler getHandler() {
		return hand;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_call);
		initView();
		initData();
		isConnect();
		hand = handler;
	}

	private void isConnect() {
		if(isConnect){
			rl_call_pager.setVisibility(View.GONE);
			rl_connect.setVisibility(View.VISIBLE);
			if(callNumber!=null){
				tv_connection_info.setText(callNumber);
			}
			if(incomingNumber!=null){
				tv_connection_info.setText(incomingNumber);
			}
			chronometer.setFormat("%s");
			chronometer.setBase(SystemClock.elapsedRealtime());// 澶嶄綅閿�
			chronometer.start();
			
		}else{
			rl_call_pager.setVisibility(View.VISIBLE);
			rl_connect.setVisibility(View.GONE);
			if(callNumber!=null){
				tv_call_people_name.setText(callNumber);
			}
			/*if(calloutNumber!=null){
				tv_call_people_name.setText(calloutNumber);
				callNumber = null;
			}	*/
		}
	}

	protected void callOut(String phoneNumber) {
		placeCall(phoneNumber);
		rl_call_pager.setVisibility(View.VISIBLE);
		rl_connect.setVisibility(View.GONE);
		tv_call_people_name.setText(phoneNumber);
	}

	protected void callConnect(String callNumber2) {
		rl_call_pager.setVisibility(View.GONE);
		rl_connect.setVisibility(View.VISIBLE);
		
		tv_connection_info.setText(callNumber2);
		
		chronometer.setFormat("%s");
		chronometer.setBase(SystemClock.elapsedRealtime());// 澶嶄綅閿�
		chronometer.start();
	}

	private void initData() {
		Intent intent = getIntent();
		callNumber = intent.getStringExtra("callNumber");
		System.out.println("initData callNumber="+callNumber);
		incomingNumber = intent.getStringExtra("incomingNumber");
		//calloutNumber = intent.getStringExtra("calloutNumber");
		isConnect  = intent.getBooleanExtra("isConnect", false);
		if(!TextUtils.isEmpty(callNumber)){
			SQLiteDatabase database = Database.getSystemDb();
			Database.createTable(database, Database.Sql_create_phonebook_tab);
			String phoneName = Database.queryPhoneName(database,
					Database.PhoneBookTable, callNumber);
			if (TextUtils.isEmpty(phoneName)) {
				tv_call_people_name.setText(callNumber);
				System.out.println("number with no name");
			} else {
				tv_call_people_name.setText(phoneName);
			}
			database.close();
		}
	}

	private void initView() {
		rl_call_pager = (RelativeLayout) findViewById(R.id.rl_call_pager);
		tv_call_people_name = (TextView) findViewById(R.id.tv_call_people_name);

		rl_connect = (RelativeLayout) findViewById(R.id.rl_connect);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		tv_connection_info = (TextView) findViewById(R.id.tv_connection_info);
		
		
		ll_number = (LinearLayout) findViewById(R.id.ll_number);

		iv_number = (ImageView) findViewById(R.id.iv_number);
		iv_guaduan = (ImageView) findViewById(R.id.iv_guaduan);
		iv_qieshengdao = (ImageView) findViewById(R.id.iv_qieshengdao);
		iv_bujingyin = (ImageView) findViewById(R.id.iv_bujingyin);
		
		
		iv_one = (ImageView) findViewById(R.id.iv_one);
		iv_two = (ImageView) findViewById(R.id.iv_two);
		iv_three = (ImageView) findViewById(R.id.iv_three);
		iv_four = (ImageView) findViewById(R.id.iv_four);
		iv_five = (ImageView) findViewById(R.id.iv_five);
		iv_six = (ImageView) findViewById(R.id.iv_six);
		iv_seven = (ImageView) findViewById(R.id.iv_seven);
		iv_eight = (ImageView) findViewById(R.id.iv_eight);
		iv_nine = (ImageView) findViewById(R.id.iv_nine);
		iv_xinghao = (ImageView) findViewById(R.id.iv_xinghao);
		iv_zero = (ImageView) findViewById(R.id.iv_zero);
		iv_jinghao = (ImageView) findViewById(R.id.iv_jinghao);
		
		
		iv_one.setOnClickListener(this);
		iv_two.setOnClickListener(this);
		iv_three.setOnClickListener(this);
		iv_four.setOnClickListener(this);
		iv_five.setOnClickListener(this);
		iv_six.setOnClickListener(this);
		iv_seven.setOnClickListener(this);
		iv_eight.setOnClickListener(this);
		iv_nine.setOnClickListener(this);
		iv_xinghao.setOnClickListener(this);
		iv_zero.setOnClickListener(this);
		iv_jinghao.setOnClickListener(this);

		iv_number.setOnClickListener(this);
		iv_guaduan.setOnClickListener(this);
		iv_qieshengdao.setOnClickListener(this);
		iv_bujingyin.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_number: 
			showNumber();
			break;
		case R.id.iv_guaduan:
			hangUp();
			break;
		case R.id.iv_qieshengdao:
			if (GocsdkCallbackImp.hfpStatus > 0) {
				switchCarAndphone();
			} else {
				Toast.makeText(this, this.getString(R.string.warning_connect), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.iv_bujingyin:
			switchMute();
			break;
		case R.id.iv_one:
			break;
		case R.id.iv_two:
			break;
		case R.id.iv_three:
			break;
		case R.id.iv_four:
			break;
		case R.id.iv_five:
			break;
		case R.id.iv_six:
			break;
		case R.id.iv_seven:
			break;
		case R.id.iv_eight:
			break;
		case R.id.iv_nine:
			break;
		case R.id.iv_xinghao:
			break;
		case R.id.iv_zero:
			break;
		case R.id.iv_jinghao:
			break;
		}
	}

	private void switchMute() {
		MainActivity activity = new MainActivity();
		Handler handler = activity.getHandler();
		isMute = !isMute;
		if(isMute){
			handler.sendEmptyMessage(MainActivity.MSG_SET_MICPHONE_OFF);
			iv_bujingyin.setImageResource(R.drawable.btn_jianpan_bujingyin_selector);
		}else{
			handler.sendEmptyMessage(MainActivity.MSG_SET_MICPHONE_ON);
			iv_bujingyin.setImageResource(R.drawable.btn_jianpan_jingyin_selector);
		}
	}
	// 鍒囨崲澹伴煶鍦ㄨ溅鏈虹涓庢墜鏈虹
		private void switchCarAndphone() {
			volume_flag = !volume_flag;
			if (volume_flag) {// 鎵嬫満绔�
				try {
					MainActivity.getService().phoneTransfer();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				Toast.makeText(this, this.getString(R.string.device_phone), 0).show();
			} else {// 杞︽満绔�
				try {
					MainActivity.getService().phoneTransferBack();
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				Toast.makeText(this, this.getString(R.string.device_car), 0).show();
			}
		}
	private void showNumber() {
		isShowNumber = !isShowNumber;
		if (isShowNumber) {
			ll_number.setVisibility(View.VISIBLE);
		} else {
			ll_number.setVisibility(View.INVISIBLE);
		}
	}

	// 鎸傛柇
	private void hangUp() {
		
		//Toast.makeText(this, "鎸傛柇", 0).show();
		try {
			MainActivity.getService().phoneHangUp();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
	}
	
	// 鎷ㄦ墦姝ｇ‘鐨勭數璇�
		private static void placeCall(String mLastNumber) {
			if (mLastNumber.length() == 0)
				return;
			if (PhoneNumberUtils.isGlobalPhoneNumber(mLastNumber)) {
				if (mLastNumber == null || !TextUtils.isGraphic(mLastNumber)) {
					return;
				}
				try {
					MainActivity.getService().phoneDail(mLastNumber);
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}

}
