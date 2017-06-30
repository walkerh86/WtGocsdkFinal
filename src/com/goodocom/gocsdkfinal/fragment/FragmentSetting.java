package com.goodocom.gocsdkfinal.fragment;

import com.goodocom.gocsdk.IGocsdkService;
import com.goodocom.gocsdkfinal.GocsdkSettings;
import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.activity.MainActivity;
import com.goodocom.gocsdkfinal.service.GocsdkService;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.EditText;
import android.widget.ImageView;

public class FragmentSetting extends Fragment implements OnClickListener {

	public static final int MSG_DEVICE_NAME = 0;
	public static final int MSG_PIN_CODE = 1;

	private boolean isConnectSwitch = false;
	private boolean isAnswerSwitch = false;

	private MainActivity activity;
	/*private static Handler hand = null;
	private Handler handler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case MSG_DEVICE_NAME:
				String deviceName = (String) msg.obj;
				// tv_device_name.setText(deviceName);
				et_device_name.setText(deviceName);
				break;
			case MSG_PIN_CODE:
				String pinCode = (String) msg.obj;
				// tv_pin_code.setText(pinCode);
				et_pin_code.setText(pinCode);
				break;
			}
		};
	};
	public static Handler getHandler() {
		return hand;
	}*/

	private EditText et_device_name;
	private EditText et_pin_code;
	private ImageView auto_connect_switch;
	private ImageView auto_answer_switch;
	
	private GocsdkSettings mSettings;

	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//activity = (MainActivity) getActivity();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = View.inflate(getActivity(), R.layout.fragmentsettings, null);
		initView(view);
		//initData();
		//hand = handler;
		return view;
	}

	private View initView(View view) {
		et_device_name = (EditText) view.findViewById(R.id.et_device_name);
		et_pin_code = (EditText) view.findViewById(R.id.et_pin_code);
		auto_connect_switch = (ImageView) view
				.findViewById(R.id.auto_connect_switch);
		auto_answer_switch = (ImageView) view
				.findViewById(R.id.auto_answer_switch);
		auto_answer_switch.setOnClickListener(this);
		auto_connect_switch.setOnClickListener(this);
		
		mBtSwitch = (ImageView)view.findViewById(R.id.bt_switch);
		mBtSwitch.setOnClickListener(this);
		
		if(GocsdkService.mLocalName!=null){
			et_device_name.setText(GocsdkService.mLocalName);
		}
		if(GocsdkService.mPinCode!=null){
			et_pin_code.setText(GocsdkService.mPinCode);
		}
		mSettings = GocsdkSettings.getInstance(getActivity());
		isBtSwitch = mSettings.isOpen();
		isConnectSwitch = mSettings.isAutoConnect();
		isAnswerSwitch = mSettings.isAutoAnswer();
		mBtSwitch.setImageResource(isBtSwitch ? R.drawable.ico_4157_kai : R.drawable.ico_4158_guan);
		auto_connect_switch.setImageResource(isConnectSwitch ? R.drawable.ico_4157_kai : R.drawable.ico_4158_guan);
		auto_answer_switch.setImageResource(isAnswerSwitch ? R.drawable.ico_4157_kai : R.drawable.ico_4158_guan);
		auto_connect_switch.setEnabled(isBtSwitch);
		auto_answer_switch.setEnabled(isBtSwitch);
		
		et_device_name.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String deviceName = et_device_name.getText().toString().trim();
				try {
					//MainActivity.getService().setLocalName(deviceName); 
					if(mService != null){
						mService.setLocalName(deviceName);
					}
				 } catch(RemoteException e) { 
					 e.printStackTrace();
				}
				Handler handler = FragmentBlueToothInfo.getHandler();
				if(handler==null){
					return;
				}
				Message msg = new Message();
				msg.what = FragmentBlueToothInfo.MSG_DEVICE_NAME;
				msg.obj = deviceName;
				handler.sendMessage(msg);
				
			}
		});
		et_pin_code.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				String pinCode = et_pin_code.getText().toString().trim();
				try {
					//MainActivity.getService().setPinCode(pinCode);
					if(mService != null){
						mService.setPinCode(pinCode);
					}
				} catch (RemoteException e) {
					e.printStackTrace();
				}
				Handler handler = FragmentBlueToothInfo.getHandler();
				if(handler==null){
					return;
				}
				Message msg = new Message();
				msg.what = FragmentBlueToothInfo.MSG_PIN_CODE;
				msg.obj = pinCode;
				handler.sendMessage(msg);
			}
		});
		return view;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.auto_connect_switch:
			isConnectSwitch();
			break;
		case R.id.auto_answer_switch:
			isAnswerSwitch();
			break;
		case R.id.bt_switch:
			setBtSwitch();
			break;
		}
	}

	private boolean isBtSwitch = false;
	private ImageView mBtSwitch;
	private void setBtSwitch(){
		isBtSwitch = !isBtSwitch;
		mSettings.setOpen(isBtSwitch);
		if (isBtSwitch) {
			mBtSwitch.setImageResource(R.drawable.ico_4157_kai);
			try {
				if(mService != null){
					mService.openBt();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			mBtSwitch.setImageResource(R.drawable.ico_4158_guan);
			try {
				if(mService != null){
					mService.closeBt();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		auto_connect_switch.setEnabled(isBtSwitch);
		auto_answer_switch.setEnabled(isBtSwitch);
	}

	private void isAnswerSwitch() {
		isAnswerSwitch = !isAnswerSwitch;
		mSettings.setAutoAnswer(isAnswerSwitch);
		if (isAnswerSwitch) {
			auto_answer_switch.setImageResource(R.drawable.ico_4157_kai);
			try {
				if(mService != null){
					mService.setAutoAnswer();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			auto_answer_switch.setImageResource(R.drawable.ico_4158_guan);
			try {
				if(mService != null){
					mService.cancelAutoAnswer();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	private void isConnectSwitch() {
		isConnectSwitch = !isConnectSwitch;
		mSettings.setAutoConnect(isConnectSwitch);
		if (isConnectSwitch) {
			auto_connect_switch.setImageResource(R.drawable.ico_4157_kai);
			try {
				if(mService != null){
					mService.setAutoConnect();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		} else {
			auto_connect_switch.setImageResource(R.drawable.ico_4158_guan);
			try {
				if(mService != null){
					mService.cancelAutoConnect();
				}
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
	}

	public void updateDeviceInfo(){
		if(MainActivity.mLocalName!=null){
			et_device_name.setText(MainActivity.mLocalName);
		}
		if(MainActivity.mPinCode!=null){
			et_pin_code.setText(MainActivity.mPinCode);
		}
	}
	
	private IGocsdkService mService;
	public void setGocsdkService(IGocsdkService service){
		mService = service;
	}
}
