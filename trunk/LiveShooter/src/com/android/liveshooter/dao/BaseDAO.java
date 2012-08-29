package com.android.liveshooter.dao;

import java.util.ArrayList;

import android.net.Uri;

/**
 * base DAO for storing URI path & authorities
 * */
public abstract class BaseDAO {
	
	protected final String DROP_TABLE            = "DROP TABLE IF EXISTS ";	
	protected static final char CHARACTER_SLASH  = '/';
	
	/**
	 * String for VideoProvider authorities
	 * */
	public static final	String AUTHORITIES       = "LIVE_SHOOTER";
	
	/**
	 * String for table name.
	 * */
	public static final String TABLE_COMMON = "TABLE_Common";
	public static final Uri URI_COMMON = Uri.parse(new StringBuffer()
													.append("content://")
													.append(AUTHORITIES)
													.append(CHARACTER_SLASH)
													.append(TABLE_COMMON).toString()
													);
	
	
 	/**
 	 * create a String for creating table in database
 	 * @return String , String for creating table.
 	 * */
	public abstract String createTableString();
	
	/**
	 * drop table if exists
	 * @return String , for executing ,remove table from database
	 * */
	public abstract String dropTable();
	
	/**
	 * insert data into database
	 * @param obj , completely down-loaded entity info
	 * */
	public abstract void insert(Object obj);
	
	/**
	 * delete record with filter
	 * @param where , A filter to apply to rows before deleting, formatted as an SQL WHERE clause (excluding the WHERE itself).
	 * @param selectionArgs , 
	 * */
	public abstract void delete(String where ,String[] selectionArgs);
	 
	/**
	 * query records with filter
	 * @param where , A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows for the given URI.
	 * @param selectionArgs , You may include ?s in selection, which will be replaced by the values from selectionArgs, in the order that they appear in the selection. The values will be bound as Strings.
	 * @return ArrayList<Object> , the list of records
	 * */
	public abstract ArrayList<?> query(String[] selections,String where, String[] selectionArgs,String sortOrder);
	
	/**
	 * update and reset the records
	 * @param obj,
	 * @param where,  A filter to apply to rows before deleting, formatted as an SQL WHERE clause (excluding the WHERE itself).
	 * @param selectionArgs,
	 * */
	public abstract void update(Object obj ,String where, String[] selectionArgs); 

}
