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
	private final static String[] SERVICES = { ServicePrefixConfig.PRODUCT_SRV, ServicePrefixConfig.ACCOUNTING_SRV,
			ServicePrefixConfig.DELIVERY_SRV };

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
