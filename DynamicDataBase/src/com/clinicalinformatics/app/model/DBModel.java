package com.clinicalinformatics.app.model;
/**
 * 
 * @author Rheti Inc
 * 
 * Application class
 * 		
 * Created 2013 Sep 25
 *  
 */
import java.util.HashMap;
import java.util.Map;

public class DBModel  {

	private String mName;
	private Integer mVersion;
	private Boolean mEnabled;
	
	private Map<String, TableModel> mTables;
	
	public DBModel(){
		setTables(new HashMap<String, TableModel>());
		setEnabled(true);
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public Map<String, TableModel> getTables() {
		return mTables;
	}

	public void setTables(Map<String, TableModel> mTables) {
		this.mTables = mTables;
	}
	
	public void addTable(TableModel table){
		mTables.put(table.getName(), table);
	}
	
	public TableModel removeTable(String name){
		return mTables.remove(name);
	}
	
	public TableModel getTable(String name){
		return mTables.get(name);
	}

	public int getVersion() {
		return mVersion;
	}

	public void setVersion(int mVersion) {
		this.mVersion = mVersion;
	}

	public Boolean getEnabled() {
		return mEnabled;
	}

	public void setEnabled(Boolean mEnabled) {
		this.mEnabled = mEnabled;
	}

	
}
