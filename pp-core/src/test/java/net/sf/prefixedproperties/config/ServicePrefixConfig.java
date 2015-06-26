package net.sf.prefixedproperties.config;

/**
 * The Class ServicePrefixConfig.
 */
public class ServicePrefixConfig extends DefaultPrefixConfig {

    /** The Constant SERVICE1. */
    public final static String PRODUCT_SRV = "prdsrv";

    /** The Constant SERVICE2. */
    public final static String ACCOUNTING_SRV = "accsrv";

    /** The Constant SERVICE3. */
    public final static String DELIVERY_SRV = "delsrv";

    private static final long serialVersionUID = 1L;
    private final static String[] SERVICES = { ServicePrefixConfig.PRODUCT_SRV, ServicePrefixConfig.ACCOUNTING_SRV, ServicePrefixConfig.DELIVERY_SRV };

    public ServicePrefixConfig() {
	super(ServicePrefixConfig.SERVICES);
    }

    /**
     * Instantiates a new service prefix config.
     * 
     * @param prefix
     *            the prefix
     */
    public ServicePrefixConfig(final String prefix) {
	super(ServicePrefixConfig.SERVICES, prefix);
    }

}
