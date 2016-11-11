package sc.app.smartadapter.configuration;

import java.lang.reflect.Field;
import java.util.Map;

public class RemoteBeanConfiguration {
	String remote_bean_id;
	String remote_url;
	String sp_url;
	boolean rateInfo;
	boolean searchTime;

	Map<String, String> input_filters;

	public String getRemote_bean_id() {
		return remote_bean_id;
	}

	public void setRemote_bean_id(String remote_bean_id) {
		this.remote_bean_id = remote_bean_id;
	}

	public String getRemote_url() {
		return remote_url;
	}

	public void setRemote_url(String remote_url) {
		this.remote_url = remote_url;
	}

	public String getSp_url() {
		return sp_url;
	}

	public void setSp_url(String sp_url) {
		this.sp_url = sp_url;
	}

	public Map<String, String> getInput_filters() {
		return input_filters;
	}

	public void setInput_filters(Map<String, String> input_filters) {
		this.input_filters = input_filters;
	}

	public boolean isRateInfo() {
		return rateInfo;
	}

	public void setRateInfo(boolean rateInfo) {
		this.rateInfo = rateInfo;
	}

	public boolean isSearchTime() {
		return searchTime;
	}

	public void setSearchTime(boolean searchTime) {
		this.searchTime = searchTime;
	}

	@Override
	public String toString() {
		String filterMapString = "[";
		for (String filterKey : input_filters.keySet()) {
			filterMapString += "key=" + filterKey + ",value=" + input_filters.get(filterKey) + ";";
		}
		filterMapString += "]";

		return "RemoteBeanConfiguration [remote_bean_id=" + remote_bean_id + ", remote_url=" + remote_url + ", sp_url=" + sp_url + ", rateInfo=" + rateInfo + ", searchTime=" + searchTime + ", inputFilter=" + filterMapString + "]";
	}

	public static void main(String[] args) {
		RemoteBeanConfiguration rbc = new RemoteBeanConfiguration();
		rbc.setRemote_url("rmturl");
		Class<?> clazz = rbc.getClass();
		Field field = null;
		try {
			field = clazz.getDeclaredField("remote_url");
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		try {
			System.out.println(field.get(rbc));
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}