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
package net.sf.prefixedproperties;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.sf.prefixedproperties.config.DefaultPrefixConfig;
import net.sf.prefixedproperties.config.DynamicPrefixConfig;
import net.sf.prefixedproperties.config.PrefixConfig;
import net.sf.triemap.TrieMap;
import net.sf.triemap.TrieMap.TrieMapBackedProperties;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonGenerator.Feature;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

/**
 * PrefixedProperties can be used to filter properties by an environment or key-prefix. The environment itself can be configured within this Properties by using {@link #setDefaultPrefix(String)} which
 * is for a global level and also {@link #setLocalPrefix(String)} which is used on a {@link ThreadLocal} basis.<br/>
 * So it's possible to have a global prefix for everything. And a more specific or totally different prefix on a thread-dependend basis.<br/>
 * 
 */
public class PrefixedProperties extends Properties implements Serializable {

    private class DoubleEntry<T, P> {
	private final T one;
	private final P two;

	private DoubleEntry(final T one, final P two) {
	    this.one = one;
	    this.two = two;
	}

	public T getOne() {
	    return one;
	}

	public P getTwo() {
	    return two;
	}

	@Override
	public String toString() {
	    final StringBuilder sb = new StringBuilder("One:").append(one).append(" Two: ").append(two);
	    return sb.toString();
	}

    }

    /*
     * The Class EmptyPrefix.
     */
    private static class EmptyPrefix extends DefaultPrefixConfig {

	/** The Constant serialVersionUID. */
	private final static long serialVersionUID = 1L;

	/** The Constant INSTANCE. */
	private final static EmptyPrefix INSTANCE = new EmptyPrefix();

	/* (non-Javadoc)
	 * @see net.sf.prefixedproperties.config.AbstractPrefixConfig#getClone()
	 */
	@Override
	public final PrefixConfig clone() {
	    return EmptyPrefix.INSTANCE;
	}
    }

    /*
     * The Class PPEntry.
     */
    private final class PPEntry implements Map.Entry<Object, Object> {

	/* The key. */
	private final Object key;

	/* The value. */
	private final Object value;

	/*
	 * Instantiates a new pP entry.
	 *
	 * @param key the key
	 * @param value the value
	 */
	private PPEntry(final Object aKey, final Object aValue) {
	    key = aKey;
	    value = aValue;
	}

	/* (non-Javadoc)
	 * @see java.util.Map.Entry#getKey()
	 */
	@Override
	public Object getKey() {
	    try {
		lock.readLock().lock();
		return getUnprefixedKey(key);
	    } finally {
		lock.readLock().unlock();
	    }
	}

	/* (non-Javadoc)
	 * @see java.util.Map.Entry#getValue()
	 */
	@Override
	public Object getValue() {
	    return value;
	}

	/* (non-Javadoc)
	 * @see java.util.Map.Entry#setValue(java.lang.Object)
	 */
	@Override
	public Object setValue(final Object aValue) {
	    return put(key, aValue);
	}

	@Override
	public String toString() {
	    final StringBuilder builder = new StringBuilder();
	    builder.append("[key=").append(key).append(", value=").append(value).append("]");
	    return builder.toString();
	}

    }

    /*
     * The Class PrefixedPropertiesEnumerationImpl.
     *
     * @param <E> the element type
     */
    private final class PrefixedPropertiesEnumerationImpl<E> implements PrefixedPropertiesEnumeration<E> {

	/** The it. */
	private Iterator<E> it;

	/** The last. */
	private E last;

	/** The is key. */
	private boolean isKey;

	/*
	 * Instantiates a new prefixed properties enumeration impl.
	 *
	 * @param it the it
	 */
	private PrefixedPropertiesEnumerationImpl(final Iterator<E> iterator) {
	    this(iterator, false);
	}

