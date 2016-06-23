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
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public class CostDataJSONProcessor implements CostDataProcessor {

	@SuppressWarnings("unchecked")
	@Override
	public Map<String, CostData> read(InputStream is) throws Exception {
		ObjectMapper om = new ObjectMapper();
		Map<String, CostData> result = new HashMap<String, CostData>();
		Map<String, Object> readValue = om.readValue(is, Map.class);
		for (String key : readValue.keySet()) {
			result.put(key, om.convertValue(readValue.get(key), CostData.class));
		}
		return result;
	}

}
