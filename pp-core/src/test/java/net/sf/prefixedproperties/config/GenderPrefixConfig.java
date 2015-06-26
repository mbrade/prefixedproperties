package net.sf.prefixedproperties.config;

/**
 * The Class GenderPrefixConfig.
 */
public class GenderPrefixConfig extends DefaultPrefixConfig {

    /** The Constant MALE. */
    public static final String MALE = "m";

    /** The Constant FEMALE. */
    public static final String FEMALE = "f";

    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new gender prefix config.
     */
    public GenderPrefixConfig() {
	super(new String[] { "m", "f" });
    }

}
