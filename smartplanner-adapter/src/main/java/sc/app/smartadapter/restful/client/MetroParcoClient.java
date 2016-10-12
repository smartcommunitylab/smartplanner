package sc.app.smartadapter.restful.client;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import sc.app.smartadapter.beans.MetroParcoBikePoint;
import sc.app.smartadapter.beans.MetroParcoStreet;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class MetroParcoClient<T> {

	public List<T> getBeanFromRestFullServer(String server, Class clazz){

		ObjectMapper mapper = new ObjectMapper();

		List<T> beanList = null;
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

		MetroParcoClient<MetroParcoStreet> mpBikeStation = new MetroParcoClient<MetroParcoStreet>();

		List<MetroParcoStreet> beanList = new ArrayList<MetroParcoStreet>();

		beanList = mpBikeStation.getBeanFromRestFullServer("https://tn.smartcommunitylab.it/metroparco/rest/nosec/tn/street", MetroParcoStreet.class);

		for (int i = 0; i < beanList.size(); i++) {
			MetroParcoStreet bean = beanList.get(i);
			System.out.println("bs["+i+"]: "+bean.getId());
		}
	}
}
