package com.goodocom.gocsdkfinal.view;

import com.goodocom.gocsdkfinal.R;

import android.content.Context;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class WtEditTextPreference extends EditTextPreference{
	private TextView mTextView;
	private String mDispText;

	public WtEditTextPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setWidgetLayoutResource(R.layout.preference_widget_textview);
	}
	
	@Override
	protected View onCreateView(ViewGroup parent) {
		View layout = super.onCreateView(parent);
		mTextView = (TextView)layout.findViewById(R.id.text);
		if(mDispText != null){
			mTextView.setText(mDispText);
		}
		Log.i("hcj.WtFragmentSetting", "mTextView="+mTextView);
		return layout;
	}
	
	public void setDispText(String text){
		Log.i("hcj.WtFragmentSetting", "setDispText mTextView="+mTextView+",text="+text);
		mDispText = text;
		if(mTextView != null){
			mTextView.setText(mDispText);
		}
	}

}
