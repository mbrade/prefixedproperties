/*
 * Copyright (c) 2010, Marco Brade
							[null]
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
package net.sf.prefixedproperties.config;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.commons.lang.StringUtils;

/**
 * The Class AbstractPrefixConfig.
 */
public class DefaultPrefixConfig implements PrefixConfig {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The prefixes. */
    private Set<String> prefixes = new TreeSet<String>();

    /** The lock. */
    private transient ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private volatile String defaultPrefix;

    private transient ThreadLocal<String> localPrefix = new ThreadLocal<String>();

    /**
     * Instantiates a new prefix store.
     */
    public DefaultPrefixConfig() {
    }

    /**
     * Instantiates a new default prefix config.
     * 
     * @param prefixesCollection
     *            the prefixes collection to be used as known prefixes
     */
    public DefaultPrefixConfig(final Collection<String> prefixesCollection) {
	setPrefixes(prefixesCollection);
    }

    /**
     * Instantiates a new prefix store. Using the given prefixesCollection as known prefixes. The given prefix will be used as defaultPrefix and has to be part of the given prefixesCollection
     * 
     * @param prefixesCollection
     *            the prefixes collection to be used as known prefixes
     * @param prefixString
     *            the default prefix string
     */
    public DefaultPrefixConfig(final Collection<String> prefixesCollection, final String prefixString) {
	this(prefixesCollection);
	setDefaultPrefix(prefixString);
    }

    /**
     * Instantiates a new default prefix config.
     * 
     * @param prefixesCollection
     *            the prefixes collection to be used as known prefixes
     */
    public DefaultPrefixConfig(final String[] prefixesCollection) {
	this(Arrays.asList(prefixesCollection));
    }

    /**
     * Instantiates a new abstract prefix config.
     * 
     * @param prefixesArray
     *            the prefixes collection to be used as known prefixes
     * @param prefixString
     *            the prefix string to bes used as default prefix
     */
    public DefaultPrefixConfig(final String[] prefixesArray, final String prefixString) {
	this(Arrays.asList(prefixesArray), prefixString);
    }

