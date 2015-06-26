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

import java.util.Set;

/**
 * The Class DynamicPrefixConfig will take any kind of Prefix and add it as a known prefix list.
 */
public final class DynamicPrefixConfig extends DefaultPrefixConfig {

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new dynamic PrefixConfig.
     */
    public DynamicPrefixConfig() {
    }

    /**
     * Instantiates a new dynamic PrefixConfig.
     * 
     * @param prefix
     *            the prefix
     */
    public DynamicPrefixConfig(final String prefix) {
	super(new String[] { prefix }, prefix);
    }

    @Override
    public boolean isDynamic() {
	return true;
    }

    @Override
    public void setDefaultPrefix(final String defaultPrefix) {
	addPrefix(defaultPrefix);
	super.setDefaultPrefix(defaultPrefix);
    }

    /* (non-Javadoc)
     * @see net.sf.prefixedproperties.config.AbstractPrefixConfig#setPrefix(java.lang.String)
     */
    @Override
    public void setPrefix(final String prefixString) {
	addPrefix(prefixString);
	super.setPrefix(prefixString);
    }

    /**
     * Adds the given prefix to the PrefixConfig.
     * 
     * @param prefixString
     *            the prefix string
     */
    protected synchronized void addPrefix(final String prefixString) {
	if (prefixString != null) {
	    final Set<String> prefixes = getPrefixes();
	    prefixes.add(prefixString);
	    setPrefixes(prefixes);
	}
    }

}
