package net.sf.prefixedproperties.spring.environmentfactories;

import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;

import net.sf.prefixedproperties.config.PrefixConfig;
import net.sf.prefixedproperties.spring.EnvironmentFactory;

/**
 * Gets all profiles from the Spring environment and concatenates them with a dot.<br/>
 * Per default the default profiles are excluded.
 */
public class SpringEnvironmentAwareFactory implements EnvironmentFactory, EnvironmentAware {

	/** The reverse profiles. */
	private boolean reverseProfiles;
	
	/** The exclude default profiles. */
	private boolean includeDefaultProfiles = false;
	
	/** The environment. */
	private Environment environment;

	@Override
	public void setEnvironment(Environment environment) {
		this.environment = environment;
	}

	@Override
	public String getEnvironment() {
		String[] activeProfiles = environment.getActiveProfiles();
		if (reverseProfiles){
			ArrayUtils.reverse(activeProfiles);
		}
		if (!includeDefaultProfiles){
			List<String> profiles = new LinkedList<String>();
			String[] defaultProfiles = environment.getDefaultProfiles();
			for (String profile : activeProfiles) {
				if (!ArrayUtils.contains(defaultProfiles,  profile)){
					profiles.add(profile);
				}
			}
			activeProfiles = profiles.toArray(new String[profiles.size()]);
		}
		String profiles = StringUtils.join(activeProfiles, PrefixConfig.PREFIXDELIMITER);
		return profiles;
	}

	public boolean isExcludeDefaultProfiles() {
		return includeDefaultProfiles;
	}

	
	/**
	 * To include the default profiles use this method and set it to true.
	 * The default is false.
	 * @see Environment#getDefaultProfiles()
	 *
	 * @param includeDefaultProfiles the new exclude default profiles
	 */
	public void setIncludeDefaultProfiles(boolean includeDefaultProfiles) {
		this.includeDefaultProfiles = includeDefaultProfiles;
	}

	public boolean isReverseProfiles() {
		return reverseProfiles;
	}

	
	/**
	 * To set the environment reverse the profiles list, call this method with true.
	 * Default is false
	 *
	 * @param reverseProfiles the new reverse profiles
	 */
	public void setReverseProfiles(boolean reverseProfiles) {
		this.reverseProfiles = reverseProfiles;
	}
	

}
