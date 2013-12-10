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
public class FieldModel  {

	private String mName;
	private String mType;
	private Boolean mIndexed;
	private Boolean mEnabled;
	private Integer mSize;
	
	public FieldModel(){
		setEnabled(true);
	}
	
	public FieldModel(String name, String type,Boolean indexed){
		setName(name);
		setType(type);
		setIndexed(indexed);
		setEnabled(true);
		
	}
	public FieldModel(String name, String type,Boolean indexed,Boolean enabled){
		setName(name);
		setType(type);
		setIndexed(indexed);
		setEnabled(enabled);
		
	}
	public String getName() {
		return mName;
	}
	public void setName(String mName) {
		this.mName = mName;
	}
	public Boolean getIndexed() {
		return mIndexed;
	}
	public void setIndexed(Boolean indexed) {
		this.mIndexed = indexed;
	}
	public String getType() {
		return mType;
	}
	public void setType(String mType) {
		this.mType = mType;
	}
	public Boolean getEnabled() {
		return mEnabled;
	}
	public void setEnabled(Boolean mEnabled) {
		this.mEnabled = mEnabled;
	}

	public Integer getSize() {
		return mSize;
	}

	public void setSize(Integer mSize) {
		this.mSize = mSize;
	}
	@Override
	public String toString() {
		return getName() + ":" + getType() + ":" + (getIndexed()?"Y":"N");
	}
}
