package org.nineml.coffeesacks;

import net.sf.saxon.om.FingerprintedQName;
import net.sf.saxon.om.NamespaceMap;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.value.QNameValue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Namespace utilities (an implementation detail).
 * <p>The Saxon APIs change over time. This class uses reflection to support Saxon 10, 11, or 12
 * simultaneously.</p>
 */
public class NamespaceUtils {
    public static FingerprintedQName fqName(QName qname) throws CoffeeSacksException {
        Method getns;
        try {
            // Saxon 12
            getns = QName.class.getMethod("getNamespace");
            Object ns = getns.invoke(qname);
            return fqName(qname.getPrefix(), (String) ns, qname.getLocalName());
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException err1) {
            try {
                // Saxon 10 or 11
                getns = QName.class.getMethod("getNamespaceURI");
                Object ns = getns.invoke(qname);
                return fqName(qname.getPrefix(), (String) ns, qname.getLocalName());
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException err2) {
                throw new CoffeeSacksException(CoffeeSacksException.ERR_NAMESPACE_CONSTRUCTION, "Failed to instantiate QName", err2);
            }
        }
    }

    public static FingerprintedQName fqName(String prefix, String nsuri, String localName) throws CoffeeSacksException {
        Constructor<?> fqcon;
        try {
            // Saxon 12
            Class<?> uriClass = Class.forName("net.sf.saxon.om.NamespaceUri");
            fqcon = FingerprintedQName.class.getConstructor(String.class, uriClass, String.class);
            Method uriOf = uriClass.getMethod("of", String.class);
            Object uri = uriOf.invoke(null, nsuri);
            return (FingerprintedQName) fqcon.newInstance(prefix, (Object) uri, localName);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException err1) {
            try {
                // Saxon 10 or 11
                fqcon = FingerprintedQName.class.getConstructor(String.class, String.class, String.class);
                return (FingerprintedQName) fqcon.newInstance(prefix, nsuri, localName);
            } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | InvocationTargetException err2) {
                throw new CoffeeSacksException(CoffeeSacksException.ERR_NAMESPACE_CONSTRUCTION, "Failed to instantiate FingerprintedQName", err2);
            }
        }
    }

    public static QName qName(QNameValue qname) throws CoffeeSacksException {
        String nsString;

        try {
            Method getns = qname.getClass().getMethod("getNamespaceURI");
            Object ns = getns.invoke(qname);
            nsString = (ns instanceof String) ? (String) ns : ns.toString();
        } catch (NoSuchMethodException|IllegalAccessException|InvocationTargetException err) {
            throw new CoffeeSacksException(CoffeeSacksException.ERR_NAMESPACE_CONSTRUCTION, "Failed to getNamespaceURI on QNameValue", err);
        }

        return new QName(qname.getPrefix(), nsString, qname.getLocalName());
    }

    public static NamespaceMap addToMap(NamespaceMap map, String prefix, String nsuri) throws CoffeeSacksException {
        NamespaceMap newMap = null;

        try {
            // Saxon 12
            Class<?> uriClass = Class.forName("net.sf.saxon.om.NamespaceUri");
            Method uriOf = uriClass.getMethod("of", String.class);
            Object uri = uriOf.invoke(null, nsuri);

            Class<?> mapClass = map.getClass();
            Method put = mapClass.getMethod("put", String.class, uriClass);
            newMap = (NamespaceMap) put.invoke(map, prefix, uri);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException err1) {
            try {
                // Saxon 10 or 11
                Class<?> mapClass = map.getClass();
                Method put = mapClass.getMethod("put", String.class, String.class);
                newMap = (NamespaceMap) put.invoke(map, prefix, nsuri);
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException err2) {
                throw new CoffeeSacksException(CoffeeSacksException.ERR_NAMESPACE_CONSTRUCTION, "Failed to add to NamespaceMap", err2);
            }
        }

        return newMap;
    }


}
