package it.sayservice.platform.smartplanner.model;

import it.sayservice.platform.smartplanner.areainfo.TimeSlotException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeSlot {

	String from;
	String to;

	public TimeSlot() {
		super();
	}

	public TimeSlot(String from, String to) {
		super();
		this.from = from;
		this.to = to;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) throws TimeSlotException {
		SimpleDateFormat hourFormat = new SimpleDateFormat("kk:mm");

		String formattedHourMinutes = "";

		Date date = null;
		try {
			date = hourFormat.parse(from);
		} catch (ParseException e) {
			throw new TimeSlotException("wrong time slot format for from: " + from + ", correct format is: \"hh:mm\"");
		}

		formattedHourMinutes = hourFormat.format(date);

		this.from = formattedHourMinutes;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) throws TimeSlotException {
		SimpleDateFormat hourFormat = new SimpleDateFormat("kk:mm");
		String formattedHourMinutes = "";

		Date date = null;
		try {
			date = hourFormat.parse(to);
		} catch (ParseException e) {
			throw new TimeSlotException("wrong time slot format for to: " + to + ", correct format is: \"hh:mm\"");
		}

		formattedHourMinutes = hourFormat.format(date);

		this.to = formattedHourMinutes;
	}

}
