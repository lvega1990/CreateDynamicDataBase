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
public class FieldValues {

	private String mName;
	private String mType;
	private Object mValue;

	public String getType() {
		return mType;
	}
	public void setType(String type) {
		this.mType = type;
	}
	public Object getValue() {
		return mValue;
	}
	public void setValue(Object value) {
		this.mValue = value;
	}
	public String getName() {
		return mName;
	}
	public void setName(String name) {
		this.mName = name;
	}
}
