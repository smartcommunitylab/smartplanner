package sc.app.smartadapter.adapter;

import it.sayservice.platform.smartplanner.data.message.otpbeans.Parking;

import java.util.ArrayList;
import java.util.List;

import sc.app.smartadapter.beans.MetroParcoParkingStructure;
import sc.app.smartadapter.restful.client.MetroParcoClient;
import sc.app.smartadapter.restful.client.RemoteBean;

public class AdaptMetroParcoParkingStructure {

	private final String metroParcoParkingStructureUrl = "https://tn.smartcommunitylab.it/metroparco/rest/nosec/tn/parkingstructure";
	private final MetroParcoClient mpBikeStation = new MetroParcoClient();

	public List<Parking> getParkingStructureFromMetroParcoParkingStructure(){

		List<Parking> parkingList = new ArrayList<Parking>();
		List<RemoteBean> beanList = new ArrayList<RemoteBean>();
		beanList = mpBikeStation.getBeanFromRestFullServer(metroParcoParkingStructureUrl, MetroParcoParkingStructure.class);

		for (int i = 0; i < beanList.size(); i++) {

		}


		return parkingList;
	}
}
