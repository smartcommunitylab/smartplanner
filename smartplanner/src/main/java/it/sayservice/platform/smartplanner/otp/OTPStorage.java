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

package it.sayservice.platform.smartplanner.otp;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;

@Component
public class OTPStorage {

	MongoTemplate template;

	public OTPStorage(MongoTemplate mongo) throws UnknownHostException, MongoException {
		template = mongo;
	}

	public OTPStorage() {
	}

	public void clean(MongoTemplate mongo, String collectionName) {
		DBCollection collection = template.getCollection(collectionName);
		collection.drop();
	}

	public void store(MongoTemplate template, Object o, String collectionName) {
		ObjectMapper mapper = new ObjectMapper();
		Map<String, Object> map = mapper.convertValue(o, Map.class);
		storeMap(template, map, collectionName);
	}

	public void storeMap(MongoTemplate template, Map<String, Object> map, String collectionName) {
		DBCollection collection = template.getCollection(collectionName);
		DBObject obj = new BasicDBObject(map);
		collection.save(obj);
	}

	public Object getObjectByField(MongoTemplate template, String key, String value, String collectionName,
			Class destinationClass) {
		Object result = null;

		DBCollection collection = template.getCollection(collectionName);
		QueryBuilder qb = QueryBuilder.start(key).is(value);

		BasicDBObject dbObject = (BasicDBObject) collection.findOne(qb.get());

		if (dbObject != null) {
			dbObject.remove("_id");

			ObjectMapper mapper = new ObjectMapper();
			result = mapper.convertValue(dbObject, destinationClass);
		}

		return result;
	}

	public Object getObjectByField(MongoTemplate template, String key, String value, String collectionName,
			Class destinationClass, List<String> fieldsToRemove) {
		Object result = null;

		DBCollection collection = template.getCollection(collectionName);
		QueryBuilder qb = QueryBuilder.start(key).is(value);

		BasicDBObject dbObject = (BasicDBObject) collection.findOne(qb.get());

		if (dbObject != null) {
			dbObject.remove("_id");
			for (String toRemove : fieldsToRemove) {
				dbObject.remove(toRemove);
			}

			ObjectMapper mapper = new ObjectMapper();
			result = mapper.convertValue(dbObject, destinationClass);
		}

		return result;
	}

	public Object getObjectByFields(MongoTemplate template, Map<String, Object> map, String collectionName,
			Class destinationClass) {
		DBCollection collection = template.getCollection(collectionName);

		QueryBuilder qb = QueryBuilder.start();
		for (String key : map.keySet()) {
			qb = qb.and(key).is(map.get(key));
		}

		BasicDBObject dbObject = (BasicDBObject) collection.findOne(qb.get());

		if (dbObject != null) {
			dbObject.remove("_id");

			ObjectMapper mapper = new ObjectMapper();
			Object result = mapper.convertValue(dbObject, destinationClass);

			return result;
		} else {
			return null;
		}
	}

	public List<Object> getObjectsByField(MongoTemplate template, String key, String value, String collectionName,
			Class destinationClass, String orderBy) {
		DBCollection collection = template.getCollection(collectionName);
		List<Object> result = new ArrayList<Object>();

		QueryBuilder qb = QueryBuilder.start(key).is(value);

		DBCursor cursor = collection.find(qb.get());
		if (orderBy != null) {
			BasicDBObject sb = new BasicDBObject();
			sb.put(orderBy, 1);
			cursor = cursor.sort(sb);
		}

		while (cursor.hasNext()) {
			BasicDBObject dbObject = (BasicDBObject) cursor.next();
			dbObject.remove("_id");

			ObjectMapper mapper = new ObjectMapper();
			Object res = mapper.convertValue(dbObject, destinationClass);
			result.add(res);
		}

		return result;
	}

	public List<Object> getObjectsByFields(MongoTemplate template, Map<String, Object> map, String collectionName,
			Class destinationClass, String orderBy) {
		DBCollection collection = template.getCollection(collectionName);
		List<Object> result = new ArrayList<Object>();

		QueryBuilder qb = QueryBuilder.start();
		for (String key : map.keySet()) {
			qb = qb.and(key).is(map.get(key));
		}

		DBCursor cursor = collection.find(qb.get());
		if (orderBy != null) {
			BasicDBObject sb = new BasicDBObject();
			sb.put(orderBy, 1);
			cursor = cursor.sort(sb);
		}

		while (cursor.hasNext()) {
			BasicDBObject dbObject = (BasicDBObject) cursor.next();
			dbObject.remove("_id");

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			Object res = mapper.convertValue(dbObject, destinationClass);
			result.add(res);
		}

		return result;
	}

	public List<Object> getObjectsByQuery(MongoTemplate template, DBObject query, String collectionName,
			Class destinationClass, String orderBy, DBObject... fields) {
		DBCollection collection = template.getCollection(collectionName);
		List<Object> result = new ArrayList<Object>();

		DBCursor cursor;

		if (fields.length == 0) {
			cursor = collection.find(query);
		} else {
			cursor = collection.find(query, fields[0]);
		}

		if (orderBy != null) {
			BasicDBObject sb = new BasicDBObject();
			sb.put(orderBy, 1);
			cursor = cursor.sort(sb);
		}

		while (cursor.hasNext()) {
			BasicDBObject dbObject = (BasicDBObject) cursor.next();
			dbObject.remove("_id");

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			Object res = mapper.convertValue(dbObject, destinationClass);
			result.add(res);
		}

		return result;
	}

	public List<Object> getPagedObjectsByQuery(MongoTemplate template, DBObject query, String collectionName,
			Class destinationClass, String orderBy, int pageSize, int pageN) {
		DBCollection collection = template.getCollection(collectionName);
		List<Object> result = new ArrayList<Object>();

		DBCursor cursor = collection.find(query).skip(pageN * pageSize).limit(pageSize);
		if (orderBy != null) {
			BasicDBObject sb = new BasicDBObject();
			sb.put(orderBy, 1);
			cursor = cursor.sort(sb);
		}

		while (cursor.hasNext()) {
			BasicDBObject dbObject = (BasicDBObject) cursor.next();
			dbObject.remove("_id");

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			Object res = mapper.convertValue(dbObject, destinationClass);
			result.add(res);
		}

		return result;
	}

	/**
	 * Spring boot version.
	 * 
	 * @param query
	 * @param collectionClass
	 * @param collectionName
	 * @param destinationClass
	 * @return
	 */
	public List getPagedObjectsByQuery(MongoTemplate template, Query query, Class collectionClass,
			String collectionName, Class destinationClass) {
		List<Object> result = new ArrayList<Object>();
		for (Object dbObject : template.find(query, collectionClass, collectionName)) {

			ObjectMapper mapper = new ObjectMapper();
			mapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			Object res = mapper.convertValue(dbObject, destinationClass);
			if (!result.contains(res)) {
				result.add(res);
			}
		}

		return result;
	}

	public List<?> getObjectsByCriteria(MongoTemplate template, Criteria criteria, String collectionName,
			Class destinationClass) {
		List<?> result = template.find(Query.query(criteria), destinationClass, collectionName);

		return result;
	}

	public void bulkDelete(MongoTemplate template, String key, Collection<Object> values, String collectionName) {
		DBCollection collection = template.getCollection(collectionName);
		QueryBuilder qb = QueryBuilder.start(key).notIn(values);

		collection.remove(qb.get());

	}

}