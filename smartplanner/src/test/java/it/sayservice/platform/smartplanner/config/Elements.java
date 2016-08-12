package it.sayservice.platform.smartplanner.config;

import java.util.List;

public class Elements {

	String agencyId;
	boolean hasMap;
	
	List<Route> groups;

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public boolean isHasMap() {
		return hasMap;
	}

	public void setHasMap(boolean hasMap) {
		this.hasMap = hasMap;
	}

	public List<Route> getGroups() {
		return groups;
	}

	public void setGroups(List<Route> groups) {
		this.groups = groups;
	}
}
