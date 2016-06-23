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

package it.sayservice.platform.smartplanner.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.odftoolkit.simple.SpreadsheetDocument;
import org.odftoolkit.simple.table.Row;
import org.odftoolkit.simple.table.Table;

public class ODFParser {

	public static Map<String, List<List<String>>> parseTable(String uri, int columnCount) throws Exception {
		SpreadsheetDocument td = SpreadsheetDocument.loadDocument(ODFParser.class.getResourceAsStream(uri));
		List<Table> tables = td.getTableList();
		Map<String, List<List<String>>> map = new HashMap<String, List<List<String>>>();
		for (Table table : tables) {
			List<List<String>> result = new ArrayList<List<String>>();
			for (int i = 1;; i++) {
				Row row = table.getRowByIndex(i);
				List<String> list = new ArrayList<String>();
				for (int j = 0; j < columnCount; j++) {
					list.add(row.getCellByIndex(j).getStringValue());
				}
				if (list.get(0) == null || list.get(0).isEmpty())
					break;
				result.add(list);
			}
			map.put(table.getTableName(), result);
		}
		return map;
	}
}
