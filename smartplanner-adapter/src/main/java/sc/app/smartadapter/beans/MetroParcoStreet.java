package sc.app.smartadapter.beans;

import sc.app.smartadapter.restful.client.RemoteBean;
import sc.app.smartadapter.restful.client.SmartPlannerBean;

public class MetroParcoStreet implements RemoteBean{

	String id;
	String id_app;
	String streetReference;
	int slotNumber;
	SlotsConfiguration[] slotsConfiguration;
	boolean subscritionAllowedPark;
	String rateAreaId; // ": "5637351e389c9d26bf96762a", --> bisogna fare una
											// query con la rateaAreaId e reperire il costo dei
											// parcheggi dell'area da associare a
	GeometryPoints geometry;
	String color;
	String[] zones;
	String[] parkingMeters;
	String lastChange;
	int occupancyRate;
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

	public SlotsConfiguration[] getSlotsConfiguration() {
		return slotsConfiguration;
	}

	public void setSlotsConfiguration(SlotsConfiguration[] slotsConfiguration) {
		this.slotsConfiguration = slotsConfiguration;
	}

	public boolean isSubscritionAllowedPark() {
		return subscritionAllowedPark;
	}

	public void setSubscritionAllowedPark(boolean subscritionAllowedPark) {
		this.subscritionAllowedPark = subscritionAllowedPark;
	}

	public String getRateAreaId() {
		return rateAreaId;
	}

	public void setRateAreaId(String rateAreaId) {
		this.rateAreaId = rateAreaId;
	}

	public GeometryPoints getGeometry() {
		return geometry;
	}

	public void setGeometry(GeometryPoints geometry) {
		this.geometry = geometry;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String[] getZones() {
		return zones;
	}

	public void setZones(String[] zones) {
		this.zones = zones;
	}

	public String getLastChange() {
		return lastChange;
	}

	public void setLastChange(String lastChange) {
		this.lastChange = lastChange;
	}

	public int getOccupancyRate() {
		return occupancyRate;
	}

	public void setOccupancyRate(int occupancyRate) {
		this.occupancyRate = occupancyRate;
	}

	public String[] getParkingMeters() {
		return parkingMeters;
	}

	public void setParkingMeters(String[] parkingMeters) {
		this.parkingMeters = parkingMeters;
	}

	public String[] getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String[] agencyId) {
		this.agencyId = agencyId;
	}

	@Override
	public SmartPlannerBean adaptBean() {
		// TODO Auto-generated method stub
		return null;
	}

}
