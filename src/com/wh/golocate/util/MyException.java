package com.wh.golocate.util;

public class MyException extends Exception{
	public MyException(String ret){
		System.out.println("服务器出现错误:"+ret);
	}
}