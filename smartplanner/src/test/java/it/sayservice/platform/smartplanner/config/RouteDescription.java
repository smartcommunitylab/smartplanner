package it.sayservice.platform.smartplanner.config;

public class RouteDescription {

	RouteId id;

	String routeShortName;
	String routeLongName;

	public RouteId getId() {
		return id;
	}

	public void setId(RouteId id) {
		this.id = id;
	}

	public String getRouteShortName() {
		return routeShortName;
	}

	public void setRouteShortName(String routeShortName) {
		this.routeShortName = routeShortName;
	}

	public String getRouteLongName() {
		return routeLongName;
	}

	public void setRouteLongName(String routeLongName) {
		this.routeLongName = routeLongName;
	}

}
