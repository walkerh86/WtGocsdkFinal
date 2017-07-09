package com.goodocom.gocsdk;

import com.goodocom.gocsdk.IGocsdkCallback;

interface IGocsdkService {
	void openBt();
	void closeBt();
	void queryBtSwitch();
	
	//钃濈墮鐘舵�佸洖璋冩敞鍐屽幓娉ㄩ攢
	void registerCallback(IGocsdkCallback callback);
	// 娉ㄩ攢钃濈墮鐘舵��
	void unregisterCallback(IGocsdkCallback callback);
	
	//娉ㄩ噴鍚庨潰甯︾殑涓烘搷浣滃悗鐩稿簲鐨勫洖璋冨洖澶�
//setting
	//钃濈墮鍗忚杞浣�  ---銆� onInitSucceed()
	void restBluetooth();
	
	//鑾峰彇鏈湴钃濈墮鍚嶇О  ---銆媜nCurrentDeviceName()
	void getLocalName();
	
	//璁剧疆鏈湴钃濈墮鍚嶇О  ---銆媜nCurrentDeviceName()
	void setLocalName(String name);
	
	//鑾峰彇钃濈墮pin鐮�  ---銆媜nCurrentPinCode()
	void getPinCode();
	
	//璁剧疆钃濈墮pin鐮� 4bit  ---銆媜nCurrentPinCode()
	void setPinCode(String pincode);
	
	//鑾峰彇鏈湴钃濈墮鍦板潃   ---銆媜nLocalAddress()
	void getLocalAddress();
	
	//鑾峰彇鑷姩杩炴帴鍙婅嚜鍔ㄦ帴鍚�   ---銆媜nAutoConnectAccept()
	void getAutoConnectAnswer();
	
	//璁剧疆鑷姩杩炴帴  ---銆媜nAutoConnectAccept()
	void setAutoConnect();
	
	//鍙栨秷鑷姩杩炴帴  ---銆媜nAutoConnectAccept()
	void cancelAutoConnect();
	
	//璁剧疆鑷姩鎺ュ惉 ---銆媜nAutoConnectAccept()
	void setAutoAnswer();
	
	//鍙栨秷鑷姩鎺ュ惉   ---銆媜nAutoConnectAccept()
	void cancelAutoAnswer();
	
	//鑾峰彇钃濈墮鐗堟湰淇℃伅  ---銆媜nVersionDate()
	void getVersion();

//connect info
	//杩涘叆閰嶅妯″紡(钃濈墮鍙)  ---銆媜nInPairMode()
	void setPairMode();
	
	//閫�鍑洪厤瀵规ā寮�(钃濈墮涓嶅彲瑙�)   ---銆媜nExitPairMode()
	void cancelPairMode();

	//杩炴帴涓婃杩炴帴杩囩殑璁惧   ---銆媜nHfpConnected() onA2dpConnected() onCurrentAndPairList() etc;
	void connectLast();
	
	//杩炴帴鎸囧畾鍦板潃璁惧 鍦板潃鍙粠鎼滅储鎴栭厤瀵瑰垪琛ㄤ腑鑾峰彇   ---銆嬪洖璋冨悓涓�
	void connectDevice(String addr);
	
	//杩炴帴鎸囧畾鍦板潃a2dp鏈嶅姟  ---銆媜nA2dpConnected()
	void connectA2dp(String addr);

	//杩炴帴鎸囧畾鍦板潃hfp鏈嶅姟  ---銆媜nHfpConnected()
	void connectHFP(String addr);
	
	//杩炴帴鎸囧畾鍦板潃hid鏈嶅姟  ---銆媜nHidConnected()
	void connectHid(String addr);
	
	//杩炴帴鎸囧畾鍦板潃spp鏈嶅姟  ---銆媜nSppConnect()
	void connectSpp(String addr);

	//鏂紑褰撳墠杩炴帴璁惧鐨勬墍鏈夋湇鍔� --->onSppDisconnect() onHfpDisconnected() onA2dpDisconnected() onHidDisconnected() etc;
	void disconnect();
	
	//鏂紑褰撳墠杩炴帴璁惧鐨凙2DP鏈嶅姟  ---銆媜nA2dpDisconnected()
	void disconnectA2DP();

	//鏂紑褰撳墠杩炴帴璁惧鐨凥FP鏈嶅姟  ---銆媜nHfpDisconnected()
	void disconnectHFP();
	
	//鏂紑褰撳墠杩炴帴璁惧鐨凥ID鏈嶅姟  ---銆媜nHidDisconnected()
	void disconnectHid();
	
