package sc.app.smartadapter;

import it.sayservice.platform.smartplanner.model.FaresZone;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sc.app.smartadapter.beans.MetroParcoStreet;
import sc.app.smartadapter.restful.client.MetroParcoClient;
import sc.app.smartadapter.restful.client.RemoteBean;
import sc.app.smartadapter.restful.client.SmartPlannerBean;
import sc.app.smartadapter.restful.client.SmartPlannerClient;
import sc.app.smartdapter.configuration.ClassAdapterConfiguration;
import sc.app.smartdapter.configuration.ConfigurationManager;
import sc.app.smartdapter.configuration.RemoteBeanConfiguration;

public class SmartAdapterApplication {

	public static void main(String[] args) throws Exception {

		List<RemoteBeanConfiguration> rbcList = ConfigurationManager.getRemoteBeanConfiguration();
		Map<String, ClassAdapterConfiguration> classAdapterConfigurationMap = ConfigurationManager.getClassAdapterConfiguration();
		for (int i = 0; i < rbcList.size(); i++) {
			RemoteBeanConfiguration rbc = rbcList.get(i);

			String remoteBeanId = rbc.getId();
			ClassAdapterConfiguration classAdapterConfiguration = classAdapterConfigurationMap.get(remoteBeanId);

			Class beanClass = null;
			try {
				beanClass = Class.forName(classAdapterConfiguration.getSource_bean());
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			String remoteServer = rbc.getRemote_url();
			List<RemoteBean> remoteBeanList = MetroParcoClient.getBeanFromRestFullServer(remoteServer, beanClass);

			List<SmartPlannerBean> beanList = new ArrayList<SmartPlannerBean>();

			for (RemoteBean remoteBean : remoteBeanList) {
				SmartPlannerBean smartPlannerBean = remoteBean.adaptBean();
				beanList.add(smartPlannerBean);
			}
			
			SmartPlannerClient smartPlannerClient = new SmartPlannerClient();
			String smartPlannerServer = rbc.getSp_url();
			smartPlannerClient.postBeanToRemoteServer(beanList, smartPlannerServer);
		}

	}
}
