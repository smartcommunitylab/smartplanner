package sc.app.smartadapter.ingestion;


public class MetroParcoBikeIngestion extends MetroParcoIngestionAbs{
	
	public MetroParcoBikeIngestion() {
		REMOTE_URL_SUFFIX = "/tn/bikepoint";
		SMARTPLANNER_URL_SUFFIX = "/data/bikesharing";
	}

}
