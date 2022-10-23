package com.vulcheck.text4shell.utils;

import java.io.Closeable;

public class CustomUtils {

    public static void ensureClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (Throwable t) {
                throw new RuntimeException(t);
            }
        }
    }

    public static boolean isTarget(String input) {
        String path = input.toLowerCase();

        return path.endsWith(".jar") || path.endsWith(".war") || path.endsWith(".ear") || path.endsWith(".aar") || path.endsWith(".rar") || path.endsWith(".nar");
    }

}
