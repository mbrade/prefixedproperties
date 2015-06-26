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
package net.sf.prefixedproperties;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import junit.framework.Assert;

import org.junit.Test;

public class PrefixedPropertiesResourceBundleTest {

    @Test
    public void testResourceBundle() throws IOException {

	Locale.setDefault(Locale.TAIWAN); //we have to set another default locale otherwise we will get a german or english resourcebundle as fallback.
	final ResourceBundle r1 = ResourceBundle.getBundle("rv", Locale.CHINESE);
	Assert.assertEquals("okay", r1.getString("test"));

	final PrefixedResourceBundle rb3 = PrefixedResourceBundle.getPrefixedResourceBundle("rv", Locale.CHINESE, "local");
	Assert.assertEquals("ok", rb3.getString("test"));

	final PrefixedResourceBundle rb = PrefixedResourceBundle.getPrefixedResourceBundle("rv", Locale.GERMANY, "local");
	Assert.assertEquals("wahr", rb.getString("test"));

	final PrefixedResourceBundle rb2 = PrefixedResourceBundle.getPrefixedResourceBundle("rv", Locale.ENGLISH, "local");
	Assert.assertEquals("true", rb2.getString("test"));

	final PrefixedResourceBundle rb4 = PrefixedResourceBundle.getPrefixedResourceBundle("rv", Locale.GERMANY, "local");
	Assert.assertEquals("wahr", rb4.getString("test"));

	rb4.getPrefixedProperties().setLocalPrefix("male");
	Assert.assertEquals("Hallo Herr Marco", MessageFormat.format(rb4.getString("greetings"), "Marco"));

	rb4.getPrefixedProperties().setLocalPrefix("female");
	Assert.assertEquals("Hallo Frau Marco", MessageFormat.format(rb4.getString("greetings"), "Marco"));

	rb2.getPrefixedProperties().setLocalPrefix("male");
	Assert.assertEquals("Hello Mr. Marco", MessageFormat.format(rb2.getString("greetings"), "Marco"));

	rb2.getPrefixedProperties().setLocalPrefix("female");
	Assert.assertEquals("Hello Mrs. Marco", MessageFormat.format(rb2.getString("greetings"), "Marco"));
    }
}
