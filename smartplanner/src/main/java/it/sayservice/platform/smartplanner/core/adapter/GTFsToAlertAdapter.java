/**
 * Copyright 2011-2016 SAYservice s.r.l.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.sayservice.platform.smartplanner.core.adapter;

import it.sayservice.platform.smartplanner.data.message.EffectType;
import it.sayservice.platform.smartplanner.data.message.Position;
import it.sayservice.platform.smartplanner.data.message.RoadElement;
import it.sayservice.platform.smartplanner.data.message.StopId;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.data.message.Transport;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertAccident;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertDelay;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertStrike;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertType;

import java.util.ArrayList;
import java.util.List;

import it.sayservice.platform.smartplanner.core.gtfsrealtime.*;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.TripUpdate.StopTimeUpdate;

public class GTFsToAlertAdapter {

	/*
	 * Get A list of Alert Strike to save in DB from a feed message Service Alert type posted within a REST service
	 * */
	public static List<AlertStrike> getAlertStrikeEntityListFromGTFSServiceAlert(FeedMessage feedMessage) {

		List<AlertStrike> alertStrikeList = new ArrayList<AlertStrike>();

		for (int i = 0; i < feedMessage.getEntityList().size(); i++) {
				AlertStrike alertStrike = new AlertStrike();
				alertStrike.setType(AlertType.STRIKE);
				
				if (feedMessage.getEntityList().get(i)!=null&&feedMessage.getEntityList().get(i).getAlert()!=null){
					if (feedMessage.getEntityList().get(i).getAlert().getDescriptionText() != null) {
						alertStrike.setDescription(feedMessage.getEntityList().get(i).getAlert().getDescriptionText().getTranslationList().get(0).getText());
					}
					if (feedMessage.getEntityList().get(i).getAlert().getEffect() != null) {
						alertStrike.setEffect(getEffectTypeFromGTFSEffect(feedMessage.getEntityList().get(i).getAlert().getEffect().toString()));
					}

					if (feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0) != null && feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getStart() > 0) {
						alertStrike.setFrom(feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getStart());
					}
					if (feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0) != null && feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getEnd() > 0) {
						alertStrike.setTo(feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getEnd());
					}

					Transport transport = null;
					if (feedMessage.getEntityList().get(i).getAlert().getInformedEntityList() != null) {
						transport = getTransportFromInformedEntity(feedMessage.getEntityList().get(i).getAlert().getInformedEntityList());
					}

					alertStrike.setTransport(transport);
				}

				alertStrikeList.add(alertStrike);
		}

		return alertStrikeList;
	}

	/*
	 * Get A list of Alert Delay to save in DB from a feed message TripUpdate type posted within a REST service
	 * */
	public static List<AlertDelay> getAlertDelayEntityListFromGTFS(FeedMessage feedMessage) {

		List<AlertDelay> alertDelayList = new ArrayList<AlertDelay>();

		for (int i = 0; i < feedMessage.getEntityList().size(); i++) {
				AlertDelay alertDelay = new AlertDelay();
				alertDelay.setType(AlertType.DELAY);

				Transport transport = getTransportFromGTFSEntity(feedMessage.getEntityList().get(i));
				alertDelay.setTransport(transport);

				alertDelay.setDelay(0);

				if (feedMessage.getEntityList().get(i).getTripUpdate().getStopTimeUpdateList().get(0).getArrival() != null) {
					alertDelay.setDelay(feedMessage.getEntityList().get(i).getTripUpdate().getStopTimeUpdateList().get(0).getArrival().getDelay());
				}else if (feedMessage.getEntityList().get(i).getTripUpdate().getStopTimeUpdateList().get(0).getDeparture() != null) {
					alertDelay.setDelay(feedMessage.getEntityList().get(i).getTripUpdate().getStopTimeUpdateList().get(0).getDeparture().getDelay());
				}

				Position position = getPositionFromGTFSStopTimeUpdate(feedMessage.getEntityList().get(i).getTripUpdate().getStopTimeUpdateList().get(0));

				alertDelay.setPosition(position);

				alertDelayList.add(alertDelay);
		}

		return alertDelayList;
	}

	/*
	 * Get A list of AlertAccident to save in DB from a feed message Service Alert type posted within a REST service
	 * */
	public static List<AlertAccident> getAlertAccidentListFromGTFSServiceAlert(FeedMessage feedMessage) {

		List<AlertAccident> alertAccidentList = new ArrayList<AlertAccident>();
			
		for (int i = 0; i < feedMessage.getEntityList().size(); i++) {
				AlertAccident alertAccident = new AlertAccident();
				alertAccident.setType(AlertType.ACCIDENT);

				if (feedMessage.getEntityList().get(i)!=null &&
						feedMessage.getEntityList().get(i).getAlert()!=null){
					if (feedMessage.getEntityList().get(i).getAlert().getDescriptionText() != null) {
						alertAccident.setDescription(feedMessage.getEntityList().get(i).getAlert().getDescriptionText().getTranslationList().get(0).getText());
					}

					if (feedMessage.getEntityList().get(i).getAlert().getEffect() != null) {
						alertAccident.setEffect(getEffectTypeFromGTFSEffect(feedMessage.getEntityList().get(i).getAlert().getEffect().toString()));
					}
				}
				
				if (feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0) != null && 
						feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getStart() > 0) {
					alertAccident.setFrom(feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getStart());
				}
				if (feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0) != null && 
						feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getEnd() > 0) {
					alertAccident.setTo(feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getEnd());
				}

				Transport transport = null;
				Position position = null;

				if (feedMessage.getEntityList().get(i).getAlert().getInformedEntityList() != null) {
					transport = getTransportFromInformedEntity(feedMessage.getEntityList().get(i).getAlert().getInformedEntityList());
					alertAccident.setTransport(transport);
					position = getPositionFromInformedEntity(feedMessage.getEntityList().get(i).getAlert().getInformedEntityList());
					alertAccident.setPosition(position);
				}

				alertAccidentList.add(alertAccident);
		}

		return alertAccidentList;
	}

	/*
	 * Get A list of AlertRoad to save in DB from a feed message Service Alert type posted within a REST service
	 * */
	public static List<AlertRoad> getAlertRoadListFromGTFS(FeedMessage feedMessage) {

		List<AlertRoad> alertRoadList = new ArrayList<AlertRoad>();

		for (int i = 0; i < feedMessage.getEntityList().size(); i++) {
				AlertRoad alertRoad = new AlertRoad();
				alertRoad.setType(AlertType.ROAD);

				if (feedMessage.getEntityList().get(i)!=null &&
						feedMessage.getEntityList().get(i).getAlert()!=null){
					if (feedMessage.getEntityList().get(i).getAlert().getDescriptionText() != null) {
						alertRoad.setDescription(feedMessage.getEntityList().get(i).getAlert().getDescriptionText().getTranslationList().get(0).getText());
					}

					if (feedMessage.getEntityList().get(i).getAlert().getEffect() != null) {
						alertRoad.setEffect(getEffectTypeFromGTFSEffect(feedMessage.getEntityList().get(i).getAlert().getEffect().toString()));
					}
				}

				if (feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0) != null && 
						feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getStart() > 0) {
					alertRoad.setFrom(feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getStart());
				}
				if (feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0) != null && 
						feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getEnd() > 0) {
					alertRoad.setTo(feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getEnd());
				}

				if (feedMessage.getEntityList().get(i).getAlert().getInformedEntityList() != null) {
					Transport transport = getTransportFromInformedEntity(feedMessage.getEntityList().get(i).getAlert().getInformedEntityList());
					alertRoad.setTransport(transport);
					RoadElement roadElement = getRoadElementFromInformedEntity(feedMessage.getEntityList().get(i).getAlert().getInformedEntityList());
					alertRoad.setRoad(roadElement);
				}

				alertRoadList.add(alertRoad);
		}

		return alertRoadList;
	}

	/*
	 * Get Transport Object from GTFS ServiceAlert informed Entity List to put into Generic Alert Object
	 */
	private static Transport getTransportFromInformedEntity(List<EntitySelector> informedEntityList) {
		Transport transport = new Transport();
		for (EntitySelector entitySelector : informedEntityList) {
			if (entitySelector.getAgencyId() != null) {
				transport.setAgencyId(entitySelector.getAgencyId());
			}

			if (entitySelector.getRouteId() != null) {
				transport.setRouteId(entitySelector.getRouteId());
			}

			if (entitySelector.getTrip().getTripId() != null) {
				transport.setTripId(entitySelector.getTrip().getTripId());
			}
		
			if (entitySelector.getRouteType()> 0) {
				transport.setType(getTransportTypeFromRouteType(entitySelector.getRouteType()));
			}
		}

		return transport;
	}


	/*
	 * Get Transport Object from GTFS TripUpdate Entity to put into Generic Alert Object
	 */
	public static Transport getTransportFromGTFSEntity(FeedEntity feedEntity) {

		Transport transport = new Transport();
		if (feedEntity.getTripUpdate() != null &&
				feedEntity.getTripUpdate().getTrip() != null) {
			if(feedEntity.getTripUpdate().getTrip().getRouteId() != null){
				transport.setRouteId(feedEntity.getTripUpdate().getTrip().getRouteId());
			}
			if(feedEntity.getTripUpdate().getTrip().getTripId() != null){
				transport.setTripId(feedEntity.getTripUpdate().getTrip().getTripId());
			}
		}

		return transport;
	}

	/*
	 * Get Position Object from GTFS TripUpdate StopTimeUpdate to put into Generic Alert Object
	 */
	private static Position getPositionFromGTFSStopTimeUpdate(StopTimeUpdate stopTimeUpdate) {
		Position position = new Position();

		stopTimeUpdate.getArrival().getDelay();
		StopId stopId = new StopId();
		if (stopTimeUpdate.getStopId() != null) {
			stopId.setId(stopTimeUpdate.getStopId());
		}

		if (stopTimeUpdate.getStopSequence() > 0) {
			//TO DO
			//stopId.setId(String.valueOf(stopTimeUpdate.getStopSequence()));
		}

		position.setStopId(stopId);

		return position;
	}

	/*
	 * Get Position Object from GTFS ServiceAlert informed Entity List
	 */
	private static Position getPositionFromInformedEntity(List<EntitySelector> entitySelectorList) {
		Position position = new Position();

		for (EntitySelector entitySelector : entitySelectorList) {
			StopId stopId = new StopId();

			if (entitySelector.getStopId() != null) {
				stopId.setId(entitySelector.getStopId());
			}

			if (entitySelector.getAgencyId() != null) {
				stopId.setAgencyId(entitySelector.getAgencyId());
			}

			position.setStopId(stopId);
		}

		return position;
	}
	
	//TO DO (manca la toponomastica in GTFS)
	private static RoadElement getRoadElementFromInformedEntity(List<EntitySelector> entitySelectorList){
		RoadElement roadElement = new RoadElement();

		return roadElement;
	}
	
	
	/*
	 * Adapt GTFS Route Type To Alert Transport Type
	 */
	private static TType getTransportTypeFromRouteType(int routeType) {
			if (routeType == 900)
				return TType.CAR;
			else if (routeType == -1)
				return TType.TRANSIT;
			else if (routeType == 700)
				return TType.BUS;
			else if (routeType == 100)
				return TType.RAIL;
			else if (routeType == 1300)
				return TType.GONDOLA;
			else if (routeType == 1701)
				return TType.CABLE_CAR;
			else if (routeType == 1400)
				return TType.FUNICULAR;
			else if (routeType == 400)
				return TType.SUBWAY;
			else if (routeType == 1000)
				return TType.FERRY;
			
			return TType.WALK;
	}

	/*
	 * Adapt GTFS Effect To Alert Effect Type
	 */
	private static EffectType getEffectTypeFromGTFSEffect(String effect) {
		EffectType effectType = null;
		try {
			effectType = EffectType.valueOf(effect);
		} catch (java.lang.IllegalArgumentException e) {
			effectType = EffectType.UNKNOWN_EFFECT;
		}
		
		return effectType;
	}

	
	//TO DO
	public static List<AlertStrike> getAlertStrikeListFromGTFS(FeedMessage feedMessage) {

		List<AlertStrike> alertStrikeList = new ArrayList<AlertStrike>();

		for (int i = 0; i < feedMessage.getEntityList().size(); i++) {
			//for (int j = 0; j < feedMessage.getEntityList().get(i).getAlert().getActivePeriodCount(); j++) {
				AlertStrike alertStrike = new AlertStrike();
				alertStrike.setType(AlertType.STRIKE);

				if (feedMessage.getHeader().getTimestamp() > 0) {
					//alertStrike.setId(String.valueOf(feedMessage.getHeader().getTimestamp()));
				} else {
					//alertStrike.setId(String.valueOf(Calendar.getInstance().getTimeInMillis()));
				}

				//alertStrike.setEntity(feedMessage.getEntityList().get(i).getId());
				
				if (feedMessage.getEntityList().get(i)!=null&&feedMessage.getEntityList().get(i).getAlert()!=null){
					if (feedMessage.getEntityList().get(i).getAlert().getDescriptionText() != null) {
						alertStrike.setDescription(feedMessage.getEntityList().get(i).getAlert().getDescriptionText().getTranslationList().get(0).getText());
					}
					if (feedMessage.getEntityList().get(i).getAlert().getEffect() != null) {
						alertStrike.setEffect(getEffectTypeFromGTFSEffect(feedMessage.getEntityList().get(i).getAlert().getEffect().toString()));
					}

					if (feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0) != null && feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getStart() > 0) {
						alertStrike.setFrom(feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getStart());
					}
					if (feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0) != null && feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getEnd() > 0) {
						alertStrike.setTo(feedMessage.getEntityList().get(i).getAlert().getActivePeriodList().get(0).getEnd());
					}

					alertStrike.setType(AlertType.STRIKE);

					Transport transport = null;
					if (feedMessage.getEntityList().get(i).getAlert().getInformedEntityList() != null) {
						transport = getTransportFromInformedEntity(feedMessage.getEntityList().get(i).getAlert().getInformedEntityList());
					}

					alertStrike.setTransport(transport);
				}

				alertStrikeList.add(alertStrike);
			//}

		}

		return alertStrikeList;
	}

	
	//TO DO
	public static List<AlertDelay> getAlertDelayListFromGTFSTripUpdate(FeedMessage feedMessage) {

		List<AlertDelay> alertDelayList = new ArrayList<AlertDelay>();

		for (int i = 0; i < feedMessage.getEntityList().size(); i++) {
			for (int j = 0; j < feedMessage.getEntityList().get(i).getTripUpdate().getStopTimeUpdateList().size(); j++) {
				AlertDelay alertDelay = new AlertDelay();

				alertDelay.setId(feedMessage.getEntityList().get(i).getId());
				alertDelay.setType(AlertType.DELAY);

				Transport transport = getTransportFromGTFSEntity(feedMessage.getEntityList().get(i));
				alertDelay.setTransport(transport);

				alertDelay.setDelay(0);

				if (feedMessage.getEntityList().get(i).getTripUpdate().getStopTimeUpdateList().get(j).getArrival() != null) {
					alertDelay.setDelay(feedMessage.getEntityList().get(i).getTripUpdate().getStopTimeUpdateList().get(j).getArrival().getDelay());
				}

				if (feedMessage.getEntityList().get(i).getTripUpdate().getStopTimeUpdateList().get(j).getDeparture() != null) {
					alertDelay.setDelay(feedMessage.getEntityList().get(i).getTripUpdate().getStopTimeUpdateList().get(j).getDeparture().getDelay());
				}

				Position position = getPositionFromGTFSStopTimeUpdate(feedMessage.getEntityList().get(i).getTripUpdate().getStopTimeUpdateList().get(j));

				alertDelay.setPosition(position);

				alertDelayList.add(alertDelay);
			}
		}

		return alertDelayList;
	}
	
}