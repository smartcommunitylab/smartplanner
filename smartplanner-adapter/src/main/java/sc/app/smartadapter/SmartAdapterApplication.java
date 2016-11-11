package sc.app.smartadapter;

import java.util.Map;

import sc.app.smartadapter.configuration.ClassAdapterConfiguration;
import sc.app.smartadapter.configuration.ConfigurationManager;
import sc.app.smartadapter.configuration.RemoteBeanConfiguration;
import sc.app.smartadapter.ingestion.MetroParcoIngestion;
import sc.app.smartadapter.ingestion.MetroParcoIngestionFactory;

public class SmartAdapterApplication {

	public static void main(String[] args) throws Exception {

		Map<String,Map<String,RemoteBeanConfiguration>> remoteBeanConfigurationMapOfTypeMap = ConfigurationManager.getRemoteBeanConfiguration();
		Map<String, ClassAdapterConfiguration> classAdapterConfigurationMap = ConfigurationManager.getClassAdapterConfiguration();

		for (String type : remoteBeanConfigurationMapOfTypeMap.keySet()) {
			MetroParcoIngestion metroParcoIngestion = MetroParcoIngestionFactory.getIngestion(type);

			ClassAdapterConfiguration classAdapterConfiguration = classAdapterConfigurationMap.get(type);
			Map<String,RemoteBeanConfiguration> remoteBeanConfigurationAgencyMap = remoteBeanConfigurationMapOfTypeMap.get(type);
			
			metroParcoIngestion.ingest(classAdapterConfiguration, remoteBeanConfigurationAgencyMap);
		}
	}

}
