package com.wh.golocate.log;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SendEmailActivity{
	
            public void onClick(View v) { 
                // TODO Auto-generated method stub                    
                try { 
               	 MailSenderInfo mailInfo = new MailSenderInfo();    
                 mailInfo.setMailServerHost("smtp.qq.com");    
                 mailInfo.setMailServerPort("25");    
                 mailInfo.setValidate(true);    
//                 mailInfo.setUserName(userid.getText().toString());    
//                 mailInfo.setPassword(password.getText().toString());//������������    
//                 mailInfo.setFromAddress(from.getText().toString());    
//                 mailInfo.setToAddress(to.getText().toString());    
//                 mailInfo.setSubject(subject.getText().toString());    
//                 mailInfo.setContent(body.getText().toString());    
                    //�������Ҫ�������ʼ�   
                 SimpleMailSender sms = new SimpleMailSender();   
                     sms.sendTextMail(mailInfo);//���������ʽ    
                     //sms.sendHtmlMail(mailInfo);//����html��ʽ 

                } catch (Exception e) { 
                    Log.e("SendMail", e.getMessage(), e); 
                }
            } 
}