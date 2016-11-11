package sc.app.smartadapter.beans;

import sc.app.smartadapter.configuration.RemoteBeanConfiguration;
import sc.app.smartadapter.restful.client.RemoteBean;
import sc.app.smartadapter.restful.client.SmartPlannerBean;

public class MetroParcoArea implements RemoteBean {

	String id;
	String id_app;
	String name;
	ValidityPeriod[] validityPeriod;
	String smsCode;
	String color;
	String note;
	GeometryPoints[] geometry;
	String[] zones;
	String[] agencyId;

	@Override
	public SmartPlannerBean adaptBean(String agencyId, RemoteBeanConfiguration remoteBeanConfiguration) {
		EnhancedFaresData enhancedFaresZones = new EnhancedFaresData();
		
		
		return null;
	}

}
