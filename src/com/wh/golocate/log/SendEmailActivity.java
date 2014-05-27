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
//                 mailInfo.setPassword(password.getText().toString());//您的邮箱密码    
//                 mailInfo.setFromAddress(from.getText().toString());    
//                 mailInfo.setToAddress(to.getText().toString());    
//                 mailInfo.setSubject(subject.getText().toString());    
//                 mailInfo.setContent(body.getText().toString());    
                    //这个类主要来发送邮件   
                 SimpleMailSender sms = new SimpleMailSender();   
                     sms.sendTextMail(mailInfo);//发送文体格式    
                     //sms.sendHtmlMail(mailInfo);//发送html格式 

                } catch (Exception e) { 
                    Log.e("SendMail", e.getMessage(), e); 
                }
            } 
}