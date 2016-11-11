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

import it.sayservice.platform.smartplanner.configurations.RouterConfig;
import it.sayservice.platform.smartplanner.model.AreaPoint;
import it.sayservice.platform.smartplanner.model.FaresData;
import it.sayservice.platform.smartplanner.mongo.repos.AreaPointRepository;
import it.sayservice.platform.smartplanner.utils.Agency;
import it.sayservice.platform.smartplanner.utils.Constants;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AreaInfoLoader {

	private AreaPointRepository repository;
	private SearchTimeJSONProcessor searchTimeProcessor = new SearchTimeJSONProcessor();
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
		File searchTimeFile = new File(System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + agency.getDatafilePath());
		File costFile = new File(System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + agency.getCostfilePath());
		
		List<SearchTime> searchTimeList = null;
		
		if (searchTimeFile.exists()) {
			searchTimeList = searchTimeProcessor.readList(new FileInputStream(searchTimeFile));
		}
		Map<String, SearchTime> searchTimeMap = getSearchTimeMapFromSearchTimeList(searchTimeList);

		List<FaresData> faresZonesList = null;

		if (costFile.exists()) {
			faresZonesList = costProcessor.readList(new FileInputStream(costFile));
		}

		Map<String, FaresData> faresDataMap = getFaresDataMapFromFaresZonesList(faresZonesList);

		List<AreaPoint> points = pointProcessor.read(new FileInputStream(pointsFile));
		for (AreaPoint point : points) {
			point.setId(agency.getRegion() + Constants.AREA_SEPARATOR_KEY + point.getId());
			point.setRegionId(agency.getRegion());
			
			AreaData areaData = new AreaData();
			
			if(point.getData()!=null && point.getData().getSearchTime()!=null &&
				 point.getData().getSearchTime().getSearchAreaId()!=null &&
				 searchTimeMap.get(point.getData().getSearchTime().getSearchAreaId())!=null){
				SearchTime searchTimeMapped = searchTimeMap.get(point.getData().getSearchTime().getSearchAreaId());
				areaData.setSearchTime(searchTimeMapped);
			}

			if(point.getData()!=null && point.getData().getFares()!=null &&
				 point.getData().getFares().getCostZoneId()!=null &&
				 faresDataMap.get(point.getData().getFares().getCostZoneId())!=null){
				 FaresData faresDataMapped = faresDataMap.get(point.getData().getFares().getCostZoneId());
				 areaData.setFares(faresDataMapped);
				}

			point.setData(areaData);
			
			if (repository.findOne(point.getId()) == null) {
				try {
					repository.save(point);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	private Map<String, FaresData> getFaresDataMapFromFaresZonesList(List<FaresData> faresDataList) {
		Map<String, FaresData> faresDataMap = new HashMap<String, FaresData>();

		for (int i = 0; i < faresDataList.size(); i++) {
			FaresData faresData = faresDataList.get(i);
			faresDataMap.put(faresData.getCostZoneId(), faresData);
		}

		return faresDataMap;
	}

	private Map<String, SearchTime> getSearchTimeMapFromSearchTimeList(List<SearchTime> searchTimeList) {
		Map<String, SearchTime> searchTimeMap = new HashMap<String, SearchTime>();

		for (int i = 0; i < searchTimeList.size(); i++) {
			SearchTime searchTime = searchTimeList.get(i);
			searchTimeMap.put(searchTime.getSearchAreaId(), searchTime);
		}

		return searchTimeMap;
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
