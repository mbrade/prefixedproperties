package net.sf.prefixedproperties;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * The Interface PrefixedPropertiesEnumeration.
 * 
 * @param <E>
 *            the element type
 */
public interface PrefixedPropertiesEnumeration<E> extends Iterable<E>, Iterator<E>, Enumeration<E> {

}
