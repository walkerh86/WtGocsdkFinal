package com.goodocom.gocsdkfinal.service;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.goodocom.gocsdkfinal.Config;



import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder;
import android.media.audiofx.AcousticEchoCanceler;
import android.media.audiofx.NoiseSuppressor;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

public class ScoService extends Service {
	private static final int MSG_OPEN = 1;
	private static final int MSG_CLOSE = 2;
	private static final int MSG_START_SCO = 3;
	private static final int MSG_INIT = 4;
	
	private static ScoService INSTANCE = null;
	private static final int RESTART_DELAY = 2000;
	private AudioTrack audioTrack;
	private AudioRecord audioRecord;
	private AcousticEchoCanceler canceler;
	private NoiseSuppressor ns;
	private boolean running = true;
	private ScoThread scoThread = null;
	private volatile boolean sco_running = false;
	private volatile OutputStream outputStream = null;
	private RecordThread recordThread = null;
	
	@Override
	public void onCreate() {
		//保存本服务
		INSTANCE = this;
		//发送handler消息
		handler.sendEmptyMessage(MSG_INIT);
		handler.sendEmptyMessage(MSG_START_SCO);
		super.onCreate();
	}
	
	private boolean init(){
		audioRecord = new AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION,
				8000,
				AudioFormat.CHANNEL_IN_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				AudioRecord.getMinBufferSize(8000,AudioFormat.CHANNEL_IN_MONO,AudioFormat.ENCODING_PCM_16BIT));
		if(audioRecord == null){
			//Log.e("goc","create audio record error");
			return false;
		}
		if(audioRecord.getState() == AudioRecord.STATE_UNINITIALIZED){
			//Log.e("goc", "error audiorecord state == uninitialized");
			return false;
		}
		if(AcousticEchoCanceler.isAvailable()){
			canceler = AcousticEchoCanceler.create(audioRecord.getAudioSessionId());
			if(null != canceler){
				canceler.setEnabled(true);
				//Log.d("goc", "canceler enabled:"+canceler.getEnabled());
			}else{
				//Log.e("goc","create AcousticEchoCanceler error");
			}
	    }else{
	    	//Log.e("goc", "AcousticEchoCanceler not available");
	    }
		
		if(NoiseSuppressor.isAvailable()){
			ns = NoiseSuppressor.create(audioRecord.getAudioSessionId());
			if(null != ns){
				ns.setEnabled(true);
				//Log.d("goc", "NoiseSuppressor enabled:"+ns.getEnabled());
			}else{
				//Log.e("goc", "create NoiseSuppressor error");
			}
		}else{
			//Log.e("goc","NoiseSuppressor is not available");
		}
		
		audioTrack = new AudioTrack(AudioManager.STREAM_VOICE_CALL,
				8000,
				AudioFormat.CHANNEL_OUT_MONO,
				AudioFormat.ENCODING_PCM_16BIT,
				AudioTrack.getMinBufferSize(8000,AudioFormat.CHANNEL_OUT_MONO,AudioFormat.ENCODING_PCM_16BIT),
				AudioTrack.MODE_STREAM,
				audioRecord.getAudioSessionId());
		return true;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public static void startSco(){
		if(INSTANCE == null)return;
		//Log.d("goc", "start sco");
		INSTANCE.handler.sendEmptyMessage(MSG_OPEN);
	}
	
	public static void stopSco(){
		if(INSTANCE == null)return;
		//Log.d("goc", "stop sco");
		INSTANCE.handler.sendEmptyMessage(MSG_CLOSE);
	}
	
	@SuppressLint("HandlerLeak")
	private Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == MSG_OPEN) {
				audioRecord.startRecording();//开始录音
				audioTrack.play();//播放声音
				sco_running = true;
			} else if (msg.what == MSG_CLOSE) {
				sco_running = false;
				audioRecord.stop();//停止录音
				audioTrack.pause();//暂停播放
				audioTrack.flush();
			}else if (msg.what == MSG_START_SCO) {//开启socket线程、和录音线程
				scoThread = new ScoThread();
				scoThread.start();
				recordThread = new RecordThread();
				recordThread.start();
			}else if (msg.what == MSG_INIT) {//初始化服务不成功时，就停止服务
				if(!init()){
					running = false;
					ScoService.this.stopSelf();
				}
			}
		};
	};
	/**
	 * 录音线程
	 * @author chase
	 *录音，并保存
	 */
	private class RecordThread extends Thread{
		@Override
		public void run() {
			/**
			 * 设置线程优先级为后台，
			 * 这样当多个线程并发后很多无关紧要的线程分配的CPU时间将会减少，
			 * 有利于主线程的处理
			 */
			Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
			int len;
			byte[] buffer = new byte[1024];
			FileOutputStream out = null;
			try {
				/**
				 * 如果追加是真的，
				 * 并且该文件已经存在，
				 * 它将被附加到；
				 * 否则将被截断。
				 * 如果不存在该文件将被创建。
				 */
				out = new FileOutputStream("/mnt/sdcard/sco.data", false);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
			
			while (running) {
				if(!sco_running){
					try {
						Thread.sleep(10, 0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				if(outputStream == null){
					try {
						Thread.sleep(10, 0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					continue;
				}
				len = audioRecord.read(buffer, 0 , 1024);
				try {
					if(len>0){
						outputStream.write(buffer, 0, len);
						if(null != out)out.write(buffer, 0, len);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	/**
	 * 从socket服务器端读数据，写到audioTrack里面
	 * @author chase
	 *
	 */
	private class ScoThread extends Thread {
		private LocalSocket client;
		private LocalSocketAddress address;
		private InputStream inputStream;
		private byte[] buffer = new byte[4096];
		public ScoThread() {
			client = new LocalSocket();
			address = new LocalSocketAddress(Config.SCO_SOCKET_NAME,
					LocalSocketAddress.Namespace.RESERVED);
		}
		
		@Override
		public void run() {
			int n;
			try {
				//连接socket服务器端
				client.connect(address);
				inputStream = client.getInputStream();
				outputStream = client.getOutputStream();
				while(running){
					n = inputStream.read(buffer);
					if(n < 0)throw new IOException("n==-1");
					if(audioTrack != null)audioTrack.write(buffer, 0, n);
				}
			} catch (IOException e) {
				e.printStackTrace();
				try {
					client.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				handler.sendEmptyMessageDelayed(MSG_START_SCO, RESTART_DELAY);
				return;
			}
		}
	}
}
