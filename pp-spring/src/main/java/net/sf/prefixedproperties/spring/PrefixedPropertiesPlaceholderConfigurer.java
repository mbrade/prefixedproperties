/*
 *
 * Copyright (c) 2010, Marco Brade
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * * Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.sf.prefixedproperties.spring;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.sf.prefixedproperties.PrefixedProperties;
import net.sf.prefixedproperties.config.PrefixConfig;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.PropertyPlaceholderHelper;

/**
 * Works the same as {@link PropertyPlaceholderConfigurer} with the advantage to filter properties by using a specific environment. Mainly it is enough to set the current environment by using
 * {@link #setDefaultPrefix(String)}<br/>
 * For special situation it is also possible that you can use {@link #setPrefixConfigs(List)} to configure the known prefixes.<br/>
 * To configure the default prefix you can use the following methods: {@link #setEnvironmentFactory(EnvironmentFactory)}<br/>
 * {@link #setDefaultPrefixSystemPropertyKey(String)}<br/>
 * {@link #setDefaultPrefix(String)}<br/>
 * The usage of the methods will be in the same order as above.
 */
@ManagedResource("prefixedproperties:name=PrefixedPropertiesPlaceholderConfigurer")
public class PrefixedPropertiesPlaceholderConfigurer extends PropertyPlaceholderConfigurer implements BeanFactoryAware, BeanNameAware, BeanPostProcessor {

    /** The locations. */
    protected Resource[] locations = null;

    /** The local override. */
    protected boolean localOverride = false;

    /** The local properties. */
    protected Properties[] localProperties = null;

    /** The file encoding. */
    protected String fileEncoding = Charset.defaultCharset().name();

    /** The ignore resource not found. */
    protected boolean ignoreResourceNotFound = false;

    /** The prefix config list. */
    protected List<PrefixConfig> prefixConfigList;

    /** The default prefix. */
    protected String defaultPrefix;

    protected PrefixedProperties myProperties = null;

    protected PrefixedPropertiesPersister persister = new PrefixedPropertiesPersister();

    protected String defaultPrefixSystemPropertyKey = null;

    protected EnvironmentFactory environmentFactory = null;

    private boolean processOtherProperties = false;

    private String placeholderPrefix;

    private String placeholderSuffix;

    private String valueSeparator;

    private boolean ignoreUnresolvablePlaceholders;

    private boolean mixDefaultAndLocalPrefixConfigurations = false;

    /**
     * Creates the prefixed properties.
     * 
     * @return the prefixed properties
     */
    protected synchronized PrefixedProperties createProperties() {
	if (myProperties == null) {
	    PrefixedProperties resultProperties = null;
	    String environment = defaultPrefix;
	    if (environmentFactory != null) {
		environment = environmentFactory.getEnvironment();
	    } else if (defaultPrefixSystemPropertyKey != null) {
		environment = System.getProperty(defaultPrefixSystemPropertyKey);
		if (environment == null) {
		    if (logger.isWarnEnabled()) {
			logger.warn(String.format("Didn't found system property key to set default prefix: %1s", defaultPrefixSystemPropertyKey));
		    }
		}
	    }
	    if (prefixConfigList != null) {
		resultProperties = PrefixedProperties.createCascadingPrefixProperties(prefixConfigList);
	    } else {
		if (environment != null) {
		    resultProperties = PrefixedProperties.createCascadingPrefixProperties(environment);
		} else {
		    resultProperties = new PrefixedProperties();
		}
	    }
	    resultProperties.setDefaultPrefix(environment);
	    if (logger.isInfoEnabled()) {
		logger.info(String.format("Setting default prefix to: %1s", environment));
	    }
	    resultProperties.setMixDefaultAndLocalPrefixSettings(mixDefaultAndLocalPrefixConfigurations);
	    myProperties = resultProperties;

	}
	return myProperties;
    }

    /**
     * Gets the effective properties.
     * 
     * @return the effective properties
     */
    @ManagedAttribute()
    public List<String> getEffectiveProperties() {
	final List<String> properties = new LinkedList<String>();
	for (final String key : myProperties.stringPropertyNames()) {
	    properties.add(key + "=" + myProperties.get(key));
	}
	return properties;
    }

    /**
     * Gets the properties.
     * 
     * @return the properties
     */
    public PrefixedProperties getPrefixedProperties() {
	return myProperties;
    }

