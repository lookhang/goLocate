package com.wh.golocate.task;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.wh.golocate.MainActivity;
import com.wh.golocate.MyDBHelper;
import com.wh.golocate.conf.Config;

public class CheckTask extends Thread {
	private final String TAG="CheckTask";
	private String server_addr;
	private int server_port;
	private MyDBHelper helper;
	private Activity activity;
	
	private boolean checkTaskFlag=true;
	public CheckTask(Activity activity) {
		this.activity=activity;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		String ret = "";
		Log.i(TAG, "启动每" + Config.CHECK_PERSECOND/1000 + "s的频率检查是否有离线数据！");
		while (checkTaskFlag) {
			
			this.server_addr=((MainActivity)activity).getServer_addr();
			this.server_port=((MainActivity)activity).getServer_port();
			
			String log="";//send log
			Cursor c = helper.query();
			activity.startManagingCursor(c);
			int count = c.getCount();
			if (count != 0) {
				Log.i(TAG, "发现" + count + "条离线数据，即将发送！");
				if (c.moveToFirst()) {

					do {
						String imei = c.getString(1);
						Double lng = c.getDouble(2);
						Double lat = c.getDouble(3);
						String time = c.getString(4);
						Socket socket=null;
						try {
							socket = new Socket();
							SocketAddress address = new InetSocketAddress(
									server_addr, server_port);
							socket.connect(address, 10000);

							PrintWriter pw = new PrintWriter(
									socket.getOutputStream(), true);
							pw.println("!&" + imei + "," + lng + "," + lat
									+ "," + time + ","+Config.DATA_NOMAL + "&!");
							helper.deleteById(c.getInt(0));
						} catch (UnknownHostException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
							// checkFlag=false;
							//Log.i(TAG, "离线数据发送失败！（找不到服务器）");
							log="离线数据发送失败！（找不到服务器）";
						} catch (ConnectException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
							// checkFlag=false;
							//Log.i(TAG, "离线数据发送失败！（连接错误）");
							log="离线数据发送失败！（连接错误）";
						} catch (IOException e) {
							// TODO Auto-generated catch block
							// e.printStackTrace();
							// checkFlag=false;
							//Log.i(TAG, "离线数据发送失败！（不能连接服务器）");
							log="离线数据发送失败！（不能连接服务器）";
						}finally{
							if(socket!=null){
								try {
									socket.close();
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
							
						}

						// Log.i(TAG, "imei=" + imei + ",lng=" + lng+
						// ",lat=" + lat+ ",time=" + time);

					} while (c.moveToNext());
					
					Log.i(TAG, "本次发送结果：" + log);
				}
				// c.close();

			} else {
				Log.i(TAG, "本次检查，没有离线数据！");
			}

			try {
				Thread.sleep(Config.CHECK_PERSECOND);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		Log.i(TAG, "离线数据发送线程终止！");
		return;
	}
	
	public synchronized void initData(MyDBHelper helper) {
		this.helper=helper;
    }
	
	public synchronized void startUpCheckThread() { 
		checkTaskFlag = true ; 
    } 
	
	public synchronized void shutDownCheckThread() { 
		checkTaskFlag = false ; 
    } 
	
    public synchronized boolean isShutDown() { 
        return checkTaskFlag; 
    } 
}
