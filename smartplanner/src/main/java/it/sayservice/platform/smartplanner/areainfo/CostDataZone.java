package it.sayservice.platform.smartplanner.areainfo;

import it.sayservice.platform.smartplanner.model.CostData;

public class CostDataZone {

	CostData costData;
	private String costZoneId;

	public CostData getCostData() {
		return costData;
	}

	public void setCostData(CostData costData) {
		this.costData = costData;
	}

	public String getCostZoneId() {
		return costZoneId;
	}

	public void setCostZoneId(String costZoneId) {
		this.costZoneId = costZoneId;
	}

}
