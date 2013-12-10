package com.clinicalinformatics.app.helper;
/**
 * 
 * @author Rheti Inc
 * 
 * Application class
 * 		
 * Created 2013 Sep 26
 *  
 */
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.clinicalinformatics.app.model.AuditModel;

public class AuditModelDBHelper extends DBHelperBase<AuditModel, Integer> {

	private static final int VERSION = 1;
	private static final String TABLE_NAME = "auditTrail";
	private final static String ID="_id";
	
	public AuditModelDBHelper(Context context)  {
		super(context,VERSION,TABLE_NAME,ID,AuditModel.class);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		clearTables();
		addTable(TABLE_NAME, TABLE_SQL);
		super.onCreate(db);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		clearTables();	
		addTable(TABLE_NAME, TABLE_SQL);
		super.onUpgrade(db,oldVersion,newVersion);
	}
	
	public AuditModel getObjectFromCursor(Cursor cursor) {
		AuditModel object = new AuditModel();
		return super.getObjectFromCursor(cursor, object);
	}

	@Override
	public boolean insert(AuditModel object) {
		return insert(TABLE_NAME, object);
	}

	@Override
	public boolean delete(Integer key) {
		return delete(TABLE_NAME, ID, key);
	}

	@Override
	public boolean update(Integer key, AuditModel object) {
		return update(TABLE_NAME, ID, key, object);
	}

	@Override
	public AuditModel select(Integer key) 
	{
		return select(TABLE_NAME, ID, key);
	}
	
	public ArrayList<AuditModel> select(String database_name) 
	{
		return selectAll(TABLE_NAME,"dbase=? AND termination =?", new String[]{database_name,""});
	}
	
	public ArrayList<AuditModel> select(String database_name,String table_name) 
	{
		return selectAll(TABLE_NAME,"dbase=? AND dtable=?  AND termination =?", new String[]{database_name,table_name,""});
	}
	
	public ArrayList<AuditModel> select(String database_name,String table_name,String field_name) 
	{
		return selectAll(TABLE_NAME,"dbase=? AND dtable=? AND dtfield=?  AND termination =?", new String[]{database_name,table_name,field_name,""});
	}
	
	@Override
	public ArrayList<AuditModel> selectAll() {
		return selectAll(TABLE_NAME);
	}

	@Override
	public boolean exists(Integer key) {
		return exists(TABLE_NAME, ID, key);
	}
}
