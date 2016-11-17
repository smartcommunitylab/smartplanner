package it.sayservice.platform.smartplanner.areainfo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class TimeAndRangeSlot {

	String from;
	String to;

	int min;
	int max;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) throws TimeSlotException{
		SimpleDateFormat hourFormat = new SimpleDateFormat("kk:mm");

		String formattedHourMinutes = "";

		Date date = null;
		try {
			date = hourFormat.parse(from);
		} catch (ParseException e) {
			throw new TimeSlotException("wrong time slot format for from: "+from+", correct format is: \"hh:mm\"");
		}

		formattedHourMinutes = hourFormat.format(date);

		this.from = formattedHourMinutes;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to)  throws TimeSlotException{
		SimpleDateFormat hourFormat = new SimpleDateFormat("kk:mm");
		String formattedHourMinutes = "";

		Date date = null;
		try {
			date = hourFormat.parse(to);
		} catch (ParseException e) {
			throw new TimeSlotException("wrong time slot format for to: "+to+", correct format is: \"hh:mm\"");
		}

		formattedHourMinutes = hourFormat.format(date);

		this.to = formattedHourMinutes;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}

	protected Calendar getFromCalendar(){
		Calendar fromCalendar = Calendar.getInstance();
		SimpleDateFormat hourFormat = new SimpleDateFormat("kk:mm");

		Date date = null;
		try {
			date = hourFormat.parse(from);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		fromCalendar.setTime(date);

		return fromCalendar;
	}

	protected Calendar getToCalendar(){
		Calendar toCalendar = Calendar.getInstance();
		SimpleDateFormat hourFormat = new SimpleDateFormat("kk:mm");

		Date date = null;
		try {
			date = hourFormat.parse(to);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		toCalendar.setTime(date);

		return toCalendar;
	}

	@Override
	public String toString() {
		return "from hour: "+from+", to hour: "+to+"; min: "+min + ", max: " + max;
	}

	public static void main(String[] args) {
		TimeAndRangeSlot trs = new TimeAndRangeSlot();
		try {
			trs.setFrom("00");
		} catch (TimeSlotException e) {
			e.printStackTrace();
		}
	}
}
