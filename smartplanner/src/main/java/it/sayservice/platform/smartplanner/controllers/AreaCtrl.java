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

package it.sayservice.platform.smartplanner.controllers;

import it.sayservice.platform.smartplanner.areainfo.AreaInfoLoader;
import it.sayservice.platform.smartplanner.areainfo.SearchTime;
import it.sayservice.platform.smartplanner.configurations.ConfigurationManager;
import it.sayservice.platform.smartplanner.configurations.MongoRouterMapper;
import it.sayservice.platform.smartplanner.exception.SmartPlannerException;
import it.sayservice.platform.smartplanner.model.AreaPoint;
import it.sayservice.platform.smartplanner.model.FaresData;
import it.sayservice.platform.smartplanner.model.Response;
import it.sayservice.platform.smartplanner.mongo.repos.AreaPointRepository;
import it.sayservice.platform.smartplanner.utils.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
//@RequestMapping("/smart-planner")
public class AreaCtrl {

	@Autowired
	private ConfigurationManager configurationManager;
	@Autowired
	private MongoRouterMapper mongoRouterMapper;
	private AreaPointRepository areaPointRepository;

	// AREA POINTS
	@RequestMapping(method = RequestMethod.GET, value = "/{router}/rest/data/areapoints/{region}")
	public @ResponseBody List<AreaPoint> getAreaPoints(@PathVariable String region, @PathVariable String router)
			throws SmartPlannerException {

		List<AreaPoint> areaPoints = new ArrayList<AreaPoint>();

		try {
			areaPointRepository = mongoRouterMapper.getAreaPointRepository(router);
			if (areaPointRepository != null) {
				areaPoints = areaPointRepository.findByRegionId(region);
			}

		} catch (Exception e) {
			throw new SmartPlannerException(e.getMessage());
		}

		return areaPoints;
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{router}/rest/data/areapoints")
	public @ResponseBody void deleteAreaPoints(@PathVariable String router) throws SmartPlannerException {

		areaPointRepository = mongoRouterMapper.getAreaPointRepository(router);

		if (areaPointRepository != null) {
			areaPointRepository.deleteAll();
			try {
				new AreaInfoLoader(areaPointRepository).loadData(configurationManager.getRouter(router));
			} catch (Exception e) {
				throw new SmartPlannerException(e.getMessage());
			}
		}
	}

	@RequestMapping(method = RequestMethod.DELETE, value = "/{router}/rest/data/areapoints/{region}")
	public @ResponseBody void deleteAreaPoints(@PathVariable String region, @PathVariable String router)
			throws SmartPlannerException {

		areaPointRepository = mongoRouterMapper.getAreaPointRepository(router);

		if (areaPointRepository != null) {
			areaPointRepository.deleteByRegionId(region);
			try {
				new AreaInfoLoader(areaPointRepository).loadData(configurationManager.getRouter(router), region);
			} catch (Exception e) {
				throw new SmartPlannerException(e.getMessage());
			}
		}
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/data/areapoints/{region}")
	public @ResponseBody void updateAreaPoints(@PathVariable String region, @PathVariable String router,
			@RequestBody List<AreaPoint> ap) {

		areaPointRepository = mongoRouterMapper.getAreaPointRepository(router);

		if (areaPointRepository != null) {
			areaPointRepository.deleteByRegionId(region);
			loadPoints(region, ap, null, null, router, null);
		}
	}

	//fare oggetto areadata con il solo campo searchtime
	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/data/searchtime/{region}")
	public @ResponseBody void updateAreaData(@PathVariable String region, @PathVariable String router,
			@RequestBody List<SearchTime> searchTimeZoneList) {
		Map<String, SearchTime> searchTimeMap = getSearchTimeZoneListMapByCostZoneList(searchTimeZoneList);
		loadPoints(region, null, searchTimeMap, null, router, null);
	}

	private Map<String, SearchTime> getSearchTimeZoneListMapByCostZoneList(List<SearchTime> searchTimeZoneList) {
		Map<String, SearchTime> searchTimeMap = new HashMap<String, SearchTime>();
		for (int i = 0; i < searchTimeZoneList.size(); i++) {
			SearchTime searchTimeNotMapped = searchTimeZoneList.get(i);
			SearchTime searchTimeMapped = searchTimeMap.get(searchTimeNotMapped.getSearchAreaId());

			if (searchTimeMapped==null){
				searchTimeMap.put(searchTimeNotMapped.getSearchAreaId(), searchTimeNotMapped);
			}
		}

		return searchTimeMap;
	}

	@RequestMapping(method = RequestMethod.POST, value = "/{router}/rest/data/fares/{region}")
	public @ResponseBody void updateAreaCosts(@PathVariable String region, @PathVariable String router,
			@RequestBody List<FaresData> faresDataList) {
		Map<String, FaresData> faresPeriodListMap = getFaresDataMapByFaresDataList(faresDataList);
		loadPoints(region, null, null, faresPeriodListMap, router, null);
	}

	private Map<String, FaresData> getFaresDataMapByFaresDataList(List<FaresData> faresDataList) {
		Map<String, FaresData> faresDataMap = new HashMap<String, FaresData>();
		for (int i = 0; i < faresDataList.size(); i++) {
			FaresData faresDataNotMapped = faresDataList.get(i);
			FaresData faresDataMapped = faresDataMap.get(faresDataNotMapped.getCostZoneId());

			if (faresDataMapped==null){
				faresDataMap.put(faresDataNotMapped.getCostZoneId(), faresDataNotMapped);
			}

		}

		return faresDataMap;
	}


	public void loadPoints(String region, List<AreaPoint> points, Map<String, SearchTime> searchTimeMap,
		Map<String, FaresData> faresDataMap, String router, SearchTime searchTimeData) {

		List<AreaPoint> newPoints;

		AreaPointRepository areaPointRepository = mongoRouterMapper.getAreaPointRepository(router);

		if (areaPointRepository != null) {

			if (points == null) {
				newPoints = areaPointRepository.findAll();
			} else {
				newPoints = points;
			}

			for (AreaPoint point : newPoints) {
				if (points != null) {
					point.setId(region + Constants.AREA_SEPARATOR_KEY + point.getId());
					point.setRegionId(region);
				}
				if (searchTimeMap != null && point.getData()!=null && 
						point.getData().getSearchTime()!= null && point.getData().getSearchTime().getSearchAreaId()!=null && 
						searchTimeMap.containsKey(point.getData().getSearchTime().getSearchAreaId())) {
					point.getData().setSearchTime((searchTimeMap.get(point.getData().getSearchTime().getSearchAreaId())));
				}
				if (faresDataMap != null && point.getData()!=null &&
						point.getData().getFares() != null && point.getData().getFares().getCostZoneId() != null &&
								faresDataMap.containsKey(point.getData().getFares().getCostZoneId())) {
					point.getData().setFares((faresDataMap.get(point.getData().getFares().getCostZoneId())));
				}

				areaPointRepository.save(point);
			}
		}
	}

	@ExceptionHandler(Exception.class)
	public @ResponseBody Response<Void> handleExceptions(Exception exception, HttpServletResponse response) {
		Response<Void> res = exception instanceof SmartPlannerException ? ((SmartPlannerException) exception).getBody()
				: new Response<Void>(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, exception.getMessage());
		response.setStatus(res.getErrorCode());
		return res;
	}

}
