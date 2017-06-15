package com.goodocom.gocsdkfinal.service;

import com.goodocom.gocsdk.IGocsdkCallback;
import com.goodocom.gocsdkfinal.Commands;
import com.goodocom.gocsdkfinal.GocsdkCommon;
import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.activity.MainActivity;
import com.goodocom.gocsdkfinal.activity.TransparentActivity;
import com.goodocom.gocsdkfinal.fragment.FragmentCallPhone;
import com.goodocom.gocsdkfinal.view.TransparentDialog;


import de.greenrobot.event.EventBus;

import android.R.integer;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.sax.StartElementListener;
import android.text.TextUtils;
import android.util.Log;

public class CommandParser extends GocsdkCommon {
	private static final String TAG = CommandParser.class.getName();

	private RemoteCallbackList<IGocsdkCallback> callbacks;
	private Context mContext;
	private static boolean fromBehind = false;
	public CommandParser(RemoteCallbackList<IGocsdkCallback> callbacks,
			GocsdkService gocsdkService) {
		this.callbacks = callbacks;
		mContext = gocsdkService;
	}
	private byte[] serialBuffer = new byte[1024];
	private int count = 0;
	private void onSerialCommand(String cmd) {
		System.out.println("接收到命令：" + cmd);
		if (GocsdkService.isBehind) {
			
			System.out.println("从后台");
			if (cmd.startsWith(Commands.IND_INCOMING)) {// 来电
				fromBehind = true;
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(
						"com.goodocom.gocsdkfinal",
						"com.goodocom.gocsdkfinal.activity.TransparentActivity"));
				intent.putExtra("isInComing", true);
				intent.putExtra("inComingNumber", cmd.substring(2));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				mContext.startActivity(intent);
				
			} else if (cmd.startsWith(Commands.IND_OUTGOING_TALKING_NUMBER)) {// 拨出
				fromBehind = true;
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(
						"com.goodocom.gocsdkfinal",
						"com.goodocom.gocsdkfinal.activity.TransparentActivity"));
				intent.putExtra("isInComing", false);
				intent.putExtra("callOutNumber", cmd.substring(2));
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
						| Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
				mContext.startActivity(intent);
			}
		} else {
			System.out.println("从前台");
			int i = callbacks.beginBroadcast();
			while (i > 0) {
				i--;
				IGocsdkCallback cbk = callbacks.getBroadcastItem(i);
				try {
					if (cmd.startsWith(Commands.IND_HFP_CONNECTED)) {// HFP已连接:::IB
						System.out.println("蓝牙连接命令：" + cmd);
						cbk.onHfpConnected();
					} else if (cmd.startsWith(Commands.IND_HFP_DISCONNECTED)) {// HFP已断开:::IA
						cbk.onHfpDisconnected();
					} else if (cmd.startsWith(Commands.IND_CALL_SUCCEED)) {// 去电:::IC[numberlen:2][number]
						
						if (cmd.length() < 4) {
							cbk.onCallSucceed("");
						} else {
							cbk.onCallSucceed(cmd.substring(4));
						}
					} else if (cmd.startsWith(Commands.IND_INCOMING)) {// 来电:::ID[numberlen:2][number]
						System.out.println("来电命令：" + cmd);
						if (cmd.length() < 2) {
							cbk.onIncoming("");
						} else {
							cbk.onIncoming(cmd.substring(2));
						}
					} else if (cmd.startsWith(Commands.IND_HANG_UP)) {// 挂机:::IF[numberlen:2][number]
						System.out.println("挂断命令"+cmd+"fromBehind="+fromBehind);
						if(fromBehind){
							Handler handler = TransparentActivity.getHandler();
							if(handler!=null){
								handler.sendEmptyMessage(TransparentActivity.MSG_HANGUP_PHONE);
								System.out.println("挂断命令发了没有哇");
							}
						}else{
							cbk.onHangUp();
						}
						fromBehind =false;
						
					} else if (cmd.startsWith(Commands.IND_TALKING)) {// 通话中:::IG[numberlen:2][number]
						System.out.println("通话命令=" + cmd);
						
						if(fromBehind){
							Handler handler = TransparentActivity.getHandler();
							if(handler!=null){
								handler.sendEmptyMessage(TransparentActivity.MSG_CONNECTION_PHONE);
							}
						}else{
							if (cmd.length() < 4) {
								cbk.onTalking("");
							} else {
								cbk.onTalking(cmd.substring(4));
							}
						}
					} else if (cmd.startsWith(Commands.IND_RING_START)) {// 开始响铃
						cbk.onRingStart();
					} else if (cmd.startsWith(Commands.IND_RING_STOP)) {// 停止响铃
						cbk.onRingStop();
					} else if (cmd.startsWith(Commands.IND_HF_LOCAL)) {//
						cbk.onHfpLocal();
					} else if (cmd.startsWith(Commands.IND_HF_REMOTE)) {// 蓝牙接听
						cbk.onHfpRemote();
					} else if (cmd.startsWith(Commands.IND_IN_PAIR_MODE)) {// 进入配对模式:::II
						cbk.onInPairMode();
					} else if (cmd.startsWith(Commands.IND_EXIT_PAIR_MODE)) {// 退出配对模式
						cbk.onExitPairMode();
					} else if (cmd.startsWith(Commands.IND_INIT_SUCCEED)) {// 上电初始化成功:::IS
						cbk.onInitSucceed();
					} else if (cmd.startsWith(Commands.IND_MUSIC_PLAYING)) {// 音乐播放
						cbk.onMusicPlaying();
					} else if (cmd.startsWith(Commands.IND_MUSIC_STOPPED)) {// 音乐停止
						cbk.onMusicStopped();
					} else if (cmd.startsWith(Commands.IND_VOICE_CONNECTED)) {
						// cbk.onVoiceConnected();
					} else if (cmd.startsWith(Commands.IND_VOICE_DISCONNECTED)) {
						// cbk.onVoiceDisconnected();
					} else if (cmd.startsWith(Commands.IND_AUTO_CONNECT_ACCEPT)) {
						if (cmd.length() < 4) {
							// Log.e(TAG, cmd + "=====error command");
						} else {
							cbk.onAutoConnectAccept(cmd.charAt(2) != '0',
									cmd.charAt(3) != '0');
						}
					} else if (cmd.startsWith(Commands.IND_CURRENT_ADDR)) {
						if (cmd.length() < 3) {
							// Log.e(TAG, cmd + "==== error command");
						} else {
							cbk.onCurrentAddr(cmd.substring(2));
						}
					} else if (cmd.startsWith(Commands.IND_CURRENT_NAME)) {
						if (cmd.length() < 3) {
							// Log.e(TAG, cmd + "==== error command");
						} else {
							cbk.onCurrentName(cmd.substring(2));
						}
					} else if (cmd.startsWith(Commands.IND_AV_STATUS)) {
						if (cmd.length() < 4) {
							// Log.e(TAG, cmd + "=====error");
						} else {
							cbk.onAvStatus(Integer.parseInt(cmd.substring(3, 4)));
						}
					} else if (cmd.startsWith(Commands.IND_HFP_STATUS)) {
						System.out.println("蓝牙状态命令=" + cmd);
						if (cmd.length() < 3) {
							// Log.e(TAG, cmd +" ==== error");
						} else {
							int status = Integer.parseInt(cmd.substring(2, 3));
							cbk.onHfpStatus(status);
						}
					} else if (cmd.startsWith(Commands.IND_VERSION_DATE)) {
						if (cmd.length() < 3) {
							// Log.e(TAG, cmd + "====error");
						} else {
							cbk.onVersionDate(cmd.substring(2));
						}
					} else if (cmd.startsWith(Commands.IND_CURRENT_DEVICE_NAME)) {
						if (cmd.length() < 3) {
							// Log.e(TAG, cmd + "====error");
						} else {
							cbk.onCurrentDeviceName(cmd.substring(2));
						}
					} else if (cmd.startsWith(Commands.IND_CURRENT_PIN_CODE)) {
						if (cmd.length() < 3) {
							// Log.e(TAG, cmd + "====error");
						} else {
							cbk.onCurrentPinCode(cmd.substring(2));
						}
					} else if (cmd.startsWith(Commands.IND_A2DP_CONNECTED)) {
						cbk.onA2dpConnected();
					} else if (cmd.startsWith(Commands.IND_A2DP_DISCONNECTED)) {
						cbk.onA2dpDisconnected();
					} else if (cmd
							.startsWith(Commands.IND_CURRENT_AND_PAIR_LIST)) {
						System.out.println("获取配对列表的命令" + cmd);
						if (cmd.length() < 15) {
							// Log.e(TAG, cmd + "====error");
						} else if (cmd.length() == 15) {
							cbk.onCurrentAndPairList(
									Integer.parseInt(cmd.substring(2, 3)), "",
									cmd.substring(3, 15));
						} else {
							cbk.onCurrentAndPairList(
									Integer.parseInt(cmd.substring(2, 3)),
									cmd.substring(15), cmd.substring(3, 15));
						}
					} else if (cmd.startsWith(Commands.IND_PHONE_BOOK)) {//
						if (cmd.length() < 6) {
							// Log.e(TAG, cmd + "====error");
						} else {
							int nameLen = Integer.parseInt(cmd.substring(2, 4));
							int numLen = Integer.parseInt(cmd.substring(4, 6));
							String name;
							String num;
							byte[] bytes = cmd.getBytes();
							if (nameLen > 0) {
								byte[] buffer = new byte[nameLen];
								System.arraycopy(bytes, 6, buffer, 0, nameLen);
								name = new String(buffer);
							} else {
								name = "";
							}
							if (numLen > 0) {
								byte[] buffer = new byte[numLen];
								System.arraycopy(bytes, 6 + nameLen, buffer, 0,
										numLen);
								num = new String(buffer);
							} else {
								num = "";
							}
							// Log.d("goc", "name:"+name+",num:"+num);
							cbk.onPhoneBook(name, num);
						}
					} /*
					 * else if (cmd.startsWith(Commands.IND_SIM_BOOK)) { if
					 * (cmd.length() < 6) { // Log.e(TAG, cmd + "====error"); }
					 * else { int nameLen = Integer.parseInt(cmd.substring(2,
					 * 4)); int numLen = Integer.parseInt(cmd.substring(4, 6));
					 * String name; String num; byte[] bytes = cmd.getBytes();
					 * if (nameLen > 0) { byte[] buffer = new byte[nameLen];
					 * System.arraycopy(bytes, 6, buffer, 0, nameLen); name =
					 * new String(buffer); } else { name = ""; } if (numLen > 0)
					 * { byte[] buffer = new byte[numLen];
					 * System.arraycopy(bytes, 6 + nameLen, buffer, 0, numLen);
					 * num = new String(buffer); } else { num = ""; }
					 * cbk.onPhoneBook(name, num); } }
					 */else if (cmd.startsWith(Commands.IND_PHONE_BOOK_DONE)) {// 更新联系人完成
						cbk.onPhoneBookDone();
					} else if (cmd.startsWith(Commands.IND_SIM_DONE)) {
						cbk.onSimDone();
					} else if (cmd.startsWith(Commands.IND_CALLLOG_DONE)) {
						System.out.println("结束命令你发几次"+cmd);
						cbk.onCalllogDone();
					} else if (cmd.startsWith(Commands.IND_CALLLOG)) {
						System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@"
								+ cmd);
						if (cmd.length() < 4) {
							// Log.e(TAG, cmd + "====error");
						} else {
							String[] split = cmd.substring(3).split("\\[FF\\]");
							for (String string : split) {
								System.out.println(string);
							}
							cbk.onCalllog(
									Integer.parseInt(cmd.substring(2, 3)),
									split[0], split[1]);
						}
					} else if (cmd.startsWith(Commands.IND_DISCOVERY)) {
						System.out.println("**************" + cmd);
						if (cmd.length() < 14) {
							// Log.e(TAG, cmd+"===error");
						} else if (cmd.length() == 14) {
							cbk.onDiscovery("", cmd.substring(2));
						} else {
							cbk.onDiscovery(cmd.substring(14),
									cmd.substring(2, 14));
						}
					} else if (cmd.startsWith(Commands.IND_DISCOVERY_DONE)) {
						cbk.onDiscoveryDone();
					} else if (cmd.startsWith(Commands.IND_LOCAL_ADDRESS)) {
						if (cmd.length() != 14) {
						}
						cbk.onLocalAddress(cmd.substring(2));
					} else if (cmd
							.startsWith(Commands.IND_OUTGOING_TALKING_NUMBER)) {
						System.out.println("拨出电话的信息：" + cmd);
						if (cmd.length() <= 2) {
							cbk.onOutGoingOrTalkingNumber("");
						} else {
							cbk.onOutGoingOrTalkingNumber(cmd.substring(2));
						}
					} else if (cmd.startsWith(Commands.IND_MUSIC_INFO_STRING)) {
						System.out.println("蓝牙音乐命令：" + cmd);
						if (cmd.length() <= 2) {
							// Log.e(TAG, cmd+"===error");
						} else {
							String info = cmd.substring(2);
							String[] arr = info.split("\\[FF\\]");
							if (arr.length != 5) {
								// Log.e(TAG, cmd+"===error");
							} else {
								cbk.onMusicInfo(arr[0], arr[1],
										Integer.parseInt(arr[2]),
										Integer.parseInt(arr[3]),
										Integer.parseInt(arr[4]));
							}
						}
					} else if (cmd.startsWith(Commands.IND_PROFILE_ENABLED)) {
						if (cmd.length() < 12) {
							// Log.e(TAG, cmd + "====error");
						} else {
							boolean[] enabled = new boolean[10];
							for (int ii = 0; ii < 10; ii++) {
								if (cmd.charAt(ii + 2) == '0') {
									enabled[ii] = false;
								} else {
									enabled[ii] = true;
								}
							}
							cbk.onProfileEnbled(enabled);
						}
					} else if (cmd.startsWith(Commands.IND_MESSAGE_LIST)) {
						String text = cmd.substring(2);
						if (text.length() == 0) {
							// Log.e("goc", "cmd error:param==0"+cmd);
						} else {
							String[] arr = text.split("\\[FF\\]", -1);
							if (arr.length != 6) {
								// Log.e("goc",
								// "cmd error:arr.length="+arr.length+";"+cmd);
							} else {
								cbk.onMessageInfo(arr[0], arr[1], arr[2],
										arr[3], arr[4], arr[5]);
							}
						}
					} else if (cmd.startsWith(Commands.IND_MESSAGE_TEXT)) {
						cbk.onMessageContent(cmd.substring(2));
					} else if (cmd.startsWith(Commands.IND_OK)) {
						// TODO:OK ind
					} else if (cmd.startsWith(Commands.IND_ERROR)) {

					} else {

					}
				} catch (RemoteException e) {

				}
			}
			callbacks.finishBroadcast();
		}
	}

	private void onByte(byte b) {
		if ('\n' == b)
			return;
		if (count >= 1000)
			count = 0;
		if ('\r' == b) {
			if (count > 0) {
				byte[] buf = new byte[count];
				System.arraycopy(serialBuffer, 0, buf, 0, count);
				onSerialCommand(new String(buf));
				count = 0;
			}
			return;
		}
		if ((b & 0xFF) == 0xFF) {
			serialBuffer[count++] = '[';
			serialBuffer[count++] = 'F';
			serialBuffer[count++] = 'F';
			serialBuffer[count++] = ']';
		} else {
			serialBuffer[count++] = b;
		}
	}

	public void onBytes(byte[] data) {
		for (byte b : data) {
			onByte(b);
		}
	}
}
