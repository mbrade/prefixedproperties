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
