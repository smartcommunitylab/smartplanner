package it.sayservice.platform.smartplanner.areainfo;

public class FaresZone {

	String costZoneId;
	FaresZonePeriod[] faresZonePeriods;

	public String getCostZoneId() {
		return costZoneId;
	}

	public void setCostZoneId(String costZoneId) {
		this.costZoneId = costZoneId;
	}

	public FaresZonePeriod[] getFaresZonePeriods() {
		return faresZonePeriods;
	}

	public void setFaresZonePeriods(FaresZonePeriod[] faresZonePeriods) {
		this.faresZonePeriods = faresZonePeriods;
	}
}
