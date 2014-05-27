package com.wh.golocate.service;

import java.util.List;

import com.wh.golocate.MainActivity;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class MonitorService extends Service {
	private final String TAG="MonitorService";
	private final String MY_PKG_NAME="com.wh.golocate";
	private boolean isAppRunning=false;
	
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		Log.i(TAG, "onCreate~~~~MonitorService"+this);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		Log.i(TAG, "onDestroy~~~~MonitorService"+this);
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		//return super.onStartCommand(intent, flags, startId);
		Log.i(TAG, "onStartCommand~~~~MonitorService"+this);
		if(!monitorThread.isAlive()){
			monitorThread.start();
		}
		return START_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		// TODO Auto-generated method stub
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	
	private Thread monitorThread=new Thread(){

		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			Log.i(TAG, "启动程序守护线程~~MonitorService");
			while(true){
				Intent mainIntent = new Intent(MonitorService.this, MainActivity.class);
				ActivityManager am = (ActivityManager)MonitorService.this.getSystemService(Context.ACTIVITY_SERVICE);
				List<RunningTaskInfo> list = am.getRunningTasks(1);
				for (RunningTaskInfo info : list) {
					Log.i(TAG, "top::::"+info.topActivity+",base::::"+info.baseActivity);
				    if (info.topActivity.equals(mainIntent.getComponent()) && 
				        info.baseActivity.equals(mainIntent.getComponent())) {
				            isAppRunning = true;
				            //find it, break
				            Log.i(TAG, "应用程序正常~~~~"+MonitorService.this);
				            break;
				    }
				}
				
				if(!isAppRunning){
					Log.i(TAG, "发现程序关闭，立即启动~~~~MonitorService");
			    	Intent it=new Intent(MonitorService.this,MainActivity.class);
			    	it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); 
			    	startActivity(it);
				}
				
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
	};

}
