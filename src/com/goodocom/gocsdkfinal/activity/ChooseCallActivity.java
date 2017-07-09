package com.goodocom.gocsdkfinal.activity;

import com.goodocom.gocsdkfinal.R;
import com.goodocom.gocsdkfinal.service.GocsdkServiceHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class ChooseCallActivity extends Activity{
	private GocsdkServiceHelper mGocsdkServiceHelper;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_choosecall);
		
		mGocsdkServiceHelper = new GocsdkServiceHelper(null);
		mGocsdkServiceHelper.bindService(this);
		
		Intent intent = getIntent();
		String name = intent.getStringExtra("name");
		String number = intent.getStringExtra("number");
		String showName = (name != null) ? name : number;
		String showTitle = this.getString(R.string.show_call_title)+"\n"+showName;
		
		TextView titleView = (TextView)findViewById(R.id.title);
		titleView.setText(showTitle);
		View acceptBt = findViewById(R.id.accept_bt);
		acceptBt.setOnClickListener(mOnClickListener);
		View rejectBt = findViewById(R.id.reject_bt);
		rejectBt.setOnClickListener(mOnClickListener);
	}
	
	@Override
	public void onDestroy(){
		super.onDestroy();
		mGocsdkServiceHelper.unbindService(this);
	}
	
	private View.OnClickListener mOnClickListener = new View.OnClickListener() {		
		@Override
		public void onClick(View view) {
			int viewId = view.getId();
			if(viewId == R.id.accept_bt){
				mGocsdkServiceHelper.endLocalCall();
				mGocsdkServiceHelper.phoneAnswer();
			}else if(viewId == R.id.reject_bt){
				mGocsdkServiceHelper.phoneHangUp();
			}
			ChooseCallActivity.this.finish();
		}
	};
}
