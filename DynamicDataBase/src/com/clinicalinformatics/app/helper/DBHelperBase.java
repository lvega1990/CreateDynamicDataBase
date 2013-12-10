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
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Semaphore;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public abstract class DBHelperBase<E,A> extends SQLiteOpenHelper 
{
	private HashMap<String, String> mTables = new HashMap<String, String>();
	protected SQLiteDatabase mDb;
	private Semaphore lock = new Semaphore(1);
	private boolean isOpen = false;
	protected String MODIFIED_DATE = "mModified_date";
	private String mTableName;
	public String TABLE_SQL;
	private  Class<?> mClass;
	public DBHelperBase(Context context,int version, String table,String id,Class<?> c) 
	{
		super(context, table, null, version);
		this.mClass = c;
		this.mTableName = table;
		TABLE_SQL = "CREATE TABLE " + table + "(" + id;
		Field fieldKey;
		try {
			fieldKey = mClass.getDeclaredField(id);
			if (Modifier.isPrivate(fieldKey.getModifiers())) {
				if(fieldKey.getType() == String.class)
					TABLE_SQL = TABLE_SQL + " TEXT PRIMARY KEY ";
				else if (fieldKey.getType().toString().equals("int") || 
						fieldKey.getType().toString().equals("long") ||
						fieldKey.getType() == Integer.class ||
						fieldKey.getType() == Long.class ||
						fieldKey.getType() == Boolean.class)  
						TABLE_SQL = TABLE_SQL + " INTEGER PRIMARY KEY AUTOINCREMENT ";
					else if (fieldKey.getType().toString().equals("double"))
						TABLE_SQL = TABLE_SQL + " REAL PRIMARY KEY ";
			}
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
		for (Field field : mClass.getDeclaredFields()) {
			field.setAccessible(true); // You might want to set modifier to public first.
			if (Modifier.isPrivate(field.getModifiers()) && !field.getName().equals(id) ) {
				if(field.getType() == String.class)
					TABLE_SQL = TABLE_SQL +  ", " + field.getName() + " TEXT";
				else if (field.getType().toString().equals("int") || 
						field.getType().toString().equals("long") ||
						field.getType() == Integer.class ||
						field.getType() == Long.class ||
						field.getType() == Boolean.class)     
					TABLE_SQL = TABLE_SQL +  ", " + field.getName() + " INTEGER";
				else if (field.getType().toString().equals("double"))
					TABLE_SQL = TABLE_SQL +  ", " + field.getName() + " REAL";
			}   
		}
		TABLE_SQL = TABLE_SQL+")";
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		for(String tableSQL:mTables.values())
			db.execSQL(tableSQL);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		for(String table:mTables.keySet())
			db.execSQL("DROP TABLE IF EXISTS " + table);
		onCreate(db);
	}

	protected final boolean addTable(String tableName,String sql)
	{
		return mTables.put(tableName, sql)!=null;
	}

	protected final void openDB()
	{
		try {
			lock.acquire();
			if(!isOpen)
			{
				mDb = getWritableDatabase();
				isOpen = true;
			}
			else
				Log.e("OpenDb","Database was not closed");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	protected final void closeDB()
	{
		if(isOpen)
		{
			mDb.close();
			mDb = null;
			isOpen = false;
			lock.release();
		}
		else
			Log.e("CloseDb","Database was not opened");
	}

	protected final boolean insertInTransaction(String table, ContentValues values)
	{
		boolean wasInserted = false;
		if(isOpen)
		{
			try
			{
				mDb.beginTransaction();
				wasInserted = mDb.insert(table, null, values)>0;
				mDb.setTransactionSuccessful();
			}
			catch(Exception e)
			{
				Log.e("Insert In Transaction", e.getMessage());
			}
			finally
			{
				mDb.endTransaction();
			}
		}
		else{
			Log.e("Insert In Db","Database is closed");
		}
		return wasInserted;
	}

	protected final boolean updateInTransaction(String table, ContentValues values,String where, String[] whereArgs)
	{
		boolean wasUpdated = false;
		if(isOpen)
		{
			try
			{
				mDb.beginTransaction();
				wasUpdated = mDb.update(table, values, where, whereArgs)>0;
				mDb.setTransactionSuccessful();
			}
			catch(Exception e)
			{
				Log.e("Insert In Transaction", e.getMessage());
			}
			finally
			{
				mDb.endTransaction();
			}
		}
		else{
			Log.e("Update In Db","Database is closed");
		}
		return wasUpdated;
	}

	protected final boolean deleteInTransaction(String table,String where, String[] whereArgs)
	{
		boolean wasDeleted = false;
		if(isOpen)
		{
			try
			{
				mDb.beginTransaction();
				wasDeleted = mDb.delete(table, where, whereArgs)>0;
				mDb.setTransactionSuccessful();
			}
			catch(Exception e)
			{
				Log.e("Insert In Transaction", e.getMessage());
			}
			finally
			{
				mDb.endTransaction();
			}
		}
		else{
			Log.e("Delete In Db","Database is closed");
		}
		return wasDeleted;
	}

	protected final void clearTables()
	{
		mTables.clear();
	}

	//public abstract ContentValues getContentValuesFor(E object);

	public ContentValues getContentValuesFor(E object, boolean b) {
		ContentValues contentValues = new ContentValues();
		for (Field field : object.getClass().getDeclaredFields()) {
			field.setAccessible(true);
			try {
				if (Modifier.isPrivate(field.getModifiers()) && !field.getName().equals("_id")){
					if(field.getType() == String.class){
						if (field.get(object)!=null)
							contentValues.put(field.getName(),field.get(object).toString());
						else
							contentValues.put(field.getName(),"");
					}else if(field.getType() == Integer.class){
						contentValues.put(field.getName(),field.getInt(object));
					}else if(field.getType() == Boolean.class){
						contentValues.put(field.getName(),field.getBoolean(object)?1:0);
					}else if (field.getType().toString().equals("int") )    {
						contentValues.put(field.getName(),field.getInt(object));
					}else if (field.getType().toString().equals("long") ){    
						contentValues.put(field.getName(),field.getLong(object));
					}else if (field.getType().toString().equals("double") )    
						contentValues.put(field.getName(),field.getDouble(object));
				}
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (b){
			contentValues.remove(MODIFIED_DATE);
			//contentValues.put(MODIFIED_DATE, DateTimeHelper.toString(System.currentTimeMillis()));
		}
		return contentValues;
	}


	public abstract E getObjectFromCursor(Cursor cursor);

	public E getObjectFromCursor(Cursor cursor, E object) {

		try {
			for (Field field : object.getClass().getDeclaredFields()) {
				field.setAccessible(true);
				if (Modifier.isPrivate(field.getModifiers())){
					if(field.getType() == String.class)
						field.set(object, cursor.getString(cursor.getColumnIndex(field.getName())));				
					else if(field.getType() == Integer.class){
						field.setInt(object, cursor.getInt(cursor.getColumnIndex(field.getName())));
					}else if(field.getType() == Boolean.class)
						field.setBoolean(object, cursor.getInt(cursor.getColumnIndex(field.getName())) == 1?true:false);
					else if (field.getType().toString().equals("int") )    
						field.setInt(object, cursor.getInt(cursor.getColumnIndex(field.getName())));
					else if (field.getType().toString().equals("long") || field.getType() == Long.class )    
						field.setLong(object, cursor.getLong(cursor.getColumnIndex(field.getName())));
					else if (field.getType().toString().equals("double") )    
						field.setDouble(object, cursor.getDouble(cursor.getColumnIndex(field.getName())));
				}		
			}
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return object;
	}
	//insert
	public abstract boolean insert(E object);

	protected boolean insert(String table, E object)
	{
		boolean wasInserted = false;
		openDB();
		wasInserted = insertInTransaction(table, getContentValuesFor(object,true));
		closeDB();
		return wasInserted;
	}
	//delete
	public abstract boolean delete(A key);
	protected boolean delete(String table, String keyName, A key)
	{
		boolean wasDeleted = false;
		openDB();
		wasDeleted = deleteInTransaction(table, keyName+"=?", new String[]{key.toString()});
		closeDB();
		return wasDeleted;
	}
	public boolean deleteAll()
	{
		boolean wasDeleted = false;
		openDB();
		wasDeleted = deleteInTransaction(mTableName, null,null);
		closeDB();
		return wasDeleted;
	}
	//update
	public abstract boolean update(A key, E object);

	protected boolean update(String table, String keyName, A key, E object)
	{
		boolean wasUpadated = false;
		openDB();
		wasUpadated = updateInTransaction(table, getContentValuesFor(object,true), keyName+"=?", new String[]{key.toString()});
		closeDB();
		return wasUpadated;
	}

	//select
	public abstract E select(A key);
	protected E select(String table,String keyName,A key)
	{
		E object = null;
		openDB();
		Cursor cursor = mDb.query(table, null, keyName+"=?", new String[]{key.toString()}, null, null, null);
		if(cursor.moveToNext())	
			object = getObjectFromCursor(cursor);
		cursor.close();
		closeDB();
		return object;
	}

	//select all
	public abstract ArrayList<E> selectAll();

	protected ArrayList<E> selectAll(String table)
	{
		ArrayList<E> objects= null;
		openDB();
		Cursor cursor = mDb.query(table, null, null, null, null, null, null);
		objects = new ArrayList<E>();
		while(cursor.moveToNext())	
			objects.add(getObjectFromCursor(cursor));
		cursor.close();
		closeDB();
		return objects;
	}
	protected ArrayList<E> selectAll(String table,String order)
	{
		ArrayList<E> objects= null;
		openDB();
		Cursor cursor = mDb.query(table, null, null, null, null, null, null);
		objects = new ArrayList<E>();
		while(cursor.moveToNext())	
			objects.add(getObjectFromCursor(cursor));
		cursor.close();
		closeDB();
		return objects;
	}
	protected ArrayList<E> selectAll(String table,String key,String[] value)
	{
		ArrayList<E> objects= null;
		openDB();
		Cursor cursor = mDb.query(table,null,key, value, null, null, null);
		objects = new ArrayList<E>();
		while(cursor.moveToNext())	
			objects.add(getObjectFromCursor(cursor));
		cursor.close();
		closeDB();
		return objects;
	}
	protected ArrayList<E> selectAllSort(String table,String sort)
	{
		ArrayList<E> objects= null;
		openDB();
		Cursor cursor = mDb.query(table, null, null, null, null, null, sort);
		objects = new ArrayList<E>();
		while(cursor.moveToNext())	
			objects.add(getObjectFromCursor(cursor));
		cursor.close();
		closeDB();
		return objects;
	}
	protected ArrayList<E> selectAllLike(String table,String sort)
	{
		ArrayList<E> objects= null;
		openDB();
		Cursor cursor = mDb.query(table, null, sort, null, null, null, null);
		objects = new ArrayList<E>();
		while(cursor.moveToNext())	
			objects.add(getObjectFromCursor(cursor));
		cursor.close();
		closeDB();
		return objects;
	}
	protected ArrayList<E> selectAllLike(String table,String key,String value,String sort)
	{
		ArrayList<E> objects= null;
		openDB();
		Cursor cursor = mDb.query(table,null, key+"=?" +" AND "+sort, new String[]{value}, null, null, null);
		objects = new ArrayList<E>();
		while(cursor.moveToNext())	
			objects.add(getObjectFromCursor(cursor));
		cursor.close();
		closeDB();
		return objects;
	}

	protected String getModifiedDate(String table, String keyName, A key)
	{
		String date = null;
		openDB();
		Cursor cursor = mDb.query(table,new String[]{MODIFIED_DATE}, keyName+"=?", new String[]{key.toString()}, null, null, null);
		if(cursor.moveToNext())
			date = cursor.getString(0);
		cursor.close();
		closeDB();
		return date;
	}

	public abstract boolean exists(A key);

	protected boolean exists(String table, String keyName, A key)
	{
		boolean exists = false;
		openDB();
		Cursor cursor = mDb.query(table, new String[]{keyName}, keyName+"=?", new String[]{key.toString()}, null, null, null);
		exists = cursor.moveToNext();
		cursor.close();
		closeDB();
		return exists;
	}
}
