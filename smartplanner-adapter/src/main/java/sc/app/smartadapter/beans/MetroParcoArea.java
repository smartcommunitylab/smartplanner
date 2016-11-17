package sc.app.smartadapter.beans;

import it.sayservice.platform.smartplanner.areainfo.TimeSlotException;
import it.sayservice.platform.smartplanner.model.CostData;
import it.sayservice.platform.smartplanner.model.DayNight;
import it.sayservice.platform.smartplanner.model.FaresPeriod;
import it.sayservice.platform.smartplanner.model.TimeSlot;
import it.sayservice.platform.smartplanner.model.WeekDay;

import java.util.ArrayList;
import java.util.List;

import sc.app.smartadapter.configuration.RemoteBeanConfiguration;
import sc.app.smartadapter.restful.client.RemoteBean;
import sc.app.smartadapter.restful.client.SmartPlannerBean;

public class MetroParcoArea implements RemoteBean {

	String id;
	String id_app;
	String name;
	ValidityPeriod[] validityPeriod;
	String smsCode;
	String color;
	String note;
	GeometryPoints[] geometry;
	String[] zones;
	String[] agencyId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getId_app() {
		return id_app;
	}

	public void setId_app(String id_app) {
		this.id_app = id_app;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ValidityPeriod[] getValidityPeriod() {
		return validityPeriod;
	}

	public void setValidityPeriod(ValidityPeriod[] validityPeriod) {
		this.validityPeriod = validityPeriod;
	}

	public String getSmsCode() {
		return smsCode;
	}

	public void setSmsCode(String smsCode) {
		this.smsCode = smsCode;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getNote() {
		return note;
	}

	public void setNote(String note) {
		this.note = note;
	}

	public GeometryPoints[] getGeometry() {
		return geometry;
	}

	public void setGeometry(GeometryPoints[] geometry) {
		this.geometry = geometry;
	}

	public String[] getZones() {
		return zones;
	}

	public void setZones(String[] zones) {
		this.zones = zones;
	}

	public String[] getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String[] agencyId) {
		this.agencyId = agencyId;
	}

	@Override
	public SmartPlannerBean adaptBean(String agencyId, RemoteBeanConfiguration remoteBeanConfiguration) {
		EnhancedFaresData enhancedFaresZones = new EnhancedFaresData();
		String costZoneId = this.id;
		enhancedFaresZones.setCostZoneId(costZoneId);
		FaresPeriod[] faresPeriod = getFaresPeriod();
		enhancedFaresZones.setFaresPeriod(faresPeriod);

		return enhancedFaresZones;
	}

	private FaresPeriod[] getFaresPeriod() {

		List<FaresPeriod> farePeriodList = new ArrayList<FaresPeriod>();

		for (int i = 0; i < validityPeriod.length; i++) {
			ValidityPeriod period = validityPeriod[i];

			FaresPeriod faresPeriod = new FaresPeriod();

			CostData costData = new CostData();
			String costDefinition = period.getNote();
			costData.setCostDefinition(costDefinition);
			String fixedCost = String.valueOf(period.getRateValue());
			costData.setFixedCost(fixedCost);
			faresPeriod.setCostData(costData);

			DayNight dayOrNight = getDayOrNight(period);
			faresPeriod.setDayOrNight(dayOrNight);

			setFromToDate(faresPeriod, period);

			boolean holiday = period.isHoliday();
			faresPeriod.setHoliday(holiday);

			TimeSlot[] timeSlots = getTimeSlots(period);
			faresPeriod.setTimeSlots(timeSlots);

			WeekDay[] weekDays = getWeekDay(period);
			faresPeriod.setWeekDays(weekDays);

			farePeriodList.add(faresPeriod);
		}

		FaresPeriod[] faresPeriodArray = farePeriodList.toArray(new FaresPeriod[farePeriodList.size()]);

		return faresPeriodArray;
	}

	private WeekDay[] getWeekDay(ValidityPeriod period) {
		List<WeekDay> weekDayList = new ArrayList<WeekDay>();
		String[] weekDayStringArray = period.getWeekDays();

		for (int i = 0; i < weekDayStringArray.length; i++) {
			String weekDayString = weekDayStringArray[i];
			WeekDay weekDay = WeekDay.valueOf(weekDayString);
			weekDayList.add(weekDay);
		}

		WeekDay[] weekDayArray = weekDayList.toArray(new WeekDay[weekDayList.size()]);

		return weekDayArray;
	}

	private TimeSlot[] getTimeSlots(ValidityPeriod period) {
		List<TimeSlot> timeSlotList = new ArrayList<TimeSlot>();

		String[] timeSlotStringArray;
		if (period.getTimeSlot() != null) {
			if (period.getTimeSlot().contains("/")) {
				timeSlotStringArray = period.getTimeSlot().split("/");
			} else {
				timeSlotStringArray = new String[1];
				timeSlotStringArray[0] = period.getTimeSlot();
			}

			for (String timeSlotItem : timeSlotStringArray) {
				TimeSlot timeSlot = new TimeSlot();

				String[] fromToHourArray = null;
				if (timeSlotItem.contains("-")) {
					fromToHourArray = timeSlotItem.split("-");
				}

				String from = "00:00";
				String to = "23:59";
				if (fromToHourArray != null) {
					from = fromToHourArray[0].trim();
					to = fromToHourArray[1].trim();
				}

				try {
					timeSlot.setFrom(from);
					timeSlot.setTo(to);
				} catch (TimeSlotException e) {
					e.printStackTrace();
				}

				timeSlotList.add(timeSlot);
			}
		}

		TimeSlot[] timeSlotArray = timeSlotList.toArray(new TimeSlot[timeSlotList.size()]);

		return timeSlotArray;
	}

	private void setFromToDate(FaresPeriod faresPeriod, ValidityPeriod period) {
		String fromDate = "01/01";
		String toDate = "31/12";

		if (period.getFrom() != null) {
			fromDate = period.getFrom();
		}

		if (period.getTo() != null) {
			toDate = period.getTo();
		}

		try {
			faresPeriod.setFromDate(fromDate);
			faresPeriod.setToDate(toDate);
		} catch (TimeSlotException e) {
			e.printStackTrace();
		}

	}

	private DayNight getDayOrNight(ValidityPeriod period) {
		String periodDayOrNight = period.getDayOrNight();

		DayNight dayOrNight = null;

		if (periodDayOrNight != null && periodDayOrNight != "") {
			if (periodDayOrNight.toLowerCase().contains("day")) {
				dayOrNight = DayNight.DAY;
			}

			if (periodDayOrNight.toLowerCase().contains("night")) {
				dayOrNight = DayNight.NIGHT;
			}
		}

		dayOrNight = DayNight.DAY;

		return dayOrNight;
	}

}