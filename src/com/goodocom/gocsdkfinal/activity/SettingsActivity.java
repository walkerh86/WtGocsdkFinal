package com.goodocom.gocsdkfinal.activity;

import com.goodocom.gocsdk.IGocsdkServiceSimple;
import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.fragment.FragmentSetting;
import com.goodocom.gocsdkfinal.fragment.WtFragmentSetting;
import com.goodocom.gocsdkfinal.service.GocsdkServiceHelper;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.annotation.Nullable;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class SettingsActivity extends BaseActivity {
	private GocsdkServiceHelper mGocsdkServiceHelper;
	//private WtFragmentSetting mFragmentSetting;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mGocsdkServiceHelper = new GocsdkServiceHelper(new GocsdkServiceHelper.OnServiceConnectListener() {			
			@Override
			public void onServiceDisconnected() {
				
			}
			
			@Override
			public void onServiceConnected(IGocsdkServiceSimple service) {
				//mGocsdkServiceHelper.registerCallback(callback);
				
			}
		});
		mGocsdkServiceHelper.bindService(this);
		
		setContentView(R.layout.activity_settings);
		
		WtFragmentSetting fragmentSettings = new WtFragmentSetting();
		fragmentSettings.setGocsdkServiceHelper(mGocsdkServiceHelper);
		Log.i("hcj.WtFragmentSetting", "onCreate mFragmentSettings="+fragmentSettings);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(R.id.root_view, fragmentSettings, "settings");
		ft.show(fragmentSettings);
        ft.commit();
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		Log.i("hcj.WtFragmentSetting", "onAttachFragment fragment="+fragment);
		/*
		if (fragment instanceof WtFragmentSetting) {
			mFragmentSetting = (WtFragmentSetting)fragment;
			if(mGocsdkServiceHelper != null){
				mFragmentSetting.setGocsdkServiceHelper(mGocsdkServiceHelper);
			}
		}*/
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mGocsdkServiceHelper.unbindService(this);
	}
}
