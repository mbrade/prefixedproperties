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

import java.io.Serializable;
import java.util.Set;

import net.sf.prefixedproperties.PrefixedProperties;

/**
 * The Interface PrefixConfig.
 */
public interface PrefixConfig extends Iterable<String>, Serializable, Cloneable {

    /** The PREFIXDELIMITER. */
    char PREFIXDELIMITER = '.';
    String PREFIXDELIMITER_STRING = "" + PREFIXDELIMITER;

    /**
     * Clones a config.
     * 
     * @return the prefix config
     */
    PrefixConfig clone();

    /**
     * If local prefix is set {@link PrefixedProperties} can decide to call {@link #getLocalPrefix()} instead of {@link #getPrefix()}
     * 
     * @return
     */
    boolean containsLocalPrefix();

    /**
     * Contains valid prefix.
     * 
     * @param key
     *            the key
     * @return true, if successful
     */
    boolean containsValidPrefix(String key);

    /**
     * Gets the local set prefix instead of the default one. This method will never fall back to the default prefix.
     * 
     * @return
     */
    String getLocalPrefix();

    /**
     * Gets the {@link ThreadLocal} prefix.
     * 
     * @return the prefix
     */
    String getPrefix();

    /**
     * Checks and prepends the current prefix if the given key starts not with the current prefix.
     * 
     * @param key
     *            the key
     * @param useOnlyLocalPrefixes
     *            forces to use only the local prefix if available
     * @return the prefixed key
     */
    String getPrefixedKey(String key, boolean useOnlyLocalPrefixes);

    /**
     * Checks and prepends the given prefix if the given key starts not with the given prefix.
     * 
     * @param prefix
     *            the prefix
     * @param key
     *            the key
     * @return the prefixed key
     */
    String getPrefixedKey(String prefix, String key);

    /**
     * Gets the prefixes.
     * 
     * @return the prefixes
     */
    Set<String> getPrefixes();

    /**
     * Gets the prefix part.
     * 
     * @param key
     *            the key
     * @return the prefix part
     */
    String getPrefixPart(String key);

    /**
     * Gets the key without the local or current prefix set.
     * 
     * @param key
     *            the key
     * @return the unprefixed key
     */
    String getUnprefixedKey(String key);

    /**
     * Checks if this PrefixConfig is dynamic and would accept prefixes on a dynamic basis.
     * 
     * @return true, if is dynamic
     */
    boolean isDynamic();

    /**
     * Sets the default prefix which will be used for all prefix if the {@link ThreadLocal} has not been set.
     * 
     * @param prefix
     *            the new default prefix
     */
    void setDefaultPrefix(String prefix);

    /**
     * Sets the prefix on a {@link ThreadLocal} basis. If the given prefix is null or empty the configured prefix will be removed and the defaultPrefix will be used if set.
     * 
     * @param prefix
     *            the new prefix
     */
    void setPrefix(String prefix);

    /**
     * Starts with current prefix.
     * 
     * @param key
     *            the key
     * @return true, if successful
     */
    boolean startsWithCurrentPrefix(String key);

}
