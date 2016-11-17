package sc.app.smartadapter.ingestion;

public class MetroParcoIngestionFactory {


	public static MetroParcoIngestion getIngestion(MetroParcoIngestionType type) {

		MetroParcoIngestion metroParcoIngestionImpl = null;
		
		if(type.equals(MetroParcoIngestionType.metroparco_bike)){
			metroParcoIngestionImpl = new MetroParcoBikeIngestion();
		}

		if(type.equals(MetroParcoIngestionType.metroparco_parking_street)){
			metroParcoIngestionImpl = new MetroParcoSteetIngestion();
		}

		return metroParcoIngestionImpl;
	}

}
