package com.goodocom.gocsdkfinal.activity;

import com.goodocom.gocsdk.IGocsdkService;
import com.goodocom.gocsdkfinal.GocsdkSettings;
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
import android.support.v4.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.sqlite.SQLiteDatabase;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
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

	public static final int MSG_PHONEBOOK_NOT_SHARE = 29;

	public static String mComingPhoneNum = null; // 鏉ョ數鍙风爜
	public static String mCalloutPhoneNum = null;// 鎷ㄥ嚭鍙风爜
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
	
	private GocsdkSettings mSettings;

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
	private String[] mString = new String[] { "閫氳瘽璁板綍", "閫氳褰�", "鎷ㄥ彿鐩�", "钃濈墮淇℃伅",
			"钃濈墮閰嶅鍒楄〃", "璁剧疆" };

	private static Handler hand = null;

	public static Handler getHandler() {
		return hand;
	}

	// 鏆撮湶鏂规硶锛岃鍏朵粬椤甸潰鑳藉鑾峰彇涓婚〉闈㈢殑鍙傛暟
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
		
		mSettings = GocsdkSettings.getInstance(this);

		System.out.println("涓荤晫闈㈠惎鍔ㄤ簡");
		// 娉ㄥ唽寮�鏈哄箍鎾帴鏀惰��
		myRegisterReceiver();

		gocsdkService = new Intent(MainActivity.this, GocsdkService.class);
		startService(gocsdkService);
		//stopService(gocsdkService);

		conn = new MyConn();
		bindService(gocsdkService, conn, BIND_AUTO_CREATE);

		// 寮�鍚挱鏀炬湇鍔�
		Intent playerService = new Intent(this, PlayerService.class);
		startService(playerService);

		// 鍒濆鍖栧竷灞�
		initView();
		tabhost.setCurrentTab(2);
		
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		callback = new GocsdkCallbackImp();

		hand = handler;
	}

	// 閿�姣�
	@Override
	protected void onDestroy() {
		System.out.println("MainActivity===onDestroy");
		super.onDestroy();

		// 娉ㄩ攢钃濈墮鍥炶皟
		try {
			iGocsdkService.unregisterCallback(callback);

		} catch (RemoteException e) {
			e.printStackTrace();
		}
		// 娉ㄩ攢寮�鏈哄箍鎾�
		unregisterReceiver(receiver);
		// 瑙ｇ粦鏈嶅姟
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
			if(iGocsdkService != null && mFragmentSetting != null){
				mFragmentSetting.setGocsdkService(iGocsdkService);
			}
			// 钃濈墮鍥炶皟娉ㄥ唽
			// 鏌ヨ褰撳墠HFP鐘舵��
			try {
				iGocsdkService.registerCallback(callback);
				//if(mSettings.isOpen()){
					//doSerialInit();
				//}

			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void onServiceDisconnected(ComponentName name) {

		}
	}
	
	private void doSerialInit(){
		handler.postDelayed(new Runnable() {

			@Override
			public void run() {
				try {
					iGocsdkService.inqueryHfpStatus();
					iGocsdkService.musicUnmute();
					//iGocsdkService.getLocalName();
					//iGocsdkService.getPinCode();
				} catch (RemoteException e) {
					e.printStackTrace();
				}
			}
		}, 500);
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
			 * case MSG_HF_CONNECTED:// 钃濈墮璁惧宸茶繛鎺�
			 * Toast.makeText(MainActivity.this, R.string.bt_connect_info,
			 * Toast.LENGTH_SHORT).show(); break; case MSG_HF_DISCONNECTED://
			 * 鏃犺澶囪繛鎺�
			 * 
			 * Toast.makeText(MainActivity.this, R.string.bt_disconnect_info,
			 * Toast.LENGTH_SHORT).show(); // 娓呯悊閰嶅鍒楄〃 pairlists.clear(); if
			 * (false == isConnected()) return; // 鑾峰彇閰嶅鍒楄〃 try {
			 * MainActivity.iGocsdkService.getPairList(); } catch
			 * (RemoteException e1) { e1.printStackTrace(); }
			 * 
			 * mCurDevName = null; // disconnect clear device info mCurDevAddr =
			 * null; break; case MSG_REMOTE_NAME:// 褰撳墠閰嶅鍚嶇О String Name =
			 * (String) msg.obj; mCurDevName = Name; setTitle(Name); try {
			 * MainActivity.iGocsdkService.getPairList(); } catch
			 * (RemoteException e1) { e1.printStackTrace(); } break;
			 * 
			 * case MSG_REMOTE_ADDRESS:// 褰撳墠閰嶅鍦板潃 String Addr = (String) msg.obj;
			 * mCurDevAddr = Addr; break;
			 */
			case MSG_COMING:// 鏉ョ數
				isInComing = true;
				String phonenum = (String) msg.obj;
				String phonename = "";
				mComingPhoneNum = phonenum;
				SQLiteDatabase mDbDataBase = Database.getSystemDb();
				Database.createTable(mDbDataBase,
						Database.Sql_create_phonebook_tab);
				phonename = Database.queryPhoneName(mDbDataBase,
						Database.PhoneBookTable, phonenum);// 鏍规嵁鍙风爜鏌ヨ鑱旂郴浜�
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
			 * case MSG_HANGUP:// 鏉ョ數鎸傛柇 String str2 =
			 * getString(R.string.phone_hangup_info);
			 * Toast.makeText(MainActivity.this, str2, Toast.LENGTH_SHORT)
			 * .show(); break;
			 */
			case MSG_TALKING:// 鎺ュ惉
				if (isInComing) {// 鏉ョ數鎺ュ惉
					Handler handler = InComingActivity.getHandler();
					if (handler == null) {
						return;
					}
					handler.sendEmptyMessage(InComingActivity.MSG_INCOMING_CONNECTION);
				} else {// 鎷ㄥ嚭鎺ュ惉
					Handler handler = CallActivity.getHandler();
					if (handler == null) {
						return;
					}
					System.out.println("鍛戒护鏉ヤ簡鎴戝氨鍙戦��");
					handler.sendEmptyMessage(CallActivity.Msg_CONNECT);
				}
				break;
			case MSG_OUTGONG:// 鎷ㄥ嚭
				isInComing = false;
				String call_number = (String) msg.obj;
				System.out.println("MainAcitivity涓嫧鍑虹殑鐢佃瘽" + call_number);
				callOut(call_number);
				break;
			case MSG_DEVICENAME:// 钃濈墮璁惧鍚嶇О
				String name = (String) msg.obj;
				mLocalName = name;
				Log.i("hcj.serial","MSG_DEVICENAME="+mLocalName);
				break;
			case MSG_DEVICEPINCODE:// 钃濈墮璁惧鐨凱IN鐮�
				String pincode = (String) msg.obj;
				mPinCode = pincode;
				break;
			case MSG_PHONEBOOK_NOT_SHARE:
				Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.warning_share_contacts_fail),Toast.LENGTH_LONG).show();
				break;
			/**
			 * case MSG_PAIRLIST:// 灏嗚澶囨坊鍔犲埌閰嶅鍒楄〃涓� BtDevices btPairList =
			 * (BtDevices) msg.obj; Map<String, String> pairlist = new
			 * HashMap<String, String>();
			 * 
			 * pairlist.put("itemName", btPairList.name);
			 * pairlist.put("itemAddr", btPairList.addr); devices.add(pairlist);
			 * // simpleAdapter.notifyDataSetChanged(); break;
			 */
			/*
			 * 
			 * case MSG_MUSIC_VOLUME_DOWN:// 闊抽噺璋冧綆
			 * mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
			 * AudioManager.ADJUST_LOWER, AudioManager.FX_FOCUS_NAVIGATION_UP);
			 * break;
			 * 
			 * case MSG_MUSIC_VOLUME_UP:// 闊抽噺璋冮珮
			 * mAudioManager.adjustStreamVolume(AudioManager.STREAM_MUSIC,
			 * AudioManager.ADJUST_RAISE, AudioManager.FX_FOCUS_NAVIGATION_UP);
			 * break;
			 * 
			 * case MSG_MUSIC_PLAY:// 鎾斁闊充箰
			 * Toast.makeText(getApplicationContext(), "钃濈墮闊充箰褰撳墠鐘舵�侊細鎾斁闊充箰", 0)
			 * .show(); break;
			 * 
			 * case MSG_MUSIC_STOP:// 鍋滄闊充箰
			 * Toast.makeText(getApplicationContext(), "钃濈墮闊充箰褰撳墠鐘舵�侊細鍋滄闊充箰",
			 * Toast.LENGTH_SHORT).show(); break;
			 * 
			 * 
			 * case MSG_SET_MICPHONE_ON:// 鎵撳紑楹﹀厠椋�
			 * mAudioManager.setMicrophoneMute(true); CharSequence str3 =
			 * getString(R.string.mic_on_info);
			 * Toast.makeText(MainActivity.this, str3,
			 * Toast.LENGTH_SHORT).show(); break;
			 * 
			 * case MSG_SET_MICPHONE_OFF:// 鍏抽棴楹﹀厠椋�
			 * mAudioManager.setMicrophoneMute(false);// 璁剧疆鏄惁璁╅害鍏嬮璁剧疆闈欓煶
			 * CharSequence str4 = getString(R.string.mic_off_info);
			 * Toast.makeText(MainActivity.this, str4,
			 * Toast.LENGTH_SHORT).show(); break;
			 * 
			 * case MSG_SET_SPEAERPHONE_ON:// 鍒囨崲澹伴亾鍒版墜鏈虹 if (service != null) try
			 * { service.phoneTransfer(); } catch (RemoteException e1) { // TODO
			 * Auto-generated catch block e1.printStackTrace(); } //
			 * mAudioManager.setMicrophoneMute(true); // CharSequence
			 * str5=getString(R.string.speaker_on_info); //
			 * Toast.makeText(MainActivity.this, str5, //
			 * Toast.LENGTH_SHORT).show(); break;
			 * 
			 * case MSG_SET_SPEAERPHONE_OFF:// 鍒囨崲澹伴亾鍒拌溅鏈虹 if (service != null) try
			 * { service.phoneTransferBack(); } catch (RemoteException e1) { //
			 * catch block e1.printStackTrace(); } //
			 * mAudioManager.setMicrophoneMute(false); // CharSequence str6 =
			 * getString(R.string.speaker_off_info); //
			 * Toast.makeText(MainActivity.this, str6, //
			 * Toast.LENGTH_SHORT).show(); break;
			 * 
			 * 
			 * case MSG_UPDATE_PHONEBOOK:// 寮瑰嚭鏇存柊鑱旂郴浜哄垪琛ㄧ殑绛夊緟瀵硅瘽妗�
			 * Toast.makeText(getApplicationContext(), "鏇存柊ing", 0).show(); //
			 * phoneBookdialog = ProgressDialog.show(MainActivity.this, //
			 * "鏇存柊鑱旂郴浜�", "璇风◢绛�...", true); showDownLoadContactDialog(); break;
			 * case MSG_UPDATE_PHONEBOOK_DONE:
			 * Toast.makeText(getApplicationContext(), "鏇存柊ed", 0).show(); if
			 * (alertDialog != null) { alertDialog.dismiss(); } break; case
			 * MSG_DIAL_DIALOG:// 鎻愮ず鏄惁鎷ㄦ墦鐢佃瘽鐨勫璇濇
			 * 
			 * final phoneBook phone = (phoneBook) msg.obj; AlertDialog.Builder
			 * builder = new Builder(MainActivity.this);
			 * builder.setMessage("纭畾瑕佹嫧鎵撳悧?" + phone.name + ":" + phone.num);
			 * builder.setTitle("鎻愮ず"); builder.setPositiveButton("纭", new
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
			 * } }); builder.setNegativeButton("鍙栨秷", new
			 * android.content.DialogInterface.OnClickListener() {
			 * 
			 * @Override public void onClick(DialogInterface dialog, int which)
			 * { dialog.dismiss(); } }); builder.create().show(); break;
			 * 
			 * 
			 * case MSG_UPDATE_DEVICE_LIST:// 鏇存柊钃濈墮璁惧鍒楄〃 PhoneBookdialog =
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
			 * ProgressDialog.show(MainActivity.this, "涓嬭浇鏉ョ數閫氳瘽璁板綍", "璇风◢绛�...",
			 * true); } else { Toast.makeText(MainActivity.this, "璇锋偍鍏堣繛鎺ヨ澶�",
			 * Toast.LENGTH_SHORT).show(); }
			 * 
			 * break; case MSG_UPDATE_CALLOUT_CALLLOG: if
			 * (GocsdkCallbackImp.hfpStatus > 0) { phoneBookdialog =
			 * ProgressDialog.show(MainActivity.this, "涓嬭浇鎷ㄥ嚭閫氳瘽璁板綍", "璇风◢绛�...",
			 * true); } else { Toast.makeText(MainActivity.this, "璇锋偍鍏堣繛鎺ヨ澶�",
			 * Toast.LENGTH_SHORT).show(); }
			 * 
			 * break; case MSG_UPDATE_MISSED_CALLLOG: if
			 * (GocsdkCallbackImp.hfpStatus > 0) { phoneBookdialog =
			 * ProgressDialog.show(MainActivity.this, "涓嬭浇鏈帴閫氳瘽璁板綍", "璇风◢绛�...",
			 * true); } else { Toast.makeText(MainActivity.this, "璇锋偍鍏堣繛鎺ヨ澶�",
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

	// 鎷ㄦ墦姝ｇ‘鐨勭數璇�
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
	
	FragmentSetting mFragmentSetting;
	@Override
	public void onAttachFragment(Fragment fragment) {
		if (fragment instanceof FragmentSetting) {
			mFragmentSetting = (FragmentSetting)fragment;
			if(iGocsdkService != null){
				mFragmentSetting.setGocsdkService(iGocsdkService);
			}
		}
	}
}
