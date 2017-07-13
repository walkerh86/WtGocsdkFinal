package com.goodocom.gocsdkfinal.activity;

import java.util.ArrayList;

import com.goodocom.gocsdk.IGocsdkServiceSimple;
import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.fragment.FragmentSetting;
import com.goodocom.gocsdkfinal.fragment.WtFragmentAvailBtList;
import com.goodocom.gocsdkfinal.fragment.WtFragmentPairedList;
import com.goodocom.gocsdkfinal.fragment.WtFragmentSetting;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;
import com.goodocom.gocsdkfinal.service.GocsdkServiceHelper;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.annotation.Nullable;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

public class SettingsActivity extends BaseActivity {
	private GocsdkServiceHelper mGocsdkServiceHelper;
	private GocsdkCallbackImp mCallBack;
	
	private WtFragmentSetting mFragmentSetting;
	private WtFragmentPairedList mFragmentPairedList;
	private WtFragmentAvailBtList mFragmentAvailList;
	private ViewPager mViewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		mCallBack = new GocsdkCallbackImp();
		
		mGocsdkServiceHelper = new GocsdkServiceHelper(new GocsdkServiceHelper.OnServiceConnectListener() {			
			@Override
			public void onServiceDisconnected() {
				
			}
			
			@Override
			public void onServiceConnected(IGocsdkServiceSimple service) {
				mGocsdkServiceHelper.registerCallback(mCallBack);				
			}
		});
		mGocsdkServiceHelper.bindService(this);
		
		
		
		setContentView(R.layout.activity_settings);
		
		mViewPager = (ViewPager)findViewById(R.id.view_pager);
		ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
		fragmentList.add(new WtFragmentSetting());
		fragmentList.add(new WtFragmentPairedList());
		fragmentList.add(new WtFragmentAvailBtList());
		mViewPager.setAdapter(new MyFragmentPagerAdapter(getFragmentManager(), fragmentList));
		
		/*
		WtFragmentSetting fragmentSettings = new WtFragmentSetting();
		fragmentSettings.setGocsdkServiceHelper(mGocsdkServiceHelper);
		Log.i("hcj.WtFragmentSetting", "onCreate mFragmentSettings="+fragmentSettings);
		FragmentTransaction ft = getFragmentManager().beginTransaction();
		ft.add(R.id.root_view, fragmentSettings, "settings");
		ft.show(fragmentSettings);
        ft.commit();*/
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		Log.i("hcj.WtFragmentSetting", "onAttachFragment fragment="+fragment);
		if (fragment instanceof WtFragmentSetting) {
			mFragmentSetting = (WtFragmentSetting)fragment;
			mFragmentSetting.setGocsdkServiceHelper(mGocsdkServiceHelper);
		}else if (fragment instanceof WtFragmentPairedList) {
			mFragmentPairedList = (WtFragmentPairedList)fragment;
			mFragmentPairedList.setGocsdkServiceHelper(mGocsdkServiceHelper,mCallBack);
		}else if (fragment instanceof WtFragmentAvailBtList) {
			mFragmentAvailList = (WtFragmentAvailBtList)fragment;
			mFragmentAvailList.setGocsdkServiceHelper(mGocsdkServiceHelper,mCallBack);
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mGocsdkServiceHelper.unbindService(this);
	}
	
	private class MyFragmentPagerAdapter extends FragmentPagerAdapter{
		private ArrayList<Fragment> mFragmentList;

		public MyFragmentPagerAdapter(FragmentManager fragmentManager, ArrayList<Fragment> fragmentList) {
			super(fragmentManager);
			
			mFragmentList = fragmentList;
		}

		@Override 
		public Fragment getItem(int arg0) {
			// TODO Auto-generated method stub
			return mFragmentList.get(arg0);
		}

		@Override
		public int getCount() {
			// TODO Auto-generated method stub
			return mFragmentList.size();
		}
		
	}
}
