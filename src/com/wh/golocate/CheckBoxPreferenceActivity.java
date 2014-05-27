package com.wh.golocate;


import com.wh.golocate.conf.Config;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;
import android.widget.EditText;
import android.widget.Toast;

public class CheckBoxPreferenceActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	private final String ETP_ADDR_KEY = "server_addr",
			ETP_PORT_KEY = "server_port",ETP_PERSECOND_KEY = "perSecond",SWIP_STOPSEND_KEY="stopSend",OPEN_MONITOR_KEY="openMonitor";
	private EditTextPreference etp_addr, etp_port,etp_persecond;
	private CheckBoxPreference swip_stopsend,swip_openMonitor;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		checkUser();
			addPreferencesFromResource(R.xml.chkbox);
			etp_addr = (EditTextPreference) getPreferenceScreen().findPreference(
					ETP_ADDR_KEY);
			etp_port = (EditTextPreference) getPreferenceScreen().findPreference(
					ETP_PORT_KEY);
		
			etp_persecond = (EditTextPreference) getPreferenceScreen().findPreference(
					ETP_PERSECOND_KEY);
			swip_stopsend = (CheckBoxPreference) getPreferenceScreen().findPreference(
					SWIP_STOPSEND_KEY);
			swip_openMonitor=(CheckBoxPreference) getPreferenceScreen().findPreference(
					OPEN_MONITOR_KEY);
	}
	
	@Override  
    protected void onResume() {  
        super.onResume();  
        // Setup the initial values  
        etp_addr.setSummary(getPreferenceScreen().getSharedPreferences().getString(ETP_ADDR_KEY, "192.168.1.50"));
        etp_port.setSummary(getPreferenceScreen().getSharedPreferences().getString(ETP_PORT_KEY, "8888"));
        etp_persecond.setSummary(getPreferenceScreen().getSharedPreferences().getString(ETP_PERSECOND_KEY, "30"));
        swip_stopsend.setSummary((getPreferenceScreen().getSharedPreferences().getBoolean(SWIP_STOPSEND_KEY, false))?"ֹͣ����":"���ڷ���");
        swip_openMonitor.setSummary((getPreferenceScreen().getSharedPreferences().getBoolean(OPEN_MONITOR_KEY, true))?"�ѿ��������ػ�":"�����ػ��ѹر�");
        // Set up a listener whenever a key changes              
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this); 
     }  
  
    @Override  
    protected void onPause() {  
        super.onPause();  
  
        // Unregister the listener whenever a key changes              
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);      
    }  

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		SharedPreferences.Editor editor=sharedPreferences.edit();
		if (key.equals(ETP_ADDR_KEY)) {
			String server_addr=sharedPreferences.getString(key, "192.168.1.50");
			if (server_addr.length() == 0){//���Ϊ������ʾ���Զ����ΪĬ�ϵ�ַ192.168.1.50
				Toast.makeText(CheckBoxPreferenceActivity.this, "�������������ַ��",Toast.LENGTH_SHORT).show();
				
				editor.putString(key, "192.168.1.50");
				editor.commit();
			}
			etp_addr.setSummary(sharedPreferences.getString(key, "192.168.1.50"));
		} else if (key.equals(ETP_PORT_KEY)) {
			int server_port=0;
			try {
				server_port=Integer.parseInt(sharedPreferences.getString("server_port", "8888"));
			} catch (NumberFormatException e) {
				server_port=0;
			}
			if(server_port==0){//����˿���д����δ������֣�����ʾ���Զ����ΪĬ�϶˿�8888
				Toast.makeText(CheckBoxPreferenceActivity.this, "��������ȷ�Ķ˿ںţ�",Toast.LENGTH_SHORT).show();
				editor.putString(key, "8888");
				editor.commit();
			}
			etp_port.setSummary(sharedPreferences.getString(key, ""));
			
		}else if (key.equals(ETP_PERSECOND_KEY)) {
			int perSecond=0;
			try {
				perSecond=Integer.parseInt(sharedPreferences.getString(key, "30"));
			} catch (NumberFormatException e) {
				perSecond=0;
			}
			
			if (perSecond > Config.PERSECOND_MAX
					|| perSecond < Config.PERSECOND_MIN){//������ʱ��û����������ֻ������������Զ����ΪĬ��ֵ30
				Toast.makeText(CheckBoxPreferenceActivity.this, "���ͼ��ֻ����10s��999s֮�䣡",Toast.LENGTH_SHORT).show();
				editor.putString(key, "30");
				editor.commit();
			}
			etp_persecond.setSummary(sharedPreferences.getString(key, ""));
		}else if (key.equals(SWIP_STOPSEND_KEY)) {
			boolean isStopSendding=sharedPreferences.getBoolean(SWIP_STOPSEND_KEY, false);
			
			if(isStopSendding){
				swip_stopsend.setEnabled(false);
			}
			
			swip_stopsend.setSummary(isStopSendding?"ֹͣ����":"���ڷ���");
		}else if (key.equals(OPEN_MONITOR_KEY)) {
			boolean isOpenMonitor=sharedPreferences.getBoolean(OPEN_MONITOR_KEY, true);
			
		
			swip_openMonitor.setSummary(isOpenMonitor?"�ѿ��������ػ�":"�����ػ��ѹر�");
		}
	}
	
	private void checkUser(){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final EditText et=new EditText(this); 
		//LayoutInflater mInflater=this.getLayoutInflater();mInflater.inflate(R.layout.main, null)
		
		builder.setTitle("����������").setCancelable(false).setIcon(android.R.drawable.ic_dialog_info)
				.setView(et).setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						
						String psw=et.getText()+"";
						
						if(psw.equals(Config.SETTING_PSW)){
							dialog.dismiss();
						}else{
							Toast.makeText(CheckBoxPreferenceActivity.this, "�����������",Toast.LENGTH_SHORT).show();
							CheckBoxPreferenceActivity.this.finish();
						}
					}
				}).setNegativeButton("ȡ��", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog,
							int which) {
						CheckBoxPreferenceActivity.this.finish();
					}
				}).show();
		
	}
}
