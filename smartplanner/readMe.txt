##########
1. Install
##########

- install java 7.

- install mongo db.

- download otp distribution from <>

- extract distribution and set OTP_HOME environment variable, pointing to the root
  folder of it. (for e.g. $OTP_HOME=C:/otp/)
  
  For Developers
  --------------
  
- in order to setup the environment within eclipse check out the project from <>

- create eclipse launch configuration for main class (SmartPlanner.java)

- set environment variables
	OTP_HOME=<path to otp distribution>
	ROUTERS=<list of routers> for e.g $ROUTERS=trentino,bologna

- start the otp instance from OTP_HOME
	 java -Duser.timezone=Europe/Rome -jar otp.jar --basePath . --graphs . --port 7575 --securePort 8585 --server --router trentino --router bologna

- check that router configuration is aligned with started instance of otp. Open $OTP_HOME/<router>/config/<router>.yml file
	otpEndpoint: http://127.0.0.1:7575/otp/routers/
  check properties like (otpEndpoint, router etc) inside corresponding router configuration yaml file.
  	 
- run/debug the eclipse launch configuration
  
  
################
2. Configuration
################

2.1 Cache & FileSystem Level
----------------------------
File system distribution at the level of routerId for e.g. trentino, bologna with all the the cache file/folder inside.
	  
	  ${OTP_HOME}
		|
		|- bin\
		|- lib\
		|
		|-trentino
		| |- config
		| |	 `- trentino.yml
		| |- cache
		| |  |- areainfo
		| |  |- bike
		| |  |- car
		| |  |- client
		| |  |- schedules
		| |  |- stops
		| |  |- taxi
		| |  `- trips
		| `- Graph.obj
		|
		.
		.
		.
		`-bologna
		  |- config
		  	 `- bologna.yml
		  |- cache
		  |  |- areainfo
		  |  |- bike
		  |  |- car
		  |  |- client
		  |  |- schedules
		  |  |- stops
		  |  |- taxi
		  |  `- trips
		    `- Graph.obj 
		  
	
2.2 Area info (Cost, Data, Points)
----------------------------------
For each region it is need to provide set of three files(cost, data, points).

- Cost file contains cost data.

"Arancio": {
		"fixedCost": "0.4",
		"costDefinition": "€. 0,40/ora  (sosta massima di 2 ore) "
	},
	"Gialla": {
		"fixedCost": "0.5",
		"costDefinition": "€. 0,50/ora "
	},
	"Rossa": {
		"fixedCost": "1.0",
		"costDefinition": "€. 1,00/ora; (dalla 3° ora) €. 1,50/ora"
	}
	
- Data file contains information about probable search time required to find parking corresponding to schedule(day, week etc).
for different areas in the region.

"CENTRO_A": {
		"searchTime": {
			"dayMap": {
				"1": [{
					"min": 1,
					"max": 3
				},
				....
				
- Points file give information about the location of parking spaces.

{
	"id": "Zotti _ 02",
	"data": null,
	"regionId": null,
	"areaId": "B. SACCO-EST_A",
	"position": [45.88591938719588,
	11.024333114534665],
	"costZoneId": null,
	"costData": null
},			

For reference check in the distribution '..trentino/cache/areainfo' folder.


2.3 Car/Bike station configuration.
-----------------------------------
Within config folder exist .yml file with router specific configuration. One such configuration is bike/car stations.

<agencyId> of xml file is the key in configuration file.

YML
---

carParking:
  COMUNE_DI_TRENTO:
    agencyId: comune
    specificProperties:
      validity: 3
      notification-threshold: 5
    filePath: /cache/car/cp-comune_di_trento.json
 
data is written in mongo collection like

{
    "_id" : "via Roggia Grande,16 - Trento@COMUNE_DI_TRENTO",
    "_class" : "it.sayservice.platform.smartplanner.model.CarStation",
    "stationId" : {
        "_id" : "via Roggia Grande,16 - Trento",
        "agencyId" : "COMUNE_DI_TRENTO"
    },
    "type" : "CAR-PARKING",
    "fullName" : "via Roggia Grande,16 - Trento",
    "location" : [ 
        46.0676, 
        11.1247
    ],
    "availableSharingVehicles" : -1,
    "posts" : 100,
    "monitored" : false,
    "parkAndRide" : false,
    "fixedCost" : "2.80",
    "costDefinition" : "Tariffa : 2,80 € /ora (prima ora) – 2,20€ prima mezz'ora (max 19 €/giorno)"
}

The source xml/json file are to be provided inside car/bike folder.

JSON
----
 
 {
  "id" : "via Roggia Grande,16 - Trento",
  "type" : "CAR-PARKING",
  "stationId" : {
    "id" : "via Roggia Grande,16 - Trento",
    "extra" : null,
    "agencyId" : "COMUNE_DI_TRENTO"
 }
  
For reference check in the distribution '..trentino/cache/bike|car' folder.


2.4 GTFS files.
---------------
GTFS configuration is distributed at agency level. For each agency it is required to provide following files.
The files are to be place inside 'trip', 'stop' and 'schedule' folders as described below.

- trips.txt
- calendar_dates
- calendar.txt
- stop_times.txt
- stop.txt

Note:  If calendar_dates.txt includes ALL dates of service, this file may be specified instead of calendar.txt.

The trips.txt and stops.txt files are to be placed inside corresponding 'trip' and 'stop' folders, while trips.txt, stop_times.txt, and
the two calendar(txt) files are to be place inside schedule folder.


2.4 Graph
---------
Graph.obj is graphical representation of network of paths and given the input constraints invoke routing algorithm to find the best path.
The trip planner uses data from many sources: transit data from GTFS, street data from OpenStreetMap or shapefiles, and elevation data 
from the National Elevation Dataset or Geographical model. Each data source has its own configuration options.

For each router, it is required to provide the graph.obj file on router root for e.g. /trentino.
 
More information about how to build graph for router, please see [http://docs.opentripplanner.org/en/latest/Basic-Usage/] 


#########
3. DEPLOY
#########

copy the smart-planner.jar file created inside target folder to OTP_HOME/lib folder

######
4. RUN
######

- start mongodb using command
	mongod
	
- startup the application by running from $OTP_HOME
  
  sh bin\start.sh
  
- one can test the APIs by browsing to <http://localhost:{port}/swagger-ui.html>
	
	Note: port is defined in the start scripts.