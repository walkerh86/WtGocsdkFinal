package com.goodocom.gocsdkfinal.activity;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.fragment.FragmentSetting;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

public class SettingsActivity extends BaseActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);
		
		FragmentSetting mFragmentSettings = new FragmentSetting();
		FragmentManager fragmentManager = this.getSupportFragmentManager();
		FragmentTransaction ft = fragmentManager.beginTransaction();
		ft.add(R.id.root_view, mFragmentSettings, "settings");
		ft.show(mFragmentSettings);
        ft.commit();
	}
}
