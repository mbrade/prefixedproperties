package net.sf.prefixedproperties;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import net.sf.prefixedproperties.config.PrefixConfig;

/**
 * The PrefixedResourceBundle extends the {@link ResourceBundle} class to
 */
public class PrefixedResourceBundle extends ResourceBundle {

    /**
     * The Class PrefixedControl.
     */
    public static class PrefixedControl extends ResourceBundle.Control {

	private final String defaultPrefix;
	private final PrefixConfig prefixConfig;

	/** The Constant FORMAT_DEFAULT. */
	public static final List<String> FORMAT_DEFAULT;

	/** The Constant FORMAT_XML. */
	public final static String FORMAT_XML = "pref.xml";

	/** The Constant FORMAT_JSON. */
	public final static String FORMAT_JSON = "pref.json";

	/** The Constant FORMAT_PROPERTIES. */
	public final static String FORMAT_PROPERTIES = "pref.properties";

	static {
	    final List<String> formatList = new ArrayList<String>(3);
	    formatList.add(FORMAT_JSON);
	    formatList.add(FORMAT_PROPERTIES);
	    formatList.add(FORMAT_XML);
	    FORMAT_DEFAULT = Collections.unmodifiableList(formatList);
	}

	protected PrefixedControl() {
	    this.defaultPrefix = null;
	    this.prefixConfig = null;
	}

	/**
	 * Instantiates a new prefixed control.
	 * 
	 * @param prefixConfig
	 *            the prefix config
	 */
	protected PrefixedControl(final PrefixConfig prefixConfig) {
	    this.prefixConfig = prefixConfig;
	    this.defaultPrefix = null;
	}

	/**
	 * Instantiates a new prefixed control.
	 * 
	 * @param defaultPrefix
	 *            the default prefix
	 */
	protected PrefixedControl(final String defaultPrefix) {
	    this.defaultPrefix = defaultPrefix;
	    this.prefixConfig = null;
	}

	private InputStream createInputStream(final ClassLoader loader, final boolean reload, final String resourceName) throws IOException {
	    InputStream stream;
	    try {
		stream = AccessController.doPrivileged(
			new PrivilegedExceptionAction<InputStream>() {
			    @Override
			    public InputStream run() throws IOException {
				InputStream is = null;
				if (reload) {
				    final URL url = loader.getResource(resourceName);
				    if (url != null) {
					final URLConnection connection = url.openConnection();
					if (connection != null) {
					    connection.setUseCaches(false);
					    is = connection.getInputStream();
					}
				    }
				} else {
				    is = loader.getResourceAsStream(resourceName);
				}
				return is;
			    }
			});
	    } catch (final PrivilegedActionException e) {
		throw (IOException) e.getException();
	    }
	    return stream;
	}

	/* (non-Javadoc)
	 * @see java.util.ResourceBundle.Control#getFormats(java.lang.String)
	 */
	@Override
	public List<String> getFormats(final String baseName) {
	    if (baseName == null) {
		throw new NullPointerException();
	    }
	    return FORMAT_DEFAULT;
	}

	/* (non-Javadoc)
	 * @see java.util.ResourceBundle.Control#newBundle(java.lang.String, java.util.Locale, java.lang.String, java.lang.ClassLoader, boolean)
	 */
	@Override
	public ResourceBundle newBundle(final String baseName, final Locale locale, final String format, final ClassLoader loader, final boolean reload) throws IllegalAccessException,
		InstantiationException, IOException {
	    final String bundleName = toBundleName(baseName, locale);
	    InputStream stream = null;
	    try {
		final PrefixedProperties properties = (prefixConfig != null) ? new PrefixedProperties(prefixConfig) : new PrefixedProperties();
		if (defaultPrefix != null) {
		    properties.setDefaultPrefix(defaultPrefix);
		}
		if (FORMAT_JSON.equals(format)) {
		    final String resourceName = toResourceName(bundleName, "json");
		    stream = createInputStream(loader, reload, resourceName);
		    if (stream != null) {
			properties.loadFromJSON(stream);
			return new PrefixedResourceBundle(properties);
		    }
		} else if (FORMAT_PROPERTIES.equals(format)) {
		    final String resourceName = toResourceName(bundleName, "properties");
		    stream = createInputStream(loader, reload, resourceName);
		    if (stream != null) {
			properties.load(stream);
			return new PrefixedResourceBundle(properties);
		    }
		} else if (FORMAT_XML.equals(format)) {
		    final String resourceName = toResourceName(bundleName, "xml");
		    stream = createInputStream(loader, reload, resourceName);
		    if (stream != null) {
			properties.loadFromXML(stream);
			return new PrefixedResourceBundle(properties);
		    }
		} else {
		    return super.newBundle(baseName, locale, format, loader, reload);
		}
	    } finally {
		if (stream != null) {
		    stream.close();
		}
	    }
	    return null;
	}
    }

