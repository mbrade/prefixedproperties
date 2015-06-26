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
	    throw new IOException("Cannot load properties JSON file - not using PrefixedProperties: " + err.getMessage());
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
	    throw new IOException("Cannot load properties JSON file - not using PrefixedProperties: " + err.getMessage());
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
	    throw new IOException("Cannot store properties JSON file - not using PrefixedProperties: " + err.getMessage());
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
	    throw new IOException("Cannot store properties JSON file - not using PrefixedProperties: " + err.getMessage());
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
    public void storeToJSON(final Properties props, final OutputStream os, final String header, final String encoding) throws IOException {
	try {
	    ((PrefixedProperties) props).storeToJSON(os, header, encoding);
	} catch (final ClassCastException err) {
	    throw new IOException("Cannot store properties JSON file - not using PrefixedProperties: " + err.getMessage());
	}
    }

}
