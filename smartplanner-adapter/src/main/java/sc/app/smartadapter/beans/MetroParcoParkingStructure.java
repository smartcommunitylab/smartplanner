package sc.app.smartadapter.beans;

import sc.app.smartadapter.restful.client.RemoteBean;
import sc.app.smartadapter.restful.client.SmartPlannerBean;
import sc.app.smartdapter.configuration.RemoteBeanConfiguration;

public class MetroParcoParkingStructure implements RemoteBean {

	String id;
	String id_app;
	String name;
	String streetReference;
	String managementMode;
	String manager;
	ValidityPeriod[] validityPeriod;
	Geometry geometry;
	int slotNumber;
	SlotsConfiguration[] slotsConfiguration;
	String[] paymentMode;
	String phoneNumber;
	String lastChange;
	int occupancyRate;
	int profit;
	int tickets;
	boolean parkAndRide;
	boolean abuttingPark;
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

	public String getStreetReference() {
		return streetReference;
	}

	public void setStreetReference(String streetReference) {
		this.streetReference = streetReference;
	}

	public String getManagementMode() {
		return managementMode;
	}

	public void setManagementMode(String managementMode) {
		this.managementMode = managementMode;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public ValidityPeriod[] getValidityPeriod() {
		return validityPeriod;
	}

	public void setValidityPeriod(ValidityPeriod[] validityPeriod) {
		this.validityPeriod = validityPeriod;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
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

	public String[] getPaymentMode() {
		return paymentMode;
	}

	public void setPaymentMode(String[] paymentMode) {
		this.paymentMode = paymentMode;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
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

	public int getProfit() {
		return profit;
	}

	public void setProfit(int profit) {
		this.profit = profit;
	}

	public int getTickets() {
		return tickets;
	}

	public void setTickets(int tickets) {
		this.tickets = tickets;
	}

	public boolean isParkAndRide() {
		return parkAndRide;
	}

	public void setParkAndRide(boolean parkAndRide) {
		this.parkAndRide = parkAndRide;
	}

	public boolean isAbuttingPark() {
		return abuttingPark;
	}

	public void setAbuttingPark(boolean abuttingPark) {
		this.abuttingPark = abuttingPark;
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
	public SmartPlannerBean adaptBean(RemoteBeanConfiguration remoteBeanConfiguration) {
		// TODO Auto-generated method stub
		return null;
	}

}
