package com.blrx.proxy;

/**
 * JNI bridge to the protected native library (libblrxproxy.so).
 * All sensitive strings and feature logic live inside the encrypted native binary.
 */
public final class Native {

    static {
        System.loadLibrary("blrxproxy");
    }

    private Native() {
    }

    /** Initialize the native library and start the hook thread. */
    public static native void init();

    /** Enable or disable a feature by index. */
    public static native void setFeature(int id, boolean enabled);

    /** Query current feature state by index. */
    public static native boolean getFeature(int id);

    /** Returns the decrypted build/version string, e.g. "BLRX PROXY 1.0". */
    public static native String buildInfo();

    /** Returns the decrypted Telegram support link. */
    public static native String tgLink();
}
