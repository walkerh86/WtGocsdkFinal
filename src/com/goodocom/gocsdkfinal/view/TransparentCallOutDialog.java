package com.goodocom.gocsdkfinal.view;

import com.goodocom.gocsdkfinal.R;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class TransparentCallOutDialog extends AlertDialog {
	private View view;
	public TransparentCallOutDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener) {
		super(context, cancelable, cancelListener);
	}

	public TransparentCallOutDialog(Context context, int theme) {
		super(context, theme);
		view = View.inflate(context, R.layout.dialog_callout, null);
	}

	public TransparentCallOutDialog(Context context) {
		super(context);
	}
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(view);
	}
	@Override
	public View findViewById(int id) {
		return super.findViewById(id);
	}
	public View getCustomView(){
		return view;
	}
}
