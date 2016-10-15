/*
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
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import net.sf.prefixedproperties.PrefixedProperties;
import net.sf.prefixedproperties.config.PrefixConfig;

import org.springframework.beans.factory.config.PropertyOverrideConfigurer;
import org.springframework.core.io.Resource;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.util.CollectionUtils;

/**
 * The PrefixedPropertyOverrideConfigurer behaves the same as
 * {@link PropertyOverrideConfigurer} with the difference that you can. To
 * configure the default prefix you can use the following methods:
 * {@link #setEnvironmentFactory(EnvironmentFactory)}<br>
 * {@link #setDefaultPrefixSystemPropertyKey(String)}<br>
 * {@link #setDefaultPrefix(String)}<br>
 * The usage of the methods will be in the same order as above.
 */
@ManagedResource("prefixedproperties:name=PrefixedPropertyOverrideConfigurer")
public class PrefixedPropertyOverrideConfigurer extends PropertyOverrideConfigurer {

	/** The local override. */
	protected boolean localOverride;

	/** The local properties. */
	protected Properties[] localProperties;

	/** The locations. */
	protected Resource[] locations;

	/** The ignore resource not found. */
	protected boolean ignoreResourceNotFound;

	/** The file encoding. */
	protected String fileEncoding;

	/** The prefix config list. */
	protected List<PrefixConfig> prefixConfigList;

	/** The default prefix. */
	protected String defaultPrefix;
	private PrefixedProperties myProperties;
	private final PrefixedPropertiesPersister persister = new PrefixedPropertiesPersister();

	private String defaultPrefixSystemPropertyKey;

	protected EnvironmentFactory environmentFactory = null;

	private boolean mixDefaultAndLocalPrefixConfigurations = false;

	/**
	 * Creates the properties.
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
						logger.warn(String.format("Didn't found system property key to set default prefix: %1s",
								defaultPrefixSystemPropertyKey));
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
	 * Gets the prefixed properties.
	 * 
	 * @return the prefixed properties
	 */
	public PrefixedProperties getPrefixedProperties() {
		return myProperties;
	}

	public boolean isMixDefaultAndLocalPrefixConfigurations() {
		return mixDefaultAndLocalPrefixConfigurations;
	}

	/**
	 * Load properties.
	 * 
	 * @param props
	 *            the props
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
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
					} catch (final IOException ie) {// ignore
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
					} else if (location.getFilename().toLowerCase().endsWith(Constants.YAML_FILE_EXTENSION)) {
						if (fileEncoding != null) {
							persister.loadFromYAML(props, new InputStreamReader(is, Charset.forName(fileEncoding)));
						} else {
							persister.loadFromYAML(props, is);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.core.io.support.PropertiesLoaderSupport#
	 * mergeProperties()
	 */
	@Override
	protected Properties mergeProperties() throws IOException {
		final PrefixedProperties myProperties = createProperties();
		if (localOverride) {
			loadProperties(myProperties);
		}

		if (localProperties != null) {
			for (int i = 0; i < localProperties.length; i++) {
				CollectionUtils.mergePropertiesIntoMap(localProperties[i], myProperties);
			}
		}

		if (!localOverride) {
			loadProperties(myProperties);
		}

		return myProperties;
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
	 * Sets this method to specify a system property to be used as an
	 * environment. The value of the property will be used for setting the
	 * default prefix. {@link PrefixedProperties#setDefaultPrefix(String)}
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

	/**
	 * Set the encoding to use for parsing properties files.
	 * <p>
	 * Default is none, using the <code>java.util.Properties</code> default
	 * encoding.
	 * <p>
	 * Only applies to classic properties files, not to XML files.
	 * 
	 * @param encoding
	 *            the new file encoding
	 * @see org.springframework.util.PropertiesPersister#load
	 */
	@Override
	public void setFileEncoding(final String encoding) {
		fileEncoding = encoding;
	}

	/**
	 * Set if failure to find the property resource should be ignored.
	 * <p>
	 * "true" is appropriate if the properties file is completely optional.
	 * Default is "false".
	 * 
	 * @param ignoreResourceNotFound
	 *            the new ignore resource not found
	 */
	@Override
	public void setIgnoreResourceNotFound(final boolean ignoreResourceNotFound) {
		this.ignoreResourceNotFound = ignoreResourceNotFound;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.springframework.core.io.support.PropertiesLoaderSupport#
	 * setLocalOverride(boolean)
	 */
	@Override
	public void setLocalOverride(final boolean localOverride) {
		this.localOverride = localOverride;
	}

	/**
	 * Set a location of a properties file to be loaded.
	 * <p>
	 * Can point to a classic properties file or to an XML file that follows JDK
	 * 1.5's properties XML format.
	 * 
	 * @param location
	 *            the new location
	 */
	@Override
	public void setLocation(final Resource location) {
		locations = new Resource[] { location };
	}

	public void setMixDefaultAndLocalPrefixConfigurations(final boolean mixDefaultAndLocalPrefixConfigurations) {
		this.mixDefaultAndLocalPrefixConfigurations = mixDefaultAndLocalPrefixConfigurations;
	}

	/**
	 * Sets the prefix configs.
	 * 
	 * @param configs
	 *            the new prefix configs
	 */
	public void setPrefixConfigs(final List<PrefixConfig> configs) {
		prefixConfigList = configs;
	}

	/**
	 * Set local properties, e.g. via the "props" tag in XML bean definitions.
	 * These can be considered defaults, to be overridden by properties loaded
	 * from files.
	 * 
	 * @param properties
	 *            the new properties
	 */
	@Override
	public void setProperties(final Properties properties) {
		localProperties = new Properties[] { properties };
	}

	/**
	 * Set local properties, e.g. via the "props" tag in XML bean definitions,
	 * allowing for merging multiple properties sets into one.
	 * 
	 * @param propertiesArray
	 *            the new properties array
	 */
	@Override
	public void setPropertiesArray(final Properties... propertiesArray) {
		localProperties = propertiesArray;
	}

}
