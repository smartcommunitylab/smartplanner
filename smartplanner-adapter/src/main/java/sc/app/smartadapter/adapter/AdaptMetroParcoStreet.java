package sc.app.smartadapter.adapter;

import java.util.ArrayList;
import java.util.List;

import sc.app.smartadapter.beans.MetroParcoStreet;
import sc.app.smartadapter.restful.client.MetroParcoClient;

public class AdaptMetroParcoStreet {


	private final String metroParcoStreetUrl = "https://tn.smartcommunitylab.it/metroparco/rest/nosec/tn/street";
	private final MetroParcoClient<MetroParcoStreet> mpStreet = new MetroParcoClient<MetroParcoStreet>();

	public List<MetroParcoStreet> getStreetFromMetroParcoStreet(){

		List<MetroParcoStreet> beanList = new ArrayList<MetroParcoStreet>();
		beanList = mpStreet.getBeanFromRestFullServer(metroParcoStreetUrl, MetroParcoStreet.class);

		return beanList;
	}
}
