package sc.app.smartadapter.adapter;

import java.util.ArrayList;
import java.util.List;

import sc.app.smartadapter.beans.Geometry;
import sc.app.smartadapter.beans.MetroParcoBikePoint;
import sc.app.smartadapter.restful.client.MetroParcoClient;
import it.sayservice.platform.smartplanner.data.message.otpbeans.BikeStation;

public class AdaptMetroParcoBikepoint {

	private final String metroParcoBikePointUrl = "https://dev.smartcommunitylab.it/metroparco/rest/nosec/tn/bikepoint";
	private final MetroParcoClient<MetroParcoBikePoint> mpClient = new MetroParcoClient<MetroParcoBikePoint>();

	public List<BikeStation> getBikeStationFromMetroParcoBikePoint(){

		List<BikeStation> bikeStationList = new ArrayList<BikeStation>();

		List<MetroParcoBikePoint> mpBikePointList = mpClient.getBeanFromRestFullServer(metroParcoBikePointUrl, MetroParcoBikePoint.class);

		for (int i = 0; i < mpBikePointList.size(); i++) {
			BikeStation bikeStation = new BikeStation();
			MetroParcoBikePoint mpBikePoint = mpBikePointList.get(i);

			int bikeNumber = mpBikePoint.getBikeNumber();
			bikeStation.setAvailableSharingVehicles(bikeNumber);

			int slotNumber = mpBikePoint.getSlotNumber();
			bikeStation.setPosts(slotNumber);

			String name = mpBikePoint.getName();
			if(name!=null){
				bikeStation.setFullName(name);
			}

			double[] position = getPositionFromGeometry(mpBikePoint.getGeometry());
			bikeStation.setPosition(position);

			String id = mpBikePoint.getId();
			bikeStation.setId(id);

			bikeStationList.add(bikeStation);
		}

		return bikeStationList;
	}

	private double[] getPositionFromGeometry(Geometry geometry) {
		double lat;
		double lng;

		lat = Double.parseDouble(geometry.getLat());
		lng = Double.parseDouble(geometry.getLng());

		double[] position = {lat, lng};

		return position;
	}


}
