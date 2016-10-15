package net.sf.prefixedproperties.spring.environmentfactories;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import net.sf.prefixedproperties.config.PrefixConfig;
import net.sf.prefixedproperties.spring.EnvironmentFactory;

public class CombinedEnvironmentFactory implements EnvironmentFactory {

	private List<EnvironmentFactory> environmentFactories = Collections.<EnvironmentFactory>emptyList();
	
	@Override
	public String getEnvironment() {
		StringBuilder environment = new StringBuilder();
		for (EnvironmentFactory environmentFactory : getEnvironmentFactories()) {
			String cur_env = environmentFactory.getEnvironment();
			if (StringUtils.isNotBlank(cur_env)){
				if (environment.length() > 0){
					environment.append(PrefixConfig.PREFIXDELIMITER);
				}
				environment.append(cur_env);
			}
		}
		return environment.toString();
	}

	public List<EnvironmentFactory> getEnvironmentFactories() {
		return environmentFactories;
	}

	public void setEnvironmentFactories(List<EnvironmentFactory> environmentFactories) {
		this.environmentFactories = environmentFactories;
	}

}
