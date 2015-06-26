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
