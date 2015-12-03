package com.andevcon.app;

public class Example {
    static {
        System.loadLibrary("example");
    }

    private native void sayHello();

}
