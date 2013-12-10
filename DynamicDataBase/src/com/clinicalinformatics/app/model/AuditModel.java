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
public class AuditModel {

	private int _id; 
	private int compID; 
	private String dbase;
	private String dtable;
	private String dtfield;
	private String action;
	private String origination;
	private String termination;
	
	//Empty Constructor 
	public AuditModel(){ ; }

	public int get_id() {
		return _id;
	}

	public int getCompID() {
		return compID;
	}

	public void setCompID(Integer compID) {
		this.compID = compID;
	}

	public String getDbase() {
		return dbase;
	}

	public void setDbase(String dbase) {
		this.dbase = dbase;
	}

	public String getDtable() {
		return dtable;
	}

	public void setDtable(String dtable) {
		this.dtable = dtable;
	}

	public String getDtfield() {
		return dtfield;
	}

	public void setDtfield(String dtfield) {
		this.dtfield = dtfield;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getOrigination() {
		return origination;
	}

	public void setOrigination(String origination) {
		this.origination = origination;
	}

	public String getTermination() {
		return termination;
	}

	public void setTermination(String termination) {
		this.termination = termination;
	}
		
}
