package net.sf.prefixedproperties.config;

/**
 * The Class ComponentPrefixConfig.
 */
public class ComponentPrefixConfig extends DefaultPrefixConfig {

    /** The Constant CACHE. */
    public final static String CACHE = "cache";

    /** The Constant COMPONENT1. */
    public final static String DATABASE = "database";

    /** The Constant COMPONENT2. */
    public final static String TRANSACTION_MANAGER = "transaction";

    private static final long serialVersionUID = 1L;

    private final static String[] COMPONENTS = { ComponentPrefixConfig.CACHE, ComponentPrefixConfig.DATABASE, ComponentPrefixConfig.TRANSACTION_MANAGER };

    public ComponentPrefixConfig() {
	super(ComponentPrefixConfig.COMPONENTS);
    }

    /**
     * Instantiates a new component prefix config.
     * 
     * @param prefix
     *            the prefix
     */
    public ComponentPrefixConfig(final String prefix) {
	super(ComponentPrefixConfig.COMPONENTS, prefix);
    }

}
