package com.goodocom.gocsdkfinal.fragment;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.activity.CallActivity;
import com.goodocom.gocsdkfinal.activity.MainActivity;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class FragmentCallPhone extends Fragment implements OnClickListener {

	public static final int MSG_CALL_STATUS = 0;// 拨出电话
	public static final int MSG_INCOMING_RING = 1;// 来电
	public static final int MSG_INCOMING_HANGUP = 2;// 拒接
	public static final int Msg_CONNECT = 3;// 接通
	private MainActivity activity;
	public static String inComingNumber;
	public static String callOutNumber;
	public static String phoneNumber;

	private static Handler hand = null;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_CALL_STATUS:
				phoneNumber = (String) msg.obj;
				// callOut(phoneNumber);
				break;
			case MSG_INCOMING_RING:
				/*
				 * isInComing = true; InComingRing();
				 */
				break;
			case MSG_INCOMING_HANGUP:
				// hangUp();
				break;
			case Msg_CONNECT:
				/*
				 * connectNumber = (String) msg.obj;
				 * InComingConnect(connectNumber);
				 */
				break;
			}
		};
	};
	private StringBuffer sb;
	private ImageView iv_one;
	private ImageView iv_two;
	private ImageView iv_three;
	private ImageView iv_four;
	private ImageView iv_five;
	private ImageView iv_six;
	private ImageView iv_seven;
	private ImageView iv_eight;
	private ImageView iv_nine;
	private ImageView iv_xinghao;
	private ImageView iv_zero;
	private ImageView iv_jinghao;
	private ImageView iv_callout;
	private ImageView iv_delete;
	private TextView tv_phonenumber;

	public static Handler getHandler() {
		return hand;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activity = (MainActivity) getActivity();
		sb = new StringBuffer();
		AudioManager audioManager = activity.getAudioManager();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = View.inflate(activity, R.layout.fragmentcallphone, null);
		iv_one = (ImageView) view.findViewById(R.id.iv_one);
		iv_two = (ImageView) view.findViewById(R.id.iv_two);
		iv_three = (ImageView) view.findViewById(R.id.iv_three);
		iv_four = (ImageView) view.findViewById(R.id.iv_four);
		iv_five = (ImageView) view.findViewById(R.id.iv_five);
		iv_six = (ImageView) view.findViewById(R.id.iv_six);
		iv_seven = (ImageView) view.findViewById(R.id.iv_seven);
		iv_eight = (ImageView) view.findViewById(R.id.iv_eight);
		iv_nine = (ImageView) view.findViewById(R.id.iv_nine);
		iv_xinghao = (ImageView) view.findViewById(R.id.iv_xinghao);
		iv_zero = (ImageView) view.findViewById(R.id.iv_zero);
		iv_jinghao = (ImageView) view.findViewById(R.id.iv_jinghao);
		iv_callout = (ImageView) view.findViewById(R.id.iv_callout);
		iv_delete = (ImageView) view.findViewById(R.id.iv_delete);
		tv_phonenumber = (TextView) view.findViewById(R.id.tv_phonenumber);

		iv_one.setOnClickListener(this);
		iv_two.setOnClickListener(this);
		iv_three.setOnClickListener(this);
		iv_four.setOnClickListener(this);
		iv_five.setOnClickListener(this);
		iv_six.setOnClickListener(this);
		iv_seven.setOnClickListener(this);
		iv_eight.setOnClickListener(this);
		iv_nine.setOnClickListener(this);
		iv_xinghao.setOnClickListener(this);
		iv_zero.setOnClickListener(this);
		iv_jinghao.setOnClickListener(this);
		iv_callout.setOnClickListener(this);
		iv_delete.setOnClickListener(this);

		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.iv_one:
			sb.append("1");
			break;
		case R.id.iv_two:
			sb.append("2");
			break;
		case R.id.iv_three:
			sb.append("3");
			break;
		case R.id.iv_four:
			sb.append("4");
			break;
		case R.id.iv_five:
			sb.append("5");
			break;
		case R.id.iv_six:
			sb.append("6");
			break;
		case R.id.iv_seven:
			sb.append("7");
			break;
		case R.id.iv_eight:
			sb.append("8");
			break;
		case R.id.iv_nine:
			sb.append("9");
			break;
		case R.id.iv_xinghao:
			sb.append("*");
			break;
		case R.id.iv_zero:
			sb.append("0");
			break;
		case R.id.iv_jinghao:
			sb.append("#");
			break;
		case R.id.iv_callout:
			if (GocsdkCallbackImp.hfpStatus > 0) {
				String phoneNumber = tv_phonenumber.getText().toString().trim();
				if (TextUtils.isEmpty(phoneNumber)) {
					Toast.makeText(activity, activity.getString(R.string.warning_input_number), 0).show();
				} else {
					callOut(phoneNumber);
				}
			} else {
				Toast.makeText(activity, activity.getString(R.string.warning_connect), Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.iv_delete:
			if (sb.length() > 0) {
				sb.deleteCharAt(sb.length() - 1);
			}
			break;
		}
		tv_phonenumber.setText(sb.toString());
	}

	private void callOut(String phoneNumber2) {
		placeCall(phoneNumber2);
	}

	// 拨打正确的电话
	private static void placeCall(String mLastNumber) {
		if (mLastNumber.length() == 0)
			return;
		if (PhoneNumberUtils.isGlobalPhoneNumber(mLastNumber)) {
			// place the call if it is a valid number
			if (mLastNumber == null || !TextUtils.isGraphic(mLastNumber)) {
				// There is no number entered.
				return;
			}
			try {
				MainActivity.getService().phoneDail(mLastNumber);
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}
}
