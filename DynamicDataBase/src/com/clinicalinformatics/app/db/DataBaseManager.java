package com.clinicalinformatics.app.db;

/**
 * 
 * @author Rheti Inc
 * 
 * Class with the methods needed to manipulate databases
 * 		
 * Created 2013 Sep 25
 *  
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;

import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import com.clinicalinformatics.app.ClinicalApplication;
import com.clinicalinformatics.app.helper.DateTimeHelper;
import com.clinicalinformatics.app.helper.FieldHelper;
import com.clinicalinformatics.app.interfaces.SQLiteTypes;
import com.clinicalinformatics.app.model.DBModel;
import com.clinicalinformatics.app.model.FieldModel;
import com.clinicalinformatics.app.model.FieldValues;
import com.clinicalinformatics.app.model.MetadataModel;
import com.clinicalinformatics.app.model.TableModel;
import com.clinicalinformatics.app.model.UpdateValues;
import com.google.gson.Gson;

public class DataBaseManager {

	private static String DB_PATH = ""; //Default DBPath
	private static DataBaseManager sDBManagerInstance; //Static references to actual instances
	private ClinicalApplication mContext; //My Application

	/**
	 * Return to actual instance of the class
	 * @param ClinicalApplication The Application Context
	 * @return The Actual DatabaseManager
	 */
	public static DataBaseManager getInstance(ClinicalApplication context)
	{
		if(sDBManagerInstance == null)
			sDBManagerInstance = new DataBaseManager(context);
		return sDBManagerInstance;
	}
	/**
	 * Default class Constructor
	 * @param ClinicalApplication The Application Context
	 */
	private DataBaseManager(ClinicalApplication context) 
	{
		this.mContext = context;
		DB_PATH ="/data/data/" + context.getPackageName() + "/databases/";
	}

	/**
	 * Create the database
	 * @param dbName name of the data base
	 * @return true if the update was successful
	 */
	public boolean add(String dbName){
		MetadataModel metadataModelExist = mContext.getCurrentMetadataDataBase().select(dbName);
		if (metadataModelExist == null){
			/*New DataBase*/
			DBModel database = new DBModel();
			database.setName(dbName);

			MetadataModel metadataModel = new MetadataModel();
			metadataModel.setDataBaseName(dbName);
			/*Create database*/
			SQLiteDatabase abstractDB =  mContext.openOrCreateDatabase(database.getName(), Activity.MODE_PRIVATE, null);
			abstractDB.close();
			metadataModel.setValue(new Gson().toJson(database));
			/*Insert and save AudiTrail*/
			if (insertOrUpdateMetadata(metadataModel)){
				TransactionManager.saveAuditTrail(mContext, dbName, dbName, TransactionManager.ADDED);
				return true;
			}else{
				return false;
			}
		}else
			return false;
	}
	/**
	 * Create the table
	 * @param dbName name of the data base
	 * @param tableName name of the table
	 * @return true if the update was successful
	 */
	public boolean add(String dbName,String tableName){
		MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(dbName);
		if (metadataModel != null){
			DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
			if (database != null){
				if (database.getTable(tableName) == null){
					/*New Table*/
					TableModel table = new TableModel();
					table.setName(tableName);
					/*Add Table to Database*/
					database.addTable(table);
					/*Add Field to Table*/
					table.addField(new FieldModel(TransactionManager.DEFAULT_FIELD_KEY, TransactionManager.DEFAULT_FIELD_KEY_TYPE, TransactionManager.DEFAULT_FIELD_KEY_INDEXED,false));
					table.addField(new FieldModel("origination", "TEXT NOT NULL ", false,false));
					table.addField(new FieldModel("termination", "TEXT ", false,false));
					/*Create table*/
					try {
						TransactionManager.createTable(mContext,dbName, table);
						/*Get new metadata*/
						metadataModel.setValue(new Gson().toJson(database));
						/*Insert and save AudiTrail*/
						if (insertOrUpdateMetadata(metadataModel)){
							TransactionManager.saveAuditTrail(mContext, dbName, tableName, tableName, TransactionManager.ADDED);
							return true;
						}
					} catch (Exception e) {
						//TODO add SQLMANAGERException
						return false;
					}
				}
			}
		}
		return false;
	}
	/**
	 * Create the field
	 * @param dbName name of the data base
	 * @param tableName name of the table
	 * @param fields represents a field in that table.  the first String in the dtfields array is the name of the field and the subsequent Strings define the field type, size, and use as an index.
	 * @return true if the update was successful
	 */
	public boolean add(String dbName,String tableName,String[] fields){
		MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(dbName);
		if (metadataModel != null){
			DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
			if (database != null){
				/*Get Table*/
				TableModel table = database.getTable(tableName);
				/*New Field*/
				FieldModel field =new FieldModel();
				/*Set values in new Field*/
				createFieldModel(field,fields);
				if (table!= null && table.getField(field.getName())== null){
					/*Add Field to Table*/
					table.addField(field);
					/*Create new Field*/
					try {
						TransactionManager.createField(mContext,dbName, tableName, field);
						metadataModel.setValue(new Gson().toJson(database));
						/*Insert and save AudiTrail*/
						if (insertOrUpdateMetadata(metadataModel)){
							TransactionManager.saveAuditTrail(mContext, dbName, tableName, field.getName()+ ":"+field.getType(), field.getName()+ ":"+field.getType(), TransactionManager.ADDED);
							return true;
						}
					} catch (Exception e) {
						//TODO add SQLMANAGERException
						return false;
					}
				}
			}
		}
		return false;
	} 

	/**
	 * Rename the database
	 * @param dbName name of the data base
	 * @param newDbName new name of the data base
	 * @return true if the update was successful
	 */
	public boolean edit(String dbName,String newDbName){
		if (mContext.getCurrentMetadataDataBase().exists(dbName)){
			MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(dbName);
			metadataModel.setDataBaseName(newDbName);
			DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
			if (database != null){
				if (mContext.getCurrentMetadataDataBase().delete(dbName)){
					database.setName(newDbName);
					/* Rename Database*/
					File file = new File(DB_PATH,dbName);
					if (file.exists()){
						File new_file = new File(file.getParentFile(),newDbName);
						file.renameTo(new_file);	
					}
					
					/*Set new Values in Metadata */
					metadataModel.setValue(new Gson().toJson(database));
					/*Insert and save AudiTrail*/
					if (insertOrUpdateMetadata(metadataModel)){
						TransactionManager.saveAuditTrail(mContext, dbName, newDbName, TransactionManager.UPDATED);
						return true;
					}

				}
			}
		}
		return false;
	}
	/**
	 * Edit the table
	 * @param dbName name of the data base
	 * @param tableName name of the table
	 * @param newTableName new name of the table
	 * @return true if the update was successful
	 */
	public boolean edit(String dbName,String tableName,String newTableName){
		if (mContext.getCurrentMetadataDataBase().exists(dbName)){
			MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(dbName);
			DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
			if (database != null){
				/*Get Table*/
				TableModel table = database.removeTable(tableName);
				table.setName(newTableName);
				/*Rename Table*/
				try {
					TransactionManager.renameTable(mContext,dbName, tableName, newTableName);
					/*Set new Values in Metadata */
					database.addTable(table);
					metadataModel.setValue(new Gson().toJson(database));
					/*Insert and save AudiTrail*/
					if (insertOrUpdateMetadata(metadataModel)){
						TransactionManager.saveAuditTrail(mContext, dbName, tableName, newTableName, TransactionManager.UPDATED);
						return true;
					}
				} catch (Exception e) {
					//TODO add SQLMANAGERException
					return false;
				}

			}
		}
		return false;
	}

	/**
	 * Edit the field
	 * @param dbName name of the data base
	 * @param tableName name of the table
	 * @param fieldName name of the field to edit
	 * @param newField represents a field in that table. The first String in the dtfields array is the name of the field and the subsequent Strings define the field type, size, and use as an index.
	 * @return true if the update was successful
	 */
	public boolean edit(String dbName,String tableName,String fieldName,String[] newField){
		if (mContext.getCurrentMetadataDataBase().exists(dbName)){
			MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(dbName);
			if (metadataModel != null){
				DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
				if (database != null){
					/*Get Table*/
					TableModel table = database.getTable(tableName);
					/*Get Field*/
					FieldModel old_field = table.removeField(FieldHelper.StringToFieldModel(fieldName).getName());
					/*Change Field*/
					FieldModel field = createFieldModel(old_field,newField);

					try {
						table.addField(field);
						TransactionManager.changeColumn(mContext,dbName, table, fieldName, field);
						/*Set new Values in Metadata */
						metadataModel.setValue(new Gson().toJson(database));
						/*Insert and save AudiTrail*/
						if (insertOrUpdateMetadata(metadataModel)){
							TransactionManager.saveAuditTrail(mContext, dbName, tableName, old_field.getName() + ":"+old_field.getType(),field.getName() + ":"+field.getType(), TransactionManager.UPDATED);
							return true;
						}

					} catch (Exception e) {
						//TODO add SQLMANAGERException
						return false;
					}
				}
			}
		}
		return false;
	}
	/**
	 * delete the database
	 * @param dbNname name of the data base
	 * @return true if the update was successful
	 */
	public boolean delete(String dbNname){
		if (mContext.getCurrentMetadataDataBase().exists(dbNname)){
			MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(dbNname);
			DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
			if (database != null){
				database.setEnabled(false);
				/*Set new Values in Metadata */
				metadataModel.setValue(new Gson().toJson(database));
				/*Insert and save AudiTrail*/
				if (insertOrUpdateMetadata(metadataModel)){
					TransactionManager.saveAuditTrail(mContext, dbNname, dbNname, TransactionManager.INACTIVATED);
					return true;
				}
			}

		}
		return false;
	}
	/**
	 * Delete the table
	 * @param dbName name of the data base
	 * @param tableName name of the table
	 * @return true if the update was successful
	 */
	public boolean delete(String dbName,String tableName){
		if (mContext.getCurrentMetadataDataBase().exists(dbName)){
			MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(dbName);
			DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
			if (database != null){
				/*Get Table*/
				TableModel table = database.getTable(tableName);
				table.setEnabled(false);
				/*Set new Values in Metadata */
				metadataModel.setValue(new Gson().toJson(database));
				/*Insert and save AudiTrail*/
				if (insertOrUpdateMetadata(metadataModel)){
					TransactionManager.saveAuditTrail(mContext, dbName, tableName, tableName, TransactionManager.INACTIVATED);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Delete the table
	 * @param dbName name of the data base
	 * @param tableName name of the table
	 * @param fieldName name of the field
	 * @return true if the update was successful
	 */
	public boolean delete(String dbName,String tableName,String fieldName){
		if (mContext.getCurrentMetadataDataBase().exists(dbName)){
			MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(dbName);
			if (metadataModel != null){
				DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
				if (database != null){
					/*Get Table*/
					TableModel table = database.getTable(tableName);
					/*Get Field*/
					FieldModel field = table.getField(FieldHelper.StringToFieldModel(fieldName).getName());

					if (field != null){
						/*Change Field*/
						field.setEnabled(false);
						/*Set new Values in Metadata */
						metadataModel.setValue(new Gson().toJson(database));
						/*Insert and save AudiTrail*/
						if (insertOrUpdateMetadata(metadataModel)){
							TransactionManager.saveAuditTrail(mContext, dbName, tableName, field.getName()+ ":"+field.getType(), field.getName()+ ":"+field.getType(), TransactionManager.INACTIVATED);
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	/**
	 * Destroy the database
	 * @param dbName name of the data base
	 * @return true if the update was successful
	 */
	public boolean destroy(String dbName){
		if (mContext.getCurrentMetadataDataBase().exists(dbName)){
			try {
				TransactionManager.dropDatabase(mContext,dbName);
				/*Insert and save AudiTrail*/
				if ( mContext.getCurrentMetadataDataBase().delete(dbName)){
					TransactionManager.saveAuditTrail(mContext, dbName, dbName, TransactionManager.DESTROYED);
					return true;
				}
			} catch (Exception e) {
				//TODO add SQLMANAGERException
				return false;
			}
		}
		return false;
	}
	/**
	 * Delete the table
	 * @param dbName name of the data base
	 * @param tableName name of the table
	 * @return true if the update was successful
	 */
	public boolean destroy(String dbName,String tableName){
		if (mContext.getCurrentMetadataDataBase().exists(dbName)){
			MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(dbName);
			DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
			if (database != null){
				/*Get Table*/
				database.removeTable(tableName);
				try {
					TransactionManager.dropTable(mContext,dbName, tableName);
					/*Set new Values in Metadata */
					metadataModel.setValue(new Gson().toJson(database));
					//Insert
					if (insertOrUpdateMetadata(metadataModel)){
						TransactionManager.saveAuditTrail(mContext, dbName, tableName, tableName, TransactionManager.DESTROYED);
						return true;
					}
				} catch (Exception e) {
					//TODO add SQLMANAGERException
					return false;
				}
			}
		}
		return false;
	}
	/**
	 * Select databases
	 * @return Arrays of database
	 */
	public String[] select(){
		List<String> list = new ArrayList<String>();
		ArrayList<MetadataModel> list_database = mContext.getCurrentMetadataDataBase().selectAll();
		for (MetadataModel database: list_database){
			DBModel databaseModel = new Gson().fromJson(database.getValue().toString(), DBModel.class);
			if (databaseModel != null){
				if (databaseModel.getEnabled()){
					list.add(database.getDataBaseName());
				}
			}
		}
		String[] resp = list.toArray(new String[list.size()]);
		Arrays.sort(resp);
		return resp;
	}
	/**
	 * Select tables
	 * @param dbName name of the data base
	 * @return Arrays of tables in database
	 */
	public String[] select(String dbName){
		List<String> list = new ArrayList<String>();
		if (mContext.getCurrentMetadataDataBase().exists(dbName)){
			MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(dbName);
			DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
			if (database != null){
				Map<String, TableModel> tables = database.getTables();
				for (Iterator <Entry <String, TableModel>> iterator = tables.entrySet().iterator(); iterator.hasNext();)
				{
					Entry <String, TableModel> entry_field = iterator.next();
					String table_name = entry_field.getKey();
					TableModel table = entry_field.getValue();
					if (table.getEnabled()){
						list.add(table_name);
					}
				}
			}
		}
		String[] resp = list.toArray(new String[list.size()]);
		Arrays.sort(resp);
		return resp;
	}
	/**
	 * Select fields
	 * @param dbName name of the data base
	 * @param tableName name of the table
	 * @return Arrays of fields in the table
	 */
	public String[] select(String dbName,String tableName){
		List<String> list = new ArrayList<String>();
		if (mContext.getCurrentMetadataDataBase().exists(dbName)){
			MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(dbName);
			if (metadataModel != null){
				DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
				if (database != null){
					/*Get Table*/
					TableModel table = database.getTable(tableName);
					if (table != null){
						Map<String, FieldModel> fields = table.getFields();
						for (Iterator <Entry <String, FieldModel>> iterator = fields.entrySet().iterator(); iterator.hasNext();)
						{
							Entry <String, FieldModel> entry_field = iterator.next();
							FieldModel field = entry_field.getValue();
							if (field.getEnabled()){
								list.add(field.toString());
							}
						}
					}
				}
			}
		}
		String[] resp = list.toArray(new String[list.size()]);
		Arrays.sort(resp);
		return resp;
	}
	/**
	 * Insert values in table
	 * @param dbName name of the data base
	 * @param tableName name of the table
	 * @param values HashMap with the FieldValues 
	 * @return true if the insert was successful
	 */
	public boolean insert(String database_name,String table_name,HashMap<String, FieldValues> values){
		FieldValues fieldValues = new FieldValues();
		fieldValues.setName("origination");
		fieldValues.setType(SQLiteTypes.TEXT);
		fieldValues.setValue(DateTimeHelper.toString(System.currentTimeMillis()));
		values.put("origination", fieldValues);
		return TransactionManager.insert(mContext.getDbManager(), database_name, table_name, values);
	}
	/**
	 * Update values in table
	 * @param dbName name of the data base
	 * @param tableName name of the table
	 * @param values Object UpdateValues 
	 * @return true if the update was successful
	 */
	public boolean update( String database_name,String table_name, UpdateValues values){
		if (mContext.getCurrentMetadataDataBase().exists(database_name)){
			MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(database_name);
			if (metadataModel != null){
				DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
				if (database != null){
					/*Get Table*/
					TableModel table = database.getTable(table_name);
					if (table != null){
						return TransactionManager.updateAndInsert(mContext, database_name, table_name, table, values);
					}
				}
			}
		}
		return true;
	}

	/**
	 * Inactivation rows in table
	 * @param dbName name of the data base
	 * @param tableName name of the table
	 * @param dtfields Array of the Strings
	 * @return number of the rows or -1 
	 */
	public int Inactivation(String database_name,String table_name,String[] dtfields){
		if (mContext.getCurrentMetadataDataBase().exists(database_name)){
			MetadataModel metadataModel = mContext.getCurrentMetadataDataBase().select(database_name);
			if (metadataModel != null){
				DBModel database = new Gson().fromJson(metadataModel.getValue().toString(), DBModel.class);
				if (database != null){
					/*Get Table*/
					TableModel table = database.getTable(table_name);
					if (table != null){
						HashMap<String, FieldValues> values = new HashMap<String, FieldValues>();
						FieldValues fieldValues = new FieldValues();
						fieldValues.setName("termination");
						fieldValues.setType("TEXT");
						fieldValues.setValue(DateTimeHelper.toString(System.currentTimeMillis()));
						values.put("termination", fieldValues);

						ArrayList<FieldValues> whereValues = new ArrayList<FieldValues>();
						for (int i = 0; i < dtfields.length;i++){

							FieldValues field = FieldHelper.StringToFieldValues(dtfields[i]);
							FieldModel field_Structure = table.getField(field.getName());

							field.setType(field_Structure.getType());

							whereValues.add(field);
						}
						return TransactionManager.update(mContext.getDbManager(), database_name, table_name, values, whereValues);

					}
				}
			}
		}
		return -1;
	}
	/**
	 * Select from a specific table.
	 * @param manager
	 * @param dbName
	 * @param table
	 * @param whereValues
	 * @return ArrayList of json of the selected values
	 */
	public  ArrayList<JSONArray> selectData(String dbName, String table)
	{
		FieldValues values = new FieldValues();
		values.setName("termination is null or termination ");
		values.setType("TEXT");
		values.setValue("");
		try {
			return TransactionManager.select(mContext.getDbManager(), dbName, table, values);
		} catch (Exception e) {
			// TODO Add SQLiteManagerException
			return new ArrayList<JSONArray>();
		}
	}
	/**
	 * Insert or update metadata in database
	 * @param metadata 
	 * @return true if the insert or update was successful
	 */
	private boolean insertOrUpdateMetadata(MetadataModel metadata){
		if (mContext.getCurrentMetadataDataBase().exists(metadata.getDataBaseName())){
			return mContext.getCurrentMetadataDataBase().update(metadata.getDataBaseName(), metadata);
		}else{
			return mContext.getCurrentMetadataDataBase().insert(metadata);
		}
	}
	/**
	 * Create FieldModel 
	 * @param field Field to add values
	 * @param fields String[] with the values (Name,Type,Indexed)
	 * @return FieldModel with the values
	 */
	private FieldModel createFieldModel(FieldModel field,String[] fields){
		for (int i=0;i<fields.length;i++){
			switch (i) {
			case 0:
				field.setName(fields[i]);
				break;
			case 1:
				field.setType(fields[i]);
				break;
			case 2:
				field.setIndexed(Boolean.parseBoolean(fields[i]));
				break;
			default:
				break;
			}
		}
		return field;
	}
}
