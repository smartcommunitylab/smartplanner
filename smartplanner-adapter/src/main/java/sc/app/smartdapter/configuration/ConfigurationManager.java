package sc.app.smartdapter.configuration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;

public class ConfigurationManager {

	public static List<RemoteBeanConfiguration> remoteBeanConfiguration;
	public static Map<String, ClassAdapterConfiguration> classAdapterConfigurationMap;

	public final static File classAdapterConfFile = new File("src/main/resources/class_adapter_configuration.json");

	public final static File configurationFile = new File(System.getenv("OTP_HOME")
			+ System.getProperty("file.separator") + "adapter-config.json");

	public static void setRemoteBeanConfiguration(){
		ObjectMapper mapper = new ObjectMapper();
    TypeFactory typeFactory = TypeFactory.defaultInstance();

		try {
			remoteBeanConfiguration = mapper.readValue(configurationFile, typeFactory.constructCollectionType(ArrayList.class,RemoteBeanConfiguration.class));
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static List<RemoteBeanConfiguration> getRemoteBeanConfiguration() {
		if (remoteBeanConfiguration != null){
			return remoteBeanConfiguration;
		}

		setRemoteBeanConfiguration();

		return remoteBeanConfiguration;
	}

	public static void setClassAdapterConfiguration(){
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<HashMap<String,ClassAdapterConfiguration>> typeReference = new TypeReference<HashMap<String,ClassAdapterConfiguration>>() {};

		try {
			classAdapterConfigurationMap = mapper.readValue(classAdapterConfFile, typeReference);
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Map<String, ClassAdapterConfiguration> getClassAdapterConfiguration() {
		if (classAdapterConfigurationMap != null){
			return classAdapterConfigurationMap;
		}

		setClassAdapterConfiguration();

		return classAdapterConfigurationMap;
	}

	public static ClassAdapterConfiguration getClassAdapterConfigurationById(String id) {
		Map<String, ClassAdapterConfiguration> classMap = getClassAdapterConfiguration();

		ClassAdapterConfiguration classAdapterConfiguration = classMap.get(id);

		return classAdapterConfiguration;
	}
}
