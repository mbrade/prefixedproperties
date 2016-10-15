package net.sf.prefixedproperties.spring.environmentfactories;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import net.sf.prefixedproperties.spring.EnvironmentFactory;

/**
 * A factory for delivering an environment based on a properties file and containing key.
 */
public class PropertiesFileEnvironmentFactory implements EnvironmentFactory{

	/** The Constant log. */
	private final static Logger log = LogManager.getLogger(PropertiesFileEnvironmentFactory.class);
	
	/** The file name. */
	private String fileName;
	
	/** The properties key. */
	private String propertiesKey;
	
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public String getPropertiesKey() {
		return propertiesKey;
	}
	
	public void setPropertiesKey(String propertiesKey) {
		this.propertiesKey = propertiesKey;
	}

	/* (non-Javadoc)
	 * @see net.sf.prefixedproperties.spring.EnvironmentFactory#getEnvironment()
	 */
	@Override
	public String getEnvironment() {
		File file = new File(getFileName());
		Properties properties = new Properties();
		if (file.exists() && file.canRead()){
			try(BufferedInputStream inStream = new BufferedInputStream(new FileInputStream(file))){
				properties.load(inStream);
			}catch(IOException ioe){
				log.info("Failed to load properties: "+getFileName(), ioe);
			}
		}
		String property = properties.getProperty(getPropertiesKey());
		return property;
	}
	
	
	
}