	//鏂紑褰撳墠杩炴帴璁惧鐨剆pp鏈嶅姟  ---銆媜nSppDisconnect()
	void disconnectSpp();
	
//devices list
	//鍒犻櫎鎸囧畾鍦板潃鐨勯厤瀵瑰垪琛�  ---銆嬪垹闄ゆ垚鍔熷洖澶岻ND_OK澶辫触鍥炲IND_ERROR(鏆傛湭鍋歝allback澶勭悊)
	void deletePair(String addr);

	//寮�濮嬫悳绱㈠懆杈硅摑鐗欒澶�  ---銆媜nDiscovery()
	void startDiscovery();

	//鑾峰彇褰撳墠閰嶅鍒楄〃  ---銆媜nCurrentAndPairList()
	void getPairList();

	//鍋滄钃濈墮鎼滅储  ---銆媜nDiscoveryDone()
	void stopDiscovery();
	
//hfp
	//鏉ョ數鎺ュ惉  ---銆媜nTalking()
	void phoneAnswer();

	//鎸傛柇鐢佃瘽  ---銆媜nHangUp()
	void phoneHangUp();

	//鎷ㄦ墦鐢佃瘽  ---銆媜nCallSucceed()
	void phoneDail(String phonenum);

	//鎷ㄦ墦鍒嗘満鍙�
	void phoneTransmitDTMFCode(char code);
	
	//鍒囨崲澹伴亾鍒版墜鏈虹  ---銆媜nHfpRemote() onVoiceDisconnected()
	void phoneTransfer();

	//鍒囨崲澹伴亾鍒拌溅鏈虹  ---銆媜nHfpLocal() onVoiceConnected()
	void phoneTransferBack();
	
	//璇煶鎷ㄥ彿  ---銆媜nTalking()
	void phoneVoiceDail();
	
	//鍙栨秷璇煶鎷ㄥ彿  ---銆媜nHangUp()
	void cancelPhoneVoiceDail();
	
//contacts
	//鐢佃瘽鏈笅杞�  ---onPhoneBook() onPhoneBookDone()
	void phoneBookStartUpdate();
	
	//閫氳瘽璁板綍涓嬭浇  ---銆媜nCalllog() onCalllogDone()
	void callLogstartUpdate(int type);
	
//music
	//闊充箰鎾斁鎴栨殏鍋�  ---銆媜nMusicPlaying() onMusicStopped() onMusicInfo()
	void musicPlayOrPause();

	//闊充箰鍋滄  ---銆媜nMusicStopped()
	void musicStop();

	//涓婁竴鏇�
	void musicPrevious();

	//涓嬩竴鏇�
	void musicNext();
	
	//闊充箰闈欓煶 鐢ㄤ簬娣烽煶澶勭悊
	void musicMute();
	
	//闊充箰瑙ｉ櫎闈欓煶 閰嶅悎mute瀹炵幇娣烽煶澶勭悊
	void musicUnmute();
	
	//闊充箰鍗婇煶锛岀敤浜嶨ps鍑哄０鏃舵贩闊冲鐞�
	void musicBackground();
	
	//闊充箰鎭㈠姝ｅ父 閰嶅悎鍗婇煶澶勭悊Gps娣烽煶鍑哄０闂
	void musicNormal();
	
//hid
	//榧犳爣绉诲姩
	//point 8bit锛� 4bit x 銆�4bit y
	void hidMouseMove(String point);
	
	//榧犳爣鎶捣
	//point 8bit锛� 4bit x 銆�4bit y
	void hidMouseUp(String point);
	
	//榧犳爣鎸変笅
	//point 8bit锛� 4bit x 銆�4bit y
	void hidMousDown(String point);
	
	//hid home鎸夐挳
	void hidHomeClick();
	
	//hid 杩斿洖鎸夐挳
	void hidBackClick();
	
	//hid 鑿滃崟鎸夐挳
	void hidMenuClick(); 	
	
//spp
	//spp 鏁版嵁鍙戦��  --銆媜nSppData()
	void sppSendData(String addr ,String data);	
	
	//鑾峰彇钃濈墮闊充箰淇℃伅 ---銆媜nMusicInfo()
	void getMusicInfo();
	
	//鏌ヨ褰撳墠hfp鐘舵�� ---銆媜nHfpStatus()
	void inqueryHfpStatus();
	
	//鏌ヨ褰撳墠杩炴帴璁惧鐨勫湴鍧�
	void getCurrentDeviceAddr();
		
	//鏌ヨ褰撳墠杩炴帴璁惧鐨勫悕绉�
	void getCurrentDeviceName();
	
	//鏆傚仠涓嬭浇鑱旂郴浜�
	void pauseDownLoadContact();
	
	
	//浠ヤ笅鍔熻兘鏆傛湭寮�鏀�
	void setProfileEnabled(in boolean[] enabled);
	void getProfileEnabled();
	
	void getMessageSentList();
	void getMessageDeletedList();
	void getMessageInboxList();
	void getMessageText(String handle);
}