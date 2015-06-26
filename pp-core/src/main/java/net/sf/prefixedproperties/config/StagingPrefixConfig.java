package net.sf.prefixedproperties.config;

import java.util.Arrays;

/**
 * The Class StagingPrefixConfig.
 */
public class StagingPrefixConfig extends DefaultPrefixConfig {

    /** The Constant LOCAL. */
    public final static String LOCAL = "local";

    /** The Constant DEV. */
    public final static String DEV = "dev";

    /** The Constant TEST. */
    public final static String TEST = "test";

    /** The Constant QA. */
    public final static String QA = "qa";

    /** The Constant INTEGRATION. */
    public final static String INTEGRATION = "int";

    /** The Constant STAGE. */
    public final static String STAGE = "stg";

    /** The Constant PRELIVE. */
    public final static String PRELIVE = "preliv";

    /** The Constant LIVE. */
    public final static String LIVE = "liv";

    private static final long serialVersionUID = 1L;

    private final static String[] STAGES = { StagingPrefixConfig.LOCAL, StagingPrefixConfig.DEV, StagingPrefixConfig.INTEGRATION, StagingPrefixConfig.QA, StagingPrefixConfig.TEST,
	    StagingPrefixConfig.STAGE, StagingPrefixConfig.PRELIVE, StagingPrefixConfig.LIVE };

    /**
     * Instantiates a new staging prefix config without setting the current prefix.
     */
    public StagingPrefixConfig() {
	super(Arrays.asList(StagingPrefixConfig.STAGES));
    }

    /**
     * Instantiates a new staging prefix config.
     * 
     * @param stage
     *            the stage
     */
    public StagingPrefixConfig(final String stage) {
	super(Arrays.asList(StagingPrefixConfig.STAGES), stage);
    }

}
