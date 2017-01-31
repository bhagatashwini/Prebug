package com.pass.prebug.input;

public abstract class AppProperties {

	private static AppProperties instance;
	
	public abstract String getProperty(String propName, String defaultValue);
	public abstract String getProperty(String propName);

	public static void setInstance(AppProperties props) {
		instance = props;
	}
	public static AppProperties getInstance(){
		return instance;
	}
}