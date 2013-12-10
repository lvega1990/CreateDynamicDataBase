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
import java.util.ArrayList;

public class UpdateValues {

	private Integer mId;
	private ArrayList<String>  mValues;
	
	public UpdateValues(){
		mValues = new ArrayList<String>();
	}
	public Integer getId() {
		return mId;
	}
	public void setId(Integer mId) {
		this.mId = mId;
	}
	public String[] getValues() {
		return mValues.toArray(new String[mValues.size()]);
	}
	public void setValues(String field, String value) {
		this.mValues.add(field + ":" + value);
	}
}
