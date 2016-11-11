package sc.app.smartadapter.ingestion;

public class MetroParcoIngestionFactory {

	private static final Object METROPARCO_BIKE = "metroparco_bike";
	private static final Object METROPARCO_STRUCTURE = "metroparco_parking";

	public static MetroParcoIngestion getIngestion(String type) {

		MetroParcoIngestion metroParcoIngestionImpl = null;
		
		if(type.equals(METROPARCO_BIKE)){
			metroParcoIngestionImpl = new MetroParcoBikeIngestion();
		}

		if(type.equals(METROPARCO_STRUCTURE)){
			metroParcoIngestionImpl = new MetroParcoStructureIngestion();
		}


		return metroParcoIngestionImpl;
	}

}
