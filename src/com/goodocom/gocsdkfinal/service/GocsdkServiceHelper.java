package com.goodocom.gocsdkfinal.service;

import java.util.ArrayList;

import com.goodocom.gocsdk.IGocsdkCallback;
import com.goodocom.gocsdk.IGocsdkService;
import com.goodocom.gocsdk.IGocsdkServiceSimple;
import com.goodocom.gocsdkfinal.Commands;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

public class GocsdkServiceHelper {
	private static final String TAG = "hcj.serial";
	private IGocsdkServiceSimple mGocsdkService;
	private GocsdkService mLocalService;
	private GocsdkConnection mConnection;
	private ArrayList<OnServiceConnectListener> mListeners;
	
	private class GocsdkConnection implements ServiceConnection {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mGocsdkService = IGocsdkServiceSimple.Stub.asInterface(service);
			mLocalService = GocsdkService.getInstance();
			Log.i("hcj.GocsdkExtService", "onServiceConnected mGocsdkService="+mGocsdkService);
			/*
			if(mOnServiceConnectListener != null){
				mOnServiceConnectListener.onServiceConnected(mGocsdkService);
			}*/
			for(int i=0;i<mListeners.size();i++){
				OnServiceConnectListener listener = mListeners.get(i);
				listener.onServiceConnected(mGocsdkService);
			}
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mGocsdkService = null;
			mLocalService = null;
			Log.i("hcj.GocsdkExtService", "onServiceDisconnected mGocsdkService="+mGocsdkService);
			/*
			if(mOnServiceConnectListener != null){
				mOnServiceConnectListener.onServiceDisconnected();
			}*/
			for(int i=0;i<mListeners.size();i++){
				OnServiceConnectListener listener = mListeners.get(i);
				listener.onServiceDisconnected();
			}
		}
	}
	
	public GocsdkServiceHelper(OnServiceConnectListener listener){
		mListeners = new ArrayList<OnServiceConnectListener>();
		mListeners.add(listener);
		//mOnServiceConnectListener = listener;
	}
	
	public void registerListener(OnServiceConnectListener listener){
		mListeners.add(listener);
	}
	
	public void unregisterListener(OnServiceConnectListener listener){
		mListeners.remove(listener);
	}
	
	public void bindService(Context context){
		Intent intent = new Intent();
		intent.setComponent(new ComponentName("com.goodocom.gocsdkfinal","com.goodocom.gocsdkfinal.service.GocsdkService"));
		if(mConnection == null){
			mConnection = new GocsdkConnection();
		}
		context.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
	}
	
	public void unbindService(Context context){
		context.unbindService(mConnection);
	}
	/*
	public String getInCallNumber(){
		return (mLoclService == null ) ? null : mLoclService.getInCallNumber();
	}
	
	public String getInCallName(){
		return (mLoclService == null ) ? null : mLoclService.getInCallName();
	}
	*/
	public interface OnServiceConnectListener{
		void onServiceConnected(IGocsdkServiceSimple service);
		void onServiceDisconnected();
	} 
	
	private OnServiceConnectListener mOnServiceConnectListener;

//local interface @{
	public void endLocalCall(){
		mLocalService.endLocalCall();
	}
//local interface @}
	