	/*
	 * Instantiates a new prefixed properties enumeration impl.
	 *
	 * @param it the it
	 * @param isKey the is key
	 */
	private PrefixedPropertiesEnumerationImpl(final Iterator<E> iterator, final boolean isKeyParam) {
	    it = iterator;
	    isKey = isKeyParam;
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	@Override
	public boolean hasMoreElements() {
	    return hasNext();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext() {
	    return it.hasNext();
	}

	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<E> iterator() {
	    return this;
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#next()
	 */
	@Override
	public E next() {
	    last = it.next();
	    return last;
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	@Override
	public E nextElement() {
	    return next();
	}

	/* (non-Javadoc)
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove() {
	    if (isKey) {
		PrefixedProperties.this.remove(last);
	    }
	}

    }

    /**
     * Creates the cascading prefix properties.
     * 
     * @param configs
     *            the configs
     * @return the prefixed properties
     */
    public static PrefixedProperties createCascadingPrefixProperties(final List<PrefixConfig> configs) {
	PrefixedProperties properties = null;
	for (final PrefixConfig config : configs) {
	    if (properties == null) {
		properties = new PrefixedProperties((config == null) ? new DynamicPrefixConfig() : config);
	    } else {
		properties = new PrefixedProperties(properties, (config == null) ? new DynamicPrefixConfig() : config);
	    }
	}
	return properties;
    }

    /**
     * Creates the cascading prefix properties.
     * 
     * @param configs
     *            the configs
     * @return the prefixed properties
     */
    public static PrefixedProperties createCascadingPrefixProperties(final PrefixConfig... configs) {
	PrefixedProperties properties = null;
	for (final PrefixConfig config : configs) {
	    if (properties == null) {
		properties = new PrefixedProperties((config == null) ? new DynamicPrefixConfig() : config);
	    } else {
		properties = new PrefixedProperties(properties, (config == null) ? new DynamicPrefixConfig() : config);
	    }
	}
	return properties;
    }

    /**
     * Creates the cascading prefix properties. This method parses the given prefixString and creates for each delimiter part a PrefixedProperties.
     * 
     * @param prefixString
     *            the prefixes
     * @return the prefixed properties
     */
    public static PrefixedProperties createCascadingPrefixProperties(final String prefixString) {
	return prefixString.indexOf(PrefixConfig.PREFIXDELIMITER) != -1 ? createCascadingPrefixProperties(prefixString.split("\\" + PrefixConfig.PREFIXDELIMITER))
		: createCascadingPrefixProperties(new String[] { prefixString });

    }

    /**
     * Creates the cascading prefix properties by using the given Prefixes.
     * 
     * @param prefixes
     *            the prefixes
     * @return the prefixed properties
     */
    public static PrefixedProperties createCascadingPrefixProperties(final String[] prefixes) {
	PrefixedProperties properties = null;
	for (final String aPrefix : prefixes) {
	    if (properties == null) {
		properties = new PrefixedProperties(aPrefix);
	    } else {
		properties = new PrefixedProperties(properties, aPrefix);
	    }
	}
	return properties;
    }

    private static final long serialVersionUID = 1L;

    private transient ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private PrefixConfig prefixes = new DynamicPrefixConfig();

    private Properties properties = new TrieMap<Object>().asProperties();

    private boolean mixDefaultAndLocalPrefixes = true;

    /**
     * Instantiates a new prefixed properties.
     */
    public PrefixedProperties() {
    }

    /**
     * Instantiates a new prefixed properties.
     * 
     * @param config
     *            the config
     */
    public PrefixedProperties(final PrefixConfig config) {
	setPrefixConfig(config);
    }

    /**
     * Instantiates a new prefixed properties.
     * 
     * @param config
     *            the PrefixConfig
     * @param defaults
     *            the defaults
     */
    public PrefixedProperties(final PrefixConfig config, final Properties defaults) {
	this(defaults);
	setPrefixConfig(config);
    }

    /**
     * Instantiates a new prefixed properties.
     * 
     * @param props
     *            the properties to be inserted.
     * @param config
     *            the config to be used for this PrefixedProperties
     */
    public PrefixedProperties(final PrefixedProperties props, final PrefixConfig config) {
	properties = props;
	setPrefixConfig(config);
    }

    /**
     * Instantiates a new prefixed properties.
     * 
     * @param props
     *            the props
     * @param default Prefix the default prefix
     */
    public PrefixedProperties(final PrefixedProperties props, final String defaultPrefix) {
	properties = props;
	setDefaultPrefix(defaultPrefix);
    }

    /**
     * Instantiates a new prefixed properties.
     * 
     * @param defaults
     *            the defaults
     */
    public PrefixedProperties(final Properties defaults) {
	properties = defaults instanceof PrefixedProperties ? defaults : new TrieMap<Object>().asProperties(defaults);
    }

    /**
     * Instantiates a new prefixed properties.
     * 
     * @param defaultPrefix
     *            the default prefix
     */
    public PrefixedProperties(final String defaultPrefix) {
	setDefaultPrefix(defaultPrefix);
    }

    private String checkAndConvertPrefix(final String prefix) {
	if (prefix == null) {
	    throw new IllegalArgumentException("The prefix has to be set and is not allowed to be null.");
	}
	String myPrefix = prefix;
	if ("*".equals(prefix)) {
	    myPrefix = StringUtils.repeat(PrefixConfig.PREFIXDELIMITER_STRING, getPrefixConfigs().size());
	}
	return myPrefix;
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#clear()
     */
    @Override
    public void clear() {
	lock.writeLock().lock();
	try {
	    properties.clear();
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /**
     * Removes all default prefixes from the {@link PrefixConfig}s
     */
    public void clearDefaultPrefixes() {
	final Map<Integer, PrefixConfig> prefixConfigs = getPrefixConfigs();
	for (final PrefixConfig config : prefixConfigs.values()) {
	    config.setDefaultPrefix(null);
	}
    }

    /**
     * Removes all local prefixes from the {@link PrefixConfig}s
     */
    public void clearLocalPrefixes() {
	final Map<Integer, PrefixConfig> prefixConfigs = getPrefixConfigs();
	for (final PrefixConfig config : prefixConfigs.values()) {
	    config.setPrefix(null);
	}
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#clone()
     */
    @SuppressWarnings("rawtypes")
    @Override
    public PrefixedProperties clone() {
	lock.readLock().lock();
	try {
	    final PrefixedProperties clone = (PrefixedProperties) super.clone();
	    if (prefixes != null) {
		clone.prefixes = prefixes.clone();
	    }
	    if (properties instanceof PrefixedProperties) {
		clone.properties = (Properties) properties.clone();
	    } else if (properties instanceof TrieMapBackedProperties) {
		clone.properties = ((TrieMapBackedProperties) properties).clone();
	    } else {
		clone.properties = new TrieMap<Object>().asProperties();
		clone.properties.putAll(properties);
	    }
	    return clone;
	} finally {
	    lock.readLock().unlock();
	}
    }

    private void configureJsonParser(final JsonParser jp) {
	jp.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
	jp.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
	jp.configure(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
	jp.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#contains(java.lang.Object)
     */
    @Override
    public boolean contains(final Object value) {
	lock.readLock().lock();
	try {
	    if (value == null) {
		return false;
	    }
	    for (@SuppressWarnings("rawtypes")
	    final Map.Entry entry : entrySet()) {
		final Object otherValue = entry.getValue();
		if (otherValue != null && otherValue.equals(value)) {
		    return true;
		}
	    }
	    return false;
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#containsKey(java.lang.Object)
     */
    @Override
    public boolean containsKey(final Object key) {
	lock.readLock().lock();
	try {
	    if (isKeyValid(key)) {
		final boolean useLocalPrefixes = !mixDefaultAndLocalPrefixes && hasLocalPrefixConfigurations();
		if (!properties.containsKey(getPrefixedKey(key, useLocalPrefixes))) {
		    return properties.containsKey(key);
		} else {
		    return true;
		}
	    }
	    return false;
	} finally {
	    lock.readLock().unlock();
	}
    }

    private boolean containsValidPrefix(final Object key) {
	if (key != null && String.class == key.getClass()) {
	    return prefixes.containsValidPrefix((String) key);
	}
	return false;
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#containsValue(java.lang.Object)
     */
    @Override
    public boolean containsValue(final Object value) {
	lock.readLock().lock();
	try {
	    return contains(value);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#elements()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public PrefixedPropertiesEnumeration<Object> elements() {
	lock.readLock().lock();
	try {
	    final Collection values = values();
	    final Iterator it = values.iterator();
	    return new PrefixedPropertiesEnumerationImpl(it);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#entrySet()
     */
    @Override
    public Set<Entry<Object, Object>> entrySet() {
	final Set<Entry<Object, Object>> entrySet = new HashSet<Entry<Object, Object>>();
	lock.readLock().lock();
	try {
	    final boolean useLocalPrefixes = !mixDefaultAndLocalPrefixes && hasLocalPrefixConfigurations();
	    for (final Map.Entry<Object, Object> keyEntry : getKeyMap(false).entrySet()) {
		final Object value = get(keyEntry.getValue(), useLocalPrefixes);
		if (String.class == keyEntry.getKey().getClass()) {
		    entrySet.add(new PPEntry(keyEntry.getKey(), value));
		} else {
		    entrySet.add(new PPEntry(keyEntry.getKey(), value));
		}
	    }
	    return entrySet;
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (!super.equals(obj)) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final PrefixedProperties other = (PrefixedProperties) obj;
	if (prefixes == null) {
	    if (other.prefixes != null) {
		return false;
	    }
	} else if (!prefixes.equals(other.prefixes)) {
	    return false;
	}
	if (properties == null) {
	    if (other.properties != null) {
		return false;
	    }
	} else if (!properties.equals(other.properties)) {
	    return false;
	}
	return true;
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#get(java.lang.Object)
     */
    @Override
    public Object get(final Object key) {
	final boolean useLocalPrefixes = !mixDefaultAndLocalPrefixes && hasLocalPrefixConfigurations();
	return get(key, useLocalPrefixes);
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#get(java.lang.Object)
     */
    protected Object get(final Object key, final boolean useLocalPrefixes) {
	Object result = null;
	lock.readLock().lock();
	try {
	    final Object prefixedKey = getPrefixedKey(key, useLocalPrefixes);
	    result = (properties instanceof PrefixedProperties) ? ((PrefixedProperties) properties).get(prefixedKey, useLocalPrefixes) : properties.get(prefixedKey);
	    //fall back by getting the property without any prefixed keys
	    if (result == null) {
		result = (properties instanceof PrefixedProperties) ? ((PrefixedProperties) properties).get(key, useLocalPrefixes) : properties.get(key);
	    }
	    return result;
	} finally {
	    lock.readLock().unlock();
	}
    }

    /**
     * Gets the prefixed key and parse it to an String[]<br/>
     * Each comma-separated list can be used.
     * 
     * @param key
     *            the key
     * 
     * @return String[] or null if the key couldn't get found.
     */
    public String[] getArray(final String key) {
	final String value = getProperty(key);
	if (value != null) {
	    final String[] strings = value.split(",[\\s]*|[\\s]*$");
	    return strings;
	}
	return null;
    }

    /**
     * Gets the prefixed key and parse it to an String[]<br/>
     * Each comma-separated list can be used. If the key couldn't get found, the default will be used.
     * 
     * @param key
     *            the key
     * @param def
     *            default value
     * 
     * @return String[]
     */
    public String[] getArray(final String key, final String[] def) {
	final String[] value = getArray(key);
	if (value != null) {
	    return value;
	}
	return def;
    }

    /**
     * Gets the prefixed key and parse it to an boolean-value. If the key was not found it will always return false.
     * 
     * @param key
     *            key value
     * 
     * @return boolean-representation of value
     */
    public boolean getBoolean(final String key) {
	return Boolean.valueOf(getProperty(key)).booleanValue();
    }

    /**
     * Gets the prefixed key and parse it to an boolean-value.
     * 
     * @param key
     *            key value
     * @param def
     *            default value
     * 
     * @return boolean-representation of value
     */
    public boolean getBoolean(final String key, final boolean def) {
	final String value = getProperty(key);
	return value != null ? Boolean.valueOf(value).booleanValue() : def;
    }

    /**
     * Gets the prefixed key and parse it to an boolean[]<br/>
     * Each comma-separated list can be used.
     * 
     * @param key
     *            the key
     * @return boolean[] or null if the key couldn't get found.
     */
    public boolean[] getBooleanArray(final String key) {
	final String[] value = getArray(key);
	if (value != null) {
	    final boolean[] result = new boolean[value.length];
	    for (int i = 0; i < value.length; i++) {
		result[i] = Boolean.valueOf(value[i]).booleanValue();
	    }
	    return result;
	}
	return null;
    }

    /**
     * Gets the prefixed key and parse it to an boolean[]<br/>
     * Each comma-separated list can be used. If the key couldn't get found, the default will be used.
     * 
     * @param key
     *            the key
     * @param def
     *            default value
     * 
     * @return boolean[]
     */
    public boolean[] getBooleanArray(final String key, final boolean[] def) {
	final boolean[] result = getBooleanArray(key);
	return result == null ? def : result;
    }

    /**
     * Gets the prefixed key and parse it to an byte-value.
     * 
     * @param key
     *            key value
     * @return byte-representation of value
     */
    public byte getByte(final String key) {
	final String value = getProperty(key);
	if (value == null) {
	    throw new NumberFormatException("Couldn't parse property to byte.");
	}
	return Byte.parseByte(value);
    }

    /**
     * Gets the prefixed key and parse it to an byte-value.
     * 
     * @param key
     *            key value
     * @param def
     *            default value
     * 
     * @return byte-representation of value
     */
    public byte getByte(final String key, final byte def) {
	try {
	    return getByte(key);
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    /**
     * Gets the byte array.
     * 
     * @param key
     *            the key
     * @return the byte array
     */
    public byte[] getByteArray(final String key) {
	final String[] value = getArray(key);
	if (value != null) {
	    final byte[] result = new byte[value.length];
	    for (int i = 0; i < value.length; i++) {
		result[i] = Byte.parseByte(value[i]);
	    }
	    return result;
	}
	return null;
    }

    /**
     * Gets the byte array.
     * 
     * @param key
     *            the key
     * @param def
     *            the def
     * @return the byte array
     */
    public byte[] getByteArray(final String key, final byte[] def) {
	try {
	    final byte[] result = getByteArray(key);
	    return result != null ? result : def;
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    /**
     * Gets the complete properties.
     * 
     * @return the complete properties
     */
    protected Properties getCompleteProperties() {
	if (properties instanceof PrefixedProperties) {
	    return ((PrefixedProperties) properties).getCompleteProperties();
	} else {
	    return properties;
	}
    }

    /**
     * Gets the prefixed key and parse it to an double-value.
     * 
     * @param key
     *            key value
     * @return double-representation of value
     */
    public double getDouble(final String key) {
	final String value = getProperty(key);
	if (value == null) {
	    throw new NumberFormatException("Couldn't parse property to double.");
	}
	return Double.parseDouble(value);
    }

    /**
     * Gets the prefixed key and parse it to an double-value.
     * 
     * @param key
     *            key value
     * @param def
     *            default value
     * 
     * @return double-representation of value
     */
    public double getDouble(final String key, final double def) {
	try {
	    return getDouble(key);
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    /**
     * Gets the prefixed key and parse it to an double[]<br/>
     * Each comma-separated list can be used.
     * 
     * @param key
     *            the key
     * @return double[] or null if the key couldn't get found.
     */
    public double[] getDoubleArray(final String key) {
	final String[] value = getArray(key);
	if (value != null) {
	    final double[] result = new double[value.length];
	    for (int i = 0; i < value.length; i++) {
		result[i] = Double.parseDouble(value[i]);
	    }
	    return result;
	}
	return null;
    }

    /**
     * Gets the prefixed key and parse it to an double[]<br/>
     * Each comma-separated list can be used. If the key couldn't get found, the default will be used.
     * 
     * @param key
     *            the key
     * @param def
     *            default value
     * 
     * @return double[]
     */
    public double[] getDoubleArray(final String key, final double[] def) {
	try {
	    final double[] result = getDoubleArray(key);
	    return result != null ? result : def;
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    /**
     * Gets the effective prefix.
     * 
     * @return the prefix
     */
    public String getEffectivePrefix() {
	lock.readLock().lock();
	try {
	    final boolean useLocalPrefixes = !mixDefaultAndLocalPrefixes && hasLocalPrefixConfigurations();
	    return getPrefix(new StringBuilder(), useLocalPrefixes).toString();
	} finally {
	    lock.readLock().unlock();
	}
    }

    /**
     * Gets the prefixed key and parse it to an float-value.
     * 
     * @param key
     *            key value
     * @return float-representation of value
     */
    public float getFloat(final String key) {
	final String value = getProperty(key);
	if (value == null) {
	    throw new NumberFormatException("Couldn't parse property to float.");
	}
	return Float.parseFloat(value);
    }

    /**
     * Gets the prefixed key and parse it to an float-value.
     * 
     * @param key
     *            key value
     * @param def
     *            default value
     * 
     * @return float-representation of value
     */
    public float getFloat(final String key, final float def) {
	try {
	    return getFloat(key);
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    /**
     * Gets the prefixed key and parse it to an float[]<br/>
     * Each comma-separated list can be used.
     * 
     * @param key
     *            the key
     * @return float[] or null if the key couldn't get found.
     */
    public float[] getFloatArray(final String key) {
	final String[] value = getArray(key);
	if (value != null) {
	    final float[] result = new float[value.length];
	    for (int i = 0; i < value.length; i++) {
		result[i] = Float.parseFloat(value[i]);
	    }
	    return result;
	}
	return null;
    }

    /**
     * Gets the prefixed key and parse it to an float[]<br/>
     * Each comma-separated list can be used. If the key couldn't get found, the default will be used.
     * 
     * @param key
     *            the key
     * @param def
     *            default value
     * 
     * @return float[]
     */
    public float[] getFloatArray(final String key, final float[] def) {
	try {
	    final float[] result = getFloatArray(key);
	    return result != null ? result : def;
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    /**
     * Gets the prefixed key and parse it to an int-value.
     * 
     * @param key
     *            key value
     * @return int-representation of value
     */
    public int getInt(final String key) {
	final String value = getProperty(key);
	if (value == null) {
	    throw new NumberFormatException("Couldn't parse property to int.");
	}
	return Integer.parseInt(value);
    }

    /**
     * Gets the prefixed key and parse it to an int-value if the key doesn't exist the default value will be used.
     * 
     * @param key
     *            key value
     * @param def
     *            default value
     * 
     * @return int-representation of value
     */
    public int getInt(final String key, final int def) {
	try {
	    return getInt(key);
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    /**
     * Gets the prefixed key and parse it to an int[]<br/>
     * Each comma-separated list can be used.
     * 
     * @param key
     *            the key
     * @return String[] or null if the key couldn't get found.
     */
    public int[] getIntArray(final String key) {
	final String[] value = getArray(key);
	if (value != null) {
	    final int[] result = new int[value.length];
	    for (int i = 0; i < value.length; i++) {
		result[i] = Integer.parseInt(value[i]);
	    }
	    return result;
	}
	return null;
    }

    /**
     * Gets the prefixed key and parse it to an int[]<br/>
     * Each comma-separated list can be used. If the key couldn't get found, the default will be used.
     * 
     * @param key
     *            the key
     * @param def
     *            default value
     * 
     * @return int[]
     */
    public int[] getIntArray(final String key, final int[] def) {
	try {
	    final int[] result = getIntArray(key);
	    return result != null ? result : def;
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    private Map<Object, Object> getKeyMap(final boolean onlyStrings) {
	final Map<Object, Object> result = new HashMap<Object, Object>();
	for (@SuppressWarnings("rawtypes")
	final Map.Entry entry : properties.entrySet()) {
	    if (String.class == entry.getKey().getClass()) {
		if (isKeyValid(entry.getKey())) {
		    final Object unprefixedKey = getUnprefixedKey(entry.getKey());
		    if (result.containsKey(unprefixedKey)) {
			if (!unprefixedKey.equals(entry.getKey())) {
			    result.put(unprefixedKey, entry.getKey());
			}
		    } else {
			result.put(unprefixedKey, entry.getKey());
		    }
		}
	    } else if (!onlyStrings) {
		result.put(entry.getKey(), entry.getKey());
	    }
	}
	return result;
    }

    /**
     * Gets the prefixed key and parse it to an long-value.
     * 
     * @param key
     *            key value
     * @return long-representation of value
     */
    public long getLong(final String key) {
	final String value = getProperty(key);
	if (value == null) {
	    throw new NumberFormatException("Couldn't parse property to long.");
	}
	return Long.parseLong(value);
    }

    /**
     * Gets the prefixed key and parse it to an long-value.
     * 
     * @param key
     *            key value
     * @param def
     *            default value
     * 
     * @return long-representation of value
     */
    public long getLong(final String key, final long def) {
	try {
	    return getLong(key);
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    /**
     * Gets the prefixed key and parse it to an long[]<br/>
     * Each comma-separated list can be used.
     * 
     * @param key
     *            the key
     * @return long[] or null if the key couldn't get found.
     */
    public long[] getLongArray(final String key) {
	final String[] value = getArray(key);
	if (value != null) {
	    final long[] result = new long[value.length];
	    for (int i = 0; i < value.length; i++) {
		result[i] = Long.parseLong(value[i]);
	    }
	    return result;
	}
	return null;
    }

    /**
     * Gets the prefixed key and parse it to an long[]<br/>
     * Each comma-separated list can be used. If the key couldn't get found, the default will be used.
     * 
     * @param key
     *            the key
     * @param def
     *            default value
     * 
     * @return long[]
     */
    public long[] getLongArray(final String key, final long[] def) {
	try {
	    final long[] result = getLongArray(key);
	    return result != null ? result : def;
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    private StringBuilder getPrefix(final StringBuilder sb, final boolean useLocalPrefixConfigurations) {
	if (properties instanceof PrefixedProperties) {
	    ((PrefixedProperties) properties).getPrefix(sb, useLocalPrefixConfigurations);
	}

	if (prefixes != null) {
	    if (!useLocalPrefixConfigurations) {
		if (prefixes.getPrefix() != null) {
		    if (sb.length() > 0) {
			sb.append(PrefixConfig.PREFIXDELIMITER);
		    }
		    sb.append(prefixes.getPrefix());
		}
	    } else {
		if (prefixes.getLocalPrefix() != null) {
		    if (sb.length() > 0) {
			sb.append(PrefixConfig.PREFIXDELIMITER);
		    }
		    sb.append(prefixes.getLocalPrefix());
		}
	    }
	}
	return sb;
    }

    /**
     * Gets the prefix config.
     * 
     * @return the prefix config
     */
    public PrefixConfig getPrefixConfig() {
	lock.readLock().lock();
	try {
	    return prefixes;
	} finally {
	    lock.readLock().unlock();
	}
    }

    private Map<Integer, PrefixConfig> getPrefixConfigs() {
	return getPrefixConfigs(new TreeMap<Integer, PrefixConfig>());
    }

    private Map<Integer, PrefixConfig> getPrefixConfigs(final Map<Integer, PrefixConfig> result) {
	if (properties instanceof PrefixedProperties) {
	    ((PrefixedProperties) properties).getPrefixConfigs(result);
	}
	result.put(result.size(), getPrefixConfig());
	return result;
    }

    @SuppressWarnings("unchecked")
    private <T> T getPrefixedKey(final T key, final boolean useLocalPrefixConfigurations) {
	if (String.class == key.getClass()) {
	    if (containsValidPrefix(key)) {
		return key;
	    } else {
		if (prefixes != null) {
		    return (T) prefixes.getPrefixedKey((String) key, useLocalPrefixConfigurations);
		} else {
		    return key;
		}
	    }
	}
	return key;
    }

    private Set<String> getPrefixes() {
	return prefixes != null ? prefixes.getPrefixes() : Collections.<String> emptySet();
    }

    /**
     * Gets the property.
     * 
     * @param key
     *            the key
     * 
     * @return the property
     * 
     * @inheritDoc
     */
    @Override
    public String getProperty(final String key) {
	lock.readLock().lock();
	try {
	    final boolean useLocalPrefixes = !mixDefaultAndLocalPrefixes && hasLocalPrefixConfigurations();
	    final Object object = get(key, useLocalPrefixes);
	    if (object instanceof String) {
		return (String) object;
	    } else {
		if (object == null) {
		    return null;
		}
		throw new IllegalStateException("The value of " + key + " is of type: " + object.getClass().getName());
	    }
	} finally {
	    lock.readLock().unlock();
	}
    }

    /**
     * Gets the property.
     * 
     * @param value
     *            the value
     * @param def
     *            the def
     * 
     * @return the property
     * 
     * @inheritDoc
     */
    @Override
    public String getProperty(final String value, final String def) {
	final String result = getProperty(value);
	return result == null ? def : result;
    }

    /**
     * Gets the prefixed key and parse it to an byte-value.
     * 
     * @param key
     *            key value
     * @return byte-representation of value
     */
    public short getShort(final String key) {
	final String value = getProperty(key);
	if (value == null) {
	    throw new NumberFormatException("Couldn't parse property to short.");
	}
	return Short.parseShort(value);
    }

    /**
     * Gets the prefixed key and parse it to an short-value.
     * 
     * @param key
     *            key value
     * @param def
     *            default value
     * 
     * @return short-representation of value
     */
    public short getShort(final String key, final short def) {
	try {
	    return getShort(key);
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    /**
     * Gets the prefixed key and parse it to an short[]<br/>
     * Each comma-separated list can be used.
     * 
     * @param key
     *            the key
     * @return short[] or null if the key couldn't get found.
     */
    public short[] getShortArray(final String key) {
	final String[] value = getArray(key);
	if (value != null) {
	    final short[] result = new short[value.length];
	    for (int i = 0; i < value.length; i++) {
		result[i] = Short.parseShort(value[i]);
	    }
	    return result;
	}
	return null;
    }

    /**
     * Gets the prefixed key and parse it to an short[]<br/>
     * Each comma-separated list can be used. If the key couldn't get found, the default will be used.
     * 
     * @param key
     *            the key
     * @param def
     *            default value
     * 
     * @return short[]
     */
    public short[] getShortArray(final String key, final short[] def) {
	try {
	    final short[] result = getShortArray(key);
	    return result != null ? result : def;
	} catch (final NumberFormatException nfe) {
	    return def;
	}
    }

    private List<DoubleEntry<PrefixConfig, String>> getToSetPrefixMap(final List<String> prefixesList, final Map<Integer, PrefixConfig> configs) throws IllegalArgumentException {
	int i = 0;
	PrefixConfig config = null;
	final Map<Integer, PrefixConfig> subConfigs = new HashMap<Integer, PrefixConfig>(configs);
	final List<DoubleEntry<PrefixConfig, String>> result = new LinkedList<DoubleEntry<PrefixConfig, String>>();
	for (final Map.Entry<Integer, PrefixConfig> entry : configs.entrySet()) {
	    if (i == prefixesList.size()) {
		break;
	    }
	    config = entry.getValue();
	    subConfigs.remove(entry.getKey());
	    if (config.isDynamic()) {
		try {
		    result.addAll(getToSetPrefixMap(prefixesList.subList(i, prefixesList.size()), subConfigs));
		    i = prefixesList.size();
		    break;
		} catch (final IllegalArgumentException iae) {
		    result.add(new DoubleEntry<PrefixConfig, String>(config, prefixesList.get(i)));
		    i++;
		}
	    } else if (config.containsValidPrefix(prefixesList.get(i))) {
		result.add(new DoubleEntry<PrefixConfig, String>(config, prefixesList.get(i)));
		i++;
	    }
	}
	if (i < prefixesList.size()) {
	    throw new IllegalArgumentException("Prefix does not match the given PrefixConfig.");
	}
	return result;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getTreeMap() {
	final Map<Integer, PrefixConfig> configs = getPrefixConfigs();
	final Map<String, Object> treeMap = new TreeMap<String, Object>();
	final Properties props = getCompleteProperties();
	String keyName = "";
	String plainProperty = "";
	for (final Enumeration<?> completition = props.propertyNames(); completition.hasMoreElements();) {
	    keyName = (String) completition.nextElement();
	    plainProperty = keyName;
	    Map<String, Object> propertyMap = treeMap;
	    for (int i = 0; i < configs.size(); i++) {
		final PrefixConfig config = configs.get(i);
		boolean found = false;
		for (final Iterator<String> prefixIterator = config.getPrefixes().iterator(); prefixIterator.hasNext() && !found;) {
		    final String prefix = prefixIterator.next();
		    if (plainProperty.startsWith(prefix + PrefixConfig.PREFIXDELIMITER)) {
			Object subMap = propertyMap.get(prefix);
			if (subMap == null) {
			    subMap = new TreeMap<String, Object>();
			    propertyMap.put(prefix, subMap);
			}
			plainProperty = plainProperty.replaceFirst(prefix + PrefixConfig.PREFIXDELIMITER, "");
			found = true;
			if (subMap instanceof Map<?, ?>) {
			    propertyMap = (Map<String, Object>) subMap;
			} else {
			    throw new IllegalStateException("Failed to render JSON-File.");
			}
		    }
		}
	    }
	    propertyMap.put((char) 1 + plainProperty, props.getProperty(keyName));
	}
	return treeMap;
    }

    @SuppressWarnings("unchecked")
    private <T> T getUnprefixedKey(final T key) {
	if (key == null) {
	    throw new IllegalArgumentException("A null key is not allowed.");
	}
	if (String.class == key.getClass()) {
	    String newKey = (String) key;
	    if (properties instanceof PrefixedProperties) {
		newKey = (String) ((PrefixedProperties) properties).getUnprefixedKey(key);
	    }
	    newKey = prefixes.getUnprefixedKey(newKey);
	    return (T) newKey;
	}
	return key;
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = super.hashCode();
	result = prime * result + (prefixes == null ? 0 : prefixes.hashCode());
	result = prime * result + (properties == null ? 0 : properties.hashCode());
	return result;
    }

    /**
     * Checks if there are local prefix configurations existing.
     * 
     * @return
     */
    public boolean hasLocalPrefixConfigurations() {
	return prefixes.containsLocalPrefix() || ((properties instanceof PrefixedProperties) && ((PrefixedProperties) properties).hasLocalPrefixConfigurations());
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#isEmpty()
     */
    @Override
    public boolean isEmpty() {
	lock.readLock().lock();
	try {
	    return entrySet().isEmpty();
	} finally {
	    lock.readLock().unlock();
	}
    }

    private boolean isKeyValid(final Object key) {
	if (key == null) {
	    throw new IllegalArgumentException("A null key is not allowed.");
	}
	if (String.class == key.getClass()) {
	    return !containsValidPrefix(key) || startsWithCurrentPrefix(key);
	}
	return true;
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#keys()
     */
    @SuppressWarnings("unchecked")
    @Override
    public PrefixedPropertiesEnumeration<Object> keys() {
	lock.readLock().lock();
	try {
	    @SuppressWarnings("rawtypes")
	    final Set keys = keySet();
	    @SuppressWarnings("rawtypes")
	    final Iterator it = keys.iterator();
	    return new PrefixedPropertiesEnumerationImpl<Object>(it, true);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#keySet()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public Set<Object> keySet() {
	return new HashSet(getKeyMap(false).keySet());
    }

    /* (non-Javadoc)
     * @see java.util.Properties#list(java.io.PrintStream)
     */
    @Override
    public void list(final PrintStream out) {
	lock.readLock().lock();
	try {
	    properties.list(out);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Properties#list(java.io.PrintWriter)
     */
    @Override
    public void list(final PrintWriter out) {
	lock.readLock().lock();
	try {
	    properties.list(out);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Properties#load(java.io.InputStream)
     */
    @Override
    public void load(final InputStream inStream) throws IOException {
	lock.writeLock().lock();
	try {
	    properties.load(inStream);
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Properties#load(java.io.Reader)
     */
    @Override
    public void load(final Reader reader) throws IOException {
	lock.writeLock().lock();
	try {
	    properties.load(reader);
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /**
     * Loads a json file. Reading from the given InputStream. The InputStream itself will not be closed after usage.
     * 
     * @param is
     *            the is
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void loadFromJSON(final InputStream is) throws IOException {
	lock.writeLock().lock();
	try {
	    final JsonFactory f = new JsonFactory();
	    final JsonParser jp = f.createJsonParser(is);
	    configureJsonParser(jp);
	    if (jp.nextToken() == JsonToken.START_OBJECT) {
		traverseJSON(jp, null);
	    }
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /**
     * Load a json file by using the given Reader. The reader will not be closed by the method.
     * 
     * @param reader
     *            the reader
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void loadFromJSON(final Reader reader) throws IOException {
	lock.writeLock().lock();
	try {
	    final JsonFactory f = new JsonFactory();
	    final JsonParser jp = f.createJsonParser(reader);
	    configureJsonParser(jp);
	    if (jp.nextToken() == JsonToken.START_OBJECT) {
		traverseJSON(jp, null);
	    }
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Properties#loadFromXML(java.io.InputStream)
     */
    @Override
    public void loadFromXML(final InputStream in) throws IOException {
	lock.writeLock().lock();
	try {
	    properties.loadFromXML(in);
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Properties#propertyNames()
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public PrefixedPropertiesEnumeration<?> propertyNames() {
	lock.readLock().lock();
	try {
	    final Set result = new HashSet();
	    for (final Object key : getKeyMap(false).keySet()) {
		result.add(getUnprefixedKey(key));
	    }
	    return new PrefixedPropertiesEnumerationImpl(result.iterator(), true);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#put(java.lang.Object, java.lang.Object)
     */
    @Override
    public Object put(final Object key, final Object value) {
	lock.writeLock().lock();
	try {
	    return properties.put(key, value);
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#putAll(java.util.Map)
     */
    @Override
    public void putAll(final Map<? extends Object, ? extends Object> t) {
	lock.writeLock().lock();
	try {
	    properties.putAll(t);
	} finally {
	    lock.writeLock().unlock();
	}
    }

    private void readObject(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
	prefixes = (PrefixConfig) ois.readObject();
	properties = (Properties) ois.readObject();
	mixDefaultAndLocalPrefixes = ois.readBoolean();
	lock = new ReentrantReadWriteLock();
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#remove(java.lang.Object)
     */
    @Override
    public Object remove(final Object key) {
	lock.writeLock().lock();
	try {
	    final boolean useLocalPrefixes = !mixDefaultAndLocalPrefixes && hasLocalPrefixConfigurations();
	    final Object someKey = getPrefixedKey(key, useLocalPrefixes);
	    Object result = properties.remove(someKey);
	    if (result == null) {
		result = properties.remove(key);
	    }
	    return result;
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /**
     * Removes the all.
     * 
     * @param key
     *            the key
     * @return the map
     */
    public Map<Object, Object> removeAll(final Object key) {
	lock.writeLock().lock();
	try {
	    final Map<Object, Object> result = new HashMap<Object, Object>();
	    Object resultObj;
	    if (containsValidPrefix(key)) {
		resultObj = properties.remove(key);
		if (resultObj != null) {
		    result.put(key, resultObj);
		}
	    } else {
		for (final String prefix : getPrefixes()) {
		    final String pkey = prefix + PrefixConfig.PREFIXDELIMITER + key;
		    resultObj = properties.remove(pkey);
		    if (resultObj != null) {
			result.put(pkey, resultObj);
		    }
		}
		resultObj = properties.remove(key);
		if (resultObj != null) {
		    result.put(key, resultObj);
		}
	    }
	    return result;
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /**
     * Removes the property. That matches the given key. (It has the same function like {@link java.util.Properties#remove(Object)}
     * 
     * @param key
     *            the key
     */
    public void removeProperty(final String key) {
	remove(key);
    }

    /* (non-Javadoc)
     * @see java.util.Properties#save(java.io.OutputStream, java.lang.String)
     */
    @SuppressWarnings("deprecation")
    @Override
    public void save(final OutputStream out, final String comments) {
	lock.readLock().lock();
	try {
	    properties.save(out, comments);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /**
     * Sets the default prefix.
     * 
     * @param prefix
     *            the new default prefix
     */
    public void setDefaultPrefix(final String prefix) {
	lock.writeLock().lock();
	try {
	    final String myPrefix = checkAndConvertPrefix(prefix);
	    final List<String> prefixList = split(myPrefix);
	    setDefaultPrefixes(prefixList);
	} finally {
	    lock.writeLock().unlock();
	}
    }

    private void setDefaultPrefixes(final List<String> prefixesList) {
	final Map<Integer, PrefixConfig> configs = getPrefixConfigs();
	try {
	    final List<DoubleEntry<PrefixConfig, String>> prefixesToSet = getToSetPrefixMap(prefixesList, configs);
	    final List<PrefixConfig> prefixesToSetList = new ArrayList<PrefixConfig>();
	    for (final DoubleEntry<PrefixConfig, String> entry : prefixesToSet) {
		entry.getOne().setDefaultPrefix(entry.getTwo());
		prefixesToSetList.add(entry.getOne());
	    }
	    final Collection<PrefixConfig> allConfigs = getPrefixConfigs().values();
	    for (final PrefixConfig config : allConfigs) {
		if (!prefixesToSetList.contains(config)) {
		    config.setDefaultPrefix(null);//this will remove the defaultPrefix.
		}
	    }
	} catch (final IllegalArgumentException iae) {
	    throw new IllegalArgumentException("The given prefixes are not part of the PrefixConfig: " + prefixesList);
	}
    }

    /**
     * Sets the local Prefix. The local Prefix is Thread depended and will only affect the current thread. You can have a combination of default and local prefix.
     * 
     * @param prefix
     *            the new prefix
     */
    public void setLocalPrefix(final String configuredPrefix) {
	lock.writeLock().lock();
	try {
	    final String myPrefix = checkAndConvertPrefix(configuredPrefix);
	    final List<String> prefixList = split(myPrefix);
	    setPrefixes(prefixList);
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /**
     * Setting to define if default prefixes should be mixed with local prefixes if local prefixes are not present.
     * 
     * @param value
     */
    public void setMixDefaultAndLocalPrefixSettings(final boolean value) {
	this.mixDefaultAndLocalPrefixes = value;
	if (properties instanceof PrefixedProperties) {
	    ((PrefixedProperties) properties).setMixDefaultAndLocalPrefixSettings(value);
	}
    }

    /**
     * Sets the prefix config.
     * 
     * @param config
     *            the new prefix config
     */
    public void setPrefixConfig(final PrefixConfig config) {
	lock.writeLock().lock();
	try {
	    if (config == null) {
		prefixes = EmptyPrefix.INSTANCE;
	    } else {
		prefixes = config;
	    }
	} finally {
	    lock.writeLock().unlock();
	}
    }

    private void setPrefixes(final List<String> prefixesList) {
	final Map<Integer, PrefixConfig> configs = getPrefixConfigs();
	try {
	    final List<DoubleEntry<PrefixConfig, String>> prefixesToSet = getToSetPrefixMap(prefixesList, configs);
	    final List<PrefixConfig> prefixesToSetList = new ArrayList<PrefixConfig>();
	    for (final DoubleEntry<PrefixConfig, String> entry : prefixesToSet) {
		entry.getOne().setPrefix(entry.getTwo());
		prefixesToSetList.add(entry.getOne());
	    }
	    final Collection<PrefixConfig> allConfigs = getPrefixConfigs().values();
	    for (final PrefixConfig config : allConfigs) {
		if (!prefixesToSetList.contains(config)) {
		    config.setPrefix(null);//this will remove the configuredPrefix.
		}
	    }
	} catch (final IllegalArgumentException iae) {
	    throw new IllegalArgumentException("The given prefixes are not part of the PrefixConfig: " + prefixesList);
	}
    }

    /* (non-Javadoc)
     * @see java.util.Properties#setProperty(java.lang.String, java.lang.String)
     */
    @Override
    public Object setProperty(final String key, final String value) {
	lock.writeLock().lock();
	try {
	    return properties.setProperty(key, value);
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#size()
     */
    @Override
    public int size() {
	lock.readLock().lock();
	try {
	    return getKeyMap(false).size();
	} finally {
	    lock.readLock().unlock();
	}
    }

    private List<String> split(final String myPrefix) {
	List<String> prefixList;
	if (myPrefix.indexOf(PrefixConfig.PREFIXDELIMITER) != -1) {
	    prefixList = new ArrayList<String>(Arrays.asList(myPrefix.split("\\" + PrefixConfig.PREFIXDELIMITER)));
	} else {
	    prefixList = new ArrayList<String>(1);
	    prefixList.add(myPrefix);
	}
	return prefixList;
    }

    private boolean startsWithCurrentPrefix(final Object key) {
	if (key != null && String.class == key.getClass()) {
	    return prefixes.startsWithCurrentPrefix((String) key);
	}
	return false;
    }

    /* (non-Javadoc)
     * @see java.util.Properties#store(java.io.OutputStream, java.lang.String)
     */
    @Override
    public void store(final OutputStream out, final String comments) throws IOException {
	lock.readLock().lock();
	try {
	    properties.store(out, comments);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /**
     * Stores a properties-file by using the given comments and encoding.
     * 
     * @param out
     *            the out
     * @param comments
     *            the comments
     * @param encoding
     *            the encoding
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void store(final OutputStream out, final String comments, final String encoding) throws IOException {
	lock.readLock().lock();
	try {
	    properties.store(new OutputStreamWriter(out, Charset.forName(encoding)), comments);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Properties#store(java.io.Writer, java.lang.String)
     */
    @Override
    public void store(final Writer writer, final String comments) throws IOException {
	lock.readLock().lock();
	try {
	    properties.store(writer, comments);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /**
     * Store to json.
     * 
     * @param os
     *            the os
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void storeToJSON(final OutputStream os) throws IOException {
	lock.readLock().lock();
	try {
	    final JsonFactory f = new JsonFactory();
	    final JsonGenerator generator = f.createJsonGenerator(os, JsonEncoding.UTF8);
	    new DefaultPrettyPrinter.Lf2SpacesIndenter();
	    generator.configure(Feature.QUOTE_FIELD_NAMES, false);
	    generator.useDefaultPrettyPrinter();

	    generator.writeStartObject();
	    writeJson(generator, getTreeMap());
	    generator.writeEndObject();
	    generator.flush();
	} finally {
	    lock.readLock().unlock();
	}
    }

    /**
     * Store to json to the given OutputStream writing an additional header as comment.
     * 
     * @param os
     *            the os
     * @param header
     *            the header
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    public void storeToJSON(final OutputStream os, final String header) throws IOException {
	storeToJSON(os, header, "UTF-8");
    }

    public void storeToJSON(final OutputStream os, final String header, final String encoding) throws IOException {
	lock.readLock().lock();
	try {
	    final JsonFactory f = new JsonFactory();
	    final JsonGenerator generator = f.createJsonGenerator(new OutputStreamWriter(os, Charset.forName(encoding)));
	    new DefaultPrettyPrinter.Lf2SpacesIndenter();
	    generator.configure(Feature.QUOTE_FIELD_NAMES, false);
	    generator.useDefaultPrettyPrinter();

	    generator.writeStartObject();
	    if (header != null) {
		generator.writeRaw("/*");
		generator.writeRaw(header);
		generator.writeRaw("*/");
	    }

	    writeJson(generator, getTreeMap());
	    generator.writeEndObject();
	    generator.flush();
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Properties#storeToXML(java.io.OutputStream, java.lang.String)
     */
    @Override
    public void storeToXML(final OutputStream os, final String comment) throws IOException {
	lock.readLock().lock();
	try {
	    properties.storeToXML(os, comment);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Properties#storeToXML(java.io.OutputStream, java.lang.String, java.lang.String)
     */
    @Override
    public void storeToXML(final OutputStream os, final String comment, final String encoding) throws IOException {
	lock.readLock().lock();
	try {
	    properties.storeToXML(os, comment, encoding);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Properties#stringPropertyNames()
     */
    @Override
    public Set<String> stringPropertyNames() {
	lock.readLock().lock();
	try {
	    final Set<String> result = new HashSet<String>();
	    for (final Object key : getKeyMap(true).keySet()) {
		result.add(getUnprefixedKey((String) key));
	    }
	    return result;
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#toString()
     */
    @Override
    public String toString() {
	lock.readLock().lock();
	try {
	    return properties.toString();
	} finally {
	    lock.readLock().unlock();
	}
    }

    private void traverseJSON(final JsonParser jp, final String prefix) throws IOException {
	while (jp.nextToken() != JsonToken.END_OBJECT) {
	    final String fieldname = jp.getCurrentName();
	    if (jp.nextToken() == JsonToken.START_OBJECT) {
		traverseJSON(jp, prefix != null ? prefix + PrefixConfig.PREFIXDELIMITER + fieldname : fieldname);
	    } else {
		final String text = jp.getText();
		put(prefix != null ? prefix + PrefixConfig.PREFIXDELIMITER + fieldname : fieldname, text);
	    }
	}
    }

    /* (non-Javadoc)
     * @see java.util.Hashtable#values()
     */
    @Override
    public Collection<Object> values() {
	lock.readLock().lock();
	try {
	    final List<Object> result = new LinkedList<Object>();
	    final boolean useLocalPrefixes = !mixDefaultAndLocalPrefixes && hasLocalPrefixConfigurations();
	    for (final Object key : getKeyMap(false).values()) {
		result.add(get(key, useLocalPrefixes));
	    }
	    return result;
	} finally {
	    lock.readLock().unlock();
	}
    }

    @SuppressWarnings("unchecked")
    private void writeJson(final JsonGenerator generator, final Map<String, Object> treeMap) throws IOException {
	if (treeMap != null) {
	    for (final Map.Entry<String, Object> entry : treeMap.entrySet()) {
		if (entry.getValue() instanceof String) {
		    generator.writeStringField(entry.getKey().substring(1), (String) entry.getValue());
		} else if (entry.getValue() instanceof Map<?, ?>) {
		    generator.writeObjectFieldStart(entry.getKey());
		    writeJson(generator, (Map<String, Object>) entry.getValue());
		    generator.writeEndObject();
		}
	    }
	}
    }

    private void writeObject(final ObjectOutputStream oos) throws IOException {
	oos.writeObject(prefixes);
	oos.writeObject(properties);
	oos.writeBoolean(mixDefaultAndLocalPrefixes);
    }

}
