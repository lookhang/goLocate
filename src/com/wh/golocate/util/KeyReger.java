package com.wh.golocate.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class KeyReger {
	public String generateKey(String imei) {
		//System.out.println("result: ");
		String key = "0";
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update((imei + "lookhang").getBytes());
			byte b[] = md.digest();

			int i;

			StringBuffer buf = new StringBuffer("");
			for (int offset = 0; offset < b.length; offset++) {
				i = b[offset];
				if (i < 0)
					i += 256;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}

			//System.out.println("result: " + buf.toString());//32位的加密

			//System.out.println("result: " + buf.toString().substring(8, 24));// 16位的加密

			key = buf.toString().substring(8, 24);

		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return key;
	}
	
	public boolean compareKey(String key){
		return false;
	}
	
	public static void main(String[] args) {
		new KeyReger().generateKey("123456789123123");
	}
}
