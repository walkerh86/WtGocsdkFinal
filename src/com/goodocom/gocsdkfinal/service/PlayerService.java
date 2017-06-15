package com.goodocom.gocsdkfinal.service;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import com.goodocom.gocsdkfinal.Config;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

public class PlayerService extends Service implements OnCompletionListener {
	private static final String TAG = "goc";
	private static final int RESTART_DELAY = 2000; // ms
	// 启动控制线程
	private static final int MSG_START_CONTROL = 1;
	// 启动数据线程
	private static final int MSG_START_DATA = 2;
	// 收到控制信息
	private static final int MSG_CONTROL_RECEIVED = 3;

	private volatile int sampleRate = -1;
	private volatile int channels = -1;
	private volatile int sampleBits = -1;

	private ControlThread controlThread = null;
	private DataThread dataThread = null;

	private volatile boolean running = true;

	private volatile AudioTrack audioTrack = null;

	private volatile MediaPlayer ringPlayer = null;

	private boolean ringing = false;

	private StringBuilder controlBuilder = new StringBuilder();

	private AudioManager mAudioManager = null;
	private DataOutputStream out = null;

	private Handler handler = new Handler(Looper.getMainLooper()) {
		@Override
		public void handleMessage(android.os.Message msg) {
			if (msg.what == MSG_START_CONTROL) {
				controlThread = new ControlThread();
				controlThread.start();
				if (audioTrack != null)
					audioTrack.pause();
			} else if (msg.what == MSG_START_DATA) {
				dataThread = new DataThread();
				dataThread.start();
			} else if (msg.what == MSG_CONTROL_RECEIVED) {
				byte[] data = (byte[]) msg.obj;
				for (byte b : data) {
					onControlByte(b);
				}
			}
		};
	};

	private void openAudioTrack(int rate, int ch, int bits) {
		if (rate == sampleRate && ch == channels && bits == sampleBits
				&& audioTrack != null) {
			audioTrack.play();
			return;
		}

		sampleRate = rate;
		channels = ch;
		sampleBits = bits;

		int minBufSize = AudioTrack.getMinBufferSize(sampleRate,
				channels == 2 ? AudioFormat.CHANNEL_OUT_STEREO
						: AudioFormat.CHANNEL_OUT_MONO,
				sampleBits == 16 ? AudioFormat.ENCODING_PCM_16BIT
						: AudioFormat.ENCODING_PCM_8BIT);
		minBufSize *= 4;
		if (audioTrack != null) {
			audioTrack.stop();
			audioTrack = null;
		}
		audioTrack = new AudioTrack(Config.STREAM_TYPE, sampleRate,
				channels == 2 ? AudioFormat.CHANNEL_OUT_STEREO
						: AudioFormat.CHANNEL_OUT_MONO,
				sampleBits == 16 ? AudioFormat.ENCODING_PCM_16BIT
						: AudioFormat.ENCODING_PCM_8BIT, minBufSize,
				AudioTrack.MODE_STREAM);
		int realRate = sampleRate;
		// if(realRate > 45000)realRate = 45000;
		// else realRate = sampleRate * 99/100;
		audioTrack.setPlaybackRate(realRate);
		//Log.d("goc", "real play rate "+ realRate);
		audioTrack.play();
	}

