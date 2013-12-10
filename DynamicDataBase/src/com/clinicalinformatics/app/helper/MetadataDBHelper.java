package com.clinicalinformatics.app.helper;
/**
 * 
 * @author Rheti Inc
 * 
 * Application class
 * 		
 * Created 2013 Sep 25
 *  
 */
import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.clinicalinformatics.app.model.MetadataModel;

public class MetadataDBHelper extends DBHelperBase<MetadataModel, String> {

	private static final int VERSION = 1;
	private static final String TABLE_NAME = "Metadata";
	private final static String ID="mName";
	
	public MetadataDBHelper(Context context)  {
		super(context,VERSION,TABLE_NAME,ID,MetadataModel.class);
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
	
	public MetadataModel getObjectFromCursor(Cursor cursor) {
		MetadataModel object = new MetadataModel();
		return super.getObjectFromCursor(cursor, object);
	}

	@Override
	public boolean insert(MetadataModel object) {
		return insert(TABLE_NAME, object);
	}

	@Override
	public boolean delete(String key) {
		return delete(TABLE_NAME, ID, key);
	}

	@Override
	public boolean update(String key, MetadataModel object) {
		return update(TABLE_NAME, ID, key, object);
	}

	@Override
	public MetadataModel select(String key) 
	{
		return select(TABLE_NAME, ID, key);
	}
	
	@Override
	public ArrayList<MetadataModel> selectAll() {
		return selectAll(TABLE_NAME);
	}

	@Override
	public boolean exists(String key) {
		return exists(TABLE_NAME, ID, key);
	}
}
