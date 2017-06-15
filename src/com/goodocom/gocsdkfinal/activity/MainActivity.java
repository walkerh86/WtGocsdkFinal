package com.goodocom.gocsdkfinal.activity;

import com.goodocom.gocsdk.IGocsdkService;
import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.db.Database;
import com.goodocom.gocsdkfinal.fragment.FragmentBlueToothInfo;
import com.goodocom.gocsdkfinal.fragment.FragmentBlueToothList;
import com.goodocom.gocsdkfinal.fragment.FragmentCallPhone;
import com.goodocom.gocsdkfinal.fragment.FragmentCallog;
import com.goodocom.gocsdkfinal.fragment.FragmentSetting;
import com.goodocom.gocsdkfinal.receiver.BootReceiver;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;
import com.goodocom.gocsdkfinal.service.GocsdkService;
import com.goodocom.gocsdkfinal.service.PlayerService;
import com.goodocom.gocsdkfinal.view.MyFragmentTabHost;

import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.AlertDialog.Builder;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.TabHost.TabSpec;

public class MainActivity extends BaseActivity {
	public static final int MSG_HF_DISCONNECTED = 0;
	public static final int MSG_HF_CONNECTED = 1;
	public static final int MSG_REMOTE_ADDRESS = 2;
	public static final int MSG_DEVICES = 3;
	public static final int MSG_COMING = 4;
	public static final int MSG_OUTGONG = 5;
	public static final int MSG_TALKING = 6;
	public static final int MSG_HANGUP = 7;
	public static final int MSG_PAIRLIST = 8;
	public static final int MSG_REMOTE_NAME = 9;
	public static final int MSG_DEVICENAME = 11;
	public static final int MSG_DEVICEPINCODE = 12;
	public static final int MSG_MUSIC_VOLUME_DOWN = 13;
	public static final int MSG_MUSIC_VOLUME_UP = 14;
	public static final int MSG_MUSIC_PLAY = 15;
	public static final int MSG_MUSIC_STOP = 16;
	public static final int MSG_UPDATE_PHONEBOOK = 17;
	public static final int MSG_UPDATE_PHONEBOOK_DONE = 18;
	public static final int MSG_SET_MICPHONE_ON = 19;
	public static final int MSG_SET_MICPHONE_OFF = 20;
	public static final int MSG_SET_SPEAERPHONE_ON = 21;
	public static final int MSG_SET_SPEAERPHONE_OFF = 22;
	public static final int MSG_DIAL_DIALOG = 23;
	public static final int MSG_UPDATE_DEVICE_LIST = 24;
	public static final int MSG_UPDATE_INCOMING_CALLLOG = 25;
	public static final int MSG_UPDATE_CALLOUT_CALLLOG = 26;
	public static final int MSG_UPDATE_MISSED_CALLLOG = 27;
	public static final int MSG_UPDATE_CALLLOG_DONE = 28;

	public static String mComingPhoneNum = null; // 来电号码
	public static String mCalloutPhoneNum = null;// 拨出号码
	public static String mLocalName = null;
	public static String mPinCode = null;
	public static boolean isInComing = false;

	private Intent gocsdkService;
	private MyConn conn;
	public static GocsdkCallbackImp callback;
	private AudioManager mAudioManager;
	private static IGocsdkService iGocsdkService;
	private static MyFragmentTabHost tabhost;
	private BootReceiver receiver;

	private int mImageID[] = { R.drawable.btn_calllog_selector,
			R.drawable.btn_contact_selector, R.drawable.btn_jianpan_selector,
			R.drawable.btn_btinfo_selector, R.drawable.btn_btpairlist_selector,
			R.drawable.btn_setting_selector };
	private Class[] mFragment = { FragmentCallog.class,
			com.goodocom.gocsdkfinal.fragment.FragmentMailList.class,
			com.goodocom.gocsdkfinal.fragment.FragmentCallPhone.class,
			FragmentBlueToothInfo.class, FragmentBlueToothList.class,
			FragmentSetting.class };
	private Class[] mNoConnectionFragment = {
			com.goodocom.gocsdkfinal.fragment.FragmentCallPhone.class,
			com.goodocom.gocsdkfinal.fragment.FragmentCallPhone.class,
			com.goodocom.gocsdkfinal.fragment.FragmentCallPhone.class,
			FragmentBlueToothInfo.class, FragmentBlueToothList.class,
			FragmentSetting.class };
	private String[] mString = new String[] { "通话记录", "通讯录", "拨号盘", "蓝牙信息",
			"蓝牙配对列表", "设置" };

	private static Handler hand = null;

	public static Handler getHandler() {
		return hand;
	}