	private void onControlCommand(String cmd) {
		//Log.d(TAG, cmd);
		if (cmd.startsWith("open")) {
			if (cmd.length() < 28) {
				//Log.e(TAG, "get error open:"+cmd);
				return;
			}
			int rate = Integer.valueOf(cmd.substring(4, 12), 16);
			int ch = Integer.valueOf(cmd.substring(12, 20), 16);
			int bits = Integer.valueOf(cmd.substring(20, 28), 16);
			//Log.d(TAG, "open rate:"+rate + " channels:"+ch+" bits:"+bits);
			openAudioTrack(rate, ch, bits);
			mAudioManager.requestAudioFocus(mAudioFocusChangeListener,
					Config.STREAM_TYPE, AudioManager.AUDIOFOCUS_GAIN);
		} else if (cmd.startsWith("stop")) {
			//Log.d(TAG,"stop");
			if (audioTrack != null) {
				audioTrack.pause();
				audioTrack.flush();
			}
			mAudioManager.abandonAudioFocus(mAudioFocusChangeListener);
		} else if (cmd.startsWith("ring start")) {// 开始响铃
			ringing = true;
			ringStart();
		} else if (cmd.startsWith("ring stop")) {// 停止响铃
			ringing = false;
			ringStop();
		} else if (cmd.startsWith("mute")) {// 静音
			//Log.d(TAG, "PlayerService mute");
			if (audioTrack != null)
				audioTrack.setStereoVolume(0f, 0f);
		} else if (cmd.startsWith("unmute")) {// 取消静音
			//Log.d(TAG, "PlayerService unmute");
			if (audioTrack != null)
				audioTrack.setStereoVolume(1.0f, 1.0f);
		} else if (cmd.startsWith("vol half")) {// 一半声音
			//Log.d(TAG, "PlayerService vol half");
			if (audioTrack != null)
				audioTrack.setStereoVolume(0.5f, 0.5f);
		} else if (cmd.startsWith("vol normal")) {// 默认声音
			//Log.d(TAG, "PlayerService vol normal");
			if (audioTrack != null)
				audioTrack.setStereoVolume(1.0f, 1.0f);
		} else if (cmd.startsWith("vol 0")) {
			//Log.d(TAG, "PlayerService vol 0");
			if (audioTrack != null)
				audioTrack.setStereoVolume(0.1f, 0.1f);
		} else if (cmd.startsWith("vol 1")) {
			//Log.d(TAG, "PlayerService vol 1");
			if (audioTrack != null)
				audioTrack.setStereoVolume(0.2f, 0.2f);
		} else if (cmd.startsWith("vol 2")) {
			//Log.d(TAG, "PlayerService vol 2");
			if (audioTrack != null)
				audioTrack.setStereoVolume(0.3f, 0.3f);
		} else if (cmd.startsWith("vol 3")) {
			//Log.d(TAG, "PlayerService vol 3");
			if (audioTrack != null)
				audioTrack.setStereoVolume(0.4f, 0.4f);
		} else if (cmd.startsWith("vol 4")) {
			//Log.d(TAG, "PlayerService vol 4");
			if (audioTrack != null)
				audioTrack.setStereoVolume(0.5f, 0.5f);
		} else if (cmd.startsWith("vol 5")) {
			//Log.d(TAG, "PlayerService vol 5");
			if (audioTrack != null)
				audioTrack.setStereoVolume(0.6f, 0.6f);
		} else if (cmd.startsWith("vol 6")) {
			//Log.d(TAG, "PlayerService vol 6");
			if (audioTrack != null)
				audioTrack.setStereoVolume(0.7f, 0.7f);
		} else if (cmd.startsWith("vol 7")) {
			//Log.d(TAG, "PlayerService vol 7");
			if (audioTrack != null)
				audioTrack.setStereoVolume(0.8f, 0.8f);
		} else if (cmd.startsWith("vol 8")) {
			//Log.d(TAG, "PlayerService vol 8");
			if (audioTrack != null)
				audioTrack.setStereoVolume(0.9f, 0.9f);
		} else if (cmd.startsWith("vol 9")) {
			//Log.d(TAG, "PlayerService vol 9");
			if (audioTrack != null)
				audioTrack.setStereoVolume(1.0f, 1.0f);
		} else if (cmd.startsWith("sco open")) {// 打开录音
			ScoService.startSco();
		} else if (cmd.startsWith("sco close")) {// 关闭录音
			ScoService.stopSco();
		}

	}

	private AudioManager.OnAudioFocusChangeListener mAudioFocusChangeListener = new AudioManager.OnAudioFocusChangeListener() {
		@Override
		public void onAudioFocusChange(int focusChange) {
			if (null == audioTrack)
				return;
			switch (focusChange) {
			case AudioManager.AUDIOFOCUS_GAIN:// 指示要申请的AudioFocus是暂时性的，会很快用完释放的；
				audioTrack.setStereoVolume(1.0f, 1.0f);// 设置声音
				break;
			case AudioManager.AUDIOFOCUS_LOSS:// 失去了Audio Focus，并将会持续很长的时间
				// audioTrack.setStereoVolume(0, 0);
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:// 暂时失去Audio
														// Focus，并会很快再次获得。必须停止Audio的播放，但是因为可能会很快再次获得AudioFocus，这里可以不释放Media资源；
				audioTrack.setStereoVolume(0, 0);
				break;
			case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:// 暂时失去AudioFocus，但是可以继续播放，不过要在降低音量。
				audioTrack.setStereoVolume(0.5f, 0.5f);
				break;
			default:
				break;
			}
		}
	};

