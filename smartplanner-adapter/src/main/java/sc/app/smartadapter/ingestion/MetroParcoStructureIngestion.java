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

public class MetroParcoStructureIngestion  extends MetroParcoIngestionAbs{

	@Override
	public void ingest(ClassAdapterConfiguration classAdapterConfiguration, Map<String, RemoteBeanConfiguration> remoteBeanConfigurationAgencyMap) throws Exception {

		for (String agencyId : remoteBeanConfigurationAgencyMap.keySet()) {
			RemoteBeanConfiguration remoteBeanConfiguration = remoteBeanConfigurationAgencyMap.get(agencyId);
			String remoteServer = remoteBeanConfiguration.getRemote_url();

			boolean isRateInfo = remoteBeanConfiguration.isRateInfo();
			
			if(isRateInfo){
				getRateAreasFromServer();
			}
			
			Class beanClass = null;
			try {
				beanClass = Class.forName(classAdapterConfiguration.getSource_bean());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			List<RemoteBean> remoteBeanList = MetroParcoClient.getBeanFromRestFullServer(remoteServer, beanClass);

			List<SmartPlannerBean> beanList = new ArrayList<SmartPlannerBean>();

			for (RemoteBean remoteBean : remoteBeanList) {
				
				if(checkFilter(remoteBeanConfiguration.getInput_filters(), remoteBean)){
					SmartPlannerBean smartPlannerBean = remoteBean.adaptBean(agencyId, remoteBeanConfiguration);
					beanList.add(smartPlannerBean);
				}
			}
			
			SmartPlannerClient smartPlannerClient = new SmartPlannerClient();
			String smartPlannerServer = remoteBeanConfiguration.getSp_url();
			smartPlannerClient.postBeanToRemoteServer(beanList, smartPlannerServer);
		}
	}


	private static void getRateAreasFromServer() {
		// TODO Auto-generated method stub
	
	}

}
