package sc.app.smartadapter.restful.client;

import it.sayservice.platform.smartplanner.model.CostData;
import it.sayservice.platform.smartplanner.model.DayNight;
import it.sayservice.platform.smartplanner.model.FaresPeriod;
import it.sayservice.platform.smartplanner.model.TimeSlot;
import it.sayservice.platform.smartplanner.model.WeekDay;

import java.util.ArrayList;
import java.util.List;

import sc.app.smartadapter.beans.EnhancedFaresData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;

public class SmartPlannerClient {

	public static void postBeanToRemoteServer(List<SmartPlannerBean> beanList, String server){
		ObjectMapper mapper = new ObjectMapper();
		String beanListJson = null;
		try {
			beanListJson = mapper.writeValueAsString(beanList);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}

		System.out.println("beanListJson: "+ beanListJson);

		Client client = Client.create();
		WebResource webResource = client
		   .resource(server);

		ClientResponse response = webResource.type("application/json")
		   .post(ClientResponse.class, beanListJson);

		if (response.getStatus() != 201 && response.getStatus() != 200) {
			throw new RuntimeException("Failed : HTTP error code : "
			     + response.getStatus());
		}
	}
	
	
	public static void main(String[] args) {
		try {

				List<SmartPlannerBean> faresZoneList = new ArrayList<SmartPlannerBean>();
				EnhancedFaresData enFaresZone = new EnhancedFaresData();
				enFaresZone.setCostZoneId("50c742d044aed8a0d530565d");
				FaresPeriod[] faresZonePeriods = new FaresPeriod[1];

				FaresPeriod faresZonePeriod = new FaresPeriod();
				CostData costData = new CostData();
				String fixedCost = "1,30";
				costData.setFixedCost(fixedCost);
				String costDefinition = "€. 1,30/ora";
				costData.setCostDefinition(costDefinition);
				faresZonePeriod.setCostData(costData);
				faresZonePeriod.setDayOrNight(DayNight.NIGHT);
				String fromDate = "03/03";
				faresZonePeriod.setFromDate(fromDate);
				boolean holiday = true;
				faresZonePeriod.setHoliday(holiday);
				TimeSlot[] timeSlots = new TimeSlot[1];
				TimeSlot timeSlot = new TimeSlot();
				String from = "24:00";
				timeSlot.setFrom(from);
				String to = "23:00";
				timeSlot.setTo(to);
				timeSlots[0] = timeSlot;
				faresZonePeriod.setTimeSlots(timeSlots);
				String toDate = "31/12";
				faresZonePeriod.setToDate(toDate);
				WeekDay[] weekDays = {WeekDay.MO,WeekDay.SU};
				faresZonePeriod.setWeekDays(weekDays);

				faresZonePeriods[0] = faresZonePeriod;
				enFaresZone.setFaresPeriod(faresZonePeriods);

				faresZoneList.add(enFaresZone);

				SmartPlannerClient spClient = new SmartPlannerClient();
				String server = "http://localhost:7676/smart-planner/trentino/rest/data/areacosts/trento";
				spClient.postBeanToRemoteServer(faresZoneList, server);

		  } catch (Exception e) {
		  	e.printStackTrace();
		  }
		}
}
