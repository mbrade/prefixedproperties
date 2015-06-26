/*
 *
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
package net.sf.prefixedproperties;

import java.util.Properties;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class PropertyPlaceHolderTest {

    @Test
    public void testPrefixedPropertiesPlaceholderConfigurer() {
	System.setProperty("environment", "test");
	final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
	final Bean bean1 = (Bean) context.getBean("bean1");
	Assert.assertEquals("myValue1", bean1.getValue());
	Assert.assertEquals("testvalue", bean1.getTest());
	final Properties props = (Properties) context.getBean("log4j");
	Assert.assertEquals("INFO, CONSOLE1", props.get("log4j.rootLogger"));
    }

    @Test
    public void testPrefixedPropertyOverrideConfigurer() {
	System.setProperty("environment", "test");
	final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
	final Bean bean1 = (Bean) context.getBean("bean2");
	Assert.assertEquals("overriddenValue", bean1.getValue());
    }

}
