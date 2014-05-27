package com.wh.golocate.task;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.conn.ConnectTimeoutException;

import com.wh.golocate.MyDBHelper;
import com.wh.golocate.conf.Config;
import com.wh.golocate.util.HttpUtil;
import com.wh.golocate.util.MyException;
import com.wh.golocate.MainActivity;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

public class ConnectTask extends AsyncTask<String, String, String> {
	private final String TAG="ConnectTask";
	private String server_addr;
	private int server_port;
	private MyDBHelper helper;
	
	private String imei, time;
	private Double lng, lat;
	private int perSecond;
	
	private boolean sendFlag=false;
	
	private Context context;

	public ConnectTask(Context context) {
		this.context=context;
	}

	@Override
	protected String doInBackground(String... params) {
		String ret = "";
		while (sendFlag) {
			// Log.i(TAG, "正在发送。。。");
			time = getTime();
			this.lng=((MainActivity)context).getLng();
			this.lat=((MainActivity)context).getLat();
			this.server_addr=((MainActivity)context).getServer_addr();
			this.server_port=((MainActivity)context).getServer_port();
			this.perSecond=((MainActivity)context).getPerSecond();
			
			publishProgress(time + "，" + lng + "，" + lat + "，" + imei);// 添加日志

			if (Config.PROTOCOL.equalsIgnoreCase("HTTP")) {
				/*try {
					HttpUtil http = new HttpUtil(5000, 5000);
					ret = http.httpGet(Config.SERVER_URL + "?param=fomtk"
							+ imei + "," + lng + "," + lat + "," + time);
					Log.i(TAG, "本次返回结果" + ret);
				} catch (ConnectTimeoutException e1) {
					Log.i(TAG, "连接超时！");
					e1.printStackTrace();
					publishProgress("数据未发送：服务器连接超时！\n");
					sendFlag = false;
				} catch (UnknownHostException e) {
					Log.i(TAG, "找不到服务器！");
					e.printStackTrace();
					publishProgress("数据未发送：找不到服务器！\n");
					sendFlag = false;
				} catch (ClientProtocolException e) {
					// TODO Auto-generated catch block
					publishProgress("数据未发送：客户端协议异常！\n");
					sendFlag = false;
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					publishProgress("数据未发送：读写错误！\n");
					sendFlag = false;
					e.printStackTrace();
				} catch (MyException e) {
					publishProgress("数据未发送：服务器响应出现异常！\n");
					sendFlag = false;
					e.printStackTrace();
				}*/

			} else if (Config.PROTOCOL.equalsIgnoreCase("SOCKET")) {
				Log.i(TAG, "开始向" + server_addr + ":" + server_port
						+ "发送数据！");
				Socket socket=null;
				try {
					socket = new Socket();
					SocketAddress address = new InetSocketAddress(
							server_addr, server_port);
					socket.connect(address, 5000);

					PrintWriter pw = new PrintWriter(
							socket.getOutputStream(), true);
					pw.println("!&" + imei + "," + lng + "," + lat + ","
							+ time + ","+Config.DATA_NOMAL + "&!");

				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, "找不到服务器！");
					// e.printStackTrace();
					publishProgress("数据未发送：找不到服务器！\n");
					// sendFlag = false;
					helper.insert(imei, lng, lat, time);

				} catch (ConnectException e) {
					// TODO Auto-generated catch block
					Log.i(TAG, "服务器连接失败！");
					// e.printStackTrace();
					publishProgress("数据未发送：服务器连接失败！\n");
					// sendFlag = false;
					helper.insert(imei, lng, lat, time);

				} catch (IOException e) {
					Log.i(TAG, "数据未发送：IO异常！");
					// TODO Auto-generated catch block
					publishProgress("数据未发送：IO异常（不能连接服务器）！\n");
					// sendFlag = false;
					// e.printStackTrace();
					helper.insert(imei, lng, lat, time);

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
			}

			try {
				Thread.sleep(perSecond * 1000);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		return ret;
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

	@Override
	protected void onPostExecute(String result) {
		Log.i(TAG, "实时数据发送线程终止：" + result);

	}

	@Override
	protected void onPreExecute() {

	}

	@Override
	protected void onProgressUpdate(String... values) {
		//send_log.append(values[0] + "\n");
	}
	
	public synchronized void startUpConnectThread() { 
		sendFlag = true ; 
    } 
	
	public synchronized void shutDownConnectThread() { 
		sendFlag = false ; 
    } 
	
    public synchronized boolean isShutDown() { 
        return sendFlag; 
    }
    
    
    public synchronized void initData(String imei,MyDBHelper helper) {
		this.imei=imei;
		this.helper=helper;
    }
    
	
	
	
	public String getTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		return formatter.format(curDate);
	}

}