    /**
     * Gets the process properties.
     * 
     * @return the process properties
     */
    public boolean getProcessOtherProperties() {
	return this.processOtherProperties;
    }

    public boolean isMixDefaultAndLocalPrefixConfigurations() {
	return mixDefaultAndLocalPrefixConfigurations;
    }

    @Override
    protected void loadProperties(final Properties props) throws IOException {
	if (locations != null) {
	    for (int i = 0; i < locations.length; i++) {
		final Resource location = locations[i];
		if (logger.isInfoEnabled()) {
		    logger.info("Loading properties file from " + location);
		}
		File file = null;
		InputStream is = null;
		try {
		    try {
			file = location.getFile();
			is = new BufferedInputStream(new FileInputStream(file));
		    } catch (final IOException ie) {//ignore
		    } finally {
			if (file == null) {
			    is = location.getInputStream();
			}
		    }

		    if (location.getFilename().toLowerCase().endsWith(Constants.XML_FILE_EXTENSION)) {
			persister.loadFromXml(props, is);
		    } else if (location.getFilename().toLowerCase().endsWith(Constants.JSON_FILE_EXTENSION)) {
			if (fileEncoding != null) {
			    persister.loadFromJson(props, new InputStreamReader(is, Charset.forName(fileEncoding)));
			} else {
			    persister.loadFromJson(props, is);
			}
		    } else {
			if (fileEncoding != null) {
			    persister.load(props, new InputStreamReader(is, Charset.forName(fileEncoding)));
			} else {
			    persister.load(props, is);
			}
		    }
		} catch (final IOException ex) {
		    if (ignoreResourceNotFound) {
			if (logger.isWarnEnabled()) {
			    logger.warn("Could not load properties from " + location + ": " + ex.getMessage());
			}
		    } else {
			throw ex;
		    }
		} finally {
		    if (is != null) {
			is.close();
		    }
		}
	    }
	}
    }

    /* (non-Javadoc)
     * @see org.springframework.core.io.support.PropertiesLoaderSupport#mergeProperties()
     */
    @Override
    protected Properties mergeProperties() throws IOException {

	final PrefixedProperties myProperties = createProperties();

	if (localOverride) {
	    // Load properties from file upfront, to let local properties override.
	    loadProperties(myProperties);
	}
	if (localProperties != null) {
	    for (int i = 0; i < localProperties.length; i++) {
		final Properties props = localProperties[i];
		if (props != null) {
		    for (final Enumeration<Object> en = props.keys(); en.hasMoreElements();) {
			final Object key = en.nextElement();
			myProperties.put(key, props.get(key));
		    }
		}
	    }
	}
	if (!localOverride) {
	    // Load properties from file afterwards, to let those properties override.
	    loadProperties(myProperties);
	}

	return myProperties;
    }

    /* (non-Javadoc)
     * @see org.springframework.beans.factory.config.BeanPostProcessor#postProcessAfterInitialization(java.lang.Object, java.lang.String)
     */
    @Override
    public Object postProcessAfterInitialization(final Object bean, final String beanName) throws BeansException {
	if (bean instanceof Properties && bean != getPrefixedProperties() && getProcessOtherProperties()) {
	    final Properties props = (Properties) bean;
	    final PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
	    final PrefixedProperties clone = getPrefixedProperties().clone();
	    clone.putAll(props);
	    final Properties beanProperty = (Properties) bean;
	    for (final Object key : beanProperty.keySet()) {
		if (key instanceof String) {
		    final String stringKey = (String) key;
		    props.setProperty(stringKey, helper.replacePlaceholders(beanProperty.getProperty(stringKey), clone));
		}
	    }
	}
	return bean;
    }

    @Override
    public Object postProcessBeforeInitialization(final Object bean, final String beanName) throws BeansException {
	return bean;
    }

    /**
     * Sets the default prefix.
     * 
     * @param defaultPrefix
     *            the new default prefix
     */
    public void setDefaultPrefix(final String defaultPrefix) {
	this.defaultPrefix = defaultPrefix;
    }

    /**
     * Sets this method to specify a system property to be used as an environment. The value of the property will be used for setting the default prefix.
     * {@link PrefixedProperties#setDefaultPrefix(String)}
     * 
     * @param defaultPrefixSystemPropertyKey
     *            the new ddefault prefix system property key
     */
    public void setDefaultPrefixSystemPropertyKey(final String defaultPrefixSystemPropertyKey) {
	this.defaultPrefixSystemPropertyKey = defaultPrefixSystemPropertyKey;
    }

