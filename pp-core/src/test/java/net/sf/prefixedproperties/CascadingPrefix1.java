package net.sf.prefixedproperties;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class CascadingPrefix1 {

    /**
     * @param args
     * @throws IOException
     */
    @Test
    public void test() throws IOException {
	final InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream("cascadingPrefix1.properties");
	final PrefixedProperties properties = PrefixedProperties.createCascadingPrefixProperties("dev.rights_db");
	properties.load(is);
	is.close();
	System.out.println(properties.get("password")); //will return Obscure
	//changes the prefix configuration
	properties.setLocalPrefix("dev.account_db");
	System.out.println(properties.get("password")); //will return Secure

	//change the prefix again now we forgot the first environment
	properties.setLocalPrefix("product_db");
	System.out.println(properties.get("password")); //will return Cryptic
    }

}
