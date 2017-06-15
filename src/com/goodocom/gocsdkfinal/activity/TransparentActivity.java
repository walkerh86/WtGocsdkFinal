package com.goodocom.gocsdkfinal.activity;

import com.goodocom.gocsdk.IGocsdkService;
import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.db.Database;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;
import com.goodocom.gocsdkfinal.service.GocsdkService;
import com.goodocom.gocsdkfinal.view.TransparentCallOutDialog;
import com.goodocom.gocsdkfinal.view.TransparentDialog;
import com.goodocom.gocsdkfinal.view.TransparentInComingDialog;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.os.SystemClock;
import android.provider.ContactsContract.Contacts.Data;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class TransparentActivity extends Activity implements OnClickListener {
	public static final int MSG_HANGUP_PHONE = 0;
	public static final int MSG_CONNECTION_PHONE = 1;

	private IGocsdkService iGocsdkService;
	public static GocsdkCallbackImp callback;
	private Intent gocsdkService;
	private MyConn conn;

	private AlertDialog alertDialog;
	private TextView incoming_name;
	private TextView incoming_number;
	private TextView tv_phone_incoming;
	private ImageButton ibt_bt_phone;
	private ImageButton ibt_off_phone_incoming;
	private TransparentInComingDialog inComingDialog;
	private TransparentCallOutDialog callOutDialog;
	private TextView callout_name;
	private TextView callout_number;
	private TextView tv_phone_calling;
	private ImageButton ibt_bt_phone_qsd;
	private ImageButton ibt_hangup_phone_callout;
	private Chronometer chronometer_incoming;
	private Chronometer chronometer_callout;
	
	
	
	private String inComingNumber;
	private String callOutNumber;

	private boolean isInComing;
	// 声音在车机端或手机端的标记，车机端:false,手机端：true
	private boolean volume_flag = false;

	private static Handler hand = null;
	private Handler handler = new Handler() {

		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_CONNECTION_PHONE:
				if(isInComing){
					tv_phone_incoming.setText("正在通话：");
					chronometer_incoming.setVisibility(View.VISIBLE);
					chronometer_incoming.setFormat("%s");
					chronometer_incoming.setBase(SystemClock.elapsedRealtime());// 复位键
					chronometer_incoming.start();
				}else{
					tv_phone_calling.setText("正在通话：");
					chronometer_callout.setVisibility(View.VISIBLE);
					chronometer_callout.setFormat("%s");
					chronometer_callout.setBase(SystemClock.elapsedRealtime());// 复位键
					chronometer_callout.start();
				}
				break;
			case MSG_HANGUP_PHONE:
				System.out.println("是不是你的问题啊isInComing"+isInComing);
				if (isInComing) {
					chronometer_incoming.stop();
					chronometer_incoming.setVisibility(View.GONE);
					inComingDialog.dismiss();
					finish();
				} else {
					chronometer_callout.stop();
					chronometer_callout.setVisibility(View.GONE);
					callOutDialog.dismiss();
					finish();
				}
				break;
			}
		};
	};
	

	public static Handler getHandler() {
		return hand;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.notallhas);
		System.out.println("透明的你肯定执行了");
		Intent intent = getIntent();
		isInComing = intent.getBooleanExtra("isInComing", false);
		inComingNumber = intent.getStringExtra("inComingNumber");
		callOutNumber = intent.getStringExtra("callOutNumber");
		if (isInComing) {
			createInComingDialog();
		} else {
			createCallOutDialog();
		}
		gocsdkService = new Intent(TransparentActivity.this, GocsdkService.class);
		stopService(gocsdkService);
		conn = new MyConn();
		bindService(gocsdkService, conn, BIND_AUTO_CREATE);
		System.out.println("上边都执行了，这块肯定执行了");
		
		callback = new GocsdkCallbackImp();
		hand = handler;
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(conn);
		startService(gocsdkService);
	}

	private void createCallOutDialog() {
		callOutDialog = new TransparentCallOutDialog(this,
				R.style.transparentdialog);
		callOutDialog.setCanceledOnTouchOutside(false);
		initCallOutDialogView();
		initCallOutDialogData();
		callOutDialog.show();
	}

	private void initCallOutDialogData() {
		if (!TextUtils.isEmpty(callOutNumber)) {
			SQLiteDatabase systemDb = Database.getSystemDb();
			Database.createTable(systemDb, Database.Sql_create_phonebook_tab);
			String callOutName = Database.queryPhoneName(systemDb,
					Database.PhoneBookTable, callOutNumber);
			systemDb.close();
			if (TextUtils.isEmpty(callOutName)) {
				callout_name.setText("未知联系人");
			} else {
				callout_name.setText(callOutName);
			}
			callout_number.setText(callOutNumber);
			tv_phone_calling.setText("正在呼叫...");
		}
	}

	private void initCallOutDialogView() {
		View customView = callOutDialog.getCustomView();
		callout_name = (TextView) customView.findViewById(R.id.callout_name);
		callout_number = (TextView) customView
				.findViewById(R.id.callout_number);
		tv_phone_calling = (TextView) customView
				.findViewById(R.id.tv_phone_calling);
		ibt_bt_phone_qsd = (ImageButton) customView
				.findViewById(R.id.ibt_bt_phone_qsd);
		ibt_hangup_phone_callout = (ImageButton) customView
				.findViewById(R.id.ibt_hangup_phone_callout);
		
		chronometer_callout = (Chronometer) customView.findViewById(R.id.chronometer_callout);
		
		ibt_bt_phone_qsd.setOnClickListener(this);
		ibt_hangup_phone_callout.setOnClickListener(this);
	}

	private void createInComingDialog() {
		inComingDialog = new TransparentInComingDialog(this,
				R.style.transparentdialog);
		inComingDialog.setCanceledOnTouchOutside(false);
		initInComingDialogView();
		initInComingDialogData();
		inComingDialog.show();
	}

	private void initInComingDialogData() {
		if (!TextUtils.isEmpty(inComingNumber)) {
			SQLiteDatabase systemDb = Database.getSystemDb();
			Database.createTable(systemDb, Database.Sql_create_phonebook_tab);
			String inComingName = Database.queryPhoneName(systemDb,
					Database.PhoneBookTable, inComingNumber);
			systemDb.close();
			if (TextUtils.isEmpty(inComingName)) {
				incoming_name.setText("未知联系人");
			} else {
				incoming_name.setText(inComingName);
			}
			incoming_number.setText(inComingNumber);
			tv_phone_incoming.setText("来电...");
		}
	}

	private void initInComingDialogView() {
		View customView = inComingDialog.getCustomView();
		incoming_name = (TextView) customView.findViewById(R.id.incoming_name);
		incoming_number = (TextView) customView
				.findViewById(R.id.incoming_number);
		tv_phone_incoming = (TextView) customView
				.findViewById(R.id.tv_phone_incoming);
		ibt_bt_phone = (ImageButton) customView.findViewById(R.id.ibt_bt_phone);
		ibt_off_phone_incoming = (ImageButton) customView
				.findViewById(R.id.ibt_off_phone_incoming);
		chronometer_incoming = (Chronometer) customView.findViewById(R.id.chronometer_incoming);
		ibt_bt_phone.setOnClickListener(this);
		ibt_off_phone_incoming.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.ibt_bt_phone:
			try {
				iGocsdkService.phoneAnswer();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			break;
		case R.id.ibt_off_phone_incoming:
			try {
				iGocsdkService.phoneHangUp();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			break;
		case R.id.ibt_bt_phone_qsd:
			switchCarAndphone();
			break;
		case R.id.ibt_hangup_phone_callout:
			try {
				iGocsdkService.phoneHangUp();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
			break;
		}
	}

	private void switchCarAndphone() {
		volume_flag = !volume_flag;
		if (volume_flag) {// 手机端
			try {
				iGocsdkService.phoneTransfer();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			Toast.makeText(this, "手机端", 0).show();
		} else {// 车机端
			try {
				iGocsdkService.phoneTransferBack();
			} catch (RemoteException e1) {
				e1.printStackTrace();
			}
			Toast.makeText(this, "车机端", 0).show();
		}
	}

	private class MyConn implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			System.out.println("有没有连接成功啊");
			iGocsdkService = IGocsdkService.Stub.asInterface(service);
			// 蓝牙回调注册
			// 查询当前HFP状态
			try {
				iGocsdkService.registerCallback(callback);

				handler.postDelayed(new Runnable() {

					@Override
					public void run() {
						try {
							iGocsdkService.inqueryHfpStatus();
							iGocsdkService.musicUnmute();
							iGocsdkService.getLocalName();
							iGocsdkService.getPinCode();
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}, 500);

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	}

}
