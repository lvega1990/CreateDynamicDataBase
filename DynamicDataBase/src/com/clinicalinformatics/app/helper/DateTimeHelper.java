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
import android.annotation.SuppressLint;
import java.text.SimpleDateFormat;
import java.util.Date;

@SuppressLint("SimpleDateFormat")
public class DateTimeHelper {
	//    Year:
	//       YYYY (eg 1997)
	//    Year and month:
	//       YYYY-MM (eg 1997-07)
	//    Complete date:
	//       YYYY-MM-DD (eg 1997-07-16)
	//    Complete date plus hours and minutes:
	//       YYYY-MM-DDThh:mmTZD (eg 1997-07-16T19:20+01:00)
	//    Complete date plus hours, minutes and seconds:
	//       YYYY-MM-DDThh:mm:ssTZD (eg 1997-07-16T19:20:30+01:00)
	//    Complete date plus hours, minutes, seconds and a decimal fraction of a
	// second
	//       YYYY-MM-DDThh:mm:ss.sTZD (eg 1997-07-16T19:20:30.45+01:00)

	// where:

	//      YYYY = four-digit year
	//      MM   = two-digit month (01=January, etc.)
	//      DD   = two-digit day of month (01 through 31)
	//      hh   = two digits of hour (00 through 23) (am/pm NOT allowed)
	//      mm   = two digits of minute (00 through 59)
	//      ss   = two digits of second (00 through 59)
	//      s    = one or more digits representing a decimal fraction of a second
	//      TZD  = time zone designator (Z or +hh:mm or -hh:mm)
	/**
	 * @param input The String representation in format yyyyMMddHHmmss
	 * @return The datetime 
	 */
	public static Date parse( String input ) throws java.text.ParseException {
		return new SimpleDateFormat ("yyyyMMddHHmmss").parse(input);

	}
	/**
	 * @param input The String representation in format yyyyMMddHHmmss
	 * @return The datetime in milliseconds
	 */
	public static Long parseMili( String input ) throws java.text.ParseException {
		if (input == null || input.equals("")){
			return new SimpleDateFormat ("yyyyMMddHHmmss").parse("19000101000000").getTime();
		}else{
			if (input.length()>21){
				input = input.substring(0, 21);
			}
			return new SimpleDateFormat ("yyyyMMddHHmmss").parse(input).getTime();
		}
	}
	/**
	 * @param datetime the actual date and time in milliseconds
	 * @return The String representation in format yyyyMMddHHmmss
	 */
	public static String toString(Long datetime ) {
		String result = new SimpleDateFormat ("yyyyMMddHHmmss").format(new Date(datetime));
		return result;
	}

}
