package com.mealscraper;
import java.util.GregorianCalendar;



public class Meal {
	int year;
	int month;
	int day;
	String mealType;
	int miliHour;
	String mealDescription;
	long start;
	long end;
	
	public Meal(String message, String time) {
		message = message.toLowerCase();
		int hour = 0;
		if (message.startsWith("lunch")) {
			mealType = "Meal:Lunch";
			hour = 11;
		} else if (message.startsWith("dinner")) {
			mealType = "Meal:Dinner";
			hour = 17;
		} else {
			mealType = "Meal:Other";
			hour = Integer.parseInt(time.substring(time.indexOf("T") + 1, time.indexOf(":")));
		}
		miliHour = hour * 60 * 60 * 1000;
		int index = time.indexOf("-");
		year = Integer.parseInt(time.substring(0, index));
		time = time.substring(index + 1);
		index = time.indexOf("-");
		month = Integer.parseInt(time.substring(0, index)) - 1;
		time = time.substring(index + 1);
		index = time.indexOf("T");
		day = Integer.parseInt(time.substring(0, index));
		mealDescription = message;
		
		GregorianCalendar calDate = new GregorianCalendar(year,month, day);
		start = calDate.getTimeInMillis() + miliHour;
		end = calDate.getTimeInMillis() + miliHour + 3600000;
		
	}
	
}
