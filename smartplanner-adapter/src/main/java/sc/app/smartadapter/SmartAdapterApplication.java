package sc.app.smartadapter;

import java.util.Map;

import sc.app.smartadapter.configuration.ClassAdapterConfiguration;
import sc.app.smartadapter.configuration.ConfigurationManager;
import sc.app.smartadapter.configuration.RemoteBeanConfiguration;
import sc.app.smartadapter.ingestion.MetroParcoIngestion;
import sc.app.smartadapter.ingestion.MetroParcoIngestionFactory;
import sc.app.smartadapter.ingestion.MetroParcoIngestionType;

public class SmartAdapterApplication {

	public static void main(String[] args) throws Exception {

		Map<String,Map<String,RemoteBeanConfiguration>> remoteBeanConfigurationMapOfTypeMap = ConfigurationManager.getRemoteBeanConfiguration();
		Map<String, ClassAdapterConfiguration> classAdapterConfigurationMap = ConfigurationManager.getClassAdapterConfiguration();

		for (String ingestionType : remoteBeanConfigurationMapOfTypeMap.keySet()) {
			MetroParcoIngestionType metroParcoIngestionType = MetroParcoIngestionType.valueOf(ingestionType);
			
			MetroParcoIngestion metroParcoIngestion = MetroParcoIngestionFactory.getIngestion(metroParcoIngestionType);

			ClassAdapterConfiguration classAdapterConfiguration = classAdapterConfigurationMap.get(ingestionType);
			Map<String,RemoteBeanConfiguration> remoteBeanConfigurationAgencyMap = remoteBeanConfigurationMapOfTypeMap.get(ingestionType);
			
			metroParcoIngestion.ingest(classAdapterConfiguration, remoteBeanConfigurationAgencyMap);
		}
	}

}
