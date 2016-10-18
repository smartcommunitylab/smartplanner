package sc.app.smartadapter.adapter;

import java.util.ArrayList;
import java.util.List;

import sc.app.smartadapter.beans.MetroParcoStreet;
import sc.app.smartadapter.restful.client.MetroParcoClient;
import sc.app.smartadapter.restful.client.RemoteBean;

public class AdaptMetroParcoStreet {


	private final String metroParcoStreetUrl = "https://tn.smartcommunitylab.it/metroparco/rest/nosec/tn/street";
	private final MetroParcoClient mpStreet = new MetroParcoClient();

	public List<RemoteBean> getStreetFromMetroParcoStreet(){

		List<RemoteBean> beanList = new ArrayList<RemoteBean>();
		beanList = mpStreet.getBeanFromRestFullServer(metroParcoStreetUrl, MetroParcoStreet.class);

		return beanList;
	}
}