//remote interface @{
	public void  setBtSwitch(boolean open){
		try{
			mGocsdkService.setBtSwitch(open);
		}catch(Exception e){
			
		}
	}
	
	public boolean isBtOpen(){
		boolean isOpen = false;
		try{
			isOpen = mGocsdkService.isBtOpen();
		}catch(Exception e){
			
		}
		return isOpen;
	}
	
	public boolean isBtConnected(){
		boolean isConnected = false;
		try{
			isConnected = mGocsdkService.isBtConnected();
		}catch(Exception e){
			
		}
		return isConnected;
	}
	
	public void dial(String number){
		try{
			mGocsdkService.dial(number);
		}catch(Exception e){
			
		}
	}
	
	public boolean isInCall(){
		boolean isInCall = false;
		try{
			isInCall = mGocsdkService.isInCall();
		}catch(Exception e){
			
		}
		return isInCall;
	}
	
	public void endCall(){
		try{
			mGocsdkService.endCall();
		}catch(Exception e){
			
		}
	}
	
	public void acceptCall(){
		try{
			mGocsdkService.acceptCall();
		}catch(Exception e){
			
		}
	}
	
	
	public void queryBt(){
		try{
			mGocsdkService.sendCommand("QS");
		}catch(Exception e){
			
		}
	}
	
	public void openBt(){
		try{
			mGocsdkService.sendCommand(Commands.BT_OPEN);
		}catch(Exception e){
			
		}
	}
	
	public void closeBt(){
		try{
			mGocsdkService.sendCommand(Commands.BT_CLOSE);
		}catch(Exception e){
			
		}
	}
	
	public void restBluetooth(){
		try{
			mGocsdkService.sendCommand(Commands.RESET_BLUE);
		}catch(Exception e){
			
		}
	}
	
	public void getLocalName(){
		try{
			mGocsdkService.sendCommand(Commands.MODIFY_LOCAL_NAME);
		}catch(Exception e){
			
		}
	}
	
	public void setLocalName(String name){
		try{
			mGocsdkService.sendCommand(Commands.MODIFY_LOCAL_NAME+name);
		}catch(Exception e){
			
		}
	}
	
	public void getPinCode() throws RemoteException {
		mGocsdkService.sendCommand(Commands.MODIFY_PIN_CODE);
	}

	
	public void setPinCode(String pincode){
		try{
			mGocsdkService.sendCommand(Commands.MODIFY_PIN_CODE+pincode);
		}catch(Exception e){
			
		}
	}
	
	
	public void getLocalAddress() throws RemoteException{
		mGocsdkService.sendCommand(Commands.LOCAL_ADDRESS);
	}
	
	
	public void getAutoConnectAnswer() throws RemoteException{
		mGocsdkService.sendCommand(Commands.INQUIRY_AUTO_CONNECT_ACCETP);
	}
	
	
	public void setAutoConnect(){
		try{
			mGocsdkService.sendCommand(Commands.SET_AUTO_CONNECT_ON_POWER);
		}catch(Exception e){
			
		}		
	}
	
	 
	public void cancelAutoConnect(){		
		try{
			mGocsdkService.sendCommand(Commands.UNSET_AUTO_CONNECT_ON_POWER);
		}catch(Exception e){
			
		}
	}
	
	 
	public void setAutoAnswer(){		
		try{
			mGocsdkService.sendCommand(Commands.SET_AUTO_ANSWER);
		}catch(Exception e){
			
		}
	}
	
	
	public void cancelAutoAnswer(){		
		try{
			mGocsdkService.sendCommand(Commands.UNSET_AUTO_ANSWER);
		}catch(Exception e){
			
		}
	}
	
	
	public void getVersion() throws RemoteException{
		mGocsdkService.sendCommand(Commands.INQUIRY_VERSION_DATE);
	}

//connect
	
	public void setPairMode() throws RemoteException{
		mGocsdkService.sendCommand(Commands.PAIR_MODE);
	}
	
	
	public void cancelPairMode() throws RemoteException{
		mGocsdkService.sendCommand(Commands.CANCEL_PAIR_MOD);
	}
	
	
	public void connectLast() throws RemoteException{
		mGocsdkService.sendCommand(Commands.CONNECT_DEVICE);
	}
	
	
	public void connectA2dp(String addr) throws RemoteException {
		mGocsdkService.sendCommand(Commands.CONNECT_A2DP+addr);
	}

	
	public void connectHFP(String addr) throws RemoteException {
		mGocsdkService.sendCommand(Commands.CONNECT_HFP+addr);
	}
	
	
	public void connectHid(String addr) throws RemoteException{
		mGocsdkService.sendCommand(Commands.CONNECT_HID);
	}
	
	
	public void connectSpp(String addr) throws RemoteException{
		mGocsdkService.sendCommand(Commands.CONNECT_SPP_ADDRESS);
	}

	
	public void disconnect(){
		try{
			mGocsdkService.sendCommand(Commands.DISCONNECT_DEVICE);
		}catch(Exception e){
			
		}
	}

	
	public void disconnectA2DP() throws RemoteException {
		mGocsdkService.sendCommand(Commands.DISCONNECT_A2DP);
	}

	
	public void disconnectHFP() throws RemoteException {
		mGocsdkService.sendCommand(Commands.DISCONNECT_HFP);
	}
	
	 
	public void disconnectHid() throws RemoteException {
		mGocsdkService.sendCommand(Commands.DISCONNECT_HID);
	}
	
	
	public void disconnectSpp() throws RemoteException{
		mGocsdkService.sendCommand(Commands.SPP_DISCONNECT);
	}

//devices list
	
	public void deletePair(String addr) throws RemoteException {
		mGocsdkService.sendCommand(Commands.DELETE_PAIR_LIST+addr);
	}

	
	public void startDiscovery(){
		try{
			mGocsdkService.sendCommand(Commands.START_DISCOVERY);
		}catch(Exception e){
			
		}
	}

	
	public void getPairList(){
		try{
			mGocsdkService.sendCommand(Commands.INQUIRY_PAIR_RECORD);
		}catch(Exception e){
			
		}
	}

	
	public void stopDiscovery(){
		try{
			mGocsdkService.sendCommand(Commands.STOP_DISCOVERY);
		}catch(Exception e){
			
		}
	}

//hfp	
	
	public void phoneAnswer(){		
		try{
			mGocsdkService.sendCommand(Commands.ACCEPT_INCOMMING);
		}catch(Exception e){
			
		}
	}
	
	public void phoneHangUp(){
		try{
			mGocsdkService.sendCommand(Commands.REJECT_INCOMMMING);
		}catch(Exception e){
			
		}
	}
	
	public void phoneDail(String phonenum) throws RemoteException {
		mGocsdkService.sendCommand(Commands.DIAL+phonenum);
	}

	
	public void phoneTransmitDTMFCode(char code) throws RemoteException {
		mGocsdkService.sendCommand(Commands.DTMF+code);
	}
	
	
	
	
	public void phoneTransfer() throws RemoteException {
		mGocsdkService.sendCommand(Commands.VOICE_TRANSFER);
	}

	
	public void phoneTransferBack() throws RemoteException {
		mGocsdkService.sendCommand(Commands.VOICE_TO_BLUE);
	}
	
	
	public void phoneVoiceDail() throws RemoteException{
		mGocsdkService.sendCommand(Commands.VOICE_DIAL);
	}
	
	
	public void cancelPhoneVoiceDail() throws RemoteException{
		mGocsdkService.sendCommand(Commands.CANCEL_VOID_DIAL);
	}

//CONTACTS
	
	public void phoneBookStartUpdate() throws RemoteException {
		mGocsdkService.sendCommand(Commands.SET_PHONE_PHONE_BOOK);
	}

	
	public void callLogstartUpdate(int type) throws RemoteException {
		System.out.println("鍘讳笅杞介�氳瘽璁板綍鍟�");
		switch (type) {
		case 1:
			mGocsdkService.sendCommand(Commands.SET_OUT_GOING_CALLLOG);
			break;
		case 2:			
			mGocsdkService.sendCommand(Commands.SET_MISSED_CALLLOG);
			break;
		case 3:
			mGocsdkService.sendCommand(Commands.SET_INCOMING_CALLLOG);
			break;
		default:
			break;
		}
	}

