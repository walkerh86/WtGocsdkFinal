package com.goodocom.gocsdkfinal.view;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class NoScrollViewPager extends ViewPager {

	public NoScrollViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoScrollViewPager(Context context) {
		super(context);
	}
	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		
		return false;//不拦截子控件的滑动事�?
	}
	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return true;//让该ViewPager不能滑动
	}
}
