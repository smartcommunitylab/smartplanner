package sc.app.smartadapter.beans;

public class ValidityPeriod {

	String from;
	String to;
	String[] weekDays;
	String timeSlot;
	int rateValue;
	String note;
	String dayOrNight;
	boolean holiday;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getTo() {
		return to;
	}

	public void setTo(String to) {
		this.to = to;
	}

	public String[] getWeekDays() {
		return weekDays;
	}

	public void setWeekDays(String[] weekDays) {
		this.weekDays = weekDays;
	}

	public String getTimeSlot() {
		return timeSlot;
	}

	public void setTimeSlot(String timeSlot) {
		this.timeSlot = timeSlot;
	}

	public int getRateValue() {
		return rateValue;
	}

	public void setRateValue(int rateValue) {
		this.rateValue = rateValue;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public String getDayOrNight() {
		return dayOrNight;
	}

	public void setDayOrNight(String dayOrNight) {
		this.dayOrNight = dayOrNight;
	}

	public boolean isHoliday() {
		return holiday;
	}

	public void setHoliday(boolean holiday) {
		this.holiday = holiday;
	}

}
