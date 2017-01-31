package com.pass.prebug.input;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.pass.prebug.hibernate.database.CommitFiles;

public class LocalProperties extends AppProperties {
	private static final Logger log = LoggerFactory.getLogger(LocalProperties.class);
    static Properties properties;
	
	/* (non-Javadoc)
	 * @see com.pass.prebug.input.AppProperties#getProperty(java.lang.String, java.lang.String)
	 */
    @Override
	public String getProperty(String propName, String defaultValue)  {
		if(properties == null) {
			readProperties();
		}
		return properties.getProperty(propName, defaultValue);
	}

	/* (non-Javadoc)
	 * @see com.pass.prebug.input.AppProperties#getProperty(java.lang.String)
	 */
    @Override
	public String getProperty(String propName)  {
		return getProperty(propName, "");
	}

	private void readProperties() {
		properties = new Properties();
		try (InputStream in = getClass().getResourceAsStream("/App.properties")){
			properties.load(in);
		} catch (IOException e) {
			// Give Message "Property File was not found.Defualt properties will be applied"
			log.error("Property File was not found.Defualt properties will be applied");
		}
	}

}