    /**
     * Sets the environment factory.
     * 
     * @param environmentFactory
     *            the new environment factory
     */
    public void setEnvironmentFactory(final EnvironmentFactory environmentFactory) {
	this.environmentFactory = environmentFactory;
    }

    /* (non-Javadoc)
     * @see org.springframework.core.io.support.PropertiesLoaderSupport#setFileEncoding(java.lang.String)
     */
    @Override
    public void setFileEncoding(final String encoding) {
	fileEncoding = encoding;
    }

    /* (non-Javadoc)
     * @see org.springframework.core.io.support.PropertiesLoaderSupport#setIgnoreResourceNotFound(boolean)
     */
    @Override
    public void setIgnoreResourceNotFound(final boolean ignoreResourceNotFound) {
	this.ignoreResourceNotFound = ignoreResourceNotFound;
    }

    @Override
    public void setIgnoreUnresolvablePlaceholders(final boolean ignoreUnresolvablePlaceholders) {
	this.ignoreUnresolvablePlaceholders = ignoreUnresolvablePlaceholders;
	super.setIgnoreUnresolvablePlaceholders(ignoreUnresolvablePlaceholders);
    }

    /* (non-Javadoc)
     * @see org.springframework.core.io.support.PropertiesLoaderSupport#setLocalOverride(boolean)
     */
    @Override
    public void setLocalOverride(final boolean localOverride) {
	this.localOverride = localOverride;
    }

    /**
     * Set a location of a properties file to be loaded.
     * <p>
     * Can point to a classic properties, json or XML file that follows JDK 1.5's properties XML format.
     * 
     * @param location
     *            the new location
     */
    @Override
    public void setLocation(final Resource location) {
	locations = new Resource[] { location };
    }

    /* (non-Javadoc)
     * @see org.springframework.core.io.support.PropertiesLoaderSupport#setLocations(org.springframework.core.io.Resource[])
     */
    @Override
    public void setLocations(final Resource... locations) {
	this.locations = locations;
    }

    public void setMixDefaultAndLocalPrefixConfigurations(final boolean mixDefaultAndLocalPrefixConfigurations) {
	this.mixDefaultAndLocalPrefixConfigurations = mixDefaultAndLocalPrefixConfigurations;
    }

    @Override
    public void setPlaceholderPrefix(final String placeholderPrefix) {
	this.placeholderPrefix = placeholderPrefix;
	super.setPlaceholderPrefix(placeholderPrefix);
    }

    @Override
    public void setPlaceholderSuffix(final String placeholderSuffix) {
	this.placeholderSuffix = placeholderSuffix;
	super.setPlaceholderSuffix(placeholderSuffix);
    }

    /**
     * Sets the prefix configs to build up a {@link PrefixedProperties}-Structure.
     * 
     * @param configList
     *            the new prefix configs
     */
    public void setPrefixConfigs(final List<PrefixConfig> configList) {
	prefixConfigList = configList;
    }

    /**
     * Indicates if other properties within this spring-context should get processed according these prefixed properties. For example other properties replacements will get replaced by properties from
     * this prefixed properties values and this environment configuration. This is usefull in the case that you have to define a set of properties which should not get mixed up with other properties
     * but should get environment specific values for example Log4j.<br/>
     * You can also use {@link PrefixedPropertiesFactoryBean} to load properties these properties on there own and have your own environment.<br/>
     * Default is false
     * 
     * @param value
     *            the new process properties
     * @see PrefixedPropertiesFactoryBean
     */
    public void setProcessOtherProperties(final boolean value) {
	this.processOtherProperties = value;
    }

    /* (non-Javadoc)
     * @see org.springframework.core.io.support.PropertiesLoaderSupport#setProperties(java.util.Properties)
     */
    @Override
    public void setProperties(final Properties properties) {
	localProperties = new Properties[] { properties };
    }

    /* (non-Javadoc)
     * @see org.springframework.core.io.support.PropertiesLoaderSupport#setPropertiesArray(java.util.Properties[])
     */
    @Override
    public void setPropertiesArray(final Properties... propertiesArray) {
	localProperties = propertiesArray;
    }

    @Override
    public void setValueSeparator(final String valueSeparator) {
	this.valueSeparator = valueSeparator;
	super.setValueSeparator(valueSeparator);
    }

}