//MUSIC	
	
	public void musicPlayOrPause() throws RemoteException {
		mGocsdkService.sendCommand(Commands.PLAY_PAUSE_MUSIC);
	}

	
	public void musicStop() throws RemoteException {
		mGocsdkService.sendCommand(Commands.STOP_MUSIC);
	}

	
	public void musicPrevious() throws RemoteException {
		mGocsdkService.sendCommand(Commands.PREV_SOUND);
	}

	
	public void musicNext() throws RemoteException {
		mGocsdkService.sendCommand(Commands.NEXT_SOUND);
	}

	
	public void musicMute() throws RemoteException{
		mGocsdkService.sendCommand(Commands.MUSIC_MUTE);
	}
	
	
	public void musicUnmute() throws RemoteException{
		mGocsdkService.sendCommand(Commands.MUSIC_UNMUTE);
	}

	
	public void musicBackground() throws RemoteException{
		mGocsdkService.sendCommand(Commands.MUSIC_BACKGROUND);
	}

	
	public void musicNormal() throws RemoteException{
		mGocsdkService.sendCommand(Commands.MUSIC_NORMAL);
	}
	
	
	public void registerCallback(IGocsdkCallback callback){
		try{
			mGocsdkService.registerCallback(callback);
		}catch(Exception e){
			
		}
	}
	
	public void unregisterCallback(IGocsdkCallback callback){
		try{
			mGocsdkService.unregisterCallback(callback);
		}catch(Exception e){
			
		}
	}
	
	public void hidMouseMove(String point) throws RemoteException {
		mGocsdkService.sendCommand(Commands.MOUSE_MOVE+ point); 
	}
	
	public void hidMouseUp(String point) throws RemoteException {
		mGocsdkService.sendCommand(Commands.MOUSE_MOVE +point);
	}
	
	public void hidMousDown(String point) throws RemoteException {
		mGocsdkService.sendCommand(Commands.MOUSE_DOWN +point);
	}
	
	public void hidHomeClick() throws RemoteException {
		mGocsdkService.sendCommand(Commands.MOUSE_HOME);
	}
	
	public void hidBackClick() throws RemoteException {
		mGocsdkService.sendCommand(Commands.MOUSE_BACK);
	}
	
	public void hidMenuClick() throws RemoteException {
		mGocsdkService.sendCommand(Commands.MOUSE_MENU);
	}
	
	public void sppSendData(String addr, String data) throws RemoteException {
		mGocsdkService.sendCommand(Commands.SPP_SEND_DATA + addr + data);
	}
	
	
	public void getMusicInfo(){
		try{
			mGocsdkService.sendCommand(Commands.GET_MUSIC_INFO);
		}catch(Exception e){
			
		}
	}
	
	
	public void inqueryHfpStatus() {		
		try{
			mGocsdkService.sendCommand(Commands.INQUIRY_HFP_STATUS);
		}catch(Exception e){
			
		}
	}
	
	
	public void getCurrentDeviceAddr(){		
		try{
			mGocsdkService.sendCommand(Commands.INQUIRY_CUR_BT_ADDR);
		}catch(Exception e){
			
		}
	}
	
	public void getCurrentDeviceName() {		
		try{
			mGocsdkService.sendCommand(Commands.INQUIRY_CUR_BT_NAME);
		}catch(Exception e){
			
		}
	}
	
	public void connectDevice(String addr){
		try{
			mGocsdkService.sendCommand(Commands.CONNECT_DEVICE + addr);
		}catch(Exception e){
			
		}
	}
	
	public void setProfileEnabled(boolean[] enabled) throws RemoteException {
		String str = "";
		for(int i=0;(i<enabled.length) && (i<10);i++){
			if(enabled[i])str += "1";
			else str += "0";
		}
		int len = str.length();
		for (int i = 0; i < 10-len; i++) {
			str += "0";
		}
		mGocsdkService.sendCommand(Commands.SET_PROFILE_ENABLED+str);
	}
	
	public void getProfileEnabled() throws RemoteException {
		mGocsdkService.sendCommand(Commands.SET_PROFILE_ENABLED);
	}
	
	
	public void getMessageInboxList() throws RemoteException {
		mGocsdkService.sendCommand(Commands.GET_MESSAGE_INBOX_LIST);		
	}
	
	
	public void getMessageText(String handle) throws RemoteException {
		mGocsdkService.sendCommand(Commands.GET_MESSAGE_TEXT + handle);		
	}
	
	public void getMessageSentList() throws RemoteException {
		mGocsdkService.sendCommand(Commands.GET_MESSAGE_SENT_LIST);			
	}
	
	public void getMessageDeletedList() throws RemoteException {
		mGocsdkService.sendCommand(Commands.GET_MESSAGE_DELETED_LIST);			
	}
	
	public void pauseDownLoadContact() throws RemoteException {
		mGocsdkService.sendCommand(Commands.PAUSE_DOWNLOEA_CONTACT);
	}
//@}
}
