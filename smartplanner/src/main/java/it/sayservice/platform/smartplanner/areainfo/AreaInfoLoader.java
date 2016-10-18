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

package it.sayservice.platform.smartplanner.areainfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.sayservice.platform.smartplanner.configurations.RouterConfig;
import it.sayservice.platform.smartplanner.model.AreaPoint;
import it.sayservice.platform.smartplanner.model.FaresZone;
import it.sayservice.platform.smartplanner.model.FaresZonePeriod;
import it.sayservice.platform.smartplanner.mongo.repos.AreaPointRepository;
import it.sayservice.platform.smartplanner.utils.Agency;
import it.sayservice.platform.smartplanner.utils.Constants;

public class AreaInfoLoader {

	private AreaPointRepository repository;
	private AreaDataJSONProcessor dataProcessor = new AreaDataJSONProcessor();
	private AreaPointJSONProcessor pointProcessor = new AreaPointJSONProcessor();
	private CostDataJSONProcessor costProcessor = new CostDataJSONProcessor();

	public AreaInfoLoader(AreaPointRepository repository) {
		super();
		this.repository = repository;
	}

	public void loadData(RouterConfig routerConfig) throws Exception {
		String path = System.getenv("OTP_HOME") + System.getProperty("file.separator") + routerConfig.getRouter()
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ Constants.AREA_CACHE_DIR + System.getProperty("file.separator");
		File dir = new File(path);
		if (dir == null || !dir.isDirectory())
			throw new FileNotFoundException("areainfo");

		repository.deleteAll();

		if (routerConfig.getAreaData() != null && !routerConfig.getAreaData().isEmpty()) {
			for (String key : routerConfig.getAreaData().keySet()) {
				loadRegionPoints(routerConfig.getAreaData().get(key), routerConfig.getRouter());
			}
		}
	}

	protected void loadRegionPoints(Agency agency, String router) throws Exception, FileNotFoundException {
		File pointsFile = new File(System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + agency.getPointsfilePath());
		File dataFile = new File(System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + agency.getDatafilePath());
		File costFile = new File(System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + agency.getCostfilePath());
		List<AreaData> areaDataList = dataProcessor.readList(new FileInputStream(dataFile));

		Map<String, AreaData> areaDataMap = getAreaDataMapFromAreaDataList(areaDataList);

		List<FaresZone> faresZonesList = null;

		if (costFile.exists()) {
			faresZonesList = costProcessor.readList(new FileInputStream(costFile));
		}

		Map<String, FaresZone> faresZoneMap = getFaresZoneMapFromFaresZonesList(faresZonesList);

		List<AreaPoint> points = pointProcessor.read(new FileInputStream(pointsFile));
		for (AreaPoint point : points) {
			point.setId(agency.getRegion() + Constants.AREA_SEPARATOR_KEY + point.getId());
			point.setRegionId(agency.getRegion());
			point.setData(areaDataMap.get(point.getAreaId()));
			if (faresZoneMap != null && faresZoneMap.containsKey(point.getCostZoneId())) {
				FaresZonePeriod[] fareZonePeriod = faresZoneMap.get(point.getCostZoneId()).getFaresZonePeriods();
				point.setFaresZonePeriod(fareZonePeriod);
			}
			if (repository.findOne(point.getId()) == null) {
				try {
					repository.save(point);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Map<String, FaresZone> getFaresZoneMapFromFaresZonesList(List<FaresZone> faresZonesList) {
		Map<String, FaresZone> faresZoneMap = new HashMap<String, FaresZone>();

		for (int i = 0; i < faresZonesList.size(); i++) {
			FaresZone faresZone = faresZonesList.get(i);
			faresZoneMap.put(faresZone.getCostZoneId(), faresZone);
		}

		return faresZoneMap;
	}

	private Map<String, AreaData> getAreaDataMapFromAreaDataList(List<AreaData> areaDataList) {
		Map<String, AreaData> areaDataMap = new HashMap<String, AreaData>();

		for (int i = 0; i < areaDataList.size(); i++) {
			AreaData areaData = areaDataList.get(i);
			areaDataMap.put(areaData.getCostZoneId(), areaData);
		}

		return areaDataMap;
	}

	public void loadData(RouterConfig routerConfig, String regionId) throws Exception {
		String path = System.getenv("OTP_HOME") + System.getProperty("file.separator") + routerConfig.getRouter()
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ Constants.AREA_CACHE_DIR + System.getProperty("file.separator");
		File dir = new File(path);
		if (dir == null || !dir.isDirectory())
			throw new FileNotFoundException("areainfo");

		loadRegionPoints(routerConfig.getAreaData().get("comune-" + regionId), routerConfig.getRouter());
	}

}
