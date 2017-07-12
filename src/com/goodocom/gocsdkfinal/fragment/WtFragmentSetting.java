package com.goodocom.gocsdkfinal.fragment;

import com.goodocom.gocsdkfinal.GocsdkSettings;
import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.view.WtEditTextPreference;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.Log;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.View;
import android.widget.ListView;

public class WtFragmentSetting extends PreferenceFragment{
	private static final String TAG = "hcj.WtFragmentSetting";
	
	private GocsdkSettings mSettings;
	private SwitchPreference mBtSwitchPreference;
	private SwitchPreference mAutoConnectPreference;
	private SwitchPreference mAutoAnswerPreference;
	private WtEditTextPreference mNamePreference;
	private WtEditTextPreference mPinPreference;
	
	private static final String KEY_BT_SWITCH = "key_bt_switch";
	private static final String KEY_AUTO_CONNECT = "key_auto_connect";
	private static final String KEY_AUTO_ANSWER = "key_auto_answer";
	private static final String KEY_DEV_NAME = "key_bt_name";
	private static final String KEY_PIN_CODE = "key_bt_pin";
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mSettings = GocsdkSettings.getInstance(getActivity());
        
        addPreferencesFromResource(R.xml.bt_settings);
        
        mBtSwitchPreference = (SwitchPreference) findPreference(KEY_BT_SWITCH);
        mBtSwitchPreference.setOnPreferenceChangeListener(mPreferenceChangeListener);
        mBtSwitchPreference.setChecked(mSettings.isOpen());
        
        mAutoConnectPreference = (SwitchPreference) findPreference(KEY_AUTO_CONNECT);
        mAutoConnectPreference.setOnPreferenceChangeListener(mPreferenceChangeListener);
        mAutoConnectPreference.setChecked(mSettings.isAutoConnect());
        
        mAutoAnswerPreference = (SwitchPreference) findPreference(KEY_AUTO_ANSWER);
        mAutoAnswerPreference.setOnPreferenceChangeListener(mPreferenceChangeListener);
        mAutoAnswerPreference.setChecked(mSettings.isAutoAnswer());
        
        mNamePreference = (WtEditTextPreference) findPreference(KEY_DEV_NAME);
        mNamePreference.setOnPreferenceChangeListener(mPreferenceChangeListener);
                
        mPinPreference = (WtEditTextPreference) findPreference(KEY_PIN_CODE);
        mPinPreference.setOnPreferenceChangeListener(mPreferenceChangeListener);        
	}
	
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		ListView listView = getListView();
		if(listView != null){
			listView.setPaddingRelative(4,0,4,0);
		}
		
		mNamePreference.setDispText(mSettings.getLocalName());
		mPinPreference.setDispText(mSettings.getLocalPin());
    }
		
	private OnPreferenceChangeListener mPreferenceChangeListener = new OnPreferenceChangeListener(){
		@Override
		public boolean onPreferenceChange(Preference preference, Object objValue) {
			final String key = preference.getKey();
			Log.i(TAG, "key="+key);
			if (KEY_BT_SWITCH.equals(key)) {
				mSettings.setOpen((Boolean)objValue);
			}else if (KEY_AUTO_CONNECT.equals(key)) {
				mSettings.setAutoConnect((Boolean)objValue);
			}else if (KEY_AUTO_ANSWER.equals(key)) {
				mSettings.setAutoAnswer((Boolean)objValue);
			}else if (KEY_DEV_NAME.equals(key)) {
				if(objValue != null){
					String name = (String)objValue;
					mSettings.setLocalName(name);
					mNamePreference.setDispText(name);
				}
			}else if (KEY_PIN_CODE.equals(key)) {
				if(objValue != null){
					String code = (String)objValue;
					mSettings.setLocalPin(code);
					mPinPreference.setDispText(code);
				}
			}
			return true;
		}		
	};
}
