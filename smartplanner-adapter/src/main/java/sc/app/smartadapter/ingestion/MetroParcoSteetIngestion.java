package sc.app.smartadapter.ingestion;

import it.sayservice.platform.smartplanner.areainfo.AreaData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sc.app.smartadapter.beans.EnhancedAreaPoint;
import sc.app.smartadapter.beans.EnhancedFaresData;
import sc.app.smartadapter.beans.EnhancedSearchTime;
import sc.app.smartadapter.beans.MetroParcoArea;
import sc.app.smartadapter.beans.MetroParcoStreet;
import sc.app.smartadapter.beans.MetroParcoStreetOccupancy;
import sc.app.smartadapter.configuration.ClassAdapterConfiguration;
import sc.app.smartadapter.configuration.RemoteBeanConfiguration;
import sc.app.smartadapter.restful.client.MetroParcoClient;
import sc.app.smartadapter.restful.client.RemoteBean;
import sc.app.smartadapter.restful.client.SmartPlannerBean;
import sc.app.smartadapter.restful.client.SmartPlannerClient;

public class MetroParcoSteetIngestion extends MetroParcoIngestionAbs {

	private final static String AREA_REMOTE_URL_SUFFIX = "/tn/area";
	private final static String SEARCH_TIME_REMOTE_URL_SUFFIX = "/tn/area";
	
	private static Map<String, String> regionMap = new HashMap<String, String>();

	{
		regionMap.put("tn", "Trento");
	}

	public MetroParcoSteetIngestion() {
		REMOTE_URL_SUFFIX = "/tn/street";
		SMARTPLANNER_URL_SUFFIX = "/data/areapoints";
	}

	@Override
	public void ingest(ClassAdapterConfiguration classAdapterConfiguration, Map<String, RemoteBeanConfiguration> remoteBeanConfigurationAgencyMap) throws Exception {

		for (String agencyId : remoteBeanConfigurationAgencyMap.keySet()) {
			RemoteBeanConfiguration remoteBeanConfiguration = remoteBeanConfigurationAgencyMap.get(agencyId);
			//String remoteServer = remoteBeanConfiguration.getRemote_url();
			String remoteServerUrl = getRemoteServerUrl(remoteBeanConfiguration);

			boolean isRateInfo = remoteBeanConfiguration.isRateInfo();
			boolean isSearchTime = remoteBeanConfiguration.isSearchTime();

			Map<String, EnhancedFaresData> faresDataIdMap = null;
			if (isRateInfo) {
				faresDataIdMap = getRateAreasMapFromServer(remoteBeanConfiguration);
			}

			Map<String, EnhancedSearchTime> searchTimeIdMap = null;
			if (isSearchTime) {
				searchTimeIdMap = getSearchTimeMapFromServer(remoteBeanConfiguration);
			}

			Class beanClass = null;
			try {
				beanClass = Class.forName(classAdapterConfiguration.getSource_bean());
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}

			List<RemoteBean> remoteBeanList = MetroParcoClient.getBeanFromRestFullServer(remoteServerUrl, beanClass);

			List<SmartPlannerBean> beanList = new ArrayList<SmartPlannerBean>();

			for (RemoteBean remoteBean : remoteBeanList) {

				if (checkFilter(remoteBeanConfiguration.getInput_filters(), remoteBean)) {
					EnhancedAreaPoint enhancedAreaPoint = (EnhancedAreaPoint) remoteBean.adaptBean(agencyId, remoteBeanConfiguration);
					MetroParcoStreet metroParcoStreet = (MetroParcoStreet) remoteBean;
					
					if(metroParcoStreet.getRateAreaId() !=null ){
						EnhancedFaresData enhancedFaresData = faresDataIdMap.get(metroParcoStreet.getRateAreaId());

						AreaData areaData = enhancedAreaPoint.getData();
						if(areaData==null){
							areaData = new AreaData();
						}

						areaData.setFares(enhancedFaresData);
						enhancedAreaPoint.setData(areaData);
					}

					if(isSearchTime){
						EnhancedSearchTime enhancedSearchTime = searchTimeIdMap.get(metroParcoStreet.getId());

						AreaData areaData = enhancedAreaPoint.getData();
						if(areaData==null){
							areaData = new AreaData();
						}

						areaData.setSearchTime(enhancedSearchTime);
						enhancedAreaPoint.setData(areaData);
					}

					beanList.add(enhancedAreaPoint);
				}
			}

			String smartPlannerServerUrl = getSmartPlannerServerUrl(remoteBeanConfiguration);
			SmartPlannerClient.postBeanToRemoteServer(beanList, smartPlannerServerUrl);
		}
	}

	@Override
	protected String getSmartPlannerServerUrl(RemoteBeanConfiguration remoteBeanConfiguration) {
		String smartPlannerServer = remoteBeanConfiguration.getSp_url();
		String smartPlannerServerUrl = smartPlannerServer + SMARTPLANNER_URL_SUFFIX+"/Trento";
		
		//regionMap.get(remoteBeanConfiguration.get)

		return smartPlannerServerUrl;
	}

	private Map<String, EnhancedFaresData> getRateAreasMapFromServer(RemoteBeanConfiguration remoteBeanConfiguration) {
		Map<String, EnhancedFaresData> faresDataIdMap = new HashMap<String, EnhancedFaresData>();
		
		String remoteServer = remoteBeanConfiguration.getRemote_url();
		String remoteServerUrl = remoteServer + AREA_REMOTE_URL_SUFFIX;
		
		List<RemoteBean> remoteBeanList = MetroParcoClient.getBeanFromRestFullServer(remoteServerUrl, MetroParcoArea.class);

		for (RemoteBean remoteBean : remoteBeanList) {
			EnhancedFaresData enhancedFaresData = (EnhancedFaresData) remoteBean.adaptBean("", remoteBeanConfiguration);
			faresDataIdMap.put(enhancedFaresData.getCostZoneId(), enhancedFaresData);
		}
		
		return faresDataIdMap;
	}

	private Map<String, EnhancedSearchTime> getSearchTimeMapFromServer(RemoteBeanConfiguration remoteBeanConfiguration) {
		Map<String, EnhancedSearchTime> searchTimeIdMap = new HashMap<String, EnhancedSearchTime>();
		
		//String remoteServer = remoteBeanConfiguration.getRemote_url();
		//String remoteServerUrl = remoteServer + SEARCH_TIME_REMOTE_URL_SUFFIX;

		String remoteServerUrl = "https://dev.smartcommunitylab.it/metroparco/dashboard/rest/nosec/occupancy/tn/streets?granularity=hour";
		
		List<RemoteBean> remoteBeanList = MetroParcoClient.getBeanFromRestFullServer(remoteServerUrl, MetroParcoStreetOccupancy.class);

		for (RemoteBean remoteBean : remoteBeanList) {
			EnhancedSearchTime enhancedSearchTime = (EnhancedSearchTime) remoteBean.adaptBean("", remoteBeanConfiguration);
			searchTimeIdMap.put(enhancedSearchTime.getSearchAreaId(), enhancedSearchTime);
		}
		
		return searchTimeIdMap;
	}

}
