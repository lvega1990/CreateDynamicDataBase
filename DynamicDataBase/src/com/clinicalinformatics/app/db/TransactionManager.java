package com.clinicalinformatics.app.db;
/**
 * 
 * @author Rheti Inc
 * 
 * Class to manager the SQLite Access, create SQLite Structured and 
 * save the AuditTrail Table
 * 
 * Created 2013 Sep 25
 *  
 *  Changes:
 *  	1) 2013 Jul 26 LO – added support to insert data in auditTrail table
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.clinicalinformatics.app.ClinicalApplication;
import com.clinicalinformatics.app.helper.DBHelperManager;
import com.clinicalinformatics.app.helper.DateTimeHelper;
import com.clinicalinformatics.app.helper.FieldHelper;
import com.clinicalinformatics.app.interfaces.SQLiteTypes;
import com.clinicalinformatics.app.model.AuditModel;
import com.clinicalinformatics.app.model.FieldModel;
import com.clinicalinformatics.app.model.FieldValues;
import com.clinicalinformatics.app.model.TableModel;
import com.clinicalinformatics.app.model.UpdateValues;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

public class TransactionManager
{
	public static final String DEFAULT_FIELD_KEY = "_id"; // sets the primary key field name 
	public static final String DEFAULT_FIELD_KEY_TYPE = "INTEGER"; // sets the primary key field type
	public static final Boolean DEFAULT_FIELD_KEY_INDEXED = true; // sets the primary key default 
	public static final String ADDED = "added"; // sets the action added 
	public static final String INACTIVATED = "inactivated";// sets the action inactivated
	public static final String DESTROYED = "destroyed"; // sets the action destroyed
	public static final String UPDATED = "updated"; // sets the action updated

	/**
	 * Constructs content values for a given Mapped Field Values
	 * @param values
	 * @return
	 */
	private static ContentValues getContentValuesFromFieldValues(HashMap<String, FieldValues> values) throws Exception
	{
		ContentValues cv = new ContentValues();
		for(String key : values.keySet())
		{
			FieldValues fv = values.get(key);
			if(fv.getType().equals(SQLiteTypes.INTEGER))
				cv.put(key, Integer.parseInt(fv.getValue().toString()));
			else if(fv.getType().equals(SQLiteTypes.REAL))
				cv.put(key,  Double.parseDouble(fv.getValue().toString()));
			else if(fv.getType().equals(SQLiteTypes.TEXT))
				cv.put(key, fv.getValue().toString());
			else if(fv.getType().equals(SQLiteTypes.BLOB)){}
				//TODO add support to blob type
		}
		return cv;
	}

	/**
	 * Insert in data base
	 * @param manager DataBaseManager
	 * @param dbName Name of the data base
	 * @param table Name of the table
	 * @param values Map of the values to insert
	 * @return true if the insertion was successful
	 */
	public static boolean insert(DBHelperManager manager, String dbName, String table, HashMap<String, FieldValues> values)
	{
		boolean wasInserted = false;
		SQLiteDatabase db = manager.getDataBase(dbName);
		db.beginTransaction();
		wasInserted = insertAtomicTransaction(db, dbName, table, values);
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return wasInserted;
	}
	/**
	 * Insert in data base
	 * @param manager DataBaseManager
	 * @param dbName Name of the data base
	 * @param table Name of the table
	 * @param values Map of the values to insert
	 * @return true if the insertion was successful
	 */
	private static boolean insertAtomicTransaction(SQLiteDatabase db, String dbName, String table, HashMap<String, FieldValues> values)
	{
		try {
			return  db.insert(table, null, getContentValuesFromFieldValues(values))>= 0;
		} catch (Exception e) {
			//TODO add SQLMANAGERException
			return false;
		}
	}
	/**
	 * Update a register in data base
	 * @param manager DataBaseManager
	 * @param dbName name of the data base
	 * @param table name of the table
	 * @param id Field value to update
	 * @param values map of values to be updated
	 * @return true if the update was successful
	 */
	public static int update(DBHelperManager manager, String dbName, String table, HashMap<String, FieldValues> values,ArrayList<FieldValues> whereValues)
	{
		int rows = -1;
		SQLiteDatabase db = manager.getDataBase(dbName);
		db.beginTransaction();

		try {
			rows = updateAtomicTransaction(db, dbName, table, values, whereValues);
		} catch (Exception e) {
			//TODO add SQLMANAGERException
			db.endTransaction();
			db.close();
			return -1;
		}

		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
		return rows;
	}
	/**
	 * Update a register in data base
	 * @param manager DataBaseManager
	 * @param dbName name of the data base
	 * @param table name of the table
	 * @param id Field value to update
	 * @param values map of values to be updated
	 * @return true if the update was successful
	 */
	private static int updateAtomicTransaction(SQLiteDatabase db, String dbName, String table, HashMap<String, FieldValues> values,ArrayList<FieldValues> whereValues) throws Exception
	{
		int rows = -1;
		if(whereValues!=null && whereValues.size()>0)
		{
			/* loop to  assemble the where clause in update statement*/
			String[] whereStringValue = new String[whereValues.size()];
			String where = "";
			for(int i = 0; i< whereValues.size(); i++)
			{
				FieldValues whereValue = whereValues.get(i);
				if(where.equals(""))
					where =  whereValue.getName()+"=?";
				else
					where = "&& " + whereValue.getName()+"=?";
				whereStringValue[i] = whereValue.getValue().toString();
			}
			rows = db.update(table, getContentValuesFromFieldValues(values), where, whereStringValue);
		}
		return rows;
	}
	/**
	 * Update a register in data base
	 * @param mContext DataBaseManager
	 * @param dbName name of the data base
	 * @param table_name name of the table
	 * @param id Field value to update
	 * @param values map of values to be updated
	 * @return true if the update was successful
	 */
	public static boolean updateAndInsert(ClinicalApplication mApp, String dbName, String table_name,TableModel table, UpdateValues values)
	{
		DBHelperManager mContext = mApp.getDbManager();
		String[] mFields = DataBaseManager.getInstance(mApp).select(dbName, table_name);
		//Get Row
		FieldValues field = new FieldValues();
		field.setName(DEFAULT_FIELD_KEY);
		field.setType(DEFAULT_FIELD_KEY_TYPE);
		field.setValue(values.getId());
		ArrayList<JSONArray> row_data;
		try {
			row_data = select(mContext, dbName, table_name, field);
		} catch (Exception e1) {
			// TODO Add SQLManagerException
			return false;
		}
		SQLiteDatabase db = mContext.getDataBase(dbName);
		try {
			db.beginTransaction();

			
			
			//Inactivation Row
			HashMap<String, FieldValues> map_values = new HashMap<String, FieldValues>();
			FieldValues fieldValues = new FieldValues();
			fieldValues.setName("termination");
			fieldValues.setType(SQLiteTypes.TEXT);
			fieldValues.setValue(DateTimeHelper.toString(System.currentTimeMillis()));
			map_values.put("termination", fieldValues);

			ArrayList<FieldValues> whereValues = new ArrayList<FieldValues>();
			whereValues.add(field);

			updateAtomicTransaction(db, dbName, table_name, map_values, whereValues);

			//Insert
			map_values  = new HashMap<String, FieldValues>();
			//Add old values in Map
			for (JSONArray json: row_data){

				for(int i = 0; i<json.length();i++)
				{
					JSONObject obj;
					obj = json.getJSONObject(i);
					FieldModel field_Structure = table.getField(obj.keys().next().toString());
					if (field_Structure != null){
						field.setName(field_Structure.getName());
						field.setType(field_Structure.getType());
						field.setValue(obj.get(obj.keys().next().toString()).toString());
						for (int j=0;j < mFields.length;j++){
							if (FieldHelper.StringToFieldModel(mFields[j]).getName().equals(field_Structure.getName())){
								map_values.put(field_Structure.getName(), field);
							}
						}
						
					}else{
						db.endTransaction();
						db.close();
						return false;
					} 

				}
			}
			//Add new Values
			for (int i = 0; i < values.getValues().length;i++){
				field = FieldHelper.StringToFieldValues(values.getValues()[i]);
				FieldModel field_Structure = table.getField(field.getName());
				if (field_Structure != null){
					field.setType(field_Structure.getType());
					map_values.remove(field.getName());
					map_values.put(field.getName(), field);
				}else{
					db.endTransaction();
					db.close();
					return false;
				}
			}
			fieldValues = new FieldValues();
			fieldValues.setName("origination");
			fieldValues.setType(SQLiteTypes.TEXT);
			fieldValues.setValue(DateTimeHelper.toString(System.currentTimeMillis()));
			map_values.put("origination", fieldValues);
			Boolean resp =insertAtomicTransaction(db, dbName, table_name, map_values);
			if (resp){
				db.setTransactionSuccessful();	
			}
			
			db.endTransaction();
			db.close();
			return resp;
		} catch (Exception e) {
			//TODO add SQLMANAGERException
			db.endTransaction();
			db.close();
			return false;
		}
	}
	/**
	 * Delete a register in the database
	 * @param manager
	 * @param dbName
	 * @param table
	 * @param id
	 * @return true if the register was successfully deleted
	 */
	public static boolean delete(DBHelperManager manager, String dbName, String table, FieldValues id) throws Exception
	{
		SQLiteDatabase db = manager.getDataBase(dbName);
		try{
			boolean wasDeleted = false;
			db.beginTransaction();
			wasDeleted = db.delete(table, id.getName()+"=?", new String[]{id.getValue().toString()})>0;
			db.setTransactionSuccessful();
			db.endTransaction();
			db.close();
			return wasDeleted;
		}catch(Exception e){
			//TODO create SQLMANAGERException
			db.endTransaction();
			db.close();
			throw e;
		}
	}

	/**
	 * Select from a specific table.
	 * @param manager
	 * @param dbName
	 * @param table
	 * @param whereValues
	 * @return ArrayList of json of the selected values
	 */
	public static ArrayList<JSONArray> select(DBHelperManager manager, String dbName, String table, FieldValues... whereValues) throws Exception
	{
		ArrayList<JSONArray> objects = new ArrayList<JSONArray>();
		SQLiteDatabase db = manager.getDataBase(dbName);
		Cursor cursor = null;
		if(whereValues!=null && whereValues.length>0)
		{
			String[] whereStringValue = new String[whereValues.length];
			String where = "";
			for(int i = 0; i< whereValues.length; i++)
			{
				if(where.equals(""))
					where = whereValues[i].getName()+"=?";
				else
					where = "&& " + whereValues[i].getName()+"=?";
				whereStringValue[i] = whereValues[i].getValue().toString();
			}
			cursor = db.query(table, null, where, whereStringValue, null, null, null);
		}
		else
			cursor = db.query(table, null, null, null, null, null, null);

		while(cursor.moveToNext())
			objects.add(objectFromCursor(cursor));
		db.close();
		return objects;
	}

	/**
	 * Maps the current cursor row to a json
	 * @param cursor
	 * @return
	 */
	private static JSONArray objectFromCursor(Cursor cursor)
	{
		JSONArray jsonObject = null;
		String[] columnNames = cursor.getColumnNames();
		if(columnNames!=null && columnNames.length>0)
		{
			jsonObject = new JSONArray();
			try {
				for(int i = 0; i< columnNames.length;i++)
				{
					JSONObject json = new JSONObject();
					String columnName = columnNames[i];
					switch(cursor.getType(i))
					{
					case Cursor.FIELD_TYPE_INTEGER:
						json.put(columnName, cursor.getInt(i));
						break;
					case Cursor.FIELD_TYPE_FLOAT:
						json.put(columnName, cursor.getDouble(i));
						break;
					case Cursor.FIELD_TYPE_STRING:
						json.put(columnName, cursor.getString(i));
						break;
					case Cursor.FIELD_TYPE_BLOB:
						//TODO add support for this
						break;
					case Cursor.FIELD_TYPE_NULL:
						json.put(columnName, "");
						break;
					default:
						json.put(columnName, "");	
					}
					jsonObject.put(json);
				}
			} catch (JSONException e) {
				//TODO add SQLMANAGERException (Field type not valid)
			}
		}
		return jsonObject;
	}
	/**
	 * Create the new table in Database, this method use Transaction to preserve the integrity of the database
	 * @param context Application Context
	 * @param dbName The name of the database
	 * @param table TableModel with the table to created
	 * 
	 */
	public static void createTable(ClinicalApplication context, String dbName, TableModel table) throws Exception {

		SQLiteDatabase db = context.getDbManager().getDataBase(dbName);
		db.beginTransaction();
		try{
			createTableAtomicTransaction(db, dbName, table);
		}catch (SQLiteException e){
			//TODO add SQLMANAGERException
			db.endTransaction();
			db.close();
			throw e;
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
	/**
	 * Create the new table in Database
	 * @param db SQLiteDataBase 
	 * @param dbName The name of the database
	 * @param table TableModel with the table to created
	 * 
	 */
	public static void createTableAtomicTransaction(SQLiteDatabase db, String dbName, TableModel table) throws Exception {
		String table_sql = "CREATE TABLE IF NOT EXISTS " + table.getName();
		table_sql = table_sql +  "( " +  DEFAULT_FIELD_KEY + " " + DEFAULT_FIELD_KEY_TYPE + " PRIMARY KEY AUTOINCREMENT";
		Map<String, FieldModel> fields = table.getFields();
		for (Iterator <Entry <String, FieldModel>> iterator_field = fields.entrySet().iterator(); iterator_field.hasNext();)
		{
			Entry <String, FieldModel> entry_field = iterator_field.next();
			String field_name = entry_field.getKey();
			FieldModel field = entry_field.getValue();
			if (!field_name.equals(DEFAULT_FIELD_KEY)){
				table_sql = table_sql +  ", " +  field.getName() + " " + field.getType();
			}
		}
		table_sql = table_sql +  ") "; 
		db.execSQL(table_sql);
	}
		
	/**
	 * Create the new field in Table, this method use Transaction to preserve the integrity of the database
	 * @param context Application Context
	 * @param dbName The name of the database
	 * @param table_name The name of the table
	 * @param field FieldModel with the field to created
	 */	
	public static void createField(ClinicalApplication context,String dbName,String table_name,FieldModel field) throws Exception{
		SQLiteDatabase db = context.getDbManager().getDataBase(dbName);

		try{
			String table_sql;
			db.beginTransaction();

			table_sql = "ALTER TABLE " + table_name + " ADD COLUMN " + field.getName()  + " " + field.getType(); 

			db.execSQL(table_sql);
		}catch (Exception e){
			//TODO add SQLMANAGERException
			db.endTransaction();
			db.close();
			throw e;
		}

		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
	/**
	 * Modify field in Table, this method use Transaction to preserve the integrity of the database
	 * @param context Application Context
	 * @param dbName The name of the database 
	 * @param table The table with initial fields
	 * @param field_name The name of the field to modify
	 * @param field FieldModel with the new field structured
	 */	
	public static void changeColumn(ClinicalApplication context,String dbName,TableModel table,String field_name,FieldModel new_fields) throws Exception
	{
		SQLiteDatabase db = context.getDbManager().getDataBase(dbName);

		try{
			//Init Transaction
			db.beginTransaction();
			// Remove the columns we don't want anymore from the table's list of columns
			List<String> updatedTableColumns = new ArrayList<String>();
			Map<String, FieldModel> fields = table.getFields();
			for (Iterator <Entry <String, FieldModel>> iterator_field = fields.entrySet().iterator(); iterator_field.hasNext();)
			{
				Entry <String, FieldModel> entry_field = iterator_field.next();
				if (!entry_field.getKey().equals(FieldHelper.StringToFieldModel(field_name).getName()) && !entry_field.getKey().equals(new_fields.getName())){
					updatedTableColumns.add(entry_field.getKey());
				}

			}
			//Columns
			String columnsSeperated = TextUtils.join(",", updatedTableColumns);
			//Delete table _old if exist
			dropTableAtomicTransaction(db, dbName, table.getName() + "_old");
			//Rename
			renameTableAtomicTransaction(db, dbName, table.getName(), table.getName() + "_old");
			//Create 
			createTableAtomicTransaction(db,dbName, table);
			//Insert Data	
			db.execSQL("INSERT INTO " + table.getName() + "(" + columnsSeperated + ") SELECT "
					+ columnsSeperated + " FROM " + table.getName() + "_old;");
			//Delete table _old if exist
			dropTableAtomicTransaction(db, dbName, table.getName() + "_old");
		}catch(Exception e){
			//TODO add SQLMANAGERException
			//RollBack and close
			db.endTransaction();
			db.close();
			throw e;
		}
		//Commit Transaction
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
	/**
	 * Modify Table name, this method use Transaction to preserve the integrity of the database
	 * @param context Application Context
	 * @param dbName The name of the database 
	 * @param table_name The name of the table
	 * @param new_table_name The new name of the table
	 */	
	public static  void renameTable(ClinicalApplication context,String dbName,String table_name,String new_table_name) throws Exception
	{
		SQLiteDatabase db = context.getDbManager().getDataBase(dbName);
		try{
			db.beginTransaction();
			//Rename Table
			renameTableAtomicTransaction(db, dbName, table_name, new_table_name);

		}catch (Exception e){
			//TODO add SQLMANAGERException
			db.endTransaction();
			db.close();
			throw e;
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
	/**
	 * Modify Table name Command
	 * @param db The SQLiteDatabase to use
	 * @param dbName The name of the database 
	 * @param table_name The name of the table
	 * @param new_table_name The new name of the table
	 */	
	public static  void renameTableAtomicTransaction(SQLiteDatabase db,String dbName,String table_name,String new_table_name) throws Exception
	{
		String table_sql = "ALTER TABLE " + table_name + " RENAME TO " + new_table_name;
		db.execSQL(table_sql);
	}
	/**
	 * Delete database, this method use Transaction to preserve the integrity of the database
	 * @param context Application Context
	 * @param dbName The name of the database 
	 */	
	public static  void dropDatabase(ClinicalApplication context,String dbName)throws Exception{
		SQLiteDatabase db = context.getDbManager().getDataBase(dbName);

		try{
			String table_sql;
			db.beginTransaction();

			table_sql = "DROP DATABASE IF EXISTS  " + dbName;

			db.execSQL(table_sql);
		}catch(Exception e){
			//TODO add SQLMANAGERException
			db.endTransaction();
			db.close();
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
	/**
	 * Delete table, this method use Transaction to preserve the integrity of the database
	 * @param context Application Context
	 * @param dbName The name of the database
	 * @param tableName The name of the table 
	 */	
	public static void dropTable(ClinicalApplication context,String dbName,String tableName) throws Exception{
		SQLiteDatabase db = context.getDbManager().getDataBase(dbName);

		try{
			String table_sql;
			db.beginTransaction();

			table_sql = "DROP TABLE IF EXISTS  " + tableName;

			db.execSQL(table_sql);
		}catch(Exception e){
			//TODO add SQLMANAGERException
			db.endTransaction();
			db.close();
			throw e;
		}
		db.setTransactionSuccessful();
		db.endTransaction();
		db.close();
	}
	/**
	 * Delete table 
	 * @param db SQLiteDatabase to use
	 * @param dbName The name of the database
	 * @param tableName The name of the table 
	 */	
	public static void dropTableAtomicTransaction(SQLiteDatabase db,String dbName,String tableName) throws Exception{
		String table_sql = "DROP TABLE IF EXISTS  " + tableName;
		db.execSQL(table_sql);
	}
	/**
	 * Save AuditTrail table.
	 * @param manager
	 * @param dbName
	 * @param new_dbName
	 * @param Action
	 */
	public static void saveAuditTrail(ClinicalApplication context,String database_name, String new_database_name,String action){
		ArrayList<AuditModel> list_model  = context.getCurrentAuditDataBase().select(database_name);
		if (list_model.size() == 0){
			AuditModel model = new AuditModel();
			model.setDbase(database_name);
			model.setAction("["+database_name+"][" + action + " to ["+new_database_name+"]]");
			model.setTermination("");
			model.setOrigination(DateTimeHelper.toString(System.currentTimeMillis()));
			context.getCurrentAuditDataBase().insert(model);
		}else{
			AuditModel model = list_model.get(0);
			model.setTermination(DateTimeHelper.toString(System.currentTimeMillis()));
			if (context.getCurrentAuditDataBase().update(model.get_id(), model)){
				AuditModel new_model = new AuditModel();
				new_model.setCompID(model.get_id());
				new_model.setDbase(new_database_name);
				new_model.setDtable(model.getDtable());
				new_model.setDtfield(model.getDtfield());
				new_model.setAction("["+database_name+"][" + action + " to ["+new_database_name+"]]");
				new_model.setTermination("");
				new_model.setOrigination(DateTimeHelper.toString(System.currentTimeMillis()));
				context.getCurrentAuditDataBase().insert(new_model);
			}
		}
	}
	/**
	 * Save AuditTrail table.
	 * @param manager
	 * @param dbName
	 * @param tableName
	 * @param new_tableName
	 * @param Action
	 */
	public static void saveAuditTrail(ClinicalApplication context,String database_name,String table_name, String new_table_name,String action){
		ArrayList<AuditModel> list_model  = context.getCurrentAuditDataBase().select(database_name);
		if (list_model.size() == 0){
			AuditModel model = new AuditModel();
			model.setDbase(database_name);
			model.setDtable(table_name);
			model.setAction("["+database_name+"][" +table_name +"][" + action + " to ["+database_name+"]["+new_table_name+"]]");
			model.setTermination("");
			model.setOrigination(DateTimeHelper.toString(System.currentTimeMillis()));
			context.getCurrentAuditDataBase().insert(model);
		}else{
			AuditModel model = list_model.get(0);
			model.setTermination(DateTimeHelper.toString(System.currentTimeMillis()));
			if (context.getCurrentAuditDataBase().update(model.get_id(), model)){
				AuditModel new_model = new AuditModel();
				new_model.setCompID(model.get_id());
				new_model.setDbase(database_name);
				new_model.setDtable(new_table_name);
				new_model.setDtfield(model.getDtfield());
				new_model.setAction("["+database_name+"][" +table_name +"][" + action + " to ["+database_name+"]["+new_table_name+"]]");
				new_model.setTermination("");
				new_model.setOrigination(DateTimeHelper.toString(System.currentTimeMillis()));
				context.getCurrentAuditDataBase().insert(new_model);
			}
		}
	}
	/**
	 * Save AuditTrail table.
	 * @param manager
	 * @param dbName
	 * @param tableName
	 * @param field_name
	 * @param new_field_name
	 * @param Action
	 */
	public static void saveAuditTrail(ClinicalApplication context,String database_name,String table_name,String field_name, String new_field_name,String action){
		ArrayList<AuditModel> list_model  = context.getCurrentAuditDataBase().select(database_name);
		if (list_model.size() == 0){
			AuditModel model = new AuditModel();
			model.setDbase(database_name);
			model.setDtable(table_name);
			model.setDtfield(field_name);
			model.setAction("["+database_name+"][" +table_name +"]["+ field_name+"][" + action + " to ["+database_name+"]["+table_name+"]["+new_field_name+"]]");
			model.setTermination("");
			model.setOrigination(DateTimeHelper.toString(System.currentTimeMillis()));
			context.getCurrentAuditDataBase().insert(model);
		}else{
			AuditModel model = list_model.get(0);
			model.setTermination(DateTimeHelper.toString(System.currentTimeMillis()));
			if (context.getCurrentAuditDataBase().update(model.get_id(), model)){
				AuditModel new_model = new AuditModel();
				new_model.setCompID(model.get_id());
				new_model.setDbase(database_name);
				new_model.setDtable(table_name);
				new_model.setDtfield(new_field_name);
				new_model.setAction("["+database_name+"][" +table_name +"]["+ field_name+"][" + action + " to ["+database_name+"]["+table_name+"]["+new_field_name+"]]");
				new_model.setTermination("");
				new_model.setOrigination(DateTimeHelper.toString(System.currentTimeMillis()));
				context.getCurrentAuditDataBase().insert(new_model);
			}
		}
	}
}
