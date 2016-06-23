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

package it.sayservice.platform.smartplanner.cache;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.jackson.map.ObjectMapper;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import it.sayservice.platform.smartplanner.data.message.cache.CacheUpdateResponse;
import it.sayservice.platform.smartplanner.data.message.cache.CompressedCalendar;
import it.sayservice.platform.smartplanner.utils.Constants;

public class AgencyCacheIndex {

	private String agencyId;
	private long version;
	private Map<String, CacheIndexEntry> entries;

	public AgencyCacheIndex() {
	}

	public AgencyCacheIndex(String agencyId) {
		this.agencyId = agencyId;
		version = 0;
		entries = new TreeMap<String, CacheIndexEntry>();
	}

	public void update(Collection<String> written, Collection<String> modified, Collection<String> deleted) {
		version++;
		List<CacheIndexEntry> added = new ArrayList<CacheIndexEntry>();

		for (String id : entries.keySet()) {
			if (!written.contains(id)) {
				CacheIndexEntry oldCie = entries.get(id);
				if (oldCie.getStatus() == CacheEntryStatus.ADDED) {
					oldCie.setStatus(CacheEntryStatus.REMOVED);
					oldCie.setVersion(version);
				}
			}
		}

		if (written != null) {
			for (String id : written) {
				if (modified != null && modified.contains(id)) {
					continue;
				}
				if (!entries.containsKey(id)) {
					CacheIndexEntry cie = new CacheIndexEntry(id, version, CacheEntryStatus.ADDED);
					added.add(cie);
				} else {
					CacheIndexEntry oldCie = entries.get(id);
					if (oldCie.getStatus() == CacheEntryStatus.REMOVED) {
						oldCie.setStatus(CacheEntryStatus.ADDED);
						oldCie.setVersion(version);
					}
				}
			}
		}

		if (modified != null) {
			for (String id : modified) {
				if (!entries.containsKey(id)) {
					CacheIndexEntry cie = new CacheIndexEntry(id, version, CacheEntryStatus.ADDED);
					added.add(cie);
				} else {
					CacheIndexEntry oldCie = entries.get(id);
					if (oldCie.getStatus() == CacheEntryStatus.REMOVED) {
						oldCie.setStatus(CacheEntryStatus.ADDED);
					}
					oldCie.setVersion(version);
				}
			}
		}

		if (deleted != null) {
			for (String id : deleted) {
				if (!entries.containsKey(id)) {
					System.err.println("Deleting an unexisting item: " + id);
					CacheIndexEntry cie = new CacheIndexEntry(id, version, CacheEntryStatus.REMOVED);
					added.add(cie);
				} else {
					CacheIndexEntry oldCie = entries.get(id);
					if (oldCie.getStatus() == CacheEntryStatus.ADDED) {
						oldCie.setStatus(CacheEntryStatus.REMOVED);
						oldCie.setVersion(version);
					}
				}
			}
		}

		for (CacheIndexEntry cie : added) {
			entries.put(cie.getId(), cie);
		}

	}

	protected CacheUpdateResponse getUpdate(String router, long fromVersion) throws IOException {
		List<CacheIndexEntry> cacheEntries = new ArrayList<CacheIndexEntry>();
		for (CacheIndexEntry entry : entries.values()) {
			if (entry.getVersion() > fromVersion) {
				cacheEntries.add(entry);
			}
		}

		CacheUpdateResponse response = new CacheUpdateResponse();
		Map<String, Boolean> newCalendars = new TreeMap<String, Boolean>();

		for (CacheIndexEntry entry : cacheEntries) {
			if (entry.getStatus() == CacheEntryStatus.ADDED) {
				// if ("calendar".equals(entry.getId())) {
				if (entry.getId().startsWith("calendar_")) {
					newCalendars.put(entry.getId(), true);
				} else {
					response.getAdded().add(entry.getId());
				}
			} else {
				response.getRemoved().add(entry.getId());
			}
		}

		response.setVersion(version);
		for (String cal : newCalendars.keySet()) {
			response.getCalendars().put(cal, loadCalendar(router, cal));
		}

		return response;
	}

	public CompressedCalendar loadCalendar(String router, String cal) throws IOException {
		String d = System.getenv("OTP_HOME") + System.getProperty("file.separator") + router
				+ System.getProperty("file.separator") + Constants.CACHE_DIR + System.getProperty("file.separator")
				+ Constants.CLIENT_CACHE_DIR + System.getProperty("file.separator") + agencyId;
		String fn = d + "/" + cal + ".js";
		Map<String, String> result = new TreeMap<String, String>();

		FileReader fr = new FileReader(fn);

		ObjectMapper mapper = new ObjectMapper();
		result = mapper.readValue(fr, Map.class);

		CompressedCalendar ccal = compressCalendar(result);

		return ccal;
	}

	private CompressedCalendar compressCalendar(Map<String, String> cal) {
		CompressedCalendar ccal = new CompressedCalendar();
		int i = 0;
		Map<String, String> days = new TreeMap<String, String>();
		BiMap<String, String> map = HashBiMap.create();

		for (String key : cal.keySet()) {
			String value = cal.get(key);
			if (!map.containsKey(value)) {
				map.put(value, "" + i++);
			}
			days.put(key, map.get(value));
		}

		ccal.setEntries(days);
		ccal.setMapping(map.inverse());

		return ccal;
	}

	public String getAgencyId() {
		return agencyId;
	}

	public void setAgencyId(String agencyId) {
		this.agencyId = agencyId;
	}

	public long getVersion() {
		return version;
	}

	public void setVersion(long version) {
		this.version = version;
	}

	public Map<String, CacheIndexEntry> getEntries() {
		return entries;
	}

	public void setEntries(Map<String, CacheIndexEntry> entries) {
		this.entries = entries;
	}

	@Override
	public String toString() {
		return agencyId + ":" + version + "=" + entries.values().toString();
	}

}
