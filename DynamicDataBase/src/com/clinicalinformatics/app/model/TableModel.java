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


public class TableModel {
	
	private Boolean mEnabled;
	private String mName;
	private Map<String, FieldModel> mFields;
	
	public TableModel(){
		setFields(new HashMap<String, FieldModel>()); 	
		setEnabled(true);
	}

	public String getName() {
		return mName;
	}

	public void setName(String mName) {
		this.mName = mName;
	}

	public Map<String, FieldModel> getFields() {
		return mFields;
	}

	public void setFields(Map<String, FieldModel> fields) {
		this.mFields = fields;
	}
	
	public void addField(FieldModel field){
		mFields.put(field.getName(), field);
	}
	
	public FieldModel getField(String name){
		return mFields.get(name);
	}
	public FieldModel removeField(String name){
		return mFields.remove(name);
	}
	public Boolean getEnabled() {
		return mEnabled;
	}
	public void setEnabled(Boolean mEnabled) {
		this.mEnabled = mEnabled;
	}
}
