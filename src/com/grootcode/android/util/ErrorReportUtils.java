package com.grootcode.android.util;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Generating stackTrace from exception
 * 
 * @author ankit dimania
 */
public class ErrorReportUtils {

    private static String getStackTraceString(Throwable th) {
        StringWriter sw = new StringWriter();
        th.printStackTrace(new PrintWriter(sw));
        return sw.toString();
    }
}
