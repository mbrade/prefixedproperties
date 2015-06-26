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
import java.util.List;
import java.util.Properties;

import net.sf.prefixedproperties.PrefixedProperties;
import net.sf.prefixedproperties.config.PrefixConfig;

import org.springframework.beans.factory.config.PropertiesFactoryBean;
import org.springframework.core.io.Resource;

public class PrefixedPropertiesFactoryBean extends PropertiesFactoryBean {

    /** The Constant JSON_FILE_EXTENSION. */
    protected final static String JSON_FILE_EXTENSION = "json";
    protected final static String XML_FILE_EXTENSION = "xml";

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

    /**
     * Creates the prefixed properties.
     * 
     * @return the prefixed properties
     */
    @Override
    protected synchronized PrefixedProperties createProperties() {
	if (myProperties == null) {
	    PrefixedProperties resultProperties = null;
	    String environment = defaultPrefix;
	    if (environmentFactory != null) {
		environment = environmentFactory.getEnvironment();
	    } else if (defaultPrefixSystemPropertyKey != null) {
		environment = System.getProperty(defaultPrefixSystemPropertyKey);
		if (environment == null) {
		    logger.warn(String.format("Didn't found system property key to set default prefix: %1s", defaultPrefixSystemPropertyKey));
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
	    myProperties = resultProperties;
	}
	return myProperties;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Class getObjectType() {
	return PrefixedProperties.class;
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
		    } catch (final IOException ie) {//ignore
		    } finally {
			if (file == null) {
			    is = location.getInputStream();
			}
		    }

		    if (location.getFilename().toLowerCase().endsWith(XML_FILE_EXTENSION)) {
			persister.loadFromXml(props, is);
		    } else if (location.getFilename().toLowerCase().endsWith(JSON_FILE_EXTENSION)) {
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
			    logger.warn(String.format("Could not load properties from %1s", location), ex);
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

    /**
     * Sets the prefix configs to build up a {@link PrefixedProperties}-Structure.
     * 
     * @param configList
     *            the new prefix configs
     */
    public void setPrefixConfigs(final List<PrefixConfig> configList) {
	prefixConfigList = configList;
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

}
