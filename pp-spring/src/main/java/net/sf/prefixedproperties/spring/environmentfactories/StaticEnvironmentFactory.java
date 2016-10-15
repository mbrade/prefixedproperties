package net.sf.prefixedproperties.spring.environmentfactories;

import net.sf.prefixedproperties.spring.EnvironmentFactory;

public class StaticEnvironmentFactory implements EnvironmentFactory {

	private String environment;
	
	@Override
	public String getEnvironment() {
		return environment;
	}

	public void setEnvironment(String environment) {
		this.environment = environment;
	}

}