	// 暴露方法，让其他页面能够获取主页面的参数
	public static IGocsdkService getService() {
		return iGocsdkService;
	}

	public boolean isConnected() {
		return iGocsdkService != null;
	}

	public AudioManager getAudioManager() {
		return mAudioManager;
	}

	public static MyFragmentTabHost getTabHost() {
		return tabhost;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		System.out.println("主界面启动了");
		// 注册开机广播接收者
		myRegisterReceiver();

		gocsdkService = new Intent(MainActivity.this, GocsdkService.class);

		stopService(gocsdkService);

		conn = new MyConn();
		bindService(gocsdkService, conn, BIND_AUTO_CREATE);

		// 开启播放服务
		Intent playerService = new Intent(this, PlayerService.class);
		startService(playerService);

		// 初始化布局
		initView();
		tabhost.setCurrentTab(2);
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		callback = new GocsdkCallbackImp();

		hand = handler;
	}

	// 销毁
	@Override
	protected void onDestroy() {
		System.out.println("MainActivity===onDestroy");
		super.onDestroy();

		// 注销蓝牙回调
		try {
			iGocsdkService.unregisterCallback(callback);

		} catch (RemoteException e) {
			e.printStackTrace();
		}
		// 注销开机广播
		unregisterReceiver(receiver);
		// 解绑服务
		unbindService(conn);
		startService(gocsdkService);
	}

	private void myRegisterReceiver() {
		IntentFilter filter = new IntentFilter();
		filter.addAction("android.intent.action.BOOT_COMPLETED");
		receiver = new BootReceiver();
		registerReceiver(receiver, filter);
	}

	private class MyConn implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {

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

	private void initView() {
		tabhost = (MyFragmentTabHost) findViewById(android.R.id.tabhost);

		tabhost.setup(MainActivity.this, getSupportFragmentManager(),
				R.id.fl_content_show);
		for (int i = 0; i < mImageID.length; i++) {
			TabSpec tabSpec = tabhost.newTabSpec(mString[i]).setIndicator(
					getTabItemView(i));
			tabhost.addTab(tabSpec, mFragment[i], null);
		}
	}

