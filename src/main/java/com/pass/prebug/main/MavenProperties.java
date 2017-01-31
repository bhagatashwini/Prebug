package com.pass.prebug.main;

import java.util.Map;

import com.pass.prebug.input.AppProperties;

public class MavenProperties extends AppProperties {
	
	private Map<String, String> propMap; 

	public MavenProperties(Map<String, String> paramMap) {
		propMap = paramMap;
	}

	@Override
	public String getProperty(String propName, String defaultValue) {
		return propMap.getOrDefault(propName, defaultValue);
	}

	@Override
	public String getProperty(String propName) {
		return propMap.get(propName);
	}

}
