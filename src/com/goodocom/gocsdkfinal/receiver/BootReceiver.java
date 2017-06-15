package com.goodocom.gocsdkfinal.receiver;



import com.goodocom.gocsdkfinal.service.GocsdkService;
import com.goodocom.gocsdkfinal.service.PlayerService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BootReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		System.out.println("接收到开机启动广播了！");
		Intent service = new Intent(context,GocsdkService.class);
		context.startService(service);
		/*
		service = new Intent(context,PlayerService.class);
		context.startService(service);*/
	}
}
