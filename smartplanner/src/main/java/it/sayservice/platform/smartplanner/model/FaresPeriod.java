package it.sayservice.platform.smartplanner.model;

import it.sayservice.platform.smartplanner.areainfo.TimeSlotException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FaresPeriod {

	String fromDate;
	String toDate;
	WeekDay[] weekDays;
	TimeSlot[] timeSlots;
	CostData costData;
	DayNight dayOrNight;
	boolean holiday;

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) throws TimeSlotException {
		SimpleDateFormat hourFormat = new SimpleDateFormat("dd/MM");

		String formattedDayMonth = "";

		Date date = null;
		try {
			date = hourFormat.parse(fromDate);
		} catch (ParseException e) {
			throw new TimeSlotException("wrong time slot format for from: " + fromDate + ", correct format is: \"dd/mm\"");
		}

		formattedDayMonth = hourFormat.format(date);

		this.fromDate = formattedDayMonth;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) throws TimeSlotException {
		SimpleDateFormat hourFormat = new SimpleDateFormat("dd/MM");

		String formattedDayMonth = "";

		Date date = null;
		try {
			date = hourFormat.parse(toDate);
		} catch (ParseException e) {
			throw new TimeSlotException("wrong time slot format for from: " + toDate + ", correct format is: \"dd/mm\"");
		}

		formattedDayMonth = hourFormat.format(date);

		this.toDate = formattedDayMonth;
	}

	public CostData getCostData() {
		return costData;
	}

	public void setCostData(CostData costData) {
		this.costData = costData;
	}

	public WeekDay[] getWeekDays() {
		return weekDays;
	}

	public void setWeekDays(WeekDay[] weekDays) {
		this.weekDays = weekDays;
	}

	public TimeSlot[] getTimeSlots() {
		return timeSlots;
	}

	public void setTimeSlots(TimeSlot[] timeSlots) {
		this.timeSlots = timeSlots;
	}

	public DayNight getDayOrNight() {
		return dayOrNight;
	}

	public void setDayOrNight(DayNight dayOrNight) {
		this.dayOrNight = dayOrNight;
	}

	public boolean isHoliday() {
		return holiday;
	}

	public void setHoliday(boolean holiday) {
		this.holiday = holiday;
	}

}
