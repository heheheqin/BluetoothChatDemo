package com.example.utli;

import java.io.Closeable;

public class CloseIO {
    public static void close(Closeable... obj) {
        for (Closeable cl : obj) {
            if (cl != null) {
                try {
                    cl.close();
                } catch (Exception e) {
                    // TODO: handle exception
                    e.printStackTrace();
                }
            }
        }
    }
}
