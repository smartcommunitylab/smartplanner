package sc.app.smartadapter.ingestion;

import java.lang.reflect.Field;
import java.util.Map;

import sc.app.smartadapter.restful.client.RemoteBean;
import sc.app.smartadapter.restful.client.SmartPlannerClient;

public abstract class MetroParcoIngestionAbs implements MetroParcoIngestion{

	protected static boolean checkFilter(Map<String, String> inputFilter, RemoteBean remoteBean) {
		boolean isFiltered = true;
		Class<?> clazz = remoteBean.getClass();
		Field field = null;

		for (String fieldName : inputFilter.keySet()) {
			try {
				field = clazz.getDeclaredField(fieldName);
				field.setAccessible(true);
				String value = (String) field.get(remoteBean);
				
				if(!value.equals(inputFilter.get(fieldName))){
					isFiltered = false;
				}

			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}		
		
		return isFiltered;
	}
}
