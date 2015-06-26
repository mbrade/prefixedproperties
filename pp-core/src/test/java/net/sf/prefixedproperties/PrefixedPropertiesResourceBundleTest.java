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
