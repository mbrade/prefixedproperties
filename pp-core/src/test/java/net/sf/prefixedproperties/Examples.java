/*
 * Copyright (c) 2010, Marco Brade
							[null]
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
package net.sf.prefixedproperties;

import java.util.concurrent.CyclicBarrier;

import net.sf.prefixedproperties.config.DefaultPrefixConfig;
import net.sf.prefixedproperties.config.GenderPrefixConfig;
import net.sf.prefixedproperties.config.StagingPrefixConfig;

public class Examples {

    /**
     * @param args
     */
    public static void main(final String[] args) {
	/*final Properties properties = new PrefixedProperties("SYSTEM_A");
	properties.put("SYSTEM_A.URL", "http://some.fance.url");
	properties.put("SYSTEM_B.URL", "htpps://some.secure.url");
	properties.put("SYSTEM_C.URL", "file:/usingALocalFile.url");
	System.out.println(properties.get("URL"));
	properties.clear();
	properties.put("SYSTEM_B.VALUE", "SPECIAL STUFF");
	properties.put("VALUE", "DEFAULT STUFF");
	System.out.println(properties.get("VALUE"));
	System.out.println(properties.get("SYSTEM_B.VALUE"));

	final Properties properties2 = new PrefixedProperties(new SystemPrefixConfig());
	properties2.put("SYSTEM_B.VALUE", "SPECIAL STUFF");
	properties2.put("VALUE", "DEFAULT STUFF");
	System.out.println(properties2.get("VALUE"));
	System.out.println(properties2.get("SYSTEM_B.VALUE"));

	final PrefixedProperties properties3 = PrefixedProperties.createCascadingPrefixProperties("SYSTEM_A.RIGHTS_DB");
	properties3.put("SYSTEM_A.PRODUCT_DB.PASSWORD", "Cryptic");
	properties3.put("SYSTEM_A.ACCOUNT_DB.PASSWORD", "Secure");
	properties3.put("SYSTEM_A.RIGHTS_DB.PASSWORD", "Obscure");
	properties3.put("SYSTEM_B.RIGHTS_DB.PASSWORD", "BObscure");
	properties3.put("SYSTEM_B.ACCOUNT_DB.PASSWORD", "BSecure");
	properties3.put("SYSTEM_B.PRODUCT_DB.PASSWORD", "BCryptic");
	System.out.println(properties3.get("PASSWORD"));
	properties3.setConfiguredPrefix("SYSTEM_A.ACCOUNT_DB");
	System.out.println(properties3.get("PASSWORD"));
	properties3.setConfiguredPrefix("SYSTEM_B");
	System.out.println(properties3.get("PASSWORD"));*/
	/*
	        final PrefixedProperties properties4 = PrefixedProperties.createCascadingPrefixProperties(new StagingPrefixConfig(), new ServicePrefixConfig(), new ComponentPrefixConfig());
	        properties4.setConfiguredPrefix(StagingPrefixConfig.LIVE + "." + ServicePrefixConfig.DELIVERY_SRV + "." + ComponentPrefixConfig.DATABASE);
	        properties4.setProperty(StagingPrefixConfig.LIVE + "." + ServicePrefixConfig.DELIVERY_SRV + "." + ComponentPrefixConfig.DATABASE + ".password", "A password");
	        properties4.setProperty(StagingPrefixConfig.LIVE + ".connections", "10");
	        properties4.setProperty(StagingPrefixConfig.LIVE + "." + ServicePrefixConfig.DELIVERY_SRV + ".connections", "20");
	        properties4.setProperty(ServicePrefixConfig.ACCOUNTING_SRV + ".connections", "30");
	        System.out.println(properties4.get("connections"));
	        properties4.setConfiguredPrefix(ServicePrefixConfig.PRODUCT_SRV);
	        System.out.println(properties4.get("connections"));
	        properties4.setConfiguredPrefix(ServicePrefixConfig.ACCOUNTING_SRV);
	        System.out.println(properties4.get("connections"));*/

	final PrefixedProperties properties5 = PrefixedProperties.createCascadingPrefixProperties(new StagingPrefixConfig("test"), new GenderPrefixConfig());
	properties5.setProperty("key1", "value1");
	properties5.setProperty("liv.key2", "livvalue2");
	properties5.setProperty("test.key2", "testvalue2");
	properties5.setProperty("test.f.key2", "female test value2");
	properties5.setProperty("test.m.key2", "male test value2");
	final int count = 10;
	final CyclicBarrier barrier = new CyclicBarrier(count);
	for (int i = 0; i < count / 2; i++) {
	    final Thread th = new Thread() {
		@Override
		public void run() {
		    properties5.setLocalPrefix(GenderPrefixConfig.MALE);
		    try {
			barrier.await();
		    } catch (final Exception e) {
		    }
		    System.out.println("MALE: " + properties5.getProperty("key2"));
		}
	    };
	    th.start();

	    final Thread th2 = new Thread() {
		@Override
		public void run() {
		    properties5.setLocalPrefix(GenderPrefixConfig.FEMALE);
		    try {
			barrier.await();
		    } catch (final Exception e) {
		    }
		    System.out.println("FEMALE: " + properties5.getProperty("key2"));
		}
	    };
	    th2.start();
	}

    }

    @SuppressWarnings("unused")
    private static class SystemPrefixConfig extends DefaultPrefixConfig {

	private static final long serialVersionUID = 1L;

	public SystemPrefixConfig() {
	    super(new String[] { "SYSTEM_A", "SYSTEM_B", "SYSTEM_C" }, "SYSTEM_A");
	}

    }

}
