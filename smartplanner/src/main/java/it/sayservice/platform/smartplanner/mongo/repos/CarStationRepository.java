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

package it.sayservice.platform.smartplanner.mongo.repos;

import java.util.List;

import org.springframework.data.geo.Box;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import it.sayservice.platform.smartplanner.model.CarStation;

public interface CarStationRepository extends MongoRepository<CarStation, String> {

	List<CarStation> findByLocationNear(Point p, Distance d);

	List<CarStation> findByPositionWithin(Circle c);

	List<CarStation> findByPositionWithin(Box b);

	@Query("{ fullName: ?0 }")
	List<CarStation> findByTheUsersFullName(String fullName);

	List<CarStation> findByType(String type);

	List<CarStation> deleteByType(String type);

	@Query("{ 'stationId.agencyId' : ?0, type : ?1 }")
	List<CarStation> findByAgencyIdAndType(String agencyId, String type);

}
