package br.org.reembolsofacil.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class UtlCalendar {

	public static Calendar getCalendar(int day, int month, int year){
		Calendar c = Calendar.getInstance();
		c.set(year, month+1, day);
		return c;
	}
	
	public static Calendar getCalendar(int day, int month, int year, int hour, int minute){
		Calendar c = Calendar.getInstance();
		c.set(year, month+1, day, hour, minute);
		return c;
	}
	
	public static String getStringFromCalendar(Calendar c){
		SimpleDateFormat sdf = new SimpleDateFormat();
		return sdf.format(c.getTime());
	}
	
	public static String getStringFromCalendar(Calendar c, String pattern){
		SimpleDateFormat sdf = new SimpleDateFormat(pattern);
		return sdf.format(c.getTime());
	}
	
	public static Calendar getCalendarFromString(String date){
		Date dt = null;
		Calendar c = null;
		try {
			dt = new SimpleDateFormat("dd/MM/yyyy").parse(date);
			c = Calendar.getInstance();
			c.setTime(dt);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}  
		return c;
	}
	
	public static String getMedium(Calendar c, Locale locale){
		DateFormat df = DateFormat.getDateInstance(DateFormat.MEDIUM, locale);  
		return df.format(c.getTime());
	}
	
	public static String getShort(Calendar c, Locale locale){
		DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT, locale);  
		return df.format(c.getTime());
	}
	
	public static String getLong(Calendar c, Locale locale){
		DateFormat df = DateFormat.getDateInstance(DateFormat.LONG, locale);  
		return df.format(c.getTime());
	}
}
