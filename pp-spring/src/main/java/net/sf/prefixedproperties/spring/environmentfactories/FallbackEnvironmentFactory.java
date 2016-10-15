package net.sf.prefixedproperties.spring.environmentfactories;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import net.sf.prefixedproperties.spring.EnvironmentFactory;

/**
 * Takes the first environment from the given list of EnvironmentFactories which is not null.<br/>
 * The order of the list will be the priority.<br/>
 * 
 * You could build up an cascaded setup where first you look for an property in an properties file, than for an system property and than you use a static one.
 * 
 */
public class FallbackEnvironmentFactory implements EnvironmentFactory {

	private List<EnvironmentFactory> environmentFactories = Collections.emptyList();
	
	@Override
	public String getEnvironment() {
		for (EnvironmentFactory environmentFactory : getEnvironmentFactories()) {
			String environment = environmentFactory.getEnvironment();
			if (StringUtils.isNotBlank(environment)){
				return environment;
			}
		}
		return null;
	}

	public List<EnvironmentFactory> getEnvironmentFactories() {
		return environmentFactories;
	}

	public void setEnvironmentFactories(List<EnvironmentFactory> environmentFactories) {
		this.environmentFactories = environmentFactories;
	}

}
