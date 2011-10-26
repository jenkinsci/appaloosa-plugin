package com.appaloosastore.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class AppaloosaErrors {
	public List<String> errors = new ArrayList<String>();

	public static AppaloosaErrors createFromJson(String json) throws JsonParseException, JsonMappingException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.readValue(json, AppaloosaErrors.class);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if ( ! errors.isEmpty()){
			for (String error : errors) {
				sb.append(error).append(",");
			}
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}
}
