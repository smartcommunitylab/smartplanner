package sc.app.smartadapter;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import sc.app.smartadapter.restful.client.MetroParcoClient;
import sc.app.smartadapter.restful.client.RemoteBean;
import sc.app.smartadapter.restful.client.SmartPlannerBean;
import sc.app.smartadapter.restful.client.SmartPlannerClient;
import sc.app.smartdapter.configuration.ClassAdapterConfiguration;
import sc.app.smartdapter.configuration.ConfigurationManager;
import sc.app.smartdapter.configuration.RemoteBeanConfiguration;

public class SmartAdapterApplication {

	public static void main(String[] args) throws Exception {

		//List<RemoteBeanConfiguration> rbcList = ConfigurationManager.getRemoteBeanConfiguration();
		Map<String,Map<String,RemoteBeanConfiguration>> remoteBeanConfigurationMapOfTypeMap = ConfigurationManager.getRemoteBeanConfiguration();
		Map<String, ClassAdapterConfiguration> classAdapterConfigurationMap = ConfigurationManager.getClassAdapterConfiguration();

		for (String type : remoteBeanConfigurationMapOfTypeMap.keySet()) {
			ClassAdapterConfiguration classAdapterConfiguration = classAdapterConfigurationMap.get(type);
			Map<String,RemoteBeanConfiguration> remoteBeanConfigurationMap = remoteBeanConfigurationMapOfTypeMap.get(type);

			for (String agencyId : remoteBeanConfigurationMap.keySet()) {
				RemoteBeanConfiguration remoteBeanConfiguration = remoteBeanConfigurationMap.get(agencyId);
				String remoteServer = remoteBeanConfiguration.getRemote_url();

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
						SmartPlannerBean smartPlannerBean = remoteBean.adaptBean(remoteBeanConfiguration);
						beanList.add(smartPlannerBean);
					}
				}
				
				SmartPlannerClient smartPlannerClient = new SmartPlannerClient();
				String smartPlannerServer = remoteBeanConfiguration.getSp_url();
				smartPlannerClient.postBeanToRemoteServer(beanList, smartPlannerServer);
			}
		}
	}

	private static boolean checkFilter(Map<String, String> inputFilter, RemoteBean remoteBean) {
		boolean isFiltered = true;
		Class<?> clazz = remoteBean.getClass();
		Field field = null;

		for (String fieldName : inputFilter.keySet()) {
			try {
				field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				String value = (String) field.get(remoteBean);
				
				if(!value.equals(inputFilter.get(fieldName))){
					isFiltered = false;
				}

			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}		
		
		return isFiltered;
	}
}
