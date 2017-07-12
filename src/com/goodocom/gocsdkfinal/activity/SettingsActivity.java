package com.goodocom.gocsdkfinal.activity;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.fragment.WtFragmentSetting;

import android.os.Bundle;
import android.view.View;
import android.annotation.Nullable;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class SettingsActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		WtFragmentSetting mFragmentSettings = new WtFragmentSetting();
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(R.id.root_view, mFragmentSettings, "settings");
		ft.show(mFragmentSettings);
        ft.commit();
	}
	
}
