package com.goodocom.gocsdkfinal.view;

import com.goodocom.gocsdkfinal.R;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;

public class TransparentDialog extends Dialog {
	private Context mContext;

	public TransparentDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
		// TODO Auto-generated constructor stub
	}

	public TransparentDialog(Context context, int theme) {
		super(context, theme);
		// TODO Auto-generated constructor stub
		mContext = context;
	}

	public TransparentDialog(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.popupwindow_connect_device);
		ImageView dialog_iv_anim = (ImageView) findViewById(R.id.dialog_iv_anim);
		Animation rotateAnim = AnimationUtils.loadAnimation(mContext, R.anim.rotate);
		LinearInterpolator li = new LinearInterpolator();
		rotateAnim.setInterpolator(li);
		dialog_iv_anim.startAnimation(rotateAnim);
	}
}
