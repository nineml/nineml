package org.nineml.coffeefilter.util;

import com.thaiopensource.util.PropertyMapBuilder;
import com.thaiopensource.validate.SchemaReader;
import com.thaiopensource.validate.ValidateProperty;
import com.thaiopensource.validate.ValidationDriver;
import com.thaiopensource.validate.prop.rng.RngProperty;
import com.thaiopensource.validate.rng.CompactSchemaReader;
import org.nineml.coffeefilter.ParserOptions;
import org.nineml.coffeefilter.exceptions.IxmlException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import java.io.*;
import java.net.URL;

public class VxmlValidator {
    private String error = null;

    public boolean valid() {
        return error == null;
    }

    public String getError() {
        return error;
    }

    public InputStream validateVxml(InputStream stream, URL schema, ParserOptions options) throws IOException {
        // This is kind of ugly. I want to read the stream in order to validate it, but
        // I have to return a stream in order to parse it. So we buffer it. *shrug*
        //
        // Also: don't throw an exception in here because it comes across as an
        // invocation exception.
        
        StringBuilder sb = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String line = reader.readLine();
        while (line != null) {
            sb.append(line).append("\n");
            line = reader.readLine();
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(sb.toString().getBytes());

        RelaxNgErrorHandler errorHandler = new RelaxNgErrorHandler();
        try {
            PropertyMapBuilder properties = new PropertyMapBuilder();
            properties.put(ValidateProperty.ERROR_HANDLER, errorHandler);
            RngProperty.CHECK_ID_IDREF.add(properties);
            properties.put(RngProperty.CHECK_ID_IDREF, null);
            RngProperty.FEASIBLE.add(properties);

            SchemaReader sr = CompactSchemaReader.getInstance();

            ValidationDriver driver = new ValidationDriver(properties.toPropertyMap(), properties.toPropertyMap(), sr);
            InputSource insrc = ValidationDriver.uriOrFileInputSource(schema.toString());

            if (driver.loadSchema(insrc)) {
                insrc = new InputSource(bais);
                if (!driver.validate(insrc)) {
                    error = errorHandler.error;
                }
            } else {
                options.getLogger().warn("InvisibleXmlRng", "Failed to load RELAX NG schema for VXML");
            }
        } catch (IOException | SAXException ex) {
            // If it wasn't XML, it was never going to succeed
            if (errorHandler.xml) {
                error = errorHandler.error;
            }
        }

        return new ByteArrayInputStream(sb.toString().getBytes());
    }

    private static class RelaxNgErrorHandler implements ErrorHandler {
        public boolean xml = true;
        public String error = null;

        @Override
        public void warning(SAXParseException exception) throws SAXException {
            // nop
        }

        @Override
        public void error(SAXParseException exception) throws SAXException {
            if (error == null) {
                error = exception.getMessage();
            }
        }

        @Override
        public void fatalError(SAXParseException exception) throws SAXException {
            if (exception.getMessage().contains("Content is not allowed in prolog")) {
                xml = false;
            }
            if (error == null) {
                error = exception.getMessage();
            }
        }
    }
}