    /**
     * Clears the prefixes, the default prefix and set the delimiter to '.'.
     */
    public void clear() {
	lock.writeLock().lock();
	try {
	    prefixes.clear();
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /**
     * Creates a clone.
     * 
     * @return the clone
     */
    @Override
    public PrefixConfig clone() {
	try {
	    final DefaultPrefixConfig config = (DefaultPrefixConfig) super.clone(); //shallowcopy
	    config.prefixes = new TreeSet<String>(prefixes); //deepcopy
	    config.localPrefix = new ThreadLocal<String>();
	    config.localPrefix.set(localPrefix.get());
	    return config;
	} catch (final CloneNotSupportedException e) {
	    throw new IllegalStateException("Failure during cloning", e);
	}
    }

    @Override
    public boolean containsLocalPrefix() {
	return getLocalPrefix() != null;
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.PrefixConfig#containsValidPrefix(java.lang.String)
     */
    @Override
    public boolean containsValidPrefix(final String key) {
	if (key != null) {
	    lock.readLock().lock();
	    try {
		for (final String aPrefix : prefixes) {
		    if (key.equals(aPrefix) || key.startsWith(aPrefix + getPrefixDelimiter())) {
			return true;
		    }
		}
	    } finally {
		lock.readLock().unlock();
	    }
	}
	return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
	if (this == obj) {
	    return true;
	}
	if (obj == null) {
	    return false;
	}
	if (getClass() != obj.getClass()) {
	    return false;
	}
	final DefaultPrefixConfig other = (DefaultPrefixConfig) obj;
	if (prefixes == null) {
	    if (other.prefixes != null) {
		return false;
	    }
	} else if (!prefixes.equals(other.prefixes)) {
	    return false;
	}
	return true;
    }

    @Override
    public String getLocalPrefix() {
	lock.readLock().lock();
	try {
	    return localPrefix.get();
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.PrefixConfig#getPrefix()
     */
    @Override
    public String getPrefix() {
	final String result = localPrefix.get();
	if (result == null) {
	    return defaultPrefix;
	}
	return result;
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.PrefixConfig#getPrefixedKey(java.lang.String)
     */
    @Override
    public String getPrefixedKey(final String key, final boolean useOnlyLocalPrefixes) {
	lock.readLock().lock();
	try {
	    final String prefix = (useOnlyLocalPrefixes) ? getLocalPrefix() : getPrefix();
	    return (prefix != null) ? getPrefixedKey(prefix, key) : key;
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.PrefixConfig#getPrefixedKey(java.lang.String, java.lang.String)
     */
    @Override
    public String getPrefixedKey(final String prefixString, final String key) {
	lock.readLock().lock();
	try {
	    if (prefixString != null && prefixes.contains(prefixString) && key != null) {
		return new StringBuilder(prefixString).append(getPrefixDelimiter()).append(key).toString();
	    }
	} finally {
	    lock.readLock().unlock();
	}
	throw new IllegalArgumentException("The given prefix is not part of this PrefixConfig or the key is null.");
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.PrefixConfig#getPrefixes()
     */
    @Override
    public Set<String> getPrefixes() {
	lock.readLock().lock();
	try {
	    return new TreeSet<String>(prefixes);
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.PrefixConfig#getPrefixPart(java.lang.String)
     */
    @Override
    public String getPrefixPart(final String key) {
	if (key != null) {
	    lock.readLock().lock();
	    try {
		for (final String prefixString : prefixes) {
		    if (key.startsWith(prefixString + getPrefixDelimiter())) {
			return prefixString + getPrefixDelimiter();
		    }
		}
	    } finally {
		lock.readLock().unlock();
	    }
	    return "";
	}
	throw new IllegalArgumentException("The given key is null.");
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.PrefixConfig#getUnprefixedKey(java.lang.String)
     */
    @Override
    public String getUnprefixedKey(final String key) {
	return key.replaceFirst("^" + getPrefixPart(key), "");
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	if (prefixes != null) {
	    //	    	    result = prime * result + prefixes.hashCode();
	    for (final String prefix : prefixes) {
		result = prime * result + prefix.hashCode();
	    }
	}
	return result;
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.PrefixConfig#isDynamic()
     */
    @Override
    public boolean isDynamic() {
	return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<String> iterator() {
	return getIterator();
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.PrefixConfig#setDefaultPrefix(java.lang.String)
     */
    @Override
    public void setDefaultPrefix(final String prefixString) {
	lock.writeLock().lock();
	try {
	    if (prefixString == null || StringUtils.isBlank(prefixString)) {
		defaultPrefix = null;
	    } else {
		if (prefixes != null) {
		    if (!prefixes.contains(prefixString)) {
			throw new IllegalArgumentException("The given prefix is not part of the prefixes.");
		    }
		}
		defaultPrefix = prefixString;
	    }
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /**
     * Sets the prefix for the current thread.
     * 
     * @param prefixString
     *            the new prefix
     */
    @Override
    public void setPrefix(final String prefixString) {
	lock.writeLock().lock();
	try {
	    if (prefixString == null || StringUtils.isBlank(prefixString)) {
		localPrefix.remove();
	    } else {
		if (prefixes != null) {
		    if (!prefixes.contains(prefixString)) {
			throw new IllegalArgumentException("The given prefix is not part of the prefixes.");
		    }
		}
		localPrefix.set(prefixString);
	    }
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.PrefixConfig#startsWithCurrentPrefix(java.lang.String)
     */
    @Override
    public boolean startsWithCurrentPrefix(final String key) {
	lock.readLock().lock();
	try {
	    if (getPrefix() != null && key != null) {
		return key.startsWith(getPrefix() + getPrefixDelimiter());
	    }
	} finally {
	    lock.readLock().unlock();
	}
	return false;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
	return "PrefixConfig [prefixDelimiter=" + PREFIXDELIMITER + ", prefixes=" + prefixes + "]";
    }

    /**
     * Gets the iterator.
     * 
     * @return the iterator
     */
    private Iterator<String> getIterator() {
	final Set<String> copy = getPrefixes();
	final Iterator<String> copyIt = copy.iterator();
	return new Iterator<String>() {
	    private String lastOne = null;

	    @Override
	    public boolean hasNext() {
		return copyIt.hasNext();
	    }

	    @Override
	    public String next() {
		if (copyIt.hasNext()) {
		    lastOne = copyIt.next();
		} else {
		    throw new NoSuchElementException();
		}
		return lastOne;
	    }

	    @Override
	    public void remove() {
		try {
		    lock.writeLock().lock();
		    prefixes.remove(lastOne);
		} finally {
		    lock.writeLock().unlock();
		}
	    }

	};
    }

    private void readObject(final ObjectInputStream ois) throws ClassNotFoundException, IOException {
	defaultPrefix = (String) ois.readObject();
	final int size = ois.readInt();
	prefixes = new TreeSet<String>();
	for (int i = 0; i < size; i++) {
	    prefixes.add((String) ois.readObject());
	}
	localPrefix = new ThreadLocal<String>();
	localPrefix.set((String) ois.readObject());
	lock = new ReentrantReadWriteLock();

    }

    private void writeObject(final ObjectOutputStream oos) throws IOException {
	lock.readLock().lock();
	try {
	    oos.writeObject(defaultPrefix);
	    oos.writeInt(prefixes.size());
	    for (final String aPrefix : prefixes) {
		oos.writeObject(aPrefix);
	    }
	    oos.writeObject(localPrefix.get());
	} finally {
	    lock.readLock().unlock();
	}
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.PrefixC#getPrefixDelimiter()
     */
    /**
     * Gets the prefix delimiter.
     * 
     * @return the prefix delimiter
     */
    protected char getPrefixDelimiter() {
	return PREFIXDELIMITER;
    }

    /**
     * Sets the prefixes.
     * 
     * @param prefixesToSet
     *            the new prefixes
     */
    protected void setPrefixes(final Collection<String> prefixesToSet) {
	if (prefixesToSet == null) {
	    throw new IllegalArgumentException("The given prefixSet is not allowed to be null.");
	}
	final Set<String> newPrefixes = new TreeSet<String>();
	for (final String prefixString : prefixesToSet) {
	    if (StringUtils.isNotBlank(prefixString)) {
		newPrefixes.add(prefixString);
	    }
	}
	lock.writeLock().lock();
	try {
	    prefixes = newPrefixes;
	    if (getPrefix() != null && !prefixes.contains(getPrefix())) {
		defaultPrefix = null;
		localPrefix.remove();
	    }
	} finally {
	    lock.writeLock().unlock();
	}
    }

    /**
     * Sets the prefixes.
     * 
     * @param prefixesToSet
     *            the new prefixes
     */
    protected void setPrefixes(final String... prefixesToSet) {
	setPrefixes(Arrays.asList(prefixesToSet));
    }

}
