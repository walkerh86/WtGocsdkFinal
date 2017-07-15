package com.goodocom.gocsdkfinal.activity;

import java.util.ArrayList;

import com.goodocom.gocsdk.IGocsdkServiceSimple;
import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.fragment.FragmentSetting;
import com.goodocom.gocsdkfinal.fragment.WtFragmentAvailBtList;
import com.goodocom.gocsdkfinal.fragment.WtFragmentCalllogList;
import com.goodocom.gocsdkfinal.fragment.WtFragmentContactsList;
import com.goodocom.gocsdkfinal.fragment.WtFragmentPairedList;
import com.goodocom.gocsdkfinal.fragment.WtFragmentSetting;
import com.goodocom.gocsdkfinal.service.GocsdkCallbackImp;
import com.goodocom.gocsdkfinal.service.GocsdkServiceHelper;

import android.os.Bundle;
import android.app.Fragment;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import android.annotation.Nullable;
//import android.support.v4.app.FragmentManager;
//import android.support.v4.app.FragmentTransaction;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;

public class WtContactsActivity extends BaseActivity {
	private GocsdkServiceHelper mGocsdkServiceHelper;
	private GocsdkCallbackImp mCallBack;
	
	private WtFragmentContactsList mFragmentContactsList;
	private WtFragmentCalllogList mFragmentCalllogList;
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
		
		setContentView(R.layout.activity_contacts);
		
		mViewPager = (ViewPager)findViewById(R.id.view_pager);
		ArrayList<Fragment> fragmentList = new ArrayList<Fragment>();
		fragmentList.add(new WtFragmentContactsList());
		fragmentList.add(new WtFragmentCalllogList());
		mViewPager.setAdapter(new MyFragmentPagerAdapter(getFragmentManager(), fragmentList));
	}
	
	@Override
	public void onAttachFragment(Fragment fragment) {
		Log.i("hcj.WtFragmentSetting", "onAttachFragment fragment="+fragment);
		if (fragment instanceof WtFragmentContactsList) {
			mFragmentContactsList = (WtFragmentContactsList)fragment;
			mFragmentContactsList.setGocsdkServiceHelper(mGocsdkServiceHelper,mCallBack);
		}else if (fragment instanceof WtFragmentCalllogList) {
			mFragmentCalllogList = (WtFragmentCalllogList)fragment;
			mFragmentCalllogList.setGocsdkServiceHelper(mGocsdkServiceHelper,mCallBack);
		}
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mGocsdkServiceHelper.unbindService(this);
	}
	
	public static void showBtDisconnected(Context context){
		Toast.makeText(context, context.getString(R.string.warning_connect), Toast.LENGTH_SHORT).show();		
	}
	
	public static  void placeCall(String number,GocsdkServiceHelper gocsdkService) {
		if (number.length() == 0)
			return;
		if (PhoneNumberUtils.isGlobalPhoneNumber(number)) {
			if (number == null || !TextUtils.isGraphic(number)) {
				return;
			}

			gocsdkService.phoneDail(number);
		}
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
