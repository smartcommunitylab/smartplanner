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

package it.sayservice.platform.smartplanner.otp.schedule.sorter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

public class BucketSet {

	private List<Bucket> buckets;
	private Multimap<String, Bucket> bucketsMap;

	public BucketSet() {
		buckets = new ArrayList<Bucket>();
		updateMap();
	}

	public BucketSet(String s) {
		buckets = parse(s);
		updateMap();
	}

	public List<Bucket> getBuckets() {
		return buckets;
	}

	public void resetBuckets() {
		for (Bucket b : buckets) {
			b.setOrder(null);
		}
	}

	public void merge(String s) {
		List<Bucket> newBuckets = parse(s);
		merge(newBuckets);
	}

	public void merge(List<Bucket> newBuckets) {
		if (buckets.size() == 0) {
			buckets = newBuckets;
			updateMap();
			return;
		}

		Multimap<String, Bucket> newMap = computeMapAndDup(newBuckets);
		updateMap();
		// resetBuckets();

		List<Bucket> result = new ArrayList<Bucket>(buckets);
		// List<Bucket> result = new ArrayList<Bucket>();
		// for (Bucket b: buckets) {
		// Bucket nb = new Bucket(b);
		// result.add(nb);
		// }

		int lo = -1;
		// int bestNewOrder = -1;
		// int lastIndex = 0;
		for (Bucket nb : newBuckets) {
			// lastIndex = Math.max(nb.getOrder(),bestNewOrder);
			Bucket best = findBestBucket(nb, newMap, bucketsMap, lo);
			boolean set = false;
			if (best != null) {
				// System.out.println(best.getOrder());
				// System.out.println(nb.getOrder());
				// int newMax =
				// Math.max(Math.max(best.getOrder(),nb.getOrder()),bestNewOrder);

				// if (best.getOrder() < newMax) {
				if (result.indexOf(best) <= lo) {
					// int delta = newMax - best.getOrder();
					// best.setOrder(newMax);

					result.remove(best);
					if (lo < result.size()) {
						result.add(lo, best);
					} else {
						result.add(best);
					}

					// int indexBest = result.indexOf(best);
					// for (int i = indexBest + 1; i < result.size(); i++) {
					// result.get(i).setOrder(result.get(i).getOrder() + delta);
					// }
				}
				set = true;
				lo = result.indexOf(best);
				// bestNewOrder = best.getOrder();
			}

			if (!set) {
				if (lo != -1) {
					// nb.setOrder(bestNewOrder+1);
					result.add(lo + 1, nb);
					// for (int i = lo + 2; i < result.size(); i++) {
					// result.get(i).setOrder(result.get(i).getOrder() + 1);
					// }
					lo = lo + 1;
				} else {
					result.add(0, nb);
					lo = 0;
				}
				// bestNewOrder = nb.getOrder();
			}
		}

		// PseudoSort.sort(result);
		buckets = result;

		updateMap();
	}

	private Bucket findBestBucket(Bucket nb, Multimap<String, Bucket> newMap, Multimap<String, Bucket> oldMap,
			int lastIndex) {
		if (oldMap.containsKey(nb.getId()) && newMap.get(nb.getId()).size() == oldMap.get(nb.getId()).size()) {
			for (Bucket ob : oldMap.get(nb.getId())) {
				if (ob.getDup() == nb.getDup()) {
					return ob;
				}
			}
		}
		List<Bucket> sameId = new ArrayList<Bucket>();
		for (Bucket ob : buckets) {
			if (ob.getId().equals(nb.getId())) {
				sameId.add(ob);
			}
		}
		Bucket best = null;
		int bestValue = Integer.MAX_VALUE;

		for (Bucket ob : sameId) {
			if (nb.getOrder() == null) {
				continue;
			}
			int d = Math.abs(buckets.indexOf(ob) - lastIndex);
			if (d < bestValue) {
				bestValue = d;
				best = ob;
			}
		}

		return best;

	}

	private void updateMap() {
		bucketsMap = computeMapAndDup(buckets);
	}

	private Multimap<String, Bucket> computeMapAndDup(List<Bucket> bucks) {
		Multimap<String, Bucket> result = ArrayListMultimap.create();
		for (Bucket b : bucks) {
			result.put(b.getId(), b);
		}
		computeDup(result);
		return result;
	}

	private void computeDup(Multimap<String, Bucket> map) {
		for (String key : map.keySet()) {
			Collection<Bucket> bs = map.get(key);
			int dup = 1;
			for (Bucket b : bs) {
				b.setDup(dup);
				dup++;
			}
		}
	}

	private List<Bucket> parse(String s) {
		List<Bucket> result = new ArrayList<Bucket>();
		String bs[] = s.split(",");
		for (String b : bs) {
			String p[] = b.split("=");
			Bucket bucket = new Bucket();
			if (p.length == 2) {
				bucket.setId(p[0]);
				bucket.setOrder(Integer.parseInt(p[1]));
			} else {
				bucket.setId(p[0]);
			}
			result.add(bucket);
		}
		return result;
	}

	public List<String> getIds() {
		List<String> result = new ArrayList<String>();
		for (Bucket b : buckets) {
			result.add(b.getId());
		}
		return result;
	}

	@Override
	public String toString() {
		return buckets.toString();
	}

}
