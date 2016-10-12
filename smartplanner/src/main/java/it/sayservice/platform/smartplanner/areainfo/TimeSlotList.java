package it.sayservice.platform.smartplanner.areainfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimeSlotList {

	List<TimeAndRangeSlot> timeSlotList;

	public TimeSlotList() {
		timeSlotList = new ArrayList<TimeAndRangeSlot>();
	}

	public List<TimeAndRangeSlot> getTimeSlotList() {
		return timeSlotList;
	}

	public void setTimeSlotList(List<TimeAndRangeSlot> timeSlotList) {
		this.timeSlotList = timeSlotList;
	}

	public void addTimeSlot(TimeAndRangeSlot timeSlot){
		timeSlotList.add(timeSlot);
	}

	public TimeAndRangeSlot getTimeSlot(Calendar calendar){
		for (int i = 0; i < timeSlotList.size(); i++) {
			TimeAndRangeSlot timeSlot = timeSlotList.get(i);

			Calendar fromCal = getCalendarFromHour(calendar, timeSlot.getFromCalendar());
			Calendar toCal = getCalendarFromHour(calendar, timeSlot.getToCalendar());

			if(calendar.after(fromCal)&&calendar.before(toCal)){

				return timeSlot;
			}
		}

		return null;
	}

	private Calendar getCalendarFromHour(Calendar calendar, Calendar simpleHourCalendar){
		Calendar hourCal = Calendar.getInstance();
		hourCal.setTime(calendar.getTime());
		hourCal.set(Calendar.HOUR, simpleHourCalendar.get(Calendar.HOUR));
		hourCal.set(Calendar.MINUTE, simpleHourCalendar.get(Calendar.MINUTE));

		return hourCal;
	}

}
