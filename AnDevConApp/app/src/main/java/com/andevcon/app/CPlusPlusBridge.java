package com.andevcon.app;

public class CPlusPlusBridge {
    private static final String TAG = "CPlusPlusBridge";

    static {
        // Load our C/C++ binary named libandevcon.so
        System.loadLibrary("andevcon");
    }

    /**
     * Native C/C++ method to encrypt a string
     * @param id    id to encrypt
     * @return      encrypted id
     */
    public native String getEncryptedID(String id);

}
