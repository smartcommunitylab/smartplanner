package it.sayservice.platform.smartplanner.areainfo;

import it.sayservice.platform.smartplanner.model.FaresData;

public class AreaData {

	SearchTime searchTime;
	private FaresData fares;

	public SearchTime getSearchTime() {
		return searchTime;
	}

	public void setSearchTime(SearchTime searchTime) {
		this.searchTime = searchTime;
	}

	public FaresData getFares() {
		return fares;
	}

	public void setFares(FaresData fares) {
		this.fares = fares;
	}
}