	private View getTabItemView(int index) {
		View view = View.inflate(this, R.layout.tabhost_item_view, null);
		ImageView iv_flg = (ImageView) view.findViewById(R.id.iv_flg);
		iv_flg.setImageResource(mImageID[index]);
		return view;
	}

	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {

			switch (msg.what) {
			/*
			 * Handler callPhoneHandler = FragmentCallPhone.getHandler(); final
			 * Handler final_callPhoneHandler = callPhoneHandler; Message
			 * msg_callphone = new Message(); final Message final_msg_callphone
			 * = msg_callphone;
			 * 
			 * switch (msg.what) {
			 * 
			 * case MSG_HF_CONNECTED:// 蓝牙设备已连接
			 * Toast.makeText(MainActivity.this, R.string.bt_connect_info,
			 * Toast.LENGTH_SHORT).show(); break; case MSG_HF_DISCONNECTED://
			 * 无设备连接
			 * 
			 * Toast.makeText(MainActivity.this, R.string.bt_disconnect_info,
			 * Toast.LENGTH_SHORT).show(); // 清理配对列表 pairlists.clear(); if
			 * (false == isConnected()) return; // 获取配对列表 try {
			 * MainActivity.iGocsdkService.getPairList(); } catch
			 * (RemoteException e1) { e1.printStackTrace(); }
			 * 
			 * mCurDevName = null; // disconnect clear device info mCurDevAddr =
			 * null; break; case MSG_REMOTE_NAME:// 当前配对名称 String Name =
			 * (String) msg.obj; mCurDevName = Name; setTitle(Name); try {
			 * MainActivity.iGocsdkService.getPairList(); } catch
			 * (RemoteException e1) { e1.printStackTrace(); } break;
			 * 
			 * case MSG_REMOTE_ADDRESS:// 当前配对地址 String Addr = (String) msg.obj;
			 * mCurDevAddr = Addr; break;
			 */
			case MSG_COMING:// 来电
				isInComing = true;
				String phonenum = (String) msg.obj;
				String phonename = "";
				mComingPhoneNum = phonenum;
				SQLiteDatabase mDbDataBase = Database.getSystemDb();
				Database.createTable(mDbDataBase,
						Database.Sql_create_phonebook_tab);
				phonename = Database.queryPhoneName(mDbDataBase,
						Database.PhoneBookTable, phonenum);// 根据号码查询联系人
				Intent intent = new Intent(MainActivity.this,
						InComingActivity.class);
				if (TextUtils.isEmpty(phonename)) {
					intent.putExtra("incomingNumber", phonenum);
				} else {
					intent.putExtra("incomingNumber", phonename);
				}
				startActivity(intent);
				break;
			/*
			 * case MSG_HANGUP:// 来电挂断 String str2 =
			 * getString(R.string.phone_hangup_info);
			 * Toast.makeText(MainActivity.this, str2, Toast.LENGTH_SHORT)
			 * .show(); break;
			 */
			case MSG_TALKING:// 接听
				if (isInComing) {// 来电接听
					Handler handler = InComingActivity.getHandler();
					if (handler == null) {
						return;
					}
					handler.sendEmptyMessage(InComingActivity.MSG_INCOMING_CONNECTION);
				} else {// 拨出接听
					Handler handler = CallActivity.getHandler();
					if (handler == null) {
						return;
					}
					System.out.println("命令来了我就发送");
					handler.sendEmptyMessage(CallActivity.Msg_CONNECT);
				}
				break;
			case MSG_OUTGONG:// 拨出
				isInComing = false;
				String call_number = (String) msg.obj;
				System.out.println("MainAcitivity中拨出的电话" + call_number);
				callOut(call_number);
				break;
			case MSG_DEVICENAME:// 蓝牙设备名称
				String name = (String) msg.obj;
				mLocalName = name;
				break;
			case MSG_DEVICEPINCODE:// 蓝牙设备的PIN码
				String pincode = (String) msg.obj;
				mPinCode = pincode;
				break;
			/**
			 * case MSG_PAIRLIST:// 将设备添加到配对列表中 BtDevices btPairList =
			 * (BtDevices) msg.obj; Map<String, String> pairlist = new
			 * HashMap<String, String>();
			 * 
			 * pairlist.put("itemName", btPairList.name);
			 * pairlist.put("itemAddr", btPairList.addr); devices.add(pairlist);
			 * // simpleAdapter.notifyDataSetChanged(); break;
			 */
			/*
			 * 
			 * case MSG_MUSIC_VOLUME_DOWN:// 音量调低
			 * mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
			 * AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
			 * break;
			 * 
			 * case MSG_MUSIC_VOLUME_UP:// 音量调高
			 * mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
			 * AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
			 * break;
			 * 
			 * case MSG_MUSIC_PLAY:// 播放音乐
			 * Toast.makeText(getApplicationContext(), "蓝牙音乐当前状态：播放音乐", 0)
			 * .show(); break;
			 * 
			 * case MSG_MUSIC_STOP:// 停止音乐
			 * Toast.makeText(getApplicationContext(), "蓝牙音乐当前状态：停止音乐",
			 * Toast.LENGTH_SHORT).show(); break;
			 * 
			 * 
			 * case MSG_SET_MICPHONE_ON:// 打开麦克风
			 * mAudioManager.setMicrophoneMute(true); CharSequence str3 =
			 * getString(R.string.mic_on_info);
			 * Toast.makeText(MainActivity.this, str3,
			 * Toast.LENGTH_SHORT).show(); break;
			 * 
			 * case MSG_SET_MICPHONE_OFF:// 关闭麦克风
			 * mAudioManager.setMicrophoneMute(false);// 设置是否让麦克风设置静音
			 * CharSequence str4 = getString(R.string.mic_off_info);
			 * Toast.makeText(MainActivity.this, str4,
			 * Toast.LENGTH_SHORT).show(); break;
			 * 
			 * case MSG_SET_SPEAERPHONE_ON:// 切换声道到手机端 if (service != null) try
			 * { service.phoneTransfer(); } catch (RemoteException e1) { // TODO
			 * Auto-generated catch block e1.printStackTrace(); } //
			 * mAudioManager.setMicrophoneMute(true); // CharSequence
			 * str5=getString(R.string.speaker_on_info); //
			 * Toast.makeText(MainActivity.this, str5, //
			 * Toast.LENGTH_SHORT).show(); break;
			 * 
			 * case MSG_SET_SPEAERPHONE_OFF:// 切换声道到车机端 if (service != null) try
			 * { service.phoneTransferBack(); } catch (RemoteException e1) { //
			 * catch block e1.printStackTrace(); } //
			 * mAudioManager.setMicrophoneMute(false); // CharSequence str6 =
			 * getString(R.string.speaker_off_info); //
			 * Toast.makeText(MainActivity.this, str6, //
			 * Toast.LENGTH_SHORT).show(); break;
			 * 
			 * 
			 * case MSG_UPDATE_PHONEBOOK:// 弹出更新联系人列表的等待对话框
			 * Toast.makeText(getApplicationContext(), "更新ing", 0).show(); //
			 * phoneBookdialog = ProgressDialog.show(MainActivity.this, //
			 * "更新联系人", "请稍等...", true); showDownLoadContactDialog(); break;
			 * case MSG_UPDATE_PHONEBOOK_DONE:
			 * Toast.makeText(getApplicationContext(), "更新ed", 0).show(); if
			 * (alertDialog != null) { alertDialog.dismiss(); } break; case
			 * MSG_DIAL_DIALOG:// 提示是否拨打电话的对话框
			 * 
			 * final phoneBook phone = (phoneBook) msg.obj; AlertDialog.Builder
			 * builder = new Builder(MainActivity.this);
			 * builder.setMessage("确定要拨打吗?" + phone.name + ":" + phone.num);
			 * builder.setTitle("提示"); builder.setPositiveButton("确认", new
			 * android.content.DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface arg0, int arg1) {
			 * // TODO Auto-generated method stub arg0.dismiss();
			 * tabhost.setCurrentTab(0); final_msg_callphone.what =
			 * FragmentCallPhone.MSG_CALL_STATUS; final_msg_callphone.obj =
			 * phone.num; if (final_callPhoneHandler == null) { return; }
			 * final_callPhoneHandler .sendMessage(final_msg_callphone);
			 * 
			 * tabhost.setCurrentTab(0); try { MainActivity.iGocsdkService
			 * .phoneDail(phone.num); FragmentCallPhone.tv_call_people_name
			 * .setText(phone.name); } catch (RemoteException e) {
			 * e.printStackTrace(); } FragmentCallPhone.ll_dialpad_pager
			 * .setVisibility(View.VISIBLE); FragmentCallPhone.rl_call_pager
			 * .setVisibility(View.GONE);
			 * 
			 * } }); builder.setNegativeButton("取消", new
			 * android.content.DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { dialog.dismiss(); } }); builder.create().show(); break;
			 * 
			 * 
			 * case MSG_UPDATE_DEVICE_LIST:// 更新蓝牙设备列表 PhoneBookdialog =
			 * ProgressDialog.show(MainActivity.this, "Wait", "Please wait...",
			 * true); // sleep 4s to wait new Thread() {
			 * 
			 * @Override public void run() { try { sleep(6000); } catch
			 * (InterruptedException e) { e.printStackTrace(); } finally { if
			 * (PhoneBookdialog != null) { PhoneBookdialog.dismiss();
			 * PhoneBookdialog = null; } } } }.start(); break;
			 * 
			 * case MSG_UPDATE_INCOMING_CALLLOG:
			 * 
			 * if (GocsdkCallbackImp.hfpStatus > 0) { phoneBookdialog =
			 * ProgressDialog.show(MainActivity.this, "下载来电通话记录", "请稍等...",
			 * true); } else { Toast.makeText(MainActivity.this, "请您先连接设备",
			 * Toast.LENGTH_SHORT).show(); }
			 * 
			 * break; case MSG_UPDATE_CALLOUT_CALLLOG: if
			 * (GocsdkCallbackImp.hfpStatus > 0) { phoneBookdialog =
			 * ProgressDialog.show(MainActivity.this, "下载拨出通话记录", "请稍等...",
			 * true); } else { Toast.makeText(MainActivity.this, "请您先连接设备",
			 * Toast.LENGTH_SHORT).show(); }
			 * 
			 * break; case MSG_UPDATE_MISSED_CALLLOG: if
			 * (GocsdkCallbackImp.hfpStatus > 0) { phoneBookdialog =
			 * ProgressDialog.show(MainActivity.this, "下载未接通话记录", "请稍等...",
			 * true); } else { Toast.makeText(MainActivity.this, "请您先连接设备",
			 * Toast.LENGTH_SHORT).show(); } break; case
			 * MSG_UPDATE_CALLLOG_DONE: if (phoneBookdialog != null) {
			 * phoneBookdialog.dismiss(); } break;
			 */
			}

		};
	};

	private void callOut(String phoneNumber2) {
		placeCall(phoneNumber2);
		Intent intent = new Intent(this, CallActivity.class);
		intent.putExtra("callNumber", phoneNumber2);
		intent.putExtra("isConnect", false);
		startActivity(intent);
	}

	// 拨打正确的电话
	private static void placeCall(String mLastNumber) {
		if (mLastNumber.length() == 0)
			return;
		if (PhoneNumberUtils.isGlobalPhoneNumber(mLastNumber)) {
			// place the call if it is a valid number
			if (mLastNumber == null || !TextUtils.isGraphic(mLastNumber)) {
				// There is no number entered.
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
