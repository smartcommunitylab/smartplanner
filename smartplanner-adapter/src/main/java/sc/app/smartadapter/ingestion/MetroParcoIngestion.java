package sc.app.smartadapter.ingestion;

import java.util.Map;

import sc.app.smartadapter.configuration.ClassAdapterConfiguration;
import sc.app.smartadapter.configuration.RemoteBeanConfiguration;

public interface MetroParcoIngestion {

	void ingest(ClassAdapterConfiguration classAdapterConfiguration, Map<String, RemoteBeanConfiguration> remoteBeanConfigurationAgencyMap) throws Exception;
}
