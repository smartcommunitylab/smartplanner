package it.sayservice.platform.smartplanner.areainfo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TimeSlotList {

	List<TimeAndRangeSlot> timeAndRangeSlotList;

	public TimeSlotList() {
		timeAndRangeSlotList = new ArrayList<TimeAndRangeSlot>();
	}

	public List<TimeAndRangeSlot> getTimeAndRangeSlotList() {
		return timeAndRangeSlotList;
	}

	public void setTimeAndRangeSlotList(List<TimeAndRangeSlot> timeAndRangeSlotList) {
		this.timeAndRangeSlotList = timeAndRangeSlotList;
	}

	public TimeAndRangeSlot getTimeSlot(Calendar calendar) {
		for (int i = 0; i < timeAndRangeSlotList.size(); i++) {
			TimeAndRangeSlot timeSlot = timeAndRangeSlotList.get(i);

			Calendar fromCal = getCalendarFromHour(calendar, timeSlot.getFromCalendar());
			Calendar toCal = getCalendarFromHour(calendar, timeSlot.getToCalendar());

			if (calendar.after(fromCal) && calendar.before(toCal)) {

				return timeSlot;
			}
		}

		return null;
	}

	private Calendar getCalendarFromHour(Calendar calendar, Calendar simpleHourCalendar) {
		Calendar hourCal = Calendar.getInstance();
		hourCal.setTime(calendar.getTime());
		hourCal.set(Calendar.HOUR, simpleHourCalendar.get(Calendar.HOUR));
		hourCal.set(Calendar.MINUTE, simpleHourCalendar.get(Calendar.MINUTE));

		return hourCal;
	}

}
