package com.goodocom.gocsdkfinal;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class GocsdkSettings {
	public static GocsdkSettings mGocsdkSettings;
	private SharedPreferences mSettings;
	
	private GocsdkSettings(Context context){
		mSettings = PreferenceManager.getDefaultSharedPreferences(context);
	}
	
	public static GocsdkSettings getInstance(Context context){
		if(mGocsdkSettings == null){
			mGocsdkSettings = new GocsdkSettings(context);
		}
		return mGocsdkSettings;
	}
	
	public boolean isOpen(){
		return mSettings.getBoolean("is_open", false);
	}
	
	public void setOpen(boolean open){
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putBoolean("is_open", open);
		editor.commit();
	}
	
	public boolean isAutoConnect(){
		return mSettings.getBoolean("auto_connect", false);
	}
	
	public void setAutoConnect(boolean autoConnect){
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putBoolean("auto_connect", autoConnect);
		editor.commit();
	}
	
	public boolean isAutoAnswer(){
		return mSettings.getBoolean("auto_answer", false);
	}
	
	public void setAutoAnswer(boolean autoAnswer){
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putBoolean("auto_answer", autoAnswer);
		editor.commit();
	}
	
	public String getLocalName(){
		return mSettings.getString("local_name", "BLINK Q9");
	}
	
	public void setLocalName(String name){
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putString("local_name", name);
		editor.commit();
	}
	
	public String getLocalPin(){
		return mSettings.getString("local_pin", "0000");
	}
	
	public void setLocalPin(String code){
		SharedPreferences.Editor editor = mSettings.edit();
		editor.putString("local_pin", code);
		editor.commit();
	}
}
