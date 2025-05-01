package ii.am.ticket;

import java.util.Calendar;
import java.util.Date;


public class ReminderUtils {
	//work hour 08:00-12:00 , 13:00-17:00

	public static Date getReminderDate(Date openDate, int needDay, int needHr) {
		Date reminderDate = null;
		reminderDate = adjustDate( openDate );
		reminderDate = adjustTime( reminderDate );
		
		if (needDay > 0)
		for (int i=0; i<needDay; i++) {
			reminderDate = incrementDate(reminderDate);
			reminderDate = adjustDate( reminderDate );
			reminderDate = adjustTime( reminderDate );
		}
		
		if (needHr > 0)
		for (int i=0; i<needHr; i++) {
			reminderDate = incrementTime(reminderDate);
			reminderDate = adjustDate( reminderDate );
			reminderDate = adjustTime( reminderDate );
		}		
		
		return reminderDate;
	}
	
	private static Date adjustDate(Date dt) {
		Calendar c = Calendar.getInstance(); c.setTime(dt);
		while (c.get(Calendar.DAY_OF_WEEK)==Calendar.SATURDAY || c.get(Calendar.DAY_OF_WEEK)==Calendar.SUNDAY)
			c.set(Calendar.DATE, c.get(Calendar.DATE)+1);
		return c.getTime();
	}
	
	private static Date adjustTime(Date dt) {
		Calendar c = Calendar.getInstance(); c.setTime(dt);
		if (c.get(Calendar.HOUR_OF_DAY) < 8) {
			c.set(Calendar.HOUR_OF_DAY,   8);
			c.set(Calendar.MINUTE, 0);
		}
		if (c.get(Calendar.HOUR_OF_DAY)==12) {
			c.set(Calendar.HOUR_OF_DAY,  13);
			c.set(Calendar.MINUTE, 0);			
		}
		if (c.get(Calendar.HOUR_OF_DAY) >17) {
			c.set(Calendar.DATE, c.get(Calendar.DATE)+1);
			c.set(Calendar.HOUR_OF_DAY,   8);
			c.set(Calendar.MINUTE, 0);			
		}		
		return c.getTime();
	}
	
	private static Date incrementDate(Date dt) {
		Calendar c = Calendar.getInstance(); c.setTime(dt);
		c.set(Calendar.DATE, c.get(Calendar.DATE)+1);
		return c.getTime(); 
	}
	
	private static Date incrementTime(Date dt) {
		Calendar c = Calendar.getInstance(); c.setTime(dt);
		c.set(Calendar.HOUR_OF_DAY, c.get(Calendar.HOUR_OF_DAY)+1);
		return c.getTime(); 
	}	
	
	
	public static long agePercent(Date openDate, Date reminderDate) {
		Date current = new Date();
		Double t1 = Double.valueOf(current.getTime() - openDate.getTime());
		Double t2 = Double.valueOf(reminderDate.getTime() - openDate.getTime());
		Double ap = (t1/t2) * 100;
		return ap.longValue();
	}
}
