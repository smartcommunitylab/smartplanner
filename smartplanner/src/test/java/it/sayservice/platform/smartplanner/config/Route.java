package it.sayservice.platform.smartplanner.config;

import java.util.List;

public class Route {

	String label;
	
	List<SingleRouteAR> routes;

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public List<SingleRouteAR> getRoutes() {
		return routes;
	}

	public void setRoutes(List<SingleRouteAR> routes) {
		this.routes = routes;
	}
}
