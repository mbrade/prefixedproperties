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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.util.Properties;

import net.sf.prefixedproperties.PrefixedProperties;

import org.springframework.util.DefaultPropertiesPersister;

/**
 * The Class PrefixedPropertiesPersister.
 */
public class PrefixedPropertiesPersister extends DefaultPropertiesPersister {

	/**
	 * Loads from json.
	 * 
	 * @param props
	 *            the props
	 * @param is
	 *            the is
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void loadFromJson(final Properties props, final InputStream is) throws IOException {
		try {
			((PrefixedProperties) props).loadFromJSON(is);
		} catch (final NoSuchMethodError err) {
			throw new IOException(
					"Cannot load properties JSON file - not using PrefixedProperties: " + err.getMessage());
		}
	}

	/**
	 * Load from json.
	 * 
	 * @param props
	 *            the props
	 * @param rd
	 *            the rd
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void loadFromJson(final Properties props, final Reader rd) throws IOException {
		try {
			((PrefixedProperties) props).loadFromJSON(rd);
		} catch (final NoSuchMethodError err) {
			throw new IOException(
					"Cannot load properties JSON file - not using PrefixedProperties: " + err.getMessage());
		}
	}
	/**
	 * Loads from json.
	 * 
	 * @param props
	 *            the props
	 * @param is
	 *            the is
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void loadFromYAML(final Properties props, final InputStream is) throws IOException {
		try {
			((PrefixedProperties) props).loadFromYAML(is);
		} catch (final NoSuchMethodError err) {
			throw new IOException(
					"Cannot load properties JSON file - not using PrefixedProperties: " + err.getMessage());
		}
	}
	
	/**
	 * Load from json.
	 * 
	 * @param props
	 *            the props
	 * @param rd
	 *            the rd
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void loadFromYAML(final Properties props, final Reader rd) throws IOException {
		try {
			((PrefixedProperties) props).loadFromYAML(rd);
		} catch (final NoSuchMethodError err) {
			throw new IOException(
					"Cannot load properties JSON file - not using PrefixedProperties: " + err.getMessage());
		}
	}

	/**
	 * Store to json.
	 * 
	 * @param props
	 *            the props
	 * @param os
	 *            the os
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void storeToJSON(final Properties props, final OutputStream os) throws IOException {
		try {
			((PrefixedProperties) props).storeToJSON(os);
		} catch (final ClassCastException err) {
			throw new IOException(
					"Cannot store properties JSON file - not using PrefixedProperties: " + err.getMessage());
		}
	}

	/**
	 * Store to json.
	 * 
	 * @param props
	 *            the props
	 * @param os
	 *            the os
	 * @param header
	 *            the header
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void storeToJSON(final Properties props, final OutputStream os, final String header) throws IOException {
		try {
			((PrefixedProperties) props).storeToJSON(os, header);
		} catch (final ClassCastException err) {
			throw new IOException(
					"Cannot store properties JSON file - not using PrefixedProperties: " + err.getMessage());
		}
	}

	/**
	 * Store to json.
	 * 
	 * @param props
	 *            the props
	 * @param os
	 *            the os
	 * @param header
	 *            the header
	 * @param encoding
	 *            the encoding
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void storeToJSON(final Properties props, final OutputStream os, final String header, final String encoding)
			throws IOException {
		try {
			((PrefixedProperties) props).storeToJSON(os, header, encoding);
		} catch (final ClassCastException err) {
			throw new IOException(
					"Cannot store properties JSON file - not using PrefixedProperties: " + err.getMessage());
		}
	}
	/**
	 * Store to json.
	 * 
	 * @param props
	 *            the props
	 * @param os
	 *            the os
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void storeToYAML(final Properties props, final OutputStream os) throws IOException {
		try {
			((PrefixedProperties) props).storeToYAML(os);
		} catch (final ClassCastException err) {
			throw new IOException(
					"Cannot store properties JSON file - not using PrefixedProperties: " + err.getMessage());
		}
	}
	
	/**
	 * Store to json.
	 * 
	 * @param props
	 *            the props
	 * @param os
	 *            the os
	 * @param header
	 *            the header
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void storeToYAML(final Properties props, final OutputStream os, final String header) throws IOException {
		try {
			((PrefixedProperties) props).storeToYAML(os, header);
		} catch (final ClassCastException err) {
			throw new IOException(
					"Cannot store properties JSON file - not using PrefixedProperties: " + err.getMessage());
		}
	}
	
	/**
	 * Store to json.
	 * 
	 * @param props
	 *            the props
	 * @param os
	 *            the os
	 * @param header
	 *            the header
	 * @param encoding
	 *            the encoding
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
	public void storeToYAML(final Properties props, final OutputStream os, final String header, final String encoding)
			throws IOException {
		try {
			((PrefixedProperties) props).storeToYAML(os, header, encoding);
		} catch (final ClassCastException err) {
			throw new IOException(
					"Cannot store properties JSON file - not using PrefixedProperties: " + err.getMessage());
		}
	}

}
