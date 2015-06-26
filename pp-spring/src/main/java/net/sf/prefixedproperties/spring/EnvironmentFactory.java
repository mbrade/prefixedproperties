package net.sf.prefixedproperties.spring;

/**
 * A factory for creating an environment string, which can be used for {@link PrefixedPropertiesPlaceholderConfigurer} or {@link PrefixedPropertyOverrideConfigurer} to set an environment.
 */
public interface EnvironmentFactory {

    /**
     * Gets the environment.
     * 
     * @return the environment
     */
    String getEnvironment();

}
