package sc.app.smartadapter.restful.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import sc.app.smartadapter.beans.MetroParcoStreet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class MetroParcoClient {

	public static List<RemoteBean> getBeanFromRestFullServer(String server, Class clazz){

		ObjectMapper mapper = new ObjectMapper();

		List<RemoteBean> beanList = null;
    TypeFactory t = TypeFactory.defaultInstance();

		try {
			beanList = mapper.readValue(new URL(server), t.constructCollectionType(ArrayList.class,clazz));
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return beanList;
	}



	public static void main(String[] args) {

		Class beanClass = null;
		try {
			beanClass = Class.forName("sc.app.smartadapter.beans.MetroParcoStreet");
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		MetroParcoClient mpBikeStation = new MetroParcoClient();

		List<RemoteBean> beanList = new ArrayList<RemoteBean>();

		beanList = mpBikeStation.getBeanFromRestFullServer("https://tn.smartcommunitylab.it/metroparco/rest/nosec/tn/street", beanClass);

		for (int i = 0; i < beanList.size(); i++) {
			MetroParcoStreet bean = (MetroParcoStreet) beanList.get(i);
			System.out.println("bs["+i+"]: "+bean.getId());
		}
	}
}
