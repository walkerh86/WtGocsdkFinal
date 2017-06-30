package com.goodocom.gocsdkfinal.service;

import android.os.RemoteException;


import com.goodocom.gocsdk.IGocsdkCallback;
import com.goodocom.gocsdk.IGocsdkService;
import com.goodocom.gocsdkfinal.Commands;

public class GocsdkServiceImp extends IGocsdkService.Stub {
	private GocsdkService service;
	public  GocsdkServiceImp(GocsdkService service){
		this.service = service;
	}
	private void write(String str){
		service.write(str);
	}

	@Override
	public void openBt() throws RemoteException{
		//write(Commands.BT_OPEN);
		service.setBtSwitch(true);
	}

	@Override
	public void closeBt() throws RemoteException{
		//write(Commands.BT_CLOSE);
		service.setBtSwitch(false);
	}
	
	@Override
	public void queryBtSwitch() throws RemoteException {
		write("QS");
	}

	@Override
	public void restBluetooth() throws RemoteException{
		write(Commands.RESET_BLUE);
	}
	
	@Override
	public void getLocalName() throws RemoteException {
		write(Commands.MODIFY_LOCAL_NAME);
	}

	@Override
	public void setLocalName(String name) throws RemoteException {
		write(Commands.MODIFY_LOCAL_NAME+name);
	}

	@Override
	public void getPinCode() throws RemoteException {
		write(Commands.MODIFY_PIN_CODE);
	}

	@Override
	public void setPinCode(String pincode) throws RemoteException {
		write(Commands.MODIFY_PIN_CODE+pincode);
	}
	
	@Override
	public void getLocalAddress() throws RemoteException{
		write(Commands.LOCAL_ADDRESS);
	}
	
	@Override
	public void getAutoConnectAnswer() throws RemoteException{
		write(Commands.INQUIRY_AUTO_CONNECT_ACCETP);
	}
	
	@Override
	public void setAutoConnect() throws RemoteException{
		write(Commands.SET_AUTO_CONNECT_ON_POWER);
	}
	
	@Override 
	public void cancelAutoConnect() throws RemoteException{
		write(Commands.UNSET_AUTO_CONNECT_ON_POWER);
	}
	
	@Override 
	public void setAutoAnswer() throws RemoteException{
		write(Commands.SET_AUTO_ANSWER);
	}
	
	@Override
	public void cancelAutoAnswer() throws RemoteException{
		write(Commands.UNSET_AUTO_ANSWER);
	}
	
	@Override
	public void getVersion() throws RemoteException{
		write(Commands.INQUIRY_VERSION_DATE);
	}

//connect
	@Override
	public void setPairMode() throws RemoteException{
		write(Commands.PAIR_MODE);
	}
	
	@Override
	public void cancelPairMode() throws RemoteException{
		write(Commands.CANCEL_PAIR_MOD);
	}
	
	@Override
	public void connectLast() throws RemoteException{
		write(Commands.CONNECT_DEVICE);
	}
	
	@Override
	public void connectA2dp(String addr) throws RemoteException {
		write(Commands.CONNECT_A2DP+addr);
	}

	@Override
	public void connectHFP(String addr) throws RemoteException {
		write(Commands.CONNECT_HFP+addr);
	}
	
	@Override
	public void connectHid(String addr) throws RemoteException{
		write(Commands.CONNECT_HID);
	}
	
	@Override
	public void connectSpp(String addr) throws RemoteException{
		write(Commands.CONNECT_SPP_ADDRESS);
	}

	@Override
	public void disconnect() throws RemoteException {
		write(Commands.DISCONNECT_DEVICE);
	}

	@Override
	public void disconnectA2DP() throws RemoteException {
		write(Commands.DISCONNECT_A2DP);
	}

	@Override
	public void disconnectHFP() throws RemoteException {
		write(Commands.DISCONNECT_HFP);
	}
	
	@Override 
	public void disconnectHid(){
		write(Commands.DISCONNECT_HID);
	}
	
	@Override
	public void disconnectSpp() throws RemoteException{
		write(Commands.SPP_DISCONNECT);
	}

//devices list
	@Override
	public void deletePair(String addr) throws RemoteException {
		write(Commands.DELETE_PAIR_LIST+addr);
	}

	@Override
	public void startDiscovery() throws RemoteException {
		write(Commands.START_DISCOVERY);
	}

	@Override
	public void getPairList() throws RemoteException {
		write(Commands.INQUIRY_PAIR_RECORD);
	}

	@Override
	public void stopDiscovery() throws RemoteException {
		write(Commands.STOP_DISCOVERY);
	}

//hfp	
	@Override
	public void phoneAnswer() throws RemoteException {
		write(Commands.ACCEPT_INCOMMING);
	}

	@Override
	public void phoneHangUp() throws RemoteException {
		write(Commands.REJECT_INCOMMMING);
	}

	@Override
	public void phoneDail(String phonenum) throws RemoteException {
		write(Commands.DIAL+phonenum);
	}

