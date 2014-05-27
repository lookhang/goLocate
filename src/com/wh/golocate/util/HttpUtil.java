package com.wh.golocate.util;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

import android.util.Log;

public class HttpUtil {
	private final String TAG="HttpUtil";
	private int request_timeout,read_timeout;
	
	public HttpUtil(int request_timeout,int read_timeout){
		this.request_timeout=request_timeout;
		this.read_timeout=read_timeout;
	}
	
	public String httpPostString(String url,String paramName, String data) {
		String ret = "";
		/* ����HTTP Post���� */
		HttpPost httpRequest = new HttpPost(url);
		// Post�������ͱ���������NameValuePair[]���д���
		// ������ ����˻�ȡ�ķ���Ϊrequest.getParameter("name")
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add(new BasicNameValuePair(paramName, java.net.URLEncoder.encode(data)));
		try {
			// ����HTTP request
			httpRequest.setEntity(new UrlEncodedFormEntity(params,HTTP.UTF_8));
			
			DefaultHttpClient dfClient=new DefaultHttpClient();
			
			HttpParams hp=dfClient.getParams();
			hp.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, request_timeout);
			hp.setParameter(CoreConnectionPNames.SO_TIMEOUT, read_timeout);
			// ȡ��HTTP response
			HttpResponse httpResponse = dfClient.execute(httpRequest);

			// ��״̬��Ϊ200 ok
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// ȡ����Ӧ�ִ�
				ret = EntityUtils.toString(httpResponse.getEntity());
			} else {
				ret = "Error Response"
						+ httpResponse.getStatusLine().toString();
			}
		} catch (ClientProtocolException e) {
			ret = e.getMessage().toString();
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			ret = e.getMessage().toString();
			e.printStackTrace();
		} catch (IOException e) {
			ret = e.getMessage().toString();
			e.printStackTrace();
		}
		return ret;
	}
	
	public String httpPostByte(String url,byte[] content) throws SocketTimeoutException {
		String ret = "";
		/* ����HTTP Post���� */
		HttpPost httpRequest = new HttpPost(url);
		ByteArrayEntity arrayEntity=new ByteArrayEntity(content);
		arrayEntity.setContentType("application/octet-stream");
		try {
			// ����HTTP request
			httpRequest.setEntity(arrayEntity);
			
			DefaultHttpClient dfClient=new DefaultHttpClient();
			
			HttpParams hp=dfClient.getParams();
			hp.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, request_timeout);
			hp.setParameter(CoreConnectionPNames.SO_TIMEOUT, read_timeout);
			
			
			// ȡ��HTTP response
			HttpResponse httpResponse = dfClient.execute(httpRequest);

			// ��״̬��Ϊ200 ok
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// ȡ����Ӧ�ִ�
				ret = EntityUtils.toString(httpResponse.getEntity());
			} else {
				ret = "Error Response"
						+ httpResponse.getStatusLine().toString();
			}
		} catch (ClientProtocolException e) {
			Log.e(TAG, ""+e.getMessage().toString());
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			ret = e.getMessage().toString();
			e.printStackTrace();
		}catch(SocketTimeoutException e1){
			
		} catch (IOException e) {
			Log.e(TAG, "IO�쳣");
			//ret = e.getMessage().toString();
			e.printStackTrace();
		}
		System.out.println(ret);
		return ret;
	}
	
	public String httpGet(String url) throws ClientProtocolException, IOException, MyException{
		String ret = "";
		HttpGet httpRequest = new HttpGet(url);
		
			DefaultHttpClient dfClient=new DefaultHttpClient();
			
			HttpParams hp=dfClient.getParams();
			hp.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, request_timeout);
			hp.setParameter(CoreConnectionPNames.SO_TIMEOUT, read_timeout);
			
			// ȡ��HTTP response
			HttpResponse httpResponse = dfClient.execute(httpRequest);

			// ��״̬��Ϊ200 ok
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				// ȡ����Ӧ�ִ�
				ret = EntityUtils.toString(httpResponse.getEntity());
			} else {
				ret = "Error Response"
						+ httpResponse.getStatusLine().toString();
				throw new MyException(httpResponse.getStatusLine().toString());
				
			}
		return ret;
	}
}