	private void onControlByte(byte b) {
		if (b != '\n') {
			controlBuilder.append((char) b);
			return;
		}

		onControlCommand(controlBuilder.toString());
		controlBuilder.delete(0, controlBuilder.length());
	}

	private void ringStart() {
		String path = null;
		for (String p : Config.RING_PATH) {
			if (new File(p).exists())
				path = p;
		}
		if (path == null) {
			//Log.e(TAG,"cannot find ring file");
			return;
		}
		try {
			ringPlayer.reset();
			ringPlayer.setDataSource(path);
			ringPlayer.prepare();
			ringPlayer.start();
			ringPlayer.setOnCompletionListener(this);
			//Log.d(TAG, "playing ring "+path);
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}

	private void ringStop() {
		ringPlayer.stop();
	}

	@Override
	public void onCreate() {
		//Log.d(TAG, "onCreate");
		handler.sendEmptyMessage(MSG_START_CONTROL);
		handler.sendEmptyMessage(MSG_START_DATA);

		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		ringPlayer = new MediaPlayer();
		super.onCreate();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	class ControlThread extends Thread {
		private LocalSocket client;
		private LocalSocketAddress address;
		private InputStream inputStream;
		private byte[] buffer = new byte[100 * 1024];

		public ControlThread() {
			client = new LocalSocket();
			address = new LocalSocketAddress(Config.CONTROL_SOCKET_NAME,
					LocalSocketAddress.Namespace.RESERVED);
		}

		@Override
		public void run() {
			int n;
			try {
				client.connect(address);
				inputStream = client.getInputStream();
				while (running) {
					n = inputStream.read(buffer);
					if (n < 0)
						throw new IOException("n==-1");
					byte[] data = new byte[n];
					System.arraycopy(buffer, 0, data, 0, n);
					handler.sendMessage(handler.obtainMessage(
							MSG_CONTROL_RECEIVED, data));
				}
			} catch (IOException e) {
				// e.printStackTrace();
				try {
					client.close();
				} catch (IOException e1) {
					// e1.printStackTrace();
				}
				handler.sendEmptyMessageDelayed(MSG_START_CONTROL,
						RESTART_DELAY);
				return;
			}
		}
	}

	// 数据线程
	class DataThread extends Thread {
		private LocalSocket client;
		private LocalSocketAddress address;
		private InputStream inputStream;
		private byte[] buffer = new byte[4096];

		public DataThread() {
			client = new LocalSocket();
			address = new LocalSocketAddress(Config.DATA_SOCKET_NAME,
					LocalSocketAddress.Namespace.RESERVED);
		}

		@Override
		public void run() {
			int n;
			try {
				client.connect(address);
				inputStream = client.getInputStream();

				//out = new DataOutputStream(new BufferedOutputStream(
				//		new FileOutputStream(new File("/data/goc/music.pcm"),
				//				true)));

				while (running) {
					n = inputStream.read(buffer);
					if (n < 0)
						throw new IOException("n==-1");
					if (audioTrack != null) {
						if (null != out)
							out.write(buffer);
						audioTrack.write(buffer, 0, n);
					}
				}
			} catch (IOException e) {
				// e.printStackTrace();
				try {
					if(out != null)out.close();
					client.close();
				} catch (IOException e1) {
					// e1.printStackTrace();
				}
				handler.sendEmptyMessageDelayed(MSG_START_DATA, RESTART_DELAY);
				return;
			}
		}
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		if (ringing)
			ringStart();
	}

	/**
	 * B方法追加文件：使用FileWriter
	 */
	public static void appendMethodB(String fileName, byte[] content) {
		try {
			// 将DataOutputStream与FileOutputStream连接可输出不同类型的数据
			// FileOutputStream类的构造函数负责打开文件kuka.dat，如果文件不存在，
			// 则创建一个新的文件，如果文件已存在则用新创建的文件代替。然后FileOutputStream
			// 类的对象与一个DataOutputStream对象连接，DataOutputStream类具有写
			// 各种数据类型的方法。
			DataOutputStream out = new DataOutputStream(
					new BufferedOutputStream(new FileOutputStream(new File(
							fileName), true)));
			out.write(content);
			out.close();
		} catch (Exception e) {
			// e.printStackTrace();
		}
	}
}
