package sc.app.smartadapter.ingestion;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sc.app.smartadapter.configuration.ClassAdapterConfiguration;
import sc.app.smartadapter.configuration.RemoteBeanConfiguration;
import sc.app.smartadapter.restful.client.MetroParcoClient;
import sc.app.smartadapter.restful.client.RemoteBean;
import sc.app.smartadapter.restful.client.SmartPlannerBean;
import sc.app.smartadapter.restful.client.SmartPlannerClient;

public class MetroParcoBikeIngestion extends MetroParcoIngestionAbs{

	String REMOTE_URL_SUFFIX = "/tn/bikepoint";
	String SMARTPLANNER_URL_SUFFIX = "/data/bikesharing";
	
	
	@Override
	public void ingest(ClassAdapterConfiguration classAdapterConfiguration, Map<String,RemoteBeanConfiguration> remoteBeanConfigurationAgencyMap) throws Exception {

		for (String agencyId : remoteBeanConfigurationAgencyMap.keySet()) {
			RemoteBeanConfiguration remoteBeanConfiguration = remoteBeanConfigurationAgencyMap.get(agencyId);
			
			String remoteServerUrl = getRemoteServerUrl(remoteBeanConfiguration);

			Class<?> beanClass = null;
			try {
				beanClass = Class.forName(classAdapterConfiguration.getSource_bean());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			List<RemoteBean> remoteBeanList = MetroParcoClient.getBeanFromRestFullServer(remoteServerUrl, beanClass);

			List<SmartPlannerBean> beanList = new ArrayList<SmartPlannerBean>();

			for (RemoteBean remoteBean : remoteBeanList) {
				
				if(checkFilter(remoteBeanConfiguration.getInput_filters(), remoteBean)){
					SmartPlannerBean smartPlannerBean = remoteBean.adaptBean(agencyId, remoteBeanConfiguration);
					beanList.add(smartPlannerBean);
				}
			}

			String smartPlannerServerUrl = getSmartPlannerServerUrl(remoteBeanConfiguration);
			SmartPlannerClient.postBeanToRemoteServer(beanList, smartPlannerServerUrl);
		}
	}

	private String getRemoteServerUrl(RemoteBeanConfiguration remoteBeanConfiguration) {
		String remoteServer = remoteBeanConfiguration.getRemote_url();
		String remoteServerUrl = remoteServer+REMOTE_URL_SUFFIX;
		
		return remoteServerUrl;
	}

	private String getSmartPlannerServerUrl(RemoteBeanConfiguration remoteBeanConfiguration) {
		String smartPlannerServer = remoteBeanConfiguration.getSp_url();
		String smartPlannerServerUrl = smartPlannerServer+SMARTPLANNER_URL_SUFFIX;
		
		return smartPlannerServerUrl;
	}

}
