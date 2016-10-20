package sc.app.smartadapter.restful.client;

import sc.app.smartdapter.configuration.RemoteBeanConfiguration;

public interface RemoteBean {
	public SmartPlannerBean adaptBean (RemoteBeanConfiguration remoteBeanConfiguration);
}
