package org.nineml.coffeesacks;

import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.Location;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;

/** The (Runtime) exceptions thrown. */
public class CoffeeSacksException extends XPathException {
    public static final String COFFEE_SACKS_ERROR_PREFIX = "cse";
    public static final String COFFEE_SACKS_ERROR_NAMESPACE = "http://nineml.com/ns/coffeesacks/errors";

    public static final String ERR_HTTP_INF_LOOP = "CSHT0001";
    public static final String ERR_HTTP_LOOP = "CSHT0002";
    public static final String ERR_INVALID_GRAMMAR = "CSIX0001";
    public static final String ERR_BAD_GRAMMAR = "CSER0001";
    public static final String ERR_BAD_OPTIONS = "CSER0002";
    public static final String ERR_BAD_INPUT_FORMAT = "CSER0003";
    public static final String ERR_BAD_OUTPUT_FORMAT = "CSER0004";
    public static final String ERR_INVALID_URI = "CSER0005";
    public static final String ERR_INVALID_CHOOSE_FUNCTION = "CSIF0001";
    public static final String ERR_INVALID_CHOICE = "CSIF0002";
    public static final String ERR_TREE_CONSTRUCTION = "CSIN0001";
    public static final String ERR_NAMESPACE_CONSTRUCTION = "CSIN0002";

    public CoffeeSacksException(String errCode, String message) {
        super(message);
        configure(errCode, message, null, null);
    }

    public CoffeeSacksException(String errCode, String message, Location location) {
        super(message);
        configure(errCode, message, location, null);
    }

    public CoffeeSacksException(String errCode, String message, Throwable cause) {
        super(message, cause);
        configure(errCode, message, null, null);
    }

    public CoffeeSacksException(String errCode, String message, Location location, Sequence value) {
        super(message);
        configure(errCode, message, location, value);
    }

    private CoffeeSacksException(String errCode, String message, Location location, Throwable cause) {
        super(message, cause);
        configure(errCode, message, location, null);
    }

    private void configure(String errCode, String message, Location location, Sequence value) {
        setErrorCodeQName(new StructuredQName(COFFEE_SACKS_ERROR_PREFIX, COFFEE_SACKS_ERROR_NAMESPACE, errCode));
        if (value != null) {
            setErrorObject(value);
        }
        if (location != null) {
            setLocation(location);
        }
    }
}
