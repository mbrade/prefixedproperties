package net.sf.prefixedproperties.spring.environmentfactories;

import org.apache.commons.lang.StringUtils;

import net.sf.prefixedproperties.spring.EnvironmentFactory;


/** 
 * The environment from System.genenv() and as a fallback from System.getProperty()
 * The order can be changed by settings checkForSystemPropertyFirst to true
 */
public class SystemEnvironmentFactory implements EnvironmentFactory{

	/** The environment key name. */
	private String environmentKeyName;
	
	/** The check for system property first. */
	private boolean checkForSystemPropertyFirst = false;
	
	@Override
	public String getEnvironment() {
		String environment = null;
		if (isCheckForSystemPropertyFirst()){
			environment = System.getProperty(getEnvironmentKeyName());
		}else{
			environment = System.getenv(getEnvironmentKeyName());
		}
		if (StringUtils.isBlank(environment)){
			if (isCheckForSystemPropertyFirst()){
				environment = System.getenv(getEnvironmentKeyName());
			}else{
				environment = System.getProperty(getEnvironmentKeyName());
			}
		}
		return environment;
	}

	public String getEnvironmentKeyName() {
		return environmentKeyName;
	}

	public void setEnvironmentKeyName(String environmentKeyName) {
		this.environmentKeyName = environmentKeyName;
	}

	public boolean isCheckForSystemPropertyFirst() {
		return checkForSystemPropertyFirst;
	}

	
	/**
	 * Use this method with true to first check the System.getProperty() Method instead of System.getenv()
	 *
	 * @param checkForSystemPropertyFirst the new check for system property first
	 */
	public void setCheckForSystemPropertyFirst(boolean checkForSystemPropertyFirst) {
		this.checkForSystemPropertyFirst = checkForSystemPropertyFirst;
	}
	
 }
