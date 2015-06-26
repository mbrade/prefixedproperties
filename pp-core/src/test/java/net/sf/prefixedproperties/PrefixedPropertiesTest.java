package net.sf.prefixedproperties;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;
import net.sf.prefixedproperties.config.ComponentPrefixConfig;
import net.sf.prefixedproperties.config.DynamicPrefixConfig;
import net.sf.prefixedproperties.config.PrefixConfig;
import net.sf.prefixedproperties.config.ServicePrefixConfig;
import net.sf.prefixedproperties.config.StagingPrefixConfig;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * The Class PrefixedPropertiesTest.
 */
public final class PrefixedPropertiesTest {

    /** The config. */
    private final PrefixConfig config = new StagingPrefixConfig(StagingPrefixConfig.TEST);

    /** The properties. */
    private final PrefixedProperties properties = new PrefixedProperties(config);

    private String getPrefixKey(final String key) {
	return properties.getPrefixConfig().getPrefixedKey(key, false);
    }

    /**
     * Setup.
     */
    @Before
    public void setUp() {

    }

    /**
     * Teardown.
     */
    @After
    public void tearDown() {
	properties.clear();
    }

    /**
     * Test add properties.
     */
    @Test
    public void testAddProperties() {
	Assert.assertTrue(properties.isEmpty());
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	Assert.assertEquals(1, properties.size());
	Assert.assertEquals("TEST-A", properties.getProperty("KEYA"));
	properties.setProperty("KEYB", "B");
	Assert.assertEquals(2, properties.size());
	Assert.assertEquals("B", properties.getProperty("KEYB"));
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	Assert.assertEquals(2, properties.size());
	Assert.assertEquals("TEST-B", properties.getProperty("KEYB"));
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "C", "LIVE-C");
	Assert.assertEquals(2, properties.size());
	Assert.assertFalse(properties.isEmpty());
    }

    /**
     * Test array.
     */
    @Test
    public void testArray() {
	properties.setProperty(getPrefixKey("Array"), "one,two,three");
	String[] result = properties.getArray("Array", new String[] { "three", "four", "five" });
	Assert.assertEquals("one", result[0]);
	Assert.assertEquals("two", result[1]);
	Assert.assertEquals("three", result[2]);
	result = properties.getArray("Array2", new String[] { "three", "four", "five" });
	Assert.assertEquals("three", result[0]);
	Assert.assertEquals("four", result[1]);
	Assert.assertEquals("five", result[2]);
    }

    /**
     * Test boolean.
     */
    @Test
    public void testBoolean() {
	properties.setProperty(getPrefixKey("Boolean"), "true");
	Assert.assertEquals(true, properties.getBoolean("Boolean", false));
	Assert.assertEquals(true, properties.getBoolean("Boolean"));
	Assert.assertEquals(false, properties.getBoolean("Boolean2", false));
    }

    /**
     * Test boolean array.
     */
    @Test
    public void testBooleanArray() {
	properties.setProperty(getPrefixKey("Array"), "true,true,false");
	boolean[] result = properties.getBooleanArray("Array", new boolean[] { false, false, true });
	Assert.assertEquals(true, result[0]);
	Assert.assertEquals(true, result[1]);
	Assert.assertEquals(false, result[2]);
	result = properties.getBooleanArray("Array2", new boolean[] { false, false, true });
	Assert.assertEquals(false, result[0]);
	Assert.assertEquals(false, result[1]);
	Assert.assertEquals(true, result[2]);
    }

    /**
     * Test byte.
     */
    @Test
    public void testByte() {
	properties.setProperty(getPrefixKey("Byte"), "2");
	Assert.assertEquals(2, properties.getByte("Byte", (byte) 3));
	Assert.assertEquals(3, properties.getByte("Byte2", (byte) 3));
    }

    /**
     * Test byte array.
     */
    @Test
    public void testByteArray() {
	properties.setProperty(getPrefixKey("Array"), "1,2,3");
	byte[] result = properties.getByteArray("Array", new byte[] { (byte) 1, (byte) 2, (byte) 3 });
	Assert.assertEquals(1, result[0]);
	Assert.assertEquals(2, result[1]);
	Assert.assertEquals(3, result[2]);
	result = properties.getByteArray("Array2", new byte[] { (byte) 4, (byte) 5, (byte) 6 });
	Assert.assertEquals(4, result[0]);
	Assert.assertEquals(5, result[1]);
	Assert.assertEquals(6, result[2]);
    }

    /**
     * Test cascading prefix properties.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testCascadingPrefixProperties() throws IOException {
	final PrefixedProperties serviceProperties = PrefixedProperties.createCascadingPrefixProperties(new StagingPrefixConfig(StagingPrefixConfig.TEST),
		new ServicePrefixConfig(
			ServicePrefixConfig.PRODUCT_SRV));

	final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("prefixed.properties");
	try {
	    serviceProperties.load(is);
	} finally {
	    is.close();
	}
	serviceProperties.setMixDefaultAndLocalPrefixSettings(false);
	Assert.assertEquals("property1 of service1 in environment test", serviceProperties.get("prop1"));
	Assert.assertEquals("property2 of service1 in environment test", serviceProperties.get("prop2"));
	Assert.assertEquals("property3 of service1", serviceProperties.get("prop3"));
	Assert.assertEquals("property4", serviceProperties.get("prop4"));
	Assert.assertEquals("property5 in environment test", serviceProperties.get("prop5"));

	serviceProperties.setDefaultPrefix(StagingPrefixConfig.TEST + "." + ServicePrefixConfig.ACCOUNTING_SRV);
	Assert.assertEquals("property1 of service2 in environment test", serviceProperties.get("prop1"));
	Assert.assertEquals("property2 of service2 in environment test", serviceProperties.get("prop2"));
	Assert.assertEquals("property3 of service2 in environment test", serviceProperties.get("prop3"));
	Assert.assertEquals("property4", serviceProperties.get("prop4"));
	Assert.assertEquals("property5 in environment test", serviceProperties.get("prop5"));
	Assert.assertEquals("property6", serviceProperties.get("prdsrv.prop6"));

	//	serviceProperties.setLocalPrefix(StagingPrefixConfig.TEST + "." + ServicePrefixConfig.ACCOUNTING_SRV);
	serviceProperties.setLocalPrefix(ServicePrefixConfig.ACCOUNTING_SRV);
	Assert.assertEquals(null, serviceProperties.get("prop1"));
	Assert.assertEquals(null, serviceProperties.get("prop2"));
	Assert.assertEquals("property3 of service2", serviceProperties.get("prop3"));
	Assert.assertEquals("property4", serviceProperties.get("prop4"));
	Assert.assertEquals("property5", serviceProperties.get("prop5"));
	Assert.assertEquals("property6", serviceProperties.get("prdsrv.prop6"));

    }

    public void testClearConfiguredPrefix() {

    }

    @Test
    public void testClearPrefixConfig() {
	final PrefixedProperties props2 = PrefixedProperties.createCascadingPrefixProperties(new StagingPrefixConfig(), new DynamicPrefixConfig(), new DynamicPrefixConfig(), new ComponentPrefixConfig());
	props2.setLocalPrefix("test");
	Assert.assertEquals("test", props2.getEffectivePrefix());
	try {
	    props2.setLocalPrefix("test.anything.cache");
	    Assert.assertEquals("test.anything.cache", props2.getEffectivePrefix());
	    props2.setLocalPrefix("test");
	    Assert.assertEquals("test", props2.getEffectivePrefix());
	    props2.setLocalPrefix("test.something..wrong");
	    Assert.fail();
	} catch (final Exception e) {
	    Assert.assertEquals("test", props2.getEffectivePrefix());
	}
	props2.setLocalPrefix("test.anything.cache");
	Assert.assertEquals("test.anything.cache", props2.getEffectivePrefix());
	props2.setLocalPrefix("*");
	Assert.assertEquals("", props2.getEffectivePrefix());

	props2.setLocalPrefix("test.anything.cache");
	Assert.assertEquals("test.anything.cache", props2.getEffectivePrefix());
	props2.setDefaultPrefix("dev.nothing.database");
	Assert.assertEquals("test.anything.cache", props2.getEffectivePrefix());
	props2.setLocalPrefix("*");//clears the configuredprefix
	Assert.assertEquals("dev.nothing.database", props2.getEffectivePrefix());//will fall to the defaults
	props2.setLocalPrefix("test.anything.cache");
	Assert.assertEquals("test.anything.cache", props2.getEffectivePrefix());
	props2.setDefaultPrefix("*"); //clear the defaults
	Assert.assertEquals("test.anything.cache", props2.getEffectivePrefix());
	props2.setLocalPrefix("*");//clears the configuredprefix
	Assert.assertEquals("", props2.getEffectivePrefix()); //since fallback is cleared, the configuredprefix will be empty.

    }

    /**
     * Test clone and equals.
     */
    @Test
    public void testCloneAndEquals() {
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "C", "LIVE-C");
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	properties.put(new Integer(5), new Integer(50));
	Assert.assertTrue(properties.contains("TEST-A"));
	Assert.assertTrue(properties.contains("TEST-B"));
	Assert.assertFalse(properties.contains("KEYA"));
	Assert.assertFalse(properties.contains("TESTC"));
	Assert.assertTrue(properties.contains(new Integer(50)));
	Assert.assertFalse(properties.contains(new Integer(5)));
	Assert.assertEquals(properties.clone(), properties);
	Assert.assertEquals(properties.clone().hashCode(), properties.hashCode());
	final PrefixedProperties props2 = properties.clone();
	Assert.assertTrue(props2.contains("TEST-A"));
	Assert.assertTrue(props2.contains("TEST-B"));
	Assert.assertFalse(props2.contains("KEYA"));
	Assert.assertFalse(props2.contains("TESTC"));
	Assert.assertTrue(props2.contains(new Integer(50)));
	Assert.assertFalse(props2.contains(new Integer(5)));
	Assert.assertNotSame(props2, properties);
	props2.getPrefixConfig().setPrefix(StagingPrefixConfig.DEV);
	Assert.assertFalse(props2.equals(properties));

    }

    /**
     * Test contains.
     */
    @Test
    public void testContains() {
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "C", "LIVE-C");
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	properties.put(new Integer(5), new Integer(50));
	Assert.assertTrue(properties.containsValue("TEST-A"));
	Assert.assertTrue(properties.containsValue("TEST-B"));
	Assert.assertFalse(properties.containsValue("KEYA"));
	Assert.assertFalse(properties.containsValue("TESTC"));
	Assert.assertTrue(properties.containsValue(new Integer(50)));
	Assert.assertFalse(properties.containsValue(new Integer(5)));
	Assert.assertFalse(properties.contains(null));
    }

    /**
     * Test contains key.
     */
    @Test
    public void testContainsKey() {
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "C", "LIVE-C");
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	properties.put(new Integer(5), new Integer(50));
	Assert.assertTrue(properties.containsKey("KEYA"));
	Assert.assertTrue(properties.containsKey("test.KEYA"));
	Assert.assertTrue(properties.containsKey(new Integer(5)));
	try {
	    properties.containsKey(null);
	    Assert.fail();
	} catch (final IllegalArgumentException iae) {
	}
	Assert.assertFalse(properties.containsKey(new Integer(7)));
    }

    /**
     * Test double.
     */
    @Test
    public void testDouble() {
	properties.setProperty(getPrefixKey("Double"), "2.5");
	Assert.assertEquals(2.5, properties.getDouble("Double", 3.0));
	Assert.assertEquals(3.0, properties.getDouble("Double2", 3.0));
    }

    /**
     * Test double array.
     */
    @Test
    public void testDoubleArray() {
	properties.setProperty(getPrefixKey("Array"), "1.0,2.0,3.0");
	double[] result = properties.getDoubleArray("Array", new double[] { 1.0, 2.0, 3.0 });
	Assert.assertEquals(1.0, result[0]);
	Assert.assertEquals(2.0, result[1]);
	Assert.assertEquals(3.0, result[2]);
	result = properties.getDoubleArray("Array2", new double[] { 4.0, 5.0, 6.0 });
	Assert.assertEquals(4.0, result[0]);
	Assert.assertEquals(5.0, result[1]);
	Assert.assertEquals(6.0, result[2]);
    }

    /**
     * Test elements.
     */
    @Test
    public void testElements() {
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "C", "LIVE-C");
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	properties.put(new Integer(5), new Integer(50));
	int i = 0;
	for (@SuppressWarnings("rawtypes")
	final Enumeration enums = properties.elements(); enums.hasMoreElements();) {
	    final Object element = enums.nextElement();
	    if (element instanceof String) {
		i++;
		Assert.assertTrue(element.equals("TEST-A") || element.equals("TEST-B"));
	    } else {
		i++;
		Assert.assertEquals(new Integer(50), element);
	    }
	}
	Assert.assertEquals(3, i);
    }

    /**
     * Test entry set.
     */
    @Test
    public void testEntrySet() {
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "C", "LIVE-C");
	properties.setProperty(StagingPrefixConfig.DEV + PrefixConfig.PREFIXDELIMITER + "C", "DEV-C");
	properties.setProperty(StagingPrefixConfig.INTEGRATION + PrefixConfig.PREFIXDELIMITER + "C", "INT-C");
	properties.put(new Integer(5), new Integer(50));
	int i = 0;
	int x = 0;
	for (final Map.Entry<Object, Object> entry : properties.entrySet()) {
	    if (entry.getKey().equals(new Integer(5))) {
		Assert.assertEquals(new Integer(50), entry.getValue());
		x++;
	    }
	    if (entry.getKey().equals("KEYA")) {
		Assert.assertEquals("TEST-A", entry.getValue());
		x++;
	    }
	    if (entry.getKey().equals("KEYB")) {
		Assert.assertEquals("TEST-B", entry.getValue());
		x++;
	    }
	    i++;
	}
	Assert.assertEquals(3, i);
	Assert.assertEquals(3, x);
    }

    /**
     * Test float.
     */
    @Test
    public void testFloat() {
	properties.setProperty(getPrefixKey("Float"), "200000.7");
	Assert.assertEquals(200000.7f, properties.getFloat("Float", 30000.7f));
	Assert.assertEquals(30000.7f, properties.getFloat("Float2", 30000.7f));
    }

    //
    //    @Test
    //    public void testForEver() throws InterruptedException {
    //	System.setProperty("environment", "test");
    //	final ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
    //	final Bean bean1 = (Bean) context.getBean("bean1");
    //	final Thread t = new Thread() {
    //	    @Override
    //	    public void run() {
    //		while (true) {
    //		    try {
    //			Thread.currentThread().sleep(1000);
    //		    } catch (final InterruptedException e) {
    //			// TODO Auto-generated catch block
    //			e.printStackTrace();
    //		    }
    //		}
    //	    }
    //	};
    //	t.start();
    //	Thread.currentThread().sleep(50000);
    //    }

    /**
     * Test float array.
     */
    @Test
    public void testFloatArray() {
	properties.setProperty(getPrefixKey("Array"), "1.0,2.0,3.0");
	float[] result = properties.getFloatArray("Array", new float[] { 1.0f, 2.0f, 3.0f });
	Assert.assertEquals(1.0f, result[0]);
	Assert.assertEquals(2.0f, result[1]);
	Assert.assertEquals(3.0f, result[2]);
	result = properties.getFloatArray("Array2", new float[] { 4.0f, 5.0f, 6.0f });
	Assert.assertEquals(4.0f, result[0]);
	Assert.assertEquals(5.0f, result[1]);
	Assert.assertEquals(6.0f, result[2]);
    }

    /**
     * Test get property.
     */
    @Test
    public void testGetProperty() {
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	properties.setProperty(StagingPrefixConfig.DEV + PrefixConfig.PREFIXDELIMITER + "KEYB", "DEV-B");
	properties.put("5", new Integer(50));
	Assert.assertEquals("TEST-B", properties.getProperty("KEYB"));
	try {
	    properties.getProperty("5");
	    Assert.fail();
	} catch (final IllegalStateException e) {
	}
	Assert.assertEquals(new Integer(50), properties.get("5"));
	Assert.assertEquals("DEV-B", properties.getProperty(StagingPrefixConfig.DEV + PrefixConfig.PREFIXDELIMITER + "KEYB"));
	Assert.assertEquals("TEST-B", properties.getProperty("KEYB"));
	Assert.assertEquals("X", properties.getProperty("KEYX", "X"));
    }

    /**
     * Test integer.
     */
    @Test
    public void testInteger() {
	properties.setProperty(getPrefixKey("Integer"), "200000");
	Assert.assertEquals(200000, properties.getInt("Integer", 300000));
	Assert.assertEquals(300000, properties.getInt("Integer2", 300000));
    }

    /**
     * Test integer array.
     */
    @Test
    public void testIntegerArray() {
	properties.setProperty(getPrefixKey("Array"), "1,2,3");
	int[] result = properties.getIntArray("Array", new int[] { 1, 2, 3 });
	Assert.assertEquals(1, result[0]);
	Assert.assertEquals(2, result[1]);
	Assert.assertEquals(3, result[2]);
	result = properties.getIntArray("Array2", new int[] { 4, 5, 6 });
	Assert.assertEquals(4, result[0]);
	Assert.assertEquals(5, result[1]);
	Assert.assertEquals(6, result[2]);
    }

    /**
     * Test is empty.
     */
    @Test
    public void testIsEmpty() {
	Assert.assertTrue(properties.isEmpty());
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "KEYA", "TEST-A");
	Assert.assertTrue(properties.isEmpty());
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	Assert.assertFalse(properties.isEmpty());
    }

    /**
     * Test json.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testJSON() throws IOException {
	final PrefixedProperties serviceProperties = PrefixedProperties.createCascadingPrefixProperties(new StagingPrefixConfig(StagingPrefixConfig.TEST),
		new ServicePrefixConfig(
			ServicePrefixConfig.PRODUCT_SRV),
		new ComponentPrefixConfig(ComponentPrefixConfig.CACHE));
	final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream("prefixed.json");
	try {
	    serviceProperties.loadFromJSON(is);
	} finally {
	    is.close();
	}
	Assert.assertEquals("property1 of service1 in environment test", serviceProperties.get("prop1"));
	Assert.assertEquals("property2 of service1 in environment test", serviceProperties.get("prop2"));
	Assert.assertEquals("property3 of service1", serviceProperties.get("prop3"));
	Assert.assertEquals("property4", serviceProperties.get("prop4"));
	Assert.assertEquals("property5 in environment test", serviceProperties.get("prop5"));

	serviceProperties.setLocalPrefix(ServicePrefixConfig.ACCOUNTING_SRV);
	Assert.assertEquals("property1 of service2 in environment test", serviceProperties.get("prop1"));
	Assert.assertEquals("property2 of service2 in environment test", serviceProperties.get("prop2"));
	Assert.assertEquals("property3 of service2", serviceProperties.get("prop3"));
	Assert.assertEquals("property4", serviceProperties.get("prop4"));
	Assert.assertEquals("property5 in environment test", serviceProperties.get("prop5"));
	final FileOutputStream fos = new FileOutputStream(new File("C:\\out.json"));
	serviceProperties.storeToJSON(fos);
	fos.flush();
	fos.close();
    }

    /**
     * Test keys.
     */
    @Test
    public void testKeys() {
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "C", "LIVE-C");
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	properties.put(new Integer(5), new Integer(50));
	int i = 0;
	int x = 0;
	for (final Object key : properties.keys()) {
	    x++;
	    if (key.equals("KEYA")) {
		i++;
	    }
	    if (key.equals("KEYB")) {
		i++;
	    }
	    if (key.equals(new Integer(5))) {
		i++;
	    }
	}
	Assert.assertEquals(3, i);
	Assert.assertEquals(i, x);
    }

    @Test
    public void testLocalOverrideConfig() throws InterruptedException, BrokenBarrierException {
	final PrefixedProperties props = PrefixedProperties.createCascadingPrefixProperties(new StagingPrefixConfig(), new DynamicPrefixConfig(), new ComponentPrefixConfig());
	props.setDefaultPrefix("test");
	props.setProperty("test.key", "TESTVALUE");
	props.setProperty("stg.key", "STAGEVALUE");
	props.setProperty("key", "FALLBACK");
	Assert.assertEquals("TESTVALUE", props.get("key"));
	props.setLocalPrefix("liv");
	Assert.assertEquals("FALLBACK", props.get("key"));
	final AtomicInteger successCount = new AtomicInteger();
	final CyclicBarrier barrier = new CyclicBarrier(4);
	for (int i = 0; i < 3; i++) {
	    final int x = i;
	    final Thread thread = new Thread() {
		@Override
		public void run() {
		    try {
			for (int u = 0; u < 10; u++) {
			    switch (x) {
			    case 0: {
				props.setLocalPrefix("stg");
				Assert.assertEquals("STAGEVALUE", props.get("key"));
				break;
			    }
			    case 1: {
				props.setLocalPrefix("liv");
				Assert.assertEquals("FALLBACK", props.get("key"));
				break;
			    }
			    case 2: {
				Assert.assertEquals("TESTVALUE", props.get("key"));
				break;
			    }
			    }
			    successCount.incrementAndGet();
			}
		    } catch (final Exception e) {
			successCount.set(Integer.MIN_VALUE);
		    } finally {
			try {
			    barrier.await();
			} catch (final InterruptedException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			} catch (final BrokenBarrierException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
			}
		    }
		}
	    };
	    thread.start();
	}
	barrier.await();
	Assert.assertEquals(30, successCount.get());
    }

    @Test
    public void testLocalProperties() throws IOException, InterruptedException, BrokenBarrierException {
	final PrefixedProperties serviceProperties = PrefixedProperties.createCascadingPrefixProperties(new StagingPrefixConfig(StagingPrefixConfig.TEST),
		new ServicePrefixConfig(
			ServicePrefixConfig.PRODUCT_SRV),
		new ComponentPrefixConfig(ComponentPrefixConfig.CACHE));
	final InputStream is = ClassLoader.getSystemResourceAsStream("prefixed.json");
	serviceProperties.loadFromJSON(is);
	serviceProperties.setDefaultPrefix("stg");
	is.close();
	final Thread[] threads = new Thread[2];
	final CyclicBarrier barrier = new CyclicBarrier(3);
	for (int i = 0; i < threads.length; i++) {
	    final int x = i;
	    threads[i] = new Thread() {
		@Override
		public void run() {
		    try {
			if (x == 0) {
			    serviceProperties.setLocalPrefix("accsrv");
			    Assert.assertEquals("property3 of service2", serviceProperties.get("prop3"));
			} else {
			    serviceProperties.setLocalPrefix("prdsrv");
			    Assert.assertEquals("property3 of service1", serviceProperties.get("prop3"));
			}
		    } finally {
			try {
			    barrier.await();
			} catch (final Exception e) {
			    e.printStackTrace();
			}
		    }
		}
	    };
	    threads[i].start();
	}
	barrier.await();
    }

    /**
     * Test long.
     */
    @Test
    public void testLong() {
	properties.setProperty(getPrefixKey("Long"), "20000000000");
	Assert.assertEquals(20000000000L, properties.getLong("Long", 30000000000L));
	Assert.assertEquals(30000000000L, properties.getLong("Long2", 30000000000L));
    }

    /**
     * Test long array.
     */
    @Test
    public void testLongArray() {
	properties.setProperty(getPrefixKey("Array"), "1,2,3");
	long[] result = properties.getLongArray("Array", new long[] { 1, 2, 3 });
	Assert.assertEquals(1, result[0]);
	Assert.assertEquals(2, result[1]);
	Assert.assertEquals(3, result[2]);
	result = properties.getLongArray("Array2", new long[] { 4, 5, 6 });
	Assert.assertEquals(4, result[0]);
	Assert.assertEquals(5, result[1]);
	Assert.assertEquals(6, result[2]);
    }

    /**
     * Test property names.
     */
    @Test
    public void testPropertyNames() {
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "C", "LIVE-C");
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	for (@SuppressWarnings("rawtypes")
	final Enumeration enums = properties.propertyNames(); enums.hasMoreElements();) {
	    final Object element = enums.nextElement();
	    Assert.assertTrue("Key was: " + element, element.equals("KEYA") || element.equals("KEYB"));
	}

	final Properties props = PrefixedProperties.createCascadingPrefixProperties(new StagingPrefixConfig("test"), new ComponentPrefixConfig("cache"));
	props.setProperty("dev.cache.keyA", "dca");
	props.setProperty("test.cache.keyA", "tca");
	props.setProperty("test.keyA", "ta");
	props.setProperty("cache.keyA", "ca");
	props.setProperty("dev.keyB", "db");
	props.setProperty("test.keyB", "tb");
	props.setProperty("liv.keyB", "pb");
	props.setProperty("liv.keyC", "pc");
	Assert.assertEquals("tca", props.get("keyA"));
	Assert.assertEquals("tb", props.get("keyB"));
	Assert.assertNull(props.get("keyC"));
	int c = 0;
	for (final Enumeration<?> enums = props.propertyNames(); enums.hasMoreElements();) {
	    final String element = (String) enums.nextElement();
	    Assert.assertTrue(element.equals("keyA") || element.equals("keyB"));
	    c++;
	}
	Assert.assertEquals(2, c);
    }

    /**
     * Test remove.
     */
    @Test
    public void testRemove() {
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "C", "LIVE-C");
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	properties.setProperty("KEYB", "TEST-B");
	properties.put(new Integer(5), new Integer(50));
	Assert.assertEquals("TEST-A", properties.getProperty("KEYA"));
	properties.removeProperty("KEYA");
	Assert.assertEquals("A", properties.getProperty("KEYA"));
	properties.removeProperty("KEYA");
	Assert.assertNull(properties.getProperty("KEYA"));
	Assert.assertEquals("TEST-B", properties.getProperty("KEYB"));
	properties.removeAll("KEYB");
	Assert.assertNull(properties.getProperty("KEYB"));
	Assert.assertEquals(new Integer(50), properties.remove(new Integer(5)));
    }

    @Test
    public void testSerialization() throws IOException, ClassNotFoundException {
	final ByteArrayOutputStream baos = new ByteArrayOutputStream();
	final ObjectOutputStream oos = new ObjectOutputStream(baos);
	testAddProperties();
	oos.writeObject(properties);
	oos.close();
	final ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
	final ObjectInputStream ois = new ObjectInputStream(bais);
	final Properties props = (Properties) ois.readObject();
	Assert.assertEquals(props, properties);
    }

    /**
     * Test set illegal prefix.
     */
    @Test
    public void testSetIllegalPrefix() {
	try {
	    config.setPrefix("something");
	    Assert.fail();
	} catch (final IllegalArgumentException iae) {

	}
    }

    /**
     * Test set no prefix.
     */
    @Test
    public void testSetNoPrefix() {
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "C", "LIVE-C");
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	//properties.put(new Integer(5), new Integer(50));
	config.setPrefix(null);
	config.setDefaultPrefix(null);
	Assert.assertEquals("A", properties.getProperty("KEYA"));
	Assert.assertNull(properties.getProperty("KEYB"));
    }

    /**
     * Test set prefix.
     * 
     * @throws IOException
     *             Signals that an I/O exception has occurred.
     */
    @Test
    public void testSetPrefix() throws IOException {
	final PrefixedProperties serviceProperties = PrefixedProperties.createCascadingPrefixProperties(
		new StagingPrefixConfig(StagingPrefixConfig.TEST),
		new ServicePrefixConfig(
			ServicePrefixConfig.PRODUCT_SRV), new ComponentPrefixConfig("cache"));
	Assert.assertEquals("test.prdsrv.cache", serviceProperties.getEffectivePrefix());
	serviceProperties.setLocalPrefix(ServicePrefixConfig.ACCOUNTING_SRV + "." + ComponentPrefixConfig.TRANSACTION_MANAGER);
	Assert.assertEquals("test.accsrv.transaction", serviceProperties.getEffectivePrefix());
    }

    /**
     * Test short.
     */
    @Test
    public void testShort() {
	properties.setProperty(getPrefixKey("Short"), "20000");
	Assert.assertEquals(20000, properties.getShort("Short", (short) 30000));
	Assert.assertEquals(30000, properties.getShort("Short2", (short) 30000));
    }

    /**
     * Test short array.
     */
    @Test
    public void testShortArray() {
	properties.setProperty(getPrefixKey("Array"), "1,2,3");
	short[] result = properties.getShortArray("Array", new short[] { 1, 2, 3 });
	Assert.assertEquals(1, result[0]);
	Assert.assertEquals(2, result[1]);
	Assert.assertEquals(3, result[2]);
	result = properties.getShortArray("Array2", new short[] { 4, 5, 6 });
	Assert.assertEquals(4, result[0]);
	Assert.assertEquals(5, result[1]);
	Assert.assertEquals(6, result[2]);
    }

    @Test
    public void testStringPropertyNames() {
	properties.setProperty(getPrefixKey("KEYA"), "TEST-A");
	properties.setProperty("KEYA", "A");
	properties.setProperty(getPrefixKey("KEYB"), "TEST-B");
	properties.setProperty(StagingPrefixConfig.LIVE + PrefixConfig.PREFIXDELIMITER + "C", "LIVE-C");
	properties.setProperty(StagingPrefixConfig.DEV + PrefixConfig.PREFIXDELIMITER + "C", "DEV-C");
	properties.setProperty(StagingPrefixConfig.INTEGRATION + PrefixConfig.PREFIXDELIMITER + "C", "INT-C");
	properties.put(new Integer(5), new Integer(50));
	int i = 0;
	int x = 0;
	for (final String entry : properties.stringPropertyNames()) {
	    if (entry.equals("KEYA") || entry.equals("KEYB")) {
		x++;
	    }
	    i++;
	}
	Assert.assertEquals(2, i);
	Assert.assertEquals(2, x);

    }

    @Test
    public void testWrongPrefixConfig() {
	final PrefixedProperties props = PrefixedProperties.createCascadingPrefixProperties(new StagingPrefixConfig(), new DynamicPrefixConfig(), new ComponentPrefixConfig());
	props.setLocalPrefix("test");
	Assert.assertEquals("test", props.getEffectivePrefix());
	try {
	    props.setLocalPrefix("test.anything.cache");
	    Assert.assertEquals("test.anything.cache", props.getEffectivePrefix());
	    props.setLocalPrefix("test");
	    props.setLocalPrefix("test.something.wrong");
	    Assert.fail();
	} catch (final Exception e) {
	    Assert.assertEquals("test", props.getEffectivePrefix());
	}
    }
}
