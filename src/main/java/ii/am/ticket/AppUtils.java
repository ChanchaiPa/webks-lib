package ii.am.ticket;

import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

public class AppUtils {

	public static Date parseDateTime(String dtstr, String format) { // dd-MM-yyyy HH:mm:ss
		java.util.Date dt = null;
		SimpleDateFormat dfm = new SimpleDateFormat( format, Locale.ENGLISH );
		try  { dt = dfm.parse( dtstr ); } catch(Exception e) {}
		return dt;
	}
	public static String toDateFormat(Date dt, String format) {
		String dateFormat = "";
		SimpleDateFormat dfm = new SimpleDateFormat( format, Locale.ENGLISH );
		try  { dateFormat = dfm.format( dt ); } catch(Exception e) {}
		return dateFormat;
	}	
	public static int toInt(String data, int defaultVal) {
		int value = 0;
		if (data.contains("."))
			try { value = (int)Math.round(Double.parseDouble(data)); }
			catch(Exception e) { value = defaultVal; }
		else
			try { value = Integer.parseInt(data); }
			catch(Exception e) { value = defaultVal; }
		return value;
	}	
	
	
	public static Integer jsonGetInt(JSONObject jsonObject, String fieldName) {
		try { return jsonObject.getInt( fieldName ); }
		catch(Exception e) { return null; }
	}
	public static String jsonGetString(JSONObject jsonObject, String fieldName) {
		try { 
			String temp = jsonObject.getString( fieldName ); 
			if (temp.equals("None"))
				temp = null;
			return temp; 
		}
		catch(Exception e) { return null; }
	}
	
	
    public static Date isoDateTime(String dateString) {
        try {
            ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateString, DateTimeFormatter.ISO_DATE_TIME);
            return Date.from(zonedDateTime.toInstant());
        } catch (DateTimeParseException e) {
            System.err.println("Error parsing date: " + e.getMessage());
            return null;
        }
    }
}
