package com.clinicalinformatics.app.interfaces;
/**
 * 
 * @author Rheti Inc
 * 
 * Application class
 * 		
 * Created 2013 Sep 25
 *  
 */
public interface SQLiteTypes 
{
	public static String INTEGER = "INTEGER";
	public static String REAL = "REAL";
	public static String TEXT = "TEXT";
	public static String BLOB = "BLOB";
	
	public enum MetadataStatus
	{
		NEW,EDIT,DONE;
	}
}
