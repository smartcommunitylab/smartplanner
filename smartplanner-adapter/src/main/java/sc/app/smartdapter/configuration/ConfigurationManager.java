package sc.app.smartdapter.configuration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ConfigurationManager {

	private static Map<String, Map<String, RemoteBeanConfiguration>> remoteBeanConfiguration;
	private static Map<String, ClassAdapterConfiguration> classAdapterConfigurationMap;

	private final static String OTP_HOME = "C:\\Users\\Lele\\Lavoro\\SMARTPLANNER\\otp-distributed";
	
	public final static File classAdapterConfFile = new File("src/main/resources/class_adapter_configuration.json");

	public final static File configurationFile = new File(System.getenv("OTP_HOME")
			+ System.getProperty("file.separator") + "adapter-config.json");

	private final static File fixedConfigurationFile = new File(OTP_HOME
			+ System.getProperty("file.separator") + "adapter-config.json");

	public static void setRemoteBeanConfiguration(){
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<HashMap<String,HashMap<String,RemoteBeanConfiguration>>> typeReference = new TypeReference<HashMap<String,HashMap<String,RemoteBeanConfiguration>>>() {};

		
		try {
			if(System.getenv("OTP_HOME")!=null){
				remoteBeanConfiguration = mapper.readValue(configurationFile, typeReference);
			}else{
				remoteBeanConfiguration = mapper.readValue(fixedConfigurationFile, typeReference);
			}
			
		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static Map<String, Map<String, RemoteBeanConfiguration>> getRemoteBeanConfiguration() {
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