	@Override
	public void phoneTransmitDTMFCode(char code) throws RemoteException {
		write(Commands.DTMF+code);
	}
	
	
	
	@Override
	public void phoneTransfer() throws RemoteException {
		write(Commands.VOICE_TRANSFER);
	}

	@Override
	public void phoneTransferBack() throws RemoteException {
		write(Commands.VOICE_TO_BLUE);
	}
	
	@Override
	public void phoneVoiceDail() throws RemoteException{
		write(Commands.VOICE_DIAL);
	}
	
	@Override
	public void cancelPhoneVoiceDail() throws RemoteException{
		write(Commands.CANCEL_VOID_DIAL);
	}

//CONTACTS
	@Override
	public void phoneBookStartUpdate() throws RemoteException {
		write(Commands.SET_PHONE_PHONE_BOOK);
	}

	@Override
	public void callLogstartUpdate(int type) throws RemoteException {
		System.out.println("鍘讳笅杞介�氳瘽璁板綍鍟�");
		switch (type) {
		case 1:
			write(Commands.SET_OUT_GOING_CALLLOG);
			break;
		case 2:			
			write(Commands.SET_MISSED_CALLLOG);
			break;
		case 3:
			write(Commands.SET_INCOMING_CALLLOG);
			break;
		default:
			break;
		}
	}

//MUSIC	
	@Override
	public void musicPlayOrPause() throws RemoteException {
		write(Commands.PLAY_PAUSE_MUSIC);
	}

	@Override
	public void musicStop() throws RemoteException {
		write(Commands.STOP_MUSIC);
	}

	@Override
	public void musicPrevious() throws RemoteException {
		write(Commands.PREV_SOUND);
	}

	@Override
	public void musicNext() throws RemoteException {
		write(Commands.NEXT_SOUND);
	}

	@Override
	public void musicMute() throws RemoteException{
		write(Commands.MUSIC_MUTE);
	}
	
	@Override
	public void musicUnmute() throws RemoteException{
		write(Commands.MUSIC_UNMUTE);
	}

	@Override
	public void musicBackground() throws RemoteException{
		write(Commands.MUSIC_BACKGROUND);
	}

	@Override
	public void musicNormal() throws RemoteException{
		write(Commands.MUSIC_NORMAL);
	}
	
	@Override
	public void registerCallback(IGocsdkCallback callback)
			throws RemoteException {
		service.registerCallback(callback);
	}

	@Override
	public void unregisterCallback(IGocsdkCallback callback)
			throws RemoteException {
		service.unregisterCallback(callback);
	}
	@Override
	public void hidMouseMove(String point) throws RemoteException {
		write(Commands.MOUSE_MOVE+ point); 
	}
	@Override
	public void hidMouseUp(String point) throws RemoteException {
		write(Commands.MOUSE_MOVE +point);
	}
	@Override
	public void hidMousDown(String point) throws RemoteException {
		write(Commands.MOUSE_DOWN +point);
	}
	@Override
	public void hidHomeClick() throws RemoteException {
		write(Commands.MOUSE_HOME);
	}
	@Override
	public void hidBackClick() throws RemoteException {
		write(Commands.MOUSE_BACK);
	}
	@Override
	public void hidMenuClick() throws RemoteException {
		write(Commands.MOUSE_MENU);
	}
	@Override
	public void sppSendData(String addr, String data) throws RemoteException {
		write(Commands.SPP_SEND_DATA + addr + data);
	}
	
	@Override
	public void getMusicInfo(){
		write(Commands.GET_MUSIC_INFO);
	}
	
	@Override
	public void inqueryHfpStatus() {
		write(Commands.INQUIRY_HFP_STATUS);
	}
	
	@Override
	public void getCurrentDeviceAddr(){
		write(Commands.INQUIRY_CUR_BT_ADDR);
	}
	@Override
	public void getCurrentDeviceName() {
		write(Commands.INQUIRY_CUR_BT_NAME);
	}
	@Override
	public void connectDevice(String addr) throws RemoteException {
		write(Commands.CONNECT_DEVICE + addr);
	}
	@Override
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
		write(Commands.SET_PROFILE_ENABLED+str);
	}
	@Override
	public void getProfileEnabled() throws RemoteException {
		write(Commands.SET_PROFILE_ENABLED);
	}
	
	@Override
	public void getMessageInboxList() throws RemoteException {
		write(Commands.GET_MESSAGE_INBOX_LIST);		
	}
	
	@Override
	public void getMessageText(String handle) throws RemoteException {
		write(Commands.GET_MESSAGE_TEXT + handle);		
	}
	@Override
	public void getMessageSentList() throws RemoteException {
		write(Commands.GET_MESSAGE_SENT_LIST);			
	}
	@Override
	public void getMessageDeletedList() throws RemoteException {
		write(Commands.GET_MESSAGE_DELETED_LIST);			
	}
	@Override
	public void pauseDownLoadContact() throws RemoteException {
		write(Commands.PAUSE_DOWNLOEA_CONTACT);
	}
}
