package com.wh.golocate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * 
 * @author 
 * 显示系统启动完成广播接收器
 */
public class BootReceiver extends BroadcastReceiver{
	private final String TAG="BootReceiver";
	@Override
	public void onReceive(Context context, Intent intent) {
		// 显示广播信息
		Log.i(TAG, "BOOT_COMPLETED~~~~~~~~~~~~~~~~");
		
		SharedPreferences prefs =
        	    PreferenceManager.getDefaultSharedPreferences(context);
		boolean autorun=prefs.getBoolean("boot_autorun", false);
		
		if(autorun){
			Intent myintent=new Intent(context,MainActivity.class);
			myintent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			context.startActivity(myintent);
		}
		
	}
	
	

}
