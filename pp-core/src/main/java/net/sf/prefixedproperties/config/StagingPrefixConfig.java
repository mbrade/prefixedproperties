/*
 * Copyright (c) 2010, Marco Brade [https://sourceforge.net/users/mbrade] All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
