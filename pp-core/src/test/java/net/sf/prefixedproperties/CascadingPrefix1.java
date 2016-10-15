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
		System.out.println(properties.get("password")); // will return Obscure
		// changes the prefix configuration
		properties.setLocalPrefix("dev.account_db");
		System.out.println(properties.get("password")); // will return Secure

		// change the prefix again now we forgot the first environment
		properties.setLocalPrefix("product_db");
		System.out.println(properties.get("password")); // will return Cryptic
	}

}
