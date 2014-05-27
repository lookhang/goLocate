package com.wh.golocate;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.TextView;

public class AboutActivity extends Activity {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.about);
        
        TextView tv=(TextView)findViewById(R.id.about_tv);
        SharedPreferences prefs =PreferenceManager.getDefaultSharedPreferences(this);
        tv.setText("Version£º"+getVerName(this)+"\r\n"+"SN£º"+prefs.getString("sn", ""));
    }
	
	
	public String getVerName(Context context) {
		String verName = "";
		try {
			verName = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {

		}
		return verName;
	}
}
