package com.wh.golocate;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyDBHelper extends SQLiteOpenHelper{
	private static final String CREATE_TABLE_SQL = " create table location(_id integer PRIMARY KEY,imei text,lng text,lat text,time text) ";
	private SQLiteDatabase db;
	MyDBHelper(Context c){
		super(c,"golocate.db", null, 2);
	}
	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_SQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
	}
	
	public void insert(String imei,Double lng,Double lat,String time){
		String sql = "insert into location(imei,lng,lat,time)values('"+imei+"','"+lng+"','"+lat+"','"+time+"') ";
		getWritableDatabase().execSQL(sql);
	}
	
	public Cursor query(){
		return getWritableDatabase().query("location", null, null, null, null, null, null);
		
	}
	
	public void deleteById(int _id){
		String sql = "delete from location where _id="+_id;
		getWritableDatabase().execSQL(sql);
	}
}
