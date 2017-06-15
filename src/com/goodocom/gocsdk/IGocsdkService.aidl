package com.goodocom.gocsdk;

import com.goodocom.gocsdk.IGocsdkCallback;

interface IGocsdkService {
	//蓝牙状态回调注册去注销
	void registerCallback(IGocsdkCallback callback);
	// 注销蓝牙状态
	void unregisterCallback(IGocsdkCallback callback);
	
	//注释后面带的为操作后相应的回调回复
//setting
	//蓝牙协议软复位  ---》 onInitSucceed()
	void restBluetooth();
	
	//获取本地蓝牙名称  ---》onCurrentDeviceName()
	void getLocalName();
	
	//设置本地蓝牙名称  ---》onCurrentDeviceName()
	void setLocalName(String name);
	
	//获取蓝牙pin码  ---》onCurrentPinCode()
	void getPinCode();
	
	//设置蓝牙pin码 4bit  ---》onCurrentPinCode()
	void setPinCode(String pincode);
	
	//获取本地蓝牙地址   ---》onLocalAddress()
	void getLocalAddress();
	
	//获取自动连接及自动接听   ---》onAutoConnectAccept()
	void getAutoConnectAnswer();
	
	//设置自动连接  ---》onAutoConnectAccept()
	void setAutoConnect();
	
	//取消自动连接  ---》onAutoConnectAccept()
	void cancelAutoConnect();
	
	//设置自动接听 ---》onAutoConnectAccept()
	void setAutoAnswer();
	
	//取消自动接听   ---》onAutoConnectAccept()
	void cancelAutoAnswer();
	
	//获取蓝牙版本信息  ---》onVersionDate()
	void getVersion();

//connect info
	//进入配对模式(蓝牙可见)  ---》onInPairMode()
	void setPairMode();
	
	//退出配对模式(蓝牙不可见)   ---》onExitPairMode()
	void cancelPairMode();

	//连接上次连接过的设备   ---》onHfpConnected() onA2dpConnected() onCurrentAndPairList() etc;
	void connectLast();
	
	//连接指定地址设备 地址可从搜索或配对列表中获取   ---》回调同上
	void connectDevice(String addr);
	
	//连接指定地址a2dp服务  ---》onA2dpConnected()
	void connectA2dp(String addr);

	//连接指定地址hfp服务  ---》onHfpConnected()
	void connectHFP(String addr);
	
	//连接指定地址hid服务  ---》onHidConnected()
	void connectHid(String addr);
	
	//连接指定地址spp服务  ---》onSppConnect()
	void connectSpp(String addr);

	//断开当前连接设备的所有服务 --->onSppDisconnect() onHfpDisconnected() onA2dpDisconnected() onHidDisconnected() etc;
	void disconnect();
	
	//断开当前连接设备的A2DP服务  ---》onA2dpDisconnected()
	void disconnectA2DP();

	//断开当前连接设备的HFP服务  ---》onHfpDisconnected()
	void disconnectHFP();
	
	//断开当前连接设备的HID服务  ---》onHidDisconnected()
	void disconnectHid();
	
	//断开当前连接设备的spp服务  ---》onSppDisconnect()
	void disconnectSpp();
	
//devices list
	//删除指定地址的配对列表  ---》删除成功回复IND_OK失败回复IND_ERROR(暂未做callback处理)
	void deletePair(String addr);

	//开始搜索周边蓝牙设备  ---》onDiscovery()
	void startDiscovery();

	//获取当前配对列表  ---》onCurrentAndPairList()
	void getPairList();

	//停止蓝牙搜索  ---》onDiscoveryDone()
	void stopDiscovery();
	
//hfp
	//来电接听  ---》onTalking()
	void phoneAnswer();

	//挂断电话  ---》onHangUp()
	void phoneHangUp();

	//拨打电话  ---》onCallSucceed()
	void phoneDail(String phonenum);

	//拨打分机号
	void phoneTransmitDTMFCode(char code);
	
	//切换声道到手机端  ---》onHfpRemote() onVoiceDisconnected()
	void phoneTransfer();

	//切换声道到车机端  ---》onHfpLocal() onVoiceConnected()
	void phoneTransferBack();
	
	//语音拨号  ---》onTalking()
	void phoneVoiceDail();
	
	//取消语音拨号  ---》onHangUp()
	void cancelPhoneVoiceDail();
	
//contacts
	//电话本下载  ---onPhoneBook() onPhoneBookDone()
	void phoneBookStartUpdate();
	
	//通话记录下载  ---》onCalllog() onCalllogDone()
	void callLogstartUpdate(int type);
	
//music
	//音乐播放或暂停  ---》onMusicPlaying() onMusicStopped() onMusicInfo()
	void musicPlayOrPause();

	//音乐停止  ---》onMusicStopped()
	void musicStop();

	//上一曲
	void musicPrevious();

	//下一曲
	void musicNext();
	
	//音乐静音 用于混音处理
	void musicMute();
	
	//音乐解除静音 配合mute实现混音处理
	void musicUnmute();
	
	//音乐半音，用于Gps出声时混音处理
	void musicBackground();
	
	//音乐恢复正常 配合半音处理Gps混音出声问题
	void musicNormal();
	
//hid
	//鼠标移动
	//point 8bit， 4bit x 、4bit y
	void hidMouseMove(String point);
	
	//鼠标抬起
	//point 8bit， 4bit x 、4bit y
	void hidMouseUp(String point);
	
	//鼠标按下
	//point 8bit， 4bit x 、4bit y
	void hidMousDown(String point);
	
	//hid home按钮
	void hidHomeClick();
	
	//hid 返回按钮
	void hidBackClick();
	
	//hid 菜单按钮
	void hidMenuClick(); 	
	
//spp
	//spp 数据发送  --》onSppData()
	void sppSendData(String addr ,String data);	
	
	//获取蓝牙音乐信息 ---》onMusicInfo()
	void getMusicInfo();
	
	//查询当前hfp状态 ---》onHfpStatus()
	void inqueryHfpStatus();
	
	//查询当前连接设备的地址
	void getCurrentDeviceAddr();
		
	//查询当前连接设备的名称
	void getCurrentDeviceName();
	
	//暂停下载联系人
	void pauseDownLoadContact();
	
	
	//以下功能暂未开放
	void setProfileEnabled(in boolean[] enabled);
	void getProfileEnabled();
	
	void getMessageSentList();
	void getMessageDeletedList();
	void getMessageInboxList();
	void getMessageText(String handle);
}