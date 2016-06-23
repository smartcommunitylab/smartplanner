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

import it.sayservice.platform.smartplanner.core.gtfsrealtime.Alert;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.Alert.Cause;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.Alert.Effect;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.EntitySelector;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.FeedEntity;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.FeedHeader;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.FeedHeader.Incrementality;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.FeedMessage;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.TimeRange;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.TranslatedString;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.TranslatedString.Translation;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.TripDescriptor;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.TripUpdate;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.TripUpdate.StopTimeEvent;
import it.sayservice.platform.smartplanner.core.gtfsrealtime.TripUpdate.StopTimeUpdate;
import it.sayservice.platform.smartplanner.data.message.EffectType;
import it.sayservice.platform.smartplanner.data.message.TType;
import it.sayservice.platform.smartplanner.data.message.Transport;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertAccident;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertDelay;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertRoad;
import it.sayservice.platform.smartplanner.data.message.alerts.AlertStrike;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AlertToGTFSAdapter {

	private static final transient Logger logger = LoggerFactory.getLogger(AlertToGTFSAdapter.class);

	//TO DO
	public static List<FeedMessage> getGTFSListFromAlertStrikeList(List<AlertStrike> alertStrikeList){

		Map<String, List<AlertStrike>> feedMessageMap = new HashMap<String, List<AlertStrike>>();

		for (AlertStrike alertStrike : alertStrikeList) {
			List<AlertStrike> mappedAlertStrikeList = feedMessageMap.get(alertStrike.getId());

			if(mappedAlertStrikeList==null || mappedAlertStrikeList.isEmpty()){
				mappedAlertStrikeList = new ArrayList<AlertStrike>();
			}

			mappedAlertStrikeList.add(alertStrike);
			feedMessageMap.put(alertStrike.getEntity().toString(), mappedAlertStrikeList);
		}

		List<FeedMessage> feedMessageList = new ArrayList<FeedMessage>();
		
		for (String entityId : feedMessageMap.keySet()) {
			FeedMessage feedMessage = getAggregatedFeedMessageListByFeedMessage(feedMessageMap.get(entityId));
	    feedMessageList.add(feedMessage);
		}

		return feedMessageList;
	}


	private static FeedMessage getAggregatedFeedMessageListByFeedMessage(List<AlertStrike> alertStrikeList) {
	  FeedMessage feedMessage = new FeedMessage();

	  FeedHeader feedHeader = getFeedHeader();
	  feedMessage.setHeader(feedHeader);

	  FeedEntity feedEntity = new FeedEntity();
	  feedEntity.setId(alertStrikeList.get(0).getEntity().toString());
	  Alert alertFeed = getAlertFeedFromAlertStrikeList(alertStrikeList);
	  feedEntity.setAlert(alertFeed);

	  List<FeedEntity> feedEntityList = new ArrayList<FeedEntity>();
	  feedEntityList.add(feedEntity);
	  feedMessage.setEntityList(feedEntityList);

		return feedMessage;
	}

	private static Alert getAlertFeedFromAlertStrikeList(List<AlertStrike> alertStrikeList) {
	  it.sayservice.platform.smartplanner.core.gtfsrealtime.Alert gtfsAlert = new Alert();
	  gtfsAlert.setCause(Cause.STRIKE);
	  
		List<TimeRange> timeRangeList = new ArrayList<TimeRange>();
	  for (AlertStrike alertStrike : alertStrikeList) {
		  TimeRange timeRange = new TimeRange();
	  		timeRange.setStart(alertStrike.getFrom());
	  		timeRange.setEnd(alertStrike.getTo());
	  		timeRangeList.add(timeRange);
		}
  	gtfsAlert.setActivePeriodList(timeRangeList);
	  
	  TranslatedString translatedString = new TranslatedString();
	  	Translation translation = new Translation();
	  		translation.setText(alertStrikeList.get(0).getDescription());
	  		
	  	List<Translation> translationList = new ArrayList<Translation>();
	  	translationList.add(translation);
			translatedString.setTranslationList(translationList);
	  gtfsAlert.setDescriptionText(translatedString);

	  gtfsAlert.setEffect(getEffect(alertStrikeList.get(0).getEffect()));
	  
	  if(alertStrikeList.get(0).getTransport().getAgencyId()!=null){
		  EntitySelector entitySelector = new EntitySelector();
	  	entitySelector.setAgencyId(alertStrikeList.get(0).getTransport().getAgencyId());
	  	
	  	List<EntitySelector> informedEntityList = new ArrayList<EntitySelector>();
	  	informedEntityList.add(entitySelector);
			gtfsAlert.setInformedEntityList(informedEntityList);
	  }

	  if(alertStrikeList.get(0).getTransport().getRouteId()!=null){
		  EntitySelector entitySelector = new EntitySelector();
	  	entitySelector.setRouteId(alertStrikeList.get(0).getTransport().getRouteId());

	  	List<EntitySelector> informedEntityList = new ArrayList<EntitySelector>();
	  	informedEntityList.add(entitySelector);
			gtfsAlert.setInformedEntityList(informedEntityList);
	  }

	  if(alertStrikeList.get(0).getTransport().getTripId()!=null){
		  EntitySelector entitySelector = new EntitySelector();
	  		TripDescriptor tripDescriptor = new TripDescriptor();
	  		tripDescriptor.setTripId(alertStrikeList.get(0).getTransport().getTripId());
	  	entitySelector.setTrip(tripDescriptor);
	  }

	  EntitySelector entitySelector = new EntitySelector();
	  	entitySelector.setRouteType(getAlertTransportCode(alertStrikeList.get(0).getTransport()));
	  
		return gtfsAlert;
	}

	public static List<FeedMessage> getGTFSEntityListFromAlertStrikeList(List<AlertStrike> alertStrikeList){
		List<FeedMessage> feedMessageList = new ArrayList<FeedMessage>();

		for (AlertStrike alertStrike : alertStrikeList) {
		  FeedMessage feedMessage = getGTFSFromAlertStrike(alertStrike);
			
		  feedMessageList.add(feedMessage);
		}

		return feedMessageList;		
	}
	
	public static FeedMessage getGTFSFromAlertStrike(AlertStrike alertStrike){
	  FeedMessage feedMessage = new FeedMessage();

	  FeedHeader feedHeader = getFeedHeader();
	  feedMessage.setHeader(feedHeader);

	  FeedEntity feedEntity = new FeedEntity();
	  Alert alertFeed = getAlertFeedFromAlert(alertStrike, Cause.STRIKE);
	  feedEntity.setAlert(alertFeed);
	  feedEntity.setId(alertStrike.getId());

	  List<FeedEntity> feedEntityList = new ArrayList<FeedEntity>();
	  feedEntityList.add(feedEntity);
	  feedMessage.setEntityList(feedEntityList);


		return feedMessage;
	}

	public static FeedMessage getGTFSFromAlertDelay(AlertDelay alertDelay){
	  FeedMessage feedMessage = new FeedMessage();

	  FeedHeader feedHeader = getFeedHeader();
	  feedMessage.setHeader(feedHeader);

	  FeedEntity feedEntity = new FeedEntity();
	  TripUpdate tripUpdate = getTripUpdateFromAlertDelay(alertDelay);
	  feedEntity.setTripUpdate(tripUpdate);
	  feedEntity.setId(alertDelay.getId());

	  List<FeedEntity> feedEntityList = new ArrayList<FeedEntity>();
	  feedEntityList.add(feedEntity);
	  feedMessage.setEntityList(feedEntityList);

		return feedMessage;
	}

	private static TripUpdate getTripUpdateFromAlertDelay(AlertDelay alertDelay) {
		TripUpdate tripUpdate =new TripUpdate();
		
		TripDescriptor tripDescriptor = getTripDescriptorFromAlertDelay(alertDelay);
		tripUpdate.setTrip(tripDescriptor);
		
		StopTimeUpdate stopTimeUpdate = getStopTimeUpdateFromAlertDelay(alertDelay);
		
		List<StopTimeUpdate> stopTimeUpdateList = new ArrayList<StopTimeUpdate>();
		stopTimeUpdateList.add(stopTimeUpdate);
		tripUpdate.setStopTimeUpdateList(stopTimeUpdateList);
		
		return tripUpdate;
	}


	private static StopTimeUpdate getStopTimeUpdateFromAlertDelay(AlertDelay alertDelay) {
		StopTimeUpdate stopTimeUpdate = new StopTimeUpdate();
		
		if(alertDelay.getPosition()!=null&&
			 alertDelay.getPosition().getStopId()!=null&&
			 alertDelay.getPosition().getStopId().getId()!=null){
			stopTimeUpdate.setStopId(alertDelay.getPosition().getStopId().getId());
		}
		
		StopTimeEvent stopTimeEvent = getStopTimeEventFromAlertDelay(alertDelay);
		stopTimeUpdate.setArrival(stopTimeEvent);
		
		return stopTimeUpdate;
	}


	private static StopTimeEvent getStopTimeEventFromAlertDelay(AlertDelay alertDelay) {
		StopTimeEvent stopTimeEventBuilder = new StopTimeEvent();
		
		stopTimeEventBuilder.setDelay(Integer.parseInt(String.valueOf(alertDelay.getDelay())));
		
		return stopTimeEventBuilder;
	}


	private static TripDescriptor getTripDescriptorFromAlertDelay(AlertDelay alertDelay) {
		TripDescriptor tripDescriptor = new TripDescriptor();
		
		if(alertDelay.getTransport()!=null){			
			if(alertDelay.getTransport().getTripId()!=null){
				tripDescriptor.setTripId(alertDelay.getTransport().getTripId());
			}

			if(alertDelay.getTransport().getRouteId()!=null){
				tripDescriptor.setRouteId(alertDelay.getTransport().getRouteId());
			}
		}
				
		return tripDescriptor;
	}


	private static int getAlertTransportCode(Transport transport) {
		
		if (transport.getType()!=null){
			if (transport.getType().equals(TType.CAR))
				return 900;
			else if (transport.getType().equals(TType.BICYCLE))
				return -1;
			else if (transport.getType().equals(TType.TRANSIT))
				return -1;
			else if (transport.getType().equals(TType.SHAREDBIKE))
				return -1;
			else if (transport.getType().equals(TType.SHAREDBIKE_WITHOUT_STATION))
				return -1;
			else if (transport.getType().equals(TType.CARWITHPARKING))
				return 900;
			else if (transport.getType().equals(TType.SHAREDCAR))
				return 900;
			else if (transport.getType().equals(TType.SHAREDCAR_WITHOUT_STATION))
				return 900;
			else if (transport.getType().equals(TType.BUS))
				return 700;
			else if (transport.getType().equals(TType.TRAIN))
				return 100;
			else if (transport.getType().equals(TType.WALK))
				return -1;
			else if (transport.getType().equals(TType.GONDOLA))
				return 1300;
			else if (transport.getType().equals(TType.CABLE_CAR))
				return 1701;
			else if (transport.getType().equals(TType.FUNICULAR))
				return 1400;
			else if (transport.getType().equals(TType.SHUTTLE))
				return -1;
			else if (transport.getType().equals(TType.PARK_AND_RIDE))
				return -1;		
			else if (transport.getType().equals(TType.TRAM))
				return 900;		
			else if (transport.getType().equals(TType.LIGHT_RAIL))
				return 900;		
			else if (transport.getType().equals(TType.STREETCAR))
				return 900;		
			else if (transport.getType().equals(TType.SUBWAY))
				return 400;		
			else if (transport.getType().equals(TType.METRO))
				return 400;		
			else if (transport.getType().equals(TType.RAIL))
				return 100;		
			else if (transport.getType().equals(TType.FERRY))
				return 1000;		
			else if (transport.getType().equals(TType.SUSPENDED_CABLE_CAR))
				return 1300;		
			else if (transport.getType().equals(TType.SUSPENDED_CABLE_CAR))
				return 1300;		
		}
		
		return -1;
	}

	private static FeedHeader getFeedHeader() {
	  FeedHeader feedHeaderBuilder = new FeedHeader();
	  
	  feedHeaderBuilder.setTimestamp(Calendar.getInstance().getTimeInMillis());
	  feedHeaderBuilder.setIncrementality(Incrementality.FULL_DATASET);
	  feedHeaderBuilder.setGtfsRealtimeVersion("3");

		return feedHeaderBuilder;
	}

	private static Effect getEffect(EffectType effect) {
		Effect enumEffect = null;

		try {
			enumEffect = Effect.valueOf(effect.toString());
		} catch (java.lang.IllegalArgumentException e) {
			enumEffect = Effect.UNKNOWN_EFFECT;
		}

		return enumEffect;
	}

	public static List<FeedMessage> getGTFSEntityListFromAlertDelayList(List<AlertDelay> alertDelayList) {
		List<FeedMessage> feedMessageList = new ArrayList<FeedMessage>();

		for (AlertDelay alertDelay : alertDelayList) {
			FeedMessage feedMessage = getGTFSFromAlertDelay(alertDelay);

		  feedMessageList.add(feedMessage);
		}

		return feedMessageList;		
	}

	public static FeedMessage getGTFSFromAlertAccident(AlertAccident alertAccident) {
	  FeedMessage feedMessage = new FeedMessage();

	  FeedHeader feedHeader = getFeedHeader();
	  feedMessage.setHeader(feedHeader);

	  FeedEntity feedEntity = new FeedEntity();
	  Alert alertFeed = getAlertFeedFromAlert(alertAccident, Cause.ACCIDENT);
	  feedEntity.setAlert(alertFeed);
	  feedEntity.setId(alertAccident.getId());

	  List<FeedEntity> feedEntityList = new ArrayList<FeedEntity>();
	  feedEntityList.add(feedEntity);
	  feedMessage.setEntityList(feedEntityList);

		return feedMessage;
	}

	private static FeedMessage getGTFSFromAlertRoad(AlertRoad alertRoad) {
	  FeedMessage feedMessage = new FeedMessage();

	  FeedHeader feedHeader = getFeedHeader();
	  feedMessage.setHeader(feedHeader);

	  FeedEntity feedEntity = new FeedEntity();
	  Alert alertFeed = getAlertFeedFromAlert(alertRoad, Cause.CONSTRUCTION);
	  feedEntity.setAlert(alertFeed);
	  feedEntity.setId(alertRoad.getId());

	  List<FeedEntity> feedEntityList = new ArrayList<FeedEntity>();
	  feedEntityList.add(feedEntity);
	  feedMessage.setEntityList(feedEntityList);

		return feedMessage;
	}

	private static Alert getAlertFeedFromAlert(it.sayservice.platform.smartplanner.data.message.alerts.Alert alert, Cause cause) {
	  it.sayservice.platform.smartplanner.core.gtfsrealtime.Alert gtfsAlert = new Alert();
	  gtfsAlert.setCause(cause);

	  TimeRange timeRange = new TimeRange();
	  if(alert.getFrom()>0){
	  	timeRange.setStart(alert.getFrom());
	  }
	  if(alert.getTo()>0){
			timeRange.setEnd(alert.getTo());
	  }
	  
	  List<TimeRange> timeRangeList = new ArrayList<TimeRange>();
	  timeRangeList.add(timeRange);
	  gtfsAlert.setActivePeriodList(timeRangeList);
	  
	  TranslatedString translatedString = new TranslatedString();
	  Translation translation = new Translation();

	  if(alert.getDescription()!=null){
	  	translation.setText(alert.getDescription());
	  }

  	List<Translation> translationList = new ArrayList<Translation>();
  	translationList.add(translation);
		translatedString.setTranslationList(translationList);
	  gtfsAlert.setDescriptionText(translatedString);

	  gtfsAlert.setEffect(getEffect(alert.getEffect()));
	  EntitySelector entitySelector = new EntitySelector();
  	if(alert.getTransport()!=null){
	  	if(alert.getTransport().getAgencyId()!=null){
		  	entitySelector.setAgencyId(alert.getTransport().getAgencyId());
	  	}
	  	if(alert.getTransport().getRouteId()!=null){
		  	entitySelector.setRouteId(alert.getTransport().getRouteId());
	  	}
	  	if(alert.getTransport().getRouteId()!=null){
		  	entitySelector.setRouteId(alert.getTransport().getRouteId());
	  	}

	  	entitySelector.setRouteType(getAlertTransportCode(alert.getTransport()));
	  	
	  	TripDescriptor tripDescriptor = new TripDescriptor();
	  	
	  	if(alert.getTransport().getTripId()!=null){
	  		tripDescriptor.setTripId(alert.getTransport().getTripId());
	  	}
	  	
  		entitySelector.setTrip(tripDescriptor);
  	}


  	List<EntitySelector> informedEntityList = new ArrayList<EntitySelector>();
  	informedEntityList.add(entitySelector);
		gtfsAlert.setInformedEntityList(informedEntityList);
	  
		return gtfsAlert;
	}


	public static List<FeedMessage> getGTFSEntityListFromAlertRoadList(List<AlertRoad> alertRoadList) {
		List<FeedMessage> feedMessageList = new ArrayList<FeedMessage>();

		for (AlertRoad alertRoad : alertRoadList) {
			FeedMessage feedMessage = getGTFSFromAlertRoad(alertRoad);
			
		  feedMessageList.add(feedMessage);
		}

		return feedMessageList;
	}


	public static List<FeedMessage> getGTFSEntityListFromAlertAccidentList(List<AlertAccident> alertAccidentList) {
		List<FeedMessage> feedMessageList = new ArrayList<FeedMessage>();

		for (AlertAccident alertAccident : alertAccidentList) {
			FeedMessage feedMessage = getGTFSFromAlertAccident(alertAccident);

		  feedMessageList.add(feedMessage);
		}

		return feedMessageList;
	}
	
	public static List<FeedMessage> getGTFSListFromAlertDelayList(List<AlertDelay> alertDelayList) {
		List<FeedMessage> feedMessageList = new ArrayList<FeedMessage>();

		for (AlertDelay alertDelay : alertDelayList) {
			FeedMessage feedMessage = getGTFSFromAlertDelay(alertDelay);
		  feedMessageList.add(feedMessage);
		}

		return feedMessageList;
	}
}