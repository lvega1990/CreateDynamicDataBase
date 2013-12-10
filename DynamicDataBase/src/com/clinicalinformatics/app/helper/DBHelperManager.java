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
import android.app.Activity;
import android.database.sqlite.SQLiteDatabase;

import com.clinicalinformatics.app.ClinicalApplication;

public class DBHelperManager 
{
	private static DBHelperManager sDBManagerInstance;
	private ClinicalApplication mContext;
	private MetadataDBHelper mMetadataDBHelper;
	private AuditModelDBHelper mAuditModelDBHelper;
	
	/**
	 * Return to actual instance of the class
	 * @param ClinicalApplication The Application Context
	 * @return The Actual DBHelperManager
	 */
	public static DBHelperManager getInstance(ClinicalApplication context)
	{
		if(sDBManagerInstance == null)
			sDBManagerInstance = new DBHelperManager(context);
		return sDBManagerInstance;
	}
	/**
	 * @return The DBHelperManager
	 */
	private DBHelperManager(ClinicalApplication context) 
	{
		this.mContext = context;
	}
	/**
	 * @return The MetadataDBHelper
	 */
	public MetadataDBHelper OpenOrCreateMetadataInstance()
	{
		if(mMetadataDBHelper == null)
		{
			mMetadataDBHelper = new MetadataDBHelper(mContext);
		}
		return mMetadataDBHelper;
	}
	/**
	 * @return The AuditModelDBHelper
	 */
	public AuditModelDBHelper OpenOrCreateAuditInstance()
	{
		if(mAuditModelDBHelper == null)
		{
			mAuditModelDBHelper = new AuditModelDBHelper(mContext);
		}
		return mAuditModelDBHelper;
	}
	/**
	 * @param dbName The name of the existing DataBase
	 * @return The data base Instance
	 */
	public SQLiteDatabase getDataBase(String dbName)
	{
		return mContext.openOrCreateDatabase(dbName, Activity.MODE_PRIVATE, null);
	}

}
