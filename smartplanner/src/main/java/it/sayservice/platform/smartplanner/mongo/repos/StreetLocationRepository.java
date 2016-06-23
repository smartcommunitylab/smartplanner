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

import it.sayservice.platform.smartplanner.model.StreetLocation;

/**
 * Nearby Street Location Repository.
 * 
 * @author nawazk
 * 
 */
public interface StreetLocationRepository extends MongoRepository<StreetLocation, String> {

	List<StreetLocation> findByLocationNear(Point p, Distance d);

	List<StreetLocation> findByLocationWithin(Circle c);

	List<StreetLocation> findByLocationWithin(Box b);
}