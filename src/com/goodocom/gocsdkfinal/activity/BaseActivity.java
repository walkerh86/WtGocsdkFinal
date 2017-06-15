package com.goodocom.gocsdkfinal.activity;

import com.goodocom.gocsdkfinal.key.HomeKey;
import com.goodocom.gocsdkfinal.key.HomeKey.OnHomePressedListener;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

public class BaseActivity extends FragmentActivity {

	private HomeKey mHomeKey;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mHomeKey = new HomeKey(this);
		mHomeKey.setOnHomePressedListener(new OnHomePressedListener() {
			
			@Override
			public void onHomePressed() {
				finish();
			}
			
			@Override
			public void onHomeLongPressed() {
				finish();
			}
		});
		mHomeKey.startWatch();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mHomeKey.stopWatch();
	}

	@Override
	protected void onPause() {
		super.onPause();
		
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onStart() {
		super.onStart();
	}

	@Override
	protected void onStop() {
		super.onStop();
	}
	
}
