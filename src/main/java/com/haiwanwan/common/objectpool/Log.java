package com.haiwanwan.common.objectpool;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Daniel
 */
public class Log {

    private static Logger logger = Logger.getLogger("FOP");

    private static String getString(Throwable ex, Object ... objects) {
        StringBuilder sb = new StringBuilder();
        for (Object object : objects)
            sb.append(object);
        sb.append(", ");
        StringWriter writer = new StringWriter();
        ex.printStackTrace(new PrintWriter(writer));
        sb.append(writer.toString());
        return sb.toString();
    }

    public static boolean isDebug() {
        return logger.isLoggable(Level.FINE);
    }

    private static String getString(Object ... objects) {
        if (objects.length > 1) {
            StringBuilder sb = new StringBuilder();
            for (Object object : objects)
                sb.append(object);
            return sb.toString();
        } else {
            return objects[0].toString();
        }
    }

    public static void debug(Object ... objects) {
        if (logger.isLoggable(Level.FINE)) {
            logger.fine(getString(objects));
        }
    }

    public static void info(Object ... objects) {
        if (logger.isLoggable(Level.INFO)) {
            logger.info(getString(objects));
        }
    }

    public static void error(Object... objects) {
        if (logger.isLoggable(Level.SEVERE)) {
            logger.severe(getString(objects));
        }
    }

    public static void error(Exception ex, Object... objects) {
        if (logger.isLoggable(Level.SEVERE)) {
            logger.severe(getString(ex, objects));
        }
    }

}
