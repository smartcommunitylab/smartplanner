package sc.app.smartadapter.beans;

import it.sayservice.platform.smartplanner.areainfo.SearchTimeWeekDayList;
import it.sayservice.platform.smartplanner.areainfo.TimeAndRangeSlot;
import it.sayservice.platform.smartplanner.areainfo.TimeSlotException;
import it.sayservice.platform.smartplanner.areainfo.TimeSlotList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sc.app.smartadapter.configuration.RemoteBeanConfiguration;
import sc.app.smartadapter.restful.client.RemoteBean;
import sc.app.smartadapter.restful.client.SmartPlannerBean;

public class MetroParcoStreetOccupancy implements RemoteBean {

	String id;
	String id_app;
	String streetReference;
	int slotNumber;
	String rateAreaId;
	String[] zones;
	OccupancyData occupancyData;
	String statValueData;

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

	public String getStreetReference() {
		return streetReference;
	}

	public void setStreetReference(String streetReference) {
		this.streetReference = streetReference;
	}

	public int getSlotNumber() {
		return slotNumber;
	}

	public void setSlotNumber(int slotNumber) {
		this.slotNumber = slotNumber;
	}

	public String getRateAreaId() {
		return rateAreaId;
	}

	public void setRateAreaId(String rateAreaId) {
		this.rateAreaId = rateAreaId;
	}

	public String[] getZones() {
		return zones;
	}

	public void setZones(String[] zones) {
		this.zones = zones;
	}

	public OccupancyData getOccupancyData() {
		return occupancyData;
	}

	public void setOccupancyData(OccupancyData occupancyData) {
		this.occupancyData = occupancyData;
	}

	public String getStatValueData() {
		return statValueData;
	}

	public void setStatValueData(String statValueData) {
		this.statValueData = statValueData;
	}

	@Override
	public SmartPlannerBean adaptBean(String agencyId, RemoteBeanConfiguration remoteBeanConfiguration) {
		EnhancedSearchTime enhancedSearchTime = new EnhancedSearchTime();

		String searchAreaId = this.id;
		enhancedSearchTime.setSearchAreaId(searchAreaId);
		SearchTimeWeekDayList weekDayList = getSearchTimeWeekDayList();
		enhancedSearchTime.setWeekDayList(weekDayList);

		return enhancedSearchTime;
	}

	private SearchTimeWeekDayList getSearchTimeWeekDayList() {
		SearchTimeWeekDayList weekDayList = new SearchTimeWeekDayList();

		TimeSlotList d1 = getTimeSlotList();

		weekDayList.setD1(d1);
		weekDayList.setD2(d1);
		weekDayList.setD3(d1);
		weekDayList.setD4(d1);
		weekDayList.setD5(d1);
		weekDayList.setD6(d1);
		weekDayList.setD7(d1);

		return weekDayList;
	}

	private TimeSlotList getTimeSlotList() {
		TimeSlotList timeSlotList = new TimeSlotList();
		List<TimeAndRangeSlot> timeAndRangeSlotList = new ArrayList<TimeAndRangeSlot>();

		if (this.getOccupancyData() != null && this.getOccupancyData().getOccupancyRateMap() != null) {
			Map<String, OccupancyRate> occupancyRateMap = this.getOccupancyData().getOccupancyRateMap();
			for (String hourNumber : occupancyRateMap.keySet()) {
				TimeAndRangeSlot timeAndRangeSlot = getRatedTimeAndRangeSlot(hourNumber, occupancyRateMap.get(hourNumber));

				timeAndRangeSlotList.add(timeAndRangeSlot);
			}
		}

		timeSlotList.setTimeAndRangeSlotList(timeAndRangeSlotList);

		return timeSlotList;
	}

	private TimeAndRangeSlot getRatedTimeAndRangeSlot(String hourNumber, OccupancyRate occupancyRate) {

		TimeAndRangeSlot timeAndRangeSlot = new TimeAndRangeSlot();
		int hourFrom = Integer.parseInt(hourNumber);
		int hourTo = hourFrom + 1;
		String to = String.valueOf(hourTo);
		try {
			timeAndRangeSlot.setFrom(formatHour(hourNumber));
			timeAndRangeSlot.setTo(formatHour(to));
		} catch (TimeSlotException e) {
			e.printStackTrace();
		}

		double aggregateValue = occupancyRate.getAggregateValue();
		String aggregateValueString = String.valueOf(aggregateValue);

		double value = Double.parseDouble(aggregateValueString.replace(',', '.').substring(0, aggregateValueString.length() - 1));
		if (value < 60) {
			timeAndRangeSlot.setMin(0);
			timeAndRangeSlot.setMax(0);

			return timeAndRangeSlot;
		}

		if (value >= 60 && value < 70) {
			timeAndRangeSlot.setMin(0);
			timeAndRangeSlot.setMax(1);

			return timeAndRangeSlot;
		}

		if (value >= 70 && value < 80) {
			timeAndRangeSlot.setMin(1);
			timeAndRangeSlot.setMax(3);

			return timeAndRangeSlot;
		}

		if (value >= 80 && value < 90) {
			timeAndRangeSlot.setMin(2);
			timeAndRangeSlot.setMax(5);

			return timeAndRangeSlot;
		}

		if (value >= 90 && value < 100) {
			timeAndRangeSlot.setMin(3);
			timeAndRangeSlot.setMax(10);

			return timeAndRangeSlot;
		}
		if (value >= 100) {
			timeAndRangeSlot.setMin(5);
			timeAndRangeSlot.setMax(15);

			return timeAndRangeSlot;
		}

		return null;
	}

	public String formatHour(String hour) {
		String formattedHour = "";

		if (hour.length() == 1) {
			hour = "0" + hour;
		}

		formattedHour = hour + ":00";

		return formattedHour;
	}
}
