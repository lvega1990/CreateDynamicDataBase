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
import org.json.JSONException;
import org.json.JSONObject;

public class MetadataModel {

	private String mName; //set DataBase name
	private String mValue; // is the value of the table
	
	//Empty Constructor 
	public MetadataModel(){ ; }
	//Constructor
	public MetadataModel(String name, JSONObject value){
		this.mName = name;
		this.mValue = value.toString();
	}
	
	public String getDataBaseName() {
		return mName;
	}
	
	public void setDataBaseName(String dataBaseName) {
		this.mName = dataBaseName;
	}
	
	public JSONObject getValue() {
		try {
			return new JSONObject(this.mValue);
		} catch (JSONException e) {
			return new JSONObject();
		}
	}
	
	public void setValue(JSONObject value) {
		this.mValue = mValue.toString();
	}
	public void setValue(String value){
		this.mValue = value;
	}
	
}
