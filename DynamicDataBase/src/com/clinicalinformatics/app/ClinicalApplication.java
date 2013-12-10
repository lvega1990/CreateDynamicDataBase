package com.clinicalinformatics.app;
/**
 * 
 * @author Rheti Inc
 * 
 * Application class
 * 		
 * Created 2013 Sep 25
 *  
 */

import com.clinicalinformatics.app.helper.AuditModelDBHelper;
import com.clinicalinformatics.app.helper.DBHelperManager;
import com.clinicalinformatics.app.helper.MetadataDBHelper;

import android.app.Application;

public class ClinicalApplication extends Application {
	private DBHelperManager mDbManager; //Manager to database access
	@Override
	public void onCreate() 
	{
		super.onCreate(); 
	}
	/**
	 * Get Database Helper Manager
	 * @return Return to actual DBHelperManager to manager dynamics databases
	 */
	public DBHelperManager getDbManager() 
	{
		if(mDbManager==null)
			mDbManager = DBHelperManager.getInstance(this);
		return mDbManager;
	}
	/**
	 * Get Metadata Helper Manager
	 * @return Return to actual MetadataDBHelper to manager DB Metadata 
	 */
	public MetadataDBHelper getCurrentMetadataDataBase()
	{
		return getDbManager().OpenOrCreateMetadataInstance();
	}
	/**
	 * Get Audit Helper Manager
	 * @return Return to actual AuditModelDBHelper to manager DB Audit 
	 */
	public AuditModelDBHelper getCurrentAuditDataBase()
	{
		return getDbManager().OpenOrCreateAuditInstance();
	}

}
