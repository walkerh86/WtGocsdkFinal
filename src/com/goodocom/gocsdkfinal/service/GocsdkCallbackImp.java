package com.goodocom.gocsdkfinal.service;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;

import com.goodocom.gocsdk.IGocsdkCallback;
import com.goodocom.gocsdkfinal.activity.CallActivity;
import com.goodocom.gocsdkfinal.activity.InComingActivity;
import com.goodocom.gocsdkfinal.activity.MainActivity;
import com.goodocom.gocsdkfinal.domain.BlueToothInfo;
import com.goodocom.gocsdkfinal.domain.BlueToothPairedInfo;
import com.goodocom.gocsdkfinal.domain.CallLogInfo;
import com.goodocom.gocsdkfinal.fragment.FragmentBlueToothInfo;
import com.goodocom.gocsdkfinal.fragment.FragmentBlueToothList;
import com.goodocom.gocsdkfinal.fragment.FragmentCallPhone;
import com.goodocom.gocsdkfinal.fragment.FragmentCallog;
import com.goodocom.gocsdkfinal.fragment.FragmentMailList;
import com.goodocom.gocsdkfinal.fragment.FragmentMailList.phoneBook;
import com.goodocom.gocsdkfinal.fragment.FragmentSetting;

import de.greenrobot.event.EventBus;

public class GocsdkCallbackImp extends IGocsdkCallback.Stub {
	public static String number = "";
	public static int hfpStatus = 0;
	private static int callType = 0;

	public class BtDevices {
		public String name = null;
		public String addr = null;
	}

	public class callLog {
		public int callType = 0;
		public String num = null;
	}

	@Override
	public void onHfpConnected() throws RemoteException {
		Log.i("hcj.cb", "onHfpConnected mOnPairedListener="+mOnPairedListener);
		if(mOnPairedListener != null){
			mOnPairedListener.onHfpStateChange(true);
		}
		if(mOnAvailListener != null){
			mOnAvailListener.onHfpStateChange(true);
		}
		Handler handler2 = FragmentBlueToothList.getHandler();
		if(handler2!=null){
			handler2.sendEmptyMessage(FragmentBlueToothList.MSG_CONNECT_SUCCESS);
		}
		Handler handler = FragmentBlueToothInfo.getHandler();
		if(handler == null)
			return;
		handler.sendEmptyMessage(FragmentBlueToothInfo.MSG_CONNECT_SUCCESS);
		GocsdkCallbackImp.hfpStatus = 1;
	}

	@Override
	public void onHfpDisconnected() throws RemoteException {
		if(mOnPairedListener != null){
			mOnPairedListener.onHfpStateChange(false);
		}
		if(mOnAvailListener != null){
			mOnAvailListener.onHfpStateChange(false);
		}
		Handler handler2 = FragmentBlueToothList.getHandler();
		if(handler2!=null){
			handler2.sendEmptyMessage(FragmentBlueToothList.MSG_CONNECT_FAILE);
		}
		Handler handler = FragmentBlueToothInfo.getHandler();
		if(handler == null)
			return;
		handler.sendEmptyMessage(FragmentBlueToothInfo.MSG_CONNECT_FAILE);
		GocsdkCallbackImp.hfpStatus = 0;
	}

	@Override
	public void onCallSucceed(String number) throws RemoteException {
		GocsdkCallbackImp.hfpStatus = 5;
	}

	@Override
	public void onIncoming(String number) throws RemoteException {
		
		Handler handler1 = MainActivity.getHandler();
		handler1.sendMessage(handler1.obtainMessage(MainActivity.MSG_COMING,
				number));
		GocsdkCallbackImp.number = number;
		GocsdkCallbackImp.hfpStatus = 4;

	}

	@Override
	public void onHangUp() throws RemoteException {
		Handler handler2 = InComingActivity.getHandler();
		if(handler2!=null){
			handler2.sendEmptyMessage(InComingActivity.MSG_INCOMINNG_HANGUP);
		}
		Handler handler = CallActivity.getHandler();
		if(handler == null){
			return;
		}
		handler.sendEmptyMessage(CallActivity.MSG_INCOMING_HANGUP);
		GocsdkCallbackImp.hfpStatus = 7;
	}

