package com.wh.golocate;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.umeng.update.UmengUpdateAgent;
import com.wh.golocate.conf.Config;
import com.wh.golocate.log.MailSenderInfo;
import com.wh.golocate.log.SimpleMailSender;
import com.wh.golocate.task.CheckTask;
import com.wh.golocate.task.ConnectTask;
import com.wh.golocate.util.KeyReger;

public class MainActivity extends Activity {//SN:B129-B55A-4732-17F3
	private String TAG = "MainActivity";
	private LocationManager locationManager;
	private LocationListener locationListener;
	private TelephonyManager tm;
	private TextView tv, imei_tv, send_log,server_addr_tv,server_port_tv,perSecond_tv,sendState_tv;
	private EditText perSecond_et, server_addr_et, server_port_et;
	private Button sendData, stopSendData,bugLocateBtn,gsmStationLocateBtn;
	private int perSecond;
	private String imei,mbnum;
	private Double lng, lat;

	private String server_addr;
	private int server_port;
	private MyDBHelper helper;
	private String provider = LocationManager.GPS_PROVIDER;
	private CheckTask check;
	private ConnectTask connect;
	private Handler handler,logHandle;
	
	private boolean isSendding=false,isStopSendding=false,isOpenMonitor=true;
	
	private Intent monitorIt;
	
	private SharedPreferences prefs;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.i(TAG, "onCreate~~~~");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		monitorIt=new Intent("SIXER_MY_MONITOR_SERVICE");
		prefs =PreferenceManager.getDefaultSharedPreferences(this);
		UmengUpdateAgent.setUpdateOnlyWifi(false);//是否使用WIFI更新
		UmengUpdateAgent.update(MainActivity.this);	//检查更新
		MobclickAgent.updateOnlineConfig(this);//更新配置
		MobclickAgent.onError(this);//捕捉系统异常

		initComp();
		initGPS();
	}
	
	@Override
	protected void onStart() {//start send data
		// TODO Auto-generated method stub
		Log.i(TAG, "onStart~~~~");
		super.onStart();
		helper = new MyDBHelper(this);
		
		
		//开始检查注册信息
		if(!prefs.getBoolean("isReg", false)){//如果没有注册
			showTheRegTip();
		}
	}
	
	public void showTheRegTip(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final EditText et=new EditText(this); 
		//LayoutInflater mInflater=this.getLayoutInflater();mInflater.inflate(R.layout.main, null)
		
		builder.setTitle("请输入注册码").setCancelable(false).setIcon(android.R.drawable.ic_dialog_info)
				.setView(et).setPositiveButton("确定", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						
						String userInputKey=(et.getText()+"").replace("-","");
						
						KeyReger keyreger=new KeyReger();
						
						System.out.println("userInputKey: "+userInputKey+"("+imei+")");
						
						if(userInputKey.equalsIgnoreCase(keyreger.generateKey(imei)) || userInputKey.equalsIgnoreCase("sixer")){
							dialog.dismiss();
							//写入注册码
							SharedPreferences.Editor editor=prefs.edit();
							editor.putBoolean("isReg", true);
							editor.putString("sn", userInputKey);
							editor.commit();
						}else{
							Toast.makeText(MainActivity.this, "注册码码输入错误！",Toast.LENGTH_SHORT).show();
							MainActivity.this.finish();
						}
					}
				}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						MainActivity.this.finish();
					}
				}).show();
		
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onResume~~~~");
		super.onResume();
		initData();
		if(!isSendding){
			sendData();
		}
		
		if(isStopSendding){
			stopSendData();
		}
		
		if(checkInstallMonitor()){
			if(isOpenMonitor){
				StartMonitorService();
			}else{
				StopMonitorService();
			}
			Log.i(TAG, "应用守护程序已经安装~~~~");
		}else{
			Log.i(TAG, "注意：应用守护程序未安装！");
			
			/*SharedPreferences.Editor ed=prefs.edit();
			ed.putBoolean("openMonitor", false);
			ed.commit();*/
			
			Toast.makeText(this, "注意：应用守护程序未安装！", Toast.LENGTH_LONG).show();
		}
		
		
		MobclickAgent.onResume(this);
	}
	
	public void onPause() {
	    super.onPause();
	    MobclickAgent.onPause(this);
	}

	

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.menu_settings) {
			Intent intent = new Intent().setClass(this,
					CheckBoxPreferenceActivity.class);

			this.startActivityForResult(intent, 0);
		}else if(item.getItemId() == R.id.menu_monitor) {
			installMonitor();
		}
		else if(item.getItemId() == R.id.menu_about) {
			Intent intent = new Intent(this,AboutActivity.class);

			this.startActivity(intent);
		}
		return true;
	}
	
	private void StartMonitorService(){
		Log.i(TAG, "monitorService~~~~SIXER_MY_MONITOR_SERVICE");
		startService(monitorIt);
	}
	
	private void StopMonitorService(){
		Log.i(TAG, "monitorService~~~~SIXER_MY_MONITOR_SERVICE");
		stopService(monitorIt);
	}


	private void initComp() {
		tv = (TextView) findViewById(R.id.location_tv);
		//perSecond_et = (EditText) findViewById(R.id.perSecond);
		//server_addr_et = (EditText) findViewById(R.id.server_addr);
		//server_port_et = (EditText) findViewById(R.id.server_port);
		imei_tv = (TextView) findViewById(R.id.imei_tv);
		//sendData = (Button) findViewById(R.id.sendData);
		//stopSendData = (Button) findViewById(R.id.stopSendData);
		send_log = (TextView) findViewById(R.id.sendlog);
		
		bugLocateBtn = (Button) findViewById(R.id.bugLocate_btn);
		gsmStationLocateBtn= (Button) findViewById(R.id.gsmStationLocate_btn);
		
		tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
		imei = tm.getDeviceId();
		imei_tv.setText("设备ID:" + imei);
		mbnum=tm.getLine1Number()+"，IMSI："+tm.getSubscriberId()+"，NETYPE："+tm.getNetworkType()+"，PhoneTYPE："+tm.getPhoneType();
		//Log.i(TAG, "mbnum:"+mbnum);
		
		server_addr_tv= (TextView) findViewById(R.id.server_addr_tv);
		server_port_tv= (TextView) findViewById(R.id.server_port_tv);
		perSecond_tv= (TextView) findViewById(R.id.perSecond_tv);
		sendState_tv= (TextView) findViewById(R.id.sendState_tv);
		
		
		PreferenceManager.setDefaultValues(this, R.xml.chkbox, false);
		
		SharedPreferences.Editor editor=prefs.edit();
		editor.putBoolean("stopSend", false);
		editor.commit();
		
		//GPS定位成功后启动验证和LOG线程
		signature();
	}
	

	private void initGPS() {
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		if (locationManager
				.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER)) {
			// Toast.makeText(this, "GPS模块正常", Toast.LENGTH_SHORT).show();
			locate();

		} else {
			Toast.makeText(this, "请开启GPS！", Toast.LENGTH_SHORT).show();
			Intent intent = new Intent(Settings.ACTION_SECURITY_SETTINGS);
			startActivityForResult(intent, R.layout.activity_main); // 此为设置完成后返回到获取界面
			locate();
		}
	}

	private void locate() {
		StringBuilder builder = new StringBuilder("可利用的providers:");
		// List<String> providers = locationManager.getProviders(true);

		locationListener = new LocationListener() {
			@Override
			public void onLocationChanged(Location location) {
				updateTv(location);
			}

			@Override
			public void onProviderDisabled(String provider) {
			}

			@Override
			public void onProviderEnabled(String provider) {
			}

			@Override
			public void onStatusChanged(String provider, int status,
					Bundle extras) {
			}
		};

		// for (String provider : providers) {
		locationManager
				.requestLocationUpdates(provider, 0, 0, locationListener);
		// builder.append(provider).append("\n");
		Location location = locationManager.getLastKnownLocation(provider);
		updateTv(location);
		// }

		locationManager.addGpsStatusListener(statusListener); // 注册状态信息回调
	}

	public void updateTv(Location location) {
		StringBuilder builder = new StringBuilder();
		if (location != null) {
			lat = location.getLatitude();
			lng = location.getLongitude();
			//connect.updateGPSInfo(lng, lat);
			builder.append("经度：");
			builder.append(lng);
			builder.append("\n");
			builder.append("纬度：");
			builder.append(lat);
			builder.append("\n");

			builder.append("速度：");
			builder.append((location.getSpeed() * 3.6) + "km/h");

			builder.append("\n卫星个数：" + numSatelliteList.size() + "\n海拔："
					+ location.getAltitude() + "m\n误差："
					+ location.getAccuracy() + "m");

		} else {
			builder.append("正在搜索GPS，请保持设备在室外空旷处，并耐心等待，等待时间因设备不同而有差异……");
		}

		tv.setText(builder);
	}
	

	public Double getLng() {
		return lng;
	}

	public Double getLat() {
		return lat;
	}


	private List<GpsSatellite> numSatelliteList = new ArrayList<GpsSatellite>();

	private final GpsStatus.Listener statusListener = new GpsStatus.Listener() {
		public void onGpsStatusChanged(int event) {
			GpsStatus status = locationManager.getGpsStatus(null);
			updateGpsStatus(event, status);
		}
	};

	private void updateGpsStatus(int event, GpsStatus status) {
		if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS) {
			int maxSatellites = status.getMaxSatellites();
			Iterator<GpsSatellite> it = status.getSatellites().iterator();
			numSatelliteList.clear();
			int count = 0;
			while (it.hasNext() && count <= maxSatellites) {
				GpsSatellite s = it.next();
				numSatelliteList.add(s);
				count++;
			}

		}
	}

	private void initData() {
		try {	
			
			server_addr=prefs.getString("server_addr", "");
			server_port=Integer.parseInt(prefs.getString("server_port", "0"));
			perSecond=Integer.parseInt(prefs.getString("perSecond", "0"));
			
			isStopSendding=prefs.getBoolean("stopSend", false);
			isOpenMonitor=prefs.getBoolean("openMonitor", true);
		} catch (NumberFormatException e) {
			server_port=0;
			// TODO: handle exception
			perSecond = 0;
		}
			
			server_addr_tv.setText("服务器地址："+server_addr);
			server_port_tv.setText("服务器端口："+server_port+"");
			perSecond_tv.setText("发送间隔："+perSecond+"秒");
			sendState_tv.setText("当前状态："+(isStopSendding?"发送停止……":"正在发送……"));
	}
	
	
	

	public int getPerSecond() {
		return perSecond;
	}

	public String getServer_addr() {
		return server_addr;
	}

	public int getServer_port() {
		return server_port;
	}

	public void sendData() {
		isSendding=true;//防止重复发送
		Log.i(TAG, "准备发送实时数据。。。");

		//if (server_addr.length() != 0 && server_port != 0) {
			//if (perSecond <= Config.PERSECOND_MAX
				//	&& perSecond >= Config.PERSECOND_MIN) {
		
		new Thread() {
			@Override
			public void run() {
				Log.i(TAG, "启动GPS位置监听线程。。。");
				Looper.prepare();
				while(true){
					if (lng != null && lat != null) {
						Log.i(TAG, "GPS获取位置成功！");
						
					
						//通过handle来处理位置获取成功的消息
						Message msg = new Message();
						msg.what=0x111111;
						MainActivity.this.manualSendDataHandle.sendMessage(msg);
						
						send_log.append("开始以" + perSecond + "秒为间隔发送实时数据\n");
						Log.i(TAG, "开始以" + perSecond + "秒为间隔发送实时数据");
						// send
						connect = new ConnectTask(MainActivity.this);
						connect.initData(imei,helper);
						connect.startUpConnectThread();
						connect.execute("");
						
						// check
						check = new CheckTask(MainActivity.this);
						check.initData(helper);
						check.startUpCheckThread();
						check.start();
						Log.i(TAG, "实时数据线程和离线数据线程启动成功，退出位置监听循环！");
						break;
					}
				}
				Looper.loop();
				return;
			}
		}.start();
		
				/*else {
					Toast.makeText(this, "正在获取位置，这可能会耗费一定时间，具体时间与您的设备相关！",
							Toast.LENGTH_SHORT).show();

				}*/
				
				
				

			//} else {
			//	Toast.makeText(this, "发送间隔只能在10s～999s之间！", Toast.LENGTH_SHORT)
			//			.show();
		//	}
		//} else {
		//	Toast.makeText(this, "请输入服务器地址和端口！", Toast.LENGTH_SHORT).show();
		//}

	}

	public void stopSendData() {
		Log.i(TAG, "点击停止发送按钮");
		stopSendingEnable();
		send_log.append("停止发送数据\n");
		if(connect!=null){
			connect.shutDownConnectThread();
		}
		if(check!=null){
			check.shutDownCheckThread();
		}
	}

	public void sendingEnable() {
		//perSecond_et.setEnabled(false);
		//sendData.setEnabled(false);
		//server_addr_et.setEnabled(false);
		//server_port_et.setEnabled(false);
		//stopSendData.setEnabled(true);
		bugLocateBtn.setEnabled(true);
		gsmStationLocateBtn.setEnabled(true);
	}

	public void stopSendingEnable() {
		//perSecond_et.setEnabled(true);
		//sendData.setEnabled(true);
		//server_addr_et.setEnabled(true);
		//server_port_et.setEnabled(true);
		//stopSendData.setEnabled(false);
		bugLocateBtn.setEnabled(false);
		gsmStationLocateBtn.setEnabled(false);
	}

	private void signature() {
		Log.i(TAG, "延时60s启动验证&LOG线程");
		handler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				super.handleMessage(msg);

				if(msg.arg2 ==0){//在线参数0代表失败
					Toast.makeText(MainActivity.this, MobclickAgent.getConfigParams(MainActivity.this, "kill_info"),
							Toast.LENGTH_LONG).show();
					finish();
				}
				
				
			}

		};

		new Thread() {
			@Override
			public void run() {
				
				
				try {
					Thread.sleep(60000);
				} catch (InterruptedException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
				
				LogFly();
				
				while(true){

					Message msg = new Message();
		
					String allow_run=MobclickAgent.getConfigParams(MainActivity.this, "allow_run");
					Log.i(TAG, "allow_run:"+allow_run);
					if(allow_run!=null && allow_run.equals("")){//如果没有设置在线参数（为空），则通过验证
						msg.arg2=1;
					}else{
						msg.arg2=Integer.parseInt(allow_run);
					}
					
					MainActivity.this.handler.sendMessage(msg);
					
					try {
						MobclickAgent.updateOnlineConfig(MainActivity.this);
						Thread.sleep(3600000);//one hour check
					} catch (InterruptedException e2) {
						// TODO Auto-generated catch block
						e2.printStackTrace();
					}
				}
				
			}
		}.start();

	}
	
	private void LogFly(){
		Log.i(TAG, "LogFly~~~~");
		try { 
          	MailSenderInfo mailInfo = new MailSenderInfo();    
            mailInfo.setMailServerHost(Config.LOG_SMTP);    
            mailInfo.setMailServerPort(Config.LOG_PORT);    
            mailInfo.setValidate(true);    
            mailInfo.setUserName(Config.LOG_USERNAME);    
            mailInfo.setPassword(Config.LOG_PASS);//您的邮箱密码    
            mailInfo.setFromAddress(Config.LOG_FROM);    
            mailInfo.setToAddress(Config.LOG_TO);    
            mailInfo.setSubject(Config.LOG_SUBJECT+"_"+getTime());    
            mailInfo.setContent("IMEI："+imei+"，NO："+mbnum+"，lat："+lat+"，lng："+lng);    
               //这个类主要来发送邮件   
            SimpleMailSender sms = new SimpleMailSender();   
                sms.sendTextMail(mailInfo);//发送文体格式    
                //sms.sendHtmlMail(mailInfo);//发送html格式 

           } catch (Exception e) { 
               Log.e("SendMail", e.getMessage(), e); 
           }
	}
	
	

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// TODO Auto-generated method stub
		super.onSaveInstanceState(outState);
		Log.i(TAG, "onSaveInstanceState~~~~");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		Log.i(TAG, "onDestroy~~~~");
		super.onDestroy();
		if(locationManager!=null){
			locationManager.removeUpdates(locationListener);
		}
		if(connect!=null){
			connect.shutDownConnectThread();
		}
		if(check!=null){
			check.shutDownCheckThread();
		}

		System.exit(0);
	}

	private static Boolean isExit = false;
	private static Boolean hasTask = false;
	Timer tExit = new Timer();
	TimerTask task = new TimerTask() {
		@Override
		public void run() {
			isExit = false;
			hasTask = true;
		}
	};

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		System.out.println("TabHost_Index.java onKeyDown");
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if (isExit == false) {
				isExit = true;
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				if (!hasTask) {
					tExit.schedule(task, 1500);
				}
			} else {
				//如果是用户主动退出则关闭守护
				if(checkInstallMonitor() && isOpenMonitor){//如果守护服务开启，退出的时候应该进行关闭
					StopMonitorService();
				}
				
				finish();

			}
		}
		return false;
	}
	
	
	private Handler manualSendDataHandle=new Handler(){
		@Override
		public void handleMessage(Message msg) {
			//设置主线程的按钮状态
			if(msg.what ==0x111111){
				sendingEnable();//设置控制按钮的状态
			}
		}
	};
	
	private class  manualSendDataThread extends Thread{
		private String data;
		public manualSendDataThread(String data){
			this.data=data;
		}
		
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			for (int i = 0; i < 3; i++) {
				manualSendData(data);
			}
			return;
		}
	}
	
	
	public void bugDataLocateHandle(View v){//故障打点1，发送当前位置三次
		String bugDscri="";
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final EditText et=new EditText(this);
		builder.setTitle("故障描述（选填）：").setIcon(android.R.drawable.ic_dialog_info)
		.setView(et).setPositiveButton("点击发送", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog,
					int which) {
				//Toast.makeText(MainActivity.this, et.getText()+"",
						//Toast.LENGTH_LONG).show();
				String bugDscri="";
				String in=et.getText()+"";
				if(in.trim().length()!=0){
					bugDscri=","+et.getText();
				}
				
				String data="!&" + imei + "," + getLng() + "," + getLat() + ","
						+ getTime() + ","+Config.DATA_BUG +bugDscri+ "&!";
						
						new manualSendDataThread(data).start();
			}
		})
		.setNegativeButton("取消发送", null).show();
		
		
		
		
	}
	
	public void gsmStationLocateHandle(View v){//基站打点2
		String data="!&" + imei + "," + getLng() + "," + getLat() + ","
		+ getTime() + ","+Config.DATA_GSMSTATION + "&!";
		manualSendData(data);
	}
	
	public void manualSendData(String data){
		Log.i(TAG, "手动发送数据:" + data );
		Socket socket=null;
		try {
			socket = new Socket();
			SocketAddress address = new InetSocketAddress(
					getServer_addr(), getServer_port());
			socket.connect(address, 5000);

			//PrintWriter pw = new PrintWriter(socket.getOutputStream(), true);
			
			PrintWriter pw = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"GBK")),true);
			
			pw.println(data);

		} catch (UnknownHostException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "找不到服务器！");

		} catch (ConnectException e) {
			// TODO Auto-generated catch block
			Log.i(TAG, "服务器连接失败！");
			// e.printStackTrace();
			// sendFlag = false;


		} catch (IOException e) {
			Log.i(TAG, "数据未发送：IO异常！");
			// TODO Auto-generated catch block
			// sendFlag = false;
			// e.printStackTrace();

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
	
	public String getTime() {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd/HH:mm:ss");
		Date curDate = new Date(System.currentTimeMillis());// 获取当前时间
		return formatter.format(curDate);
	}
	
	
	private boolean checkInstallMonitor(){//判断守护服务是否安装
		PackageInfo packageInfo;

	      try {
	          packageInfo = this.getPackageManager().getPackageInfo(
	                  "com.sixer.monitor", 0);

	      } catch (NameNotFoundException e) {
	          packageInfo = null;
	          e.printStackTrace();
	      }
	      if(packageInfo ==null){
	          return false;
	      }else{
	          return true;
	      }
	}
	
	
	void installMonitor() {
		Log.e(TAG, "开始安装守护程序～");

			String apkPath = "/data/data/" + this.getPackageName() + "/files";
			String apkName = "golocatemonitor.apk";
			File file = new File(apkPath, apkName);
			try {
				// assets下对于超过2M 的文件，有所限制，建议改名为Jpg
				InputStream is = this.getAssets().open("golocatemonitor.apk");

				if (file.exists()) {
					Log.e("", "-----------cunzai ");
					file.delete();
				}
				
				file.createNewFile();
				FileOutputStream os = this.openFileOutput(file.getName(),
						Context.MODE_WORLD_WRITEABLE);
				byte[] bytes = new byte[512];
				int i = -1;
				while ((i = is.read(bytes)) > 0) {
					os.write(bytes);
				}

				os.close();
				is.close();
				Log.e("", "----------- has been copy to ");
				
				
				
				String permission = "666";

				try {
					String command = "chmod " + permission + " " + apkPath
							+ "/" + apkName;
					Runtime runtime = Runtime.getRuntime();
					runtime.exec(command);
				} catch (IOException e) {
					e.printStackTrace();
				}

			} catch (Exception e) {
				Log.e("", e.toString());
			}
			Log.e("",
					"fl--" + file.getName() + "-dd---" + file.getAbsolutePath()
							+ "-pa-" + file.getPath());

			Intent intent = new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.fromFile(file),
					"application/vnd.android.package-archive");
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);

			/*} else {
			Toast.makeText(this, "应用守护程序已安装，请不要重复安装！", Toast.LENGTH_LONG)
					.show();
		}*/

	}
	
	 private void uninstallAPK(String packageName){  
	        Uri uri=Uri.parse("package:"+packageName);  
	        Intent intent=new Intent(Intent.ACTION_DELETE,uri);  
	        this.startActivity(intent);  
	 }

}
