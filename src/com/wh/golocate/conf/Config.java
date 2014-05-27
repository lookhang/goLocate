package com.wh.golocate.conf;


public class Config {
	public static final String SERVER_URL="http://www.iwitcity.com:9046/IwitHttpProxy/moAction.do";
	public static final String UPDATE_SERVER = "http://iwitcity.com/android/shige/";
    public static final String UPDATE_VERJSON = "ctrl.txt";
	public static final String SERVER_SOCKET_DEFAULT="ydt.gnway.net";
	public static final int SERVER_SOCKET_PORT_DEFAULT=2001;
	public static final String PROTOCOL="SOCKET";
	
	public static final int PERSECOND_MIN=10;
	public static final int PERSECOND_MAX=999;
	
	public static final int CHECK_PERSECOND=60000;
	
	public static final String SETTING_PSW="123456";
	
	public static final String DATA_NOMAL="0";
	public static final String DATA_BUG="1";
	public static final String DATA_GSMSTATION="2";
	
	
	public static final boolean LOG_ON=true;
	public static final String LOG_FROM="iwitcity@sina.cn";
	public static final String LOG_TO="hang6@qq.com";
	public static final String LOG_USERNAME="iwitcity";
	public static final String LOG_PASS="iwitech";
	public static final String LOG_SMTP="smtp.sina.cn";
	public static final String LOG_PORT="25";
	public static final String LOG_SUBJECT="GOLOCATE_LOG_FLY";
}
