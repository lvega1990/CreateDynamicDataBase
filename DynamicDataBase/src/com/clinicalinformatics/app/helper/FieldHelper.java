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
import com.clinicalinformatics.app.model.FieldModel;
import com.clinicalinformatics.app.model.FieldValues;

public class FieldHelper {
	/**
	 * Return the FieldModel from the String
	 * @param field String to converter
	 * @return The FieldModel with String values
	 */
	public static FieldModel StringToFieldModel(String field){
		FieldModel model = new FieldModel();
		String[] fields = field.split(":");
		for (int i=0;i<fields.length;i++){
			switch (i) {
			case 0:
				model.setName(fields[i]);
				break;
			case 1:
				model.setType(fields[i]);
				break;
			case 2:
				model.setIndexed(fields[i].equals("Y"));
				break;
			default:
				break;
			}
		}
		return model;
	}
	/**
	 * Return the FieldValues from the String
	 * @param field String to converter
	 * @return The FieldValues with String values
	 */
	public static FieldValues StringToFieldValues(String field){
		FieldValues model = new FieldValues();
		String[] fields = field.split(":");
		for (int i=0;i<fields.length;i++){
			switch (i) {
			case 0:
				if (fields[i].startsWith("\""))
					model.setName(fields[i].substring(1,fields[i].length()-1));
				else
					model.setName(fields[i]);
				break;
			case 1:
				if (fields[i].startsWith("\""))
					model.setValue(fields[i].substring(1,fields[i].length()-1));
				else
					model.setValue(fields[i]);
				break;
			default:
				break;
			}
		}
		return model;
	}
}
