package com.goodocom.gocsdk;
interface IGocsdkCallback{
	//callback回调的信息详情请参照CommandParser.java中相对应的回复由此查询Commands.java中对于意义
	void onHfpDisconnected ();
	void onHfpConnected ();
	void onCallSucceed (String number);
	void onIncoming (String number);
	void onHangUp ();  
	void onTalking (String number);
	void onRingStart ();
	void onRingStop ();
	void onHfpLocal ();
	void onHfpRemote ();
	void onInPairMode ();
	void onExitPairMode ();
	void onInitSucceed ();
	void onMusicPlaying ();
	void onMusicStopped ();
	void onVoiceConnected ();
	void onVoiceDisconnected ();
	void onAutoConnectAccept (boolean autoConnect,boolean autoAccept);
	void onCurrentAddr (String addr);
	void onCurrentName (String name);
	
	void onHfpStatus (int status);
	void onAvStatus (int status);
	void onVersionDate (String version);
	void onCurrentDeviceName (String name);
	void onCurrentPinCode (String code);
	void onA2dpConnected ();
	void onCurrentAndPairList (int index,String name,String addr);
	void onA2dpDisconnected ();
	void onPhoneBook (String name,String number);
	void onSimBook (String name,String number);
	void onPhoneBookDone ();
	void onSimDone ();
	void onCalllogDone ();
	void onCalllog (int type,String name,String number);
	void onDiscovery (String name,String addr);
	void onDiscoveryDone ();
	void onLocalAddress (String addr);
	void onHidConnected();
	void onHidDisconnected();
	void onMusicInfo(String music_name, String artist_nameString,int duration,int pos,int total);	
	void onOutGoingOrTalkingNumber(String number);
	void onConnecting();
	void onSppData(int index, String data);
	void onSppConnect(int index);
	void onSppDisconnect(int index);
	void onSppStatus(int status);
	void onOppReceivedFile(String path);
	void onOppPushSuccess();
	void onOppPushFailed();
	void onHidStatus(int status);
	void onPanConnect();
	void onPanDisconnect();
	void onPanStatus(int status);
	void onProfileEnbled(in boolean[] enabled);
	void onMessageInfo(String content_order,String read_status,String time,String name,String num,String title);
	void onMessageContent(String content);
}