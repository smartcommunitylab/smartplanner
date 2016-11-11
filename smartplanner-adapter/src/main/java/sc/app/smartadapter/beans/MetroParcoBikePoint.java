package sc.app.smartadapter.beans;

import it.sayservice.platform.smartplanner.data.message.StopId;
import sc.app.smartadapter.configuration.RemoteBeanConfiguration;
import sc.app.smartadapter.restful.client.RemoteBean;
import sc.app.smartadapter.restful.client.SmartPlannerBean;

/*
 *   {
 "id": "5637351e389c9d26bf96777e",
 "id_app": "tn",
 "name": "stazione FS",
 "bikeNumber": 20,
 "slotNumber": 24,
 "geometry": {
 "lat": 46.07187784256046,
 "lng": 11.119719743728638
 },
 "lastChange": null,
 "zones": [
 "56a79ed5e4b0d01ec4df0e7e"
 ],
 "agencyId": [
 "tn_mob_091516"
 ]
 }
 * */
public class MetroParcoBikePoint implements RemoteBean {

	String urlSuffix;
	String id;
	String id_app;
	String name;
	int bikeNumber;
	int slotNumber;
	Geometry geometry;
	String lastChange;
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

	public int getBikeNumber() {
		return bikeNumber;
	}

	public void setBikeNumber(int bikeNumber) {
		this.bikeNumber = bikeNumber;
	}

	public int getSlotNumber() {
		return slotNumber;
	}

	public void setSlotNumber(int slotNumber) {
		this.slotNumber = slotNumber;
	}

	public Geometry getGeometry() {
		return geometry;
	}

	public void setGeometry(Geometry geometry) {
		this.geometry = geometry;
	}

	public String getLastChange() {
		return lastChange;
	}

	public void setLastChange(String lastChange) {
		this.lastChange = lastChange;
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

	public String getUrlSuffix() {
		return urlSuffix;
	}

	public void setUrlSuffix(String urlSuffix) {
		this.urlSuffix = urlSuffix;
	}

	@Override
	public SmartPlannerBean adaptBean(String agencyId, RemoteBeanConfiguration remoteBeanConfiguration) {
		EnhancedBikeStation bikeStation = new EnhancedBikeStation();

		int bikeNumber = this.getBikeNumber();
		bikeStation.setAvailableSharingVehicles(bikeNumber);

		int slotNumber = this.getSlotNumber();
		bikeStation.setPosts(slotNumber);

		String name = this.getName();
		if (name != null) {
			bikeStation.setFullName(name);
		}

		double[] position = getPositionFromGeometry(this.getGeometry());
		bikeStation.setPosition(position);

		String id = this.getId();
		bikeStation.setId(id);

		StopId stationId = new StopId();
		stationId.setId(id);

		stationId.setAgencyId(agencyId);

		bikeStation.setStationId(stationId);
		String type = "BIKE-RENTAL";
		bikeStation.setType(type);

		return bikeStation;
	}

	private double[] getPositionFromGeometry(Geometry geometry) {
		double lat;
		double lng;

		lat = Double.parseDouble(geometry.getLat());
		lng = Double.parseDouble(geometry.getLng());

		double[] position = { lat, lng };

		return position;
	}

}
