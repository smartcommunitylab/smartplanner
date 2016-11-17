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

import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;

public class SearchTimeJSONProcessor implements SearchTimeProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public List<SearchTime> readList(InputStream is) throws Exception {
		ObjectMapper om = new ObjectMapper();
		List<SearchTime> searchTimeList = om.readValue(is, new TypeReference<List<SearchTime>>(){});
		return searchTimeList;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, SearchTime> read(InputStream is) throws Exception {
		ObjectMapper om = new ObjectMapper();
		return om.readValue(is, Map.class);
	}

}