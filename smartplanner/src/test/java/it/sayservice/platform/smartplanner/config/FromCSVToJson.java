package it.sayservice.platform.smartplanner.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig.Feature;

public class FromCSVToJson {
	
	public static void parseCSV(String csvFile) {
		
				//String csvFile = "country.csv";
		    
				File dir = new File("src/test/resources/");
				File files[] = dir.listFiles();
				
				ObjectMapper mapper = new ObjectMapper();
				
				BufferedReader br = null;
		    String line = "";
		    String cvsSplitBy = ",";
		    Map<String, Route> routeMap = new HashMap<String, Route>();

		    RouteList routeList = new RouteList();
		    List<RouteDescription> routeDescriptionList = new ArrayList<RouteDescription>();
		    routeList.setRouteDescriptionList(routeDescriptionList);
		    
		    Elements elements = new Elements();
		    elements.setAgencyId("TPERBO_EXTRA");
		    elements.setHasMap(true);

		    List<Route> groups = new ArrayList<Route>();
		    elements.setGroups(groups);
		    
		    try {		
		        br = new BufferedReader(new FileReader("src/test/resources/"+csvFile));
		        while ((line = br.readLine()) != null) {
		            // use comma as separator
		            String[] route = line.split(cvsSplitBy);

		            boolean newRoute = false;
		        		int pos = route[0].indexOf("_",route[0].indexOf("_")+1);		        		
		        		String routeId = route[0].substring(0,pos);
		        		
		        		Route routeById = routeMap.get(routeId);
		        		
		        		if(routeById==null || routeById.getRoutes().size() == 2){
		        			routeById = new Route();
		        			newRoute = true;
		        		}
		        			
		        		routeById.setLabel(route[2]);

		        		List<SingleRouteAR> singleRouteARList = routeById.getRoutes();
		        		
		        		if(singleRouteARList == null){

		        			singleRouteARList = new ArrayList<SingleRouteAR>();

		        		}
		        		
		        		SingleRouteAR singleRouteAR = new SingleRouteAR();

		        		singleRouteAR.setLabel(route[2]);
		        		singleRouteAR.setRouteId(route[0]);
		        		singleRouteAR.setRouteSymId(route[0]);
		        		singleRouteAR.setTitle(route[3]);
		        		
		        		System.out.println("Route [routeId= " + route[0] + " , agency=" + route[1] + ", label=" + route[2] + ", description=" + route[3] + "]");
		            
		        		RouteDescription routeDescription = new RouteDescription();
		        		
		        		String routeLongName = route[3];
								routeDescription.setRouteLongName(routeLongName);
		        		String routeShortName = route[2];
								routeDescription.setRouteShortName(routeShortName);

								RouteId routeDescriptionId = new RouteId();

								String agency = routeId.substring(routeId.indexOf("_")+1);
								routeDescriptionId.setAgency(agency);
		        		String id = route[0];
								routeDescriptionId.setId(id);
								routeDescription.setId(routeDescriptionId);
		        		
								routeDescriptionList.add(routeDescription);
								
		        		singleRouteARList.add(singleRouteAR);
		        		routeById.setRoutes(singleRouteARList);
		        		
		        		routeMap.put(routeId, routeById);

		        		if(routeById.getRoutes().size()==2){
			        		groups.add(routeById);
		        		}
		        		
		        }
		    } catch (FileNotFoundException e) {
		        e.printStackTrace();
		    } catch (IOException e) {
		        e.printStackTrace();
		    } finally {
		        if (br != null) {
		            try {
		                br.close();
		            } catch (IOException e) {
		                e.printStackTrace();
		            }
		        }
		    }
		    
        writeToJson(elements, routeList);
		
		}
	
	
	/*
	 * 
      {
          "label": "11",
          "routes": [
            {
              "routeId": "11_TPERBO_URBAN_1",
              "routeSymId": "11_TPERBO_URBAN_1",
              "title": "Istituto R. Luxemburg / Bertalia / Arcoveggio - rot. Corelli / Ponticella",
              "label": "11"
            },
            {
              "routeId": "11_TPERBO_URBAN_0",
              "routeSymId": "11_TPERBO_URBAN_0",
              "title": "Istituto R. Luxemburg / Bertalia / Arcoveggio - rot. Corelli / Ponticella",
              "label": "11"
            }
          ]
        }

	 * 
	 * */
	public static void writeToJson(Elements elements, RouteList routeList){

		ObjectMapper tt_file_objectMapper = new ObjectMapper();
		
		try {
			String tt_json = tt_file_objectMapper.writeValueAsString(elements);
			System.out.println(tt_json);

			FileWriter tt_file = new FileWriter("src/test/resources/tt_file_content.json");
			tt_file.write(tt_json);
			tt_file.flush();
			tt_file.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

		ObjectMapper routes_file_objectMapper = new ObjectMapper();
		
		try {
			String routes_json = routes_file_objectMapper.writeValueAsString(routeList);
			System.out.println(routes_json);

			FileWriter routes_file = new FileWriter("src/test/resources/routes_content.json");
			routes_file.write(routes_json);
			routes_file.flush();
			routes_file.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

   }

	public static void main(String[] args) {
		FromCSVToJson.parseCSV("tperbo_sub.csv");
	}
}