	@Override
	public void onTalking(String str) throws RemoteException {
		System.out.println("鎺ラ�氫簡");
		Handler handler = MainActivity.getHandler();
		if(handler==null){
			return;
		}
		handler.sendEmptyMessage(MainActivity.MSG_TALKING);
		GocsdkCallbackImp.hfpStatus = 6;

	}

	@Override
	public void onRingStart() throws RemoteException {
	}

	@Override
	public void onRingStop() throws RemoteException {
	}

	@Override
	public void onHfpLocal() throws RemoteException {
	}

	@Override
	public void onHfpRemote() throws RemoteException {
	}

	@Override
	public void onInPairMode() throws RemoteException {
	}

	@Override
	public void onExitPairMode() throws RemoteException {
	}

	@Override
	public void onInitSucceed() throws RemoteException {
	}

	@Override
	public void onMusicPlaying() throws RemoteException {/*

		Handler handler = MainActivity.getHandler();
		if (null == handler)
			return;
		handler.sendEmptyMessage(MainActivity.MSG_MUSIC_PLAY);

		Handler musicHandler = FragmentMusic.getHandler();
		Message msg_music = new Message();
		msg_music.what = FragmentMusic.MSG_FRAGMENT_MUSIC_PLAY;
		if (musicHandler == null) {
			return;
		}
		musicHandler.sendMessage(msg_music);
		EventBus.getDefault().postSticky(new PlayStatusEvent(true));
	*/}

	@Override
	public void onMusicStopped() throws RemoteException {/*
		Handler handler = MainActivity.getHandler();
		if (null == handler)
			return;
		handler.sendEmptyMessage(MainActivity.MSG_MUSIC_STOP);

		Handler musicHandler = FragmentMusic.getHandler();
		Message msg_music = new Message();
		msg_music.what = FragmentMusic.MSG_FRAGMENT_MUSIC_PAUSE;
		if (musicHandler == null) {
			return;
		}
		musicHandler.sendMessage(msg_music);
		EventBus.getDefault().postSticky(new PlayStatusEvent(false));
	*/}

	@Override
	public void onAutoConnectAccept(boolean autoConnect, boolean autoAccept)
			throws RemoteException {
	}

	@Override
	public void onCurrentAddr(String addr) throws RemoteException {
		if(mOnPairedListener != null){
			mOnPairedListener.onCurrentAddr(addr);
		}
		Handler handler2 = FragmentBlueToothList.getHandler();
		if(handler2!=null){
			Message msg = new Message();
			msg.what = FragmentBlueToothList.MSG_CONNECT_ADDRESS;
			msg.obj = addr;
			handler2.sendMessage(msg);
		}
		Handler handler = FragmentMailList.getHandler();
		if(handler == null){
			return;
		}
		Message msg = new Message();
		msg.what = FragmentMailList.MSG_CURRENT_DEVICE_ADDRESS;
		msg.obj = addr;
		handler.sendMessage(msg);
	}

	@Override
	public void onCurrentName(String name) throws RemoteException {
	}

	// 1:鏈繛鎺� 3:宸茶繛鎺� 4锛氱數璇濇嫧鍑� 5锛氱數璇濇墦鍏� 6锛氶�氳瘽涓�
	/*
	 * 0~鍒濆鍖� 1~寰呮満鐘舵�� 2~杩炴帴涓� 3~杩炴帴鎴愬姛 4~鐢佃瘽鎷ㄥ嚭 5~鐢佃瘽鎵撳叆 6~閫氳瘽涓�
	 */
	@Override
	public void onHfpStatus(int status) throws RemoteException {
		switch (status) {
		case 0:
			hfpStatus = 0;
			break;
		case 1:
			hfpStatus = 0;
			break;
		case 2:
			hfpStatus = 0;
			break;
		case 3:
			hfpStatus = 1;
			break;
		case 4:
			hfpStatus = 5;
			break;
		case 5:
			hfpStatus = 4;
			break;
		case 6:
			hfpStatus = 6;
			break;
		}
	}

	@Override
	public void onAvStatus(int status) throws RemoteException {
		boolean connected = ((status & 0x02) != 0);
		GocsdkCallbackImp.hfpStatus = connected ? 1 : 0;
		if(mOnPairedListener != null){
			mOnPairedListener.onHfpStateChange(connected);
		}
		if(mOnAvailListener != null){
			mOnAvailListener.onHfpStateChange(connected);
		}
	}

	@Override
	public void onVersionDate(String version) throws RemoteException {		
	}

	@Override
	public void onCurrentDeviceName(String name) throws RemoteException {
		Handler handler = MainActivity.getHandler();
		if(handler==null){
			return;
		}
		Message msg = new Message();
		msg.what = MainActivity.MSG_DEVICENAME;
		msg.obj = name;
		handler.sendMessage(msg);
	}

	@Override
	public void onCurrentPinCode(String code) throws RemoteException {
		Handler handler = MainActivity.getHandler();
		if(handler==null){
			return;
		}
		Message msg = new Message();
		msg.what = MainActivity.MSG_DEVICEPINCODE;
		msg.obj = code;
		handler.sendMessage(msg);
	}

	@Override
	public void onA2dpConnected() throws RemoteException {
	}
	//閰嶅鍒楄〃
	@Override
	public void onCurrentAndPairList(int index, String name, String addr)
			throws RemoteException {
		BlueToothPairedInfo info = new BlueToothPairedInfo();
		info.index = index;
		info.name = name;
		info.address = addr;
		//Log.i("hcj.cb", "onCurrentAndPairList mOnPairedListener="+mOnPairedListener);
		if(mOnPairedListener != null){
			mOnPairedListener.onPairedDeviceAdd(info);
		}
		
		Handler handler = FragmentBlueToothList.getHandler();
		if(handler == null){
			return;
		}		
		Message msg = new Message();
		msg.obj = info;
		msg.what = FragmentBlueToothList.MSG_PAIRED_DEVICE;
		handler.sendMessage(msg);
	}

	@Override
	public void onA2dpDisconnected() throws RemoteException {
	}

	@Override
	public void onPhoneBook(String name, String number) throws RemoteException {
		Handler handler = FragmentMailList.getHandler();
		if (handler == null) {
			return;
		}
		Message msg = new Message();
		msg.what = FragmentMailList.MSG_PHONE_BOOK;
		phoneBook phonebook = new phoneBook();
		phonebook.name = name;
		phonebook.num = number;
		msg.obj = phonebook;
		handler.sendMessage(msg);
	}

	@Override
	public void onPhoneBookDone() throws RemoteException {
		Handler mainActivityHandler = MainActivity.getHandler();
		if (mainActivityHandler == null)
			return;
		mainActivityHandler
				.sendEmptyMessage(MainActivity.MSG_UPDATE_PHONEBOOK_DONE);

		Handler handler = FragmentMailList.getHandler();
		if (handler == null) {
			return;
		}
		Message msg = new Message();
		msg.what = FragmentMailList.MSG_PHONE_BOOK_DONE;
		handler.sendMessage(msg);
	}

	@Override
	public void onSimBook(String name, String number) throws RemoteException {

	}

	@Override
	public void onSimDone() throws RemoteException {

	}

	@Override
	public void onCalllog(int type, String name, String number)
			throws RemoteException {
		Handler handler = FragmentCallog.getHandler();
		if (handler == null) {
			return;
		}
		CallLogInfo info = new CallLogInfo();
		info.phonenumber = number;
		info.calltype = type;
		info.phonename = name;
		Message msg = new Message();
		msg.obj = info;
		msg.what = FragmentCallog.MSG_CALLLOG;
		handler.sendMessage(msg);
	}

	@Override
	public void onCalllogDone() throws RemoteException {
		Handler mainHandler = MainActivity.getHandler();
		mainHandler.sendEmptyMessage(MainActivity.MSG_UPDATE_CALLLOG_DONE);
		
		Handler handler = FragmentCallog.getHandler();
		if (handler == null) {
			return;
		}
		Message msg = new Message();
		msg.what = FragmentCallog.MSG_CALLLOG_DONE;
		handler.sendMessage(msg);
	}

	@Override
	public void onDiscovery(String name, String addr) throws RemoteException {		
		BlueToothInfo info = new BlueToothInfo();
		info.name = name;
		info.address = addr;
		if(mOnAvailListener != null){
			mOnAvailListener.onDiscovery(info);
		}
		
		Handler handler = FragmentBlueToothInfo.getHandler();
		Message msg = new Message();
		msg.what = FragmentBlueToothInfo.MSG_SEARCHE_DEVICE;
		msg.obj = info;
		if (handler == null) {
			return;
		}
		handler.sendMessage(msg);
	}

	@Override
	public void onDiscoveryDone() throws RemoteException {
		if(mOnAvailListener != null){
			mOnAvailListener.onDiscoveryDone();
		}
		
		Handler handler = FragmentBlueToothInfo.getHandler();
		if (handler == null) {
			return;
		}
		handler.sendEmptyMessage(FragmentBlueToothInfo.MSG_SEARCHE_DEVICE_DONE);
	}

	@Override
	public void onLocalAddress(String addr) throws RemoteException {
	}

	//寰楀埌鎷ㄥ嚭鎴栬�呴�氳瘽涓殑鍙风爜
	@Override
	public void onOutGoingOrTalkingNumber(String number) throws RemoteException {

		Handler handler = MainActivity.getHandler();
		Message msg = new Message();
		msg.obj = number;
		System.out.println("onCallSucceed---" + number);
		msg.what = MainActivity.MSG_OUTGONG;
		handler.sendMessage(msg);
	}

	@Override
	public void onConnecting() throws RemoteException {
	}

	@Override
	public void onSppData(int index, String data) throws RemoteException {
	}

	@Override
	public void onSppConnect(int index) throws RemoteException {
	}

	@Override
	public void onSppDisconnect(int index) throws RemoteException {
	}

	@Override
	public void onSppStatus(int status) throws RemoteException {
	}

	@Override
	public void onOppReceivedFile(String path) throws RemoteException {
	}

	@Override
	public void onOppPushSuccess() throws RemoteException {
	}

	@Override
	public void onOppPushFailed() throws RemoteException {
	}

	@Override
	public void onHidConnected() throws RemoteException {
	}

	@Override
	public void onHidDisconnected() throws RemoteException {
	}

	@Override
	public void onHidStatus(int status) throws RemoteException {
	}

	@Override
	public void onMusicInfo(String name, String artist, int duration, int pos,
			int total) throws RemoteException {/*
		EventBus.getDefault().post(
				new MusicInfoEvent(name, artist, duration, pos, total));
	*/}

	@Override
	public void onPanConnect() throws RemoteException {
	}

	@Override
	public void onPanDisconnect() throws RemoteException {
	}

	@Override
	public void onPanStatus(int status) throws RemoteException {

	}

	@Override
	public void onVoiceConnected() throws RemoteException {
	}

	@Override
	public void onVoiceDisconnected() throws RemoteException {

	}

	@Override
	public void onProfileEnbled(boolean[] enabled) throws RemoteException {
	}

	@Override
	public void onMessageInfo(String content_order, String read_status,
			String time, String name, String num, String title)
			throws RemoteException {/*
		EventBus.getDefault().post(
				new MessageListEvent(content_order,
						read_status.equals("1") ? true : false, time, name,
						num, title));
	*/}

	@Override
	public void onMessageContent(String content) throws RemoteException {
		//EventBus.getDefault().post(new MessageTextEvent(content));
	}

	@Override
	public void onPhoneBookNotShare() throws RemoteException {
		Handler mainHandler = MainActivity.getHandler();
		mainHandler.sendEmptyMessage(MainActivity.MSG_PHONEBOOK_NOT_SHARE);
	}

	
	public interface OnPairedListener{
		void onHfpStateChange(boolean connected);
		void onPairedDeviceAdd(BlueToothPairedInfo info);
		void onCurrentAddr(String addr);
	}
	
	private OnPairedListener mOnPairedListener;
	public void setOnPairedListener(OnPairedListener listener){
		mOnPairedListener = listener;
	}
	
	public interface OnAvailListener{
		void onHfpStateChange(boolean connected);
		void onDiscovery(BlueToothInfo info);
		void onDiscoveryDone();
	}
	
	private OnAvailListener mOnAvailListener;
	public void setOnAvailListener(OnAvailListener listener){
		mOnAvailListener = listener;
	}
}
