package sc.app.smartadapter.restful.client;

import sc.app.smartadapter.configuration.RemoteBeanConfiguration;

public interface RemoteBean {
	public SmartPlannerBean adaptBean(String agencyId, RemoteBeanConfiguration remoteBeanConfiguration);
}
