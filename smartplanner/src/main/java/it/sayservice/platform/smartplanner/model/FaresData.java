package it.sayservice.platform.smartplanner.model;

public class FaresData {

	String costZoneId;
	private FaresPeriod[] faresPeriod;

	public FaresPeriod[] getFaresPeriod() {
		return faresPeriod;
	}

	public void setFaresPeriod(FaresPeriod[] faresPeriod) {
		this.faresPeriod = faresPeriod;
	}

	public String getCostZoneId() {
		return costZoneId;
	}

	public void setCostZoneId(String costZoneId) {
		this.costZoneId = costZoneId;
	}
}
