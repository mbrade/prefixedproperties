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

}