    @SuppressWarnings("rawtypes")
    private static class ResouceBundleEnumeration implements Enumeration {

	private final Iterator it;

	ResouceBundleEnumeration(final Iterator it) {
	    this.it = it;
	}

	@Override
	public boolean hasMoreElements() {
	    return it.hasNext();
	}

	@Override
	public Object nextElement() {
	    return it.next();
	}

    }

    public static PrefixedResourceBundle getPrefixedResourceBundle(final String baseFileName, final Locale locale) throws IOException {
	ResourceBundle bundle = ResourceBundle.getBundle(baseFileName, locale, new PrefixedControl());
	while (bundle != null && !(bundle instanceof PrefixedResourceBundle)) { //a prior call to getBundle might be called without the proper Control class. We need to clear the cache.
	    ResourceBundle.clearCache();
	    bundle = ResourceBundle.getBundle(baseFileName, locale, new PrefixedControl());
	}
	return (PrefixedResourceBundle) bundle;
    }

    /**
     * Gets the prefixed resource bundle.
     * 
     * @param baseFileName
     *            the base file name
     * @param locale
     *            the locale
     * @param prefixConfig
     *            the prefix config
     * @return the prefixed resource bundle
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static PrefixedResourceBundle getPrefixedResourceBundle(final String baseFileName, final Locale locale, final PrefixConfig prefixConfig) throws IOException {
	ResourceBundle bundle = ResourceBundle.getBundle(baseFileName, locale, new PrefixedControl(prefixConfig));
	while (bundle != null && !(bundle instanceof PrefixedResourceBundle)) {//a prior call to getBundle might be called without the proper Control class. We need to clear the cache.
	    ResourceBundle.clearCache();
	    bundle = ResourceBundle.getBundle(baseFileName, locale, new PrefixedControl(prefixConfig));
	}
	return (PrefixedResourceBundle) bundle;
    }

    /**
     * Gets the prefixed resource bundle.
     * 
     * @param baseFileName
     *            the base file name
     * @param locale
     *            the locale
     * @param defaultPrefix
     *            the default prefix
     * @return the prefixed resource bundle
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public static PrefixedResourceBundle getPrefixedResourceBundle(final String baseFileName, final Locale locale, final String defaultPrefix) throws IOException {
	ResourceBundle bundle = ResourceBundle.getBundle(baseFileName, locale, new PrefixedControl(defaultPrefix));
	while (bundle != null && !(bundle instanceof PrefixedResourceBundle)) { //a prior call to getBundle might be called without the proper Control class. We need to clear the cache.
	    ResourceBundle.clearCache();
	    bundle = ResourceBundle.getBundle(baseFileName, locale, new PrefixedControl(defaultPrefix));
	}
	return (PrefixedResourceBundle) bundle;
    }

    private final PrefixedProperties properties;

    /**
     * Instantiates a new prefixed resource bundle.
     * 
     * @param properties
     *            the properties
     */
    public PrefixedResourceBundle(final PrefixedProperties properties) {
	this.properties = properties;
    }

    /**
     * Gets the configured prefix.
     * 
     * @return the configured prefix
     */
    public String getConfiguredPrefix() {
	return properties.getEffectivePrefix();
    }

    /* (non-Javadoc)
     * @see java.util.ResourceBundle#getKeys()
     */
    @SuppressWarnings("unchecked")
    @Override
    public Enumeration<String> getKeys() {
	return new ResouceBundleEnumeration(properties.stringPropertyNames().iterator());
    }

    /**
     * Gets the prefixed properties.
     * 
     * @return the prefixed properties
     */
    public PrefixedProperties getPrefixedProperties() {
	return properties;
    }

    /* (non-Javadoc)
     * @see java.util.ResourceBundle#handleGetObject(java.lang.String)
     */
    @Override
    protected Object handleGetObject(final String key) {
	if (key == null) {
	    throw new NullPointerException("The given key is null.");
	}
	return properties.get(key);
    }

    public void setConfiguredPrefix(final String configuredPrefix) {
	properties.setLocalPrefix(configuredPrefix);
    }

    /**
     * Sets the default prefix.
     * 
     * @param prefix
     *            the new default prefix
     */
    public void setDefaultPrefix(final String prefix) {
	properties.setDefaultPrefix(prefix);
    }
}
