package org.nineml.coffeegrinder.exceptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Generates localized messages.
 */
public class MessageGenerator {
    private static final Map<String, String> messageCodes;
    private static final Map<String, String> englishCodes;

    static {
        Locale locale = Locale.getDefault();
        messageCodes = getMessageCodes(locale.getLanguage());
        if ("en".equals(locale.getLanguage())) {
            englishCodes = messageCodes;
        } else {
            englishCodes = getMessageCodes(locale.getLanguage());
        }
    }

    private static Map<String,String> getMessageCodes(String language) {
        Pattern errmsg = Pattern.compile("^([A-Za-z0-9]+):\\s*(.*)$");
        HashMap<String,String> messages = new HashMap<>();
        String name = "/org/nineml/coffeegrinder/" + language + "_messages.txt";
        InputStream stream = MessageGenerator.class.getResourceAsStream(name);
        if (stream != null) {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
                String line = reader.readLine();
                while (line != null) {
                    Matcher match = errmsg.matcher(line);
                    if (match.matches()) {
                        messages.put(match.group(1), match.group(2));
                    }
                    line = reader.readLine();
                }
                reader.close();
                stream.close();
            } catch (IOException ex) {
                // shrug, what to do, what to do?
            }
        }
        return messages;
    }

    protected static String getMessage(String code) {
        return getMessage(code, null);
    }

    protected static String getMessage(String code, String[] params) {
        String message;
        if (messageCodes.containsKey(code)) {
            message = messageCodes.get(code);
        } else {
            message = englishCodes.getOrDefault(code, null);
        }

        if (message == null) {
            // ???
            StringBuilder sb = new StringBuilder();
            sb.append("Unknown error: ").append(code);
            if (params != null) {
                for (String param : params) {
                    sb.append(": ").append(param);
                }
            }
            message = sb.toString();
        } else {
            if (params != null) {
                int pnum = 1;
                for (String param : params) {
                    String subst = "%" + pnum;
                    param = param.replaceAll("\\$", "\\\\\\$");
                    message = message.replaceAll(subst, param);
                    pnum++;
                }
            }
        }

        return message;
    }
}
