package com.goodocom.gocsdkfinal;

import android.media.AudioManager;

public class Config {
	public static final boolean DEBUG = true;
	
	public static final boolean JAVA_SDK = true;
	public static final boolean JAVA_PLAYER = true;
	public static final boolean JAVA_SCO = false;

	public static final int STREAM_TYPE = AudioManager.STREAM_MUSIC;

	public static final String CONTROL_SOCKET_NAME = "goc_control";
	public static final String DATA_SOCKET_NAME = "goc_data";
	public static final String SERIAL_SOCKET_NAME = "goc_serial";
	public static final String SCO_SOCKET_NAME = "goc_sco";

	
	public static final String[] RING_PATH = new String[] { 
		"/system/ring.mp3",
		"/mtc/ring.mp3" 
	};
}
