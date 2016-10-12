package sc.app.smartadapter.adapter;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;

import java.util.ArrayList;
import java.util.List;

import sc.app.smartadapter.beans.MetroParcoParkingStructure;
import sc.app.smartadapter.restful.client.MetroParcoClient;

public class AdaptMetroParcoParkingStructure {

	private final String metroParcoParkingStructureUrl = "https://tn.smartcommunitylab.it/metroparco/rest/nosec/tn/parkingstructure";
	private final MetroParcoClient<MetroParcoParkingStructure> mpBikeStation = new MetroParcoClient<MetroParcoParkingStructure>();

	public List<Parking> getParkingStructureFromMetroParcoParkingStructure(){

		List<Parking> parkingList = new ArrayList<Parking>();
		List<MetroParcoParkingStructure> beanList = new ArrayList<MetroParcoParkingStructure>();
		beanList = mpBikeStation.getBeanFromRestFullServer(metroParcoParkingStructureUrl, MetroParcoParkingStructure.class);

		for (int i = 0; i < beanList.size(); i++) {

		}


		return parkingList;
	}
}
