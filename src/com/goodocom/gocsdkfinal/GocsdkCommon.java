package com.goodocom.gocsdkfinal;

import android.util.Log;

public class GocsdkCommon {
	public final void debug() {
		if (!Config.DEBUG) return;
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
		
		Log.d("goc",
				this.getClass().getSimpleName() + ":"
						+ traceElement.getMethodName());
	}

	public final void debug(String str) {
		if (!Config.DEBUG) return;
		StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
		Log.d("goc",
				this.getClass().getSimpleName() + ":"
						+ traceElement.getMethodName()
						+"::"+str);
	}

	public final void error(String str) {
		Log.e("goc", this.getClass().getName() + ":" + str);
	}
}
