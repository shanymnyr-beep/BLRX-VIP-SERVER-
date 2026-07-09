package com.blrx.runner;

/** Bridge to the native C++ game logic (librunner.so). */
public class NativeBridge {
    static {
        System.loadLibrary("runner");
    }

    // Lifecycle
    public static native void nativeInit(int w, int h);
    public static native void nativeReset();
    public static native void nativeUpdate(float dt, int input);

    // Player
    public static native float nativeGetPlayerX();
    public static native float nativeGetPlayerY();
    public static native float nativeGetPlayerW();
    public static native float nativeGetPlayerH();
    public static native int   nativeGetPlayerState();

    // State
    public static native int   nativeGetScore();
    public static native int   nativeGetCoins();
    public static native int   nativeGetLives();
    public static native int   nativeIsGameOver();
    public static native int   nativeGetBest();
    public static native float nativeGetSpeed();
    public static native float nativeGetGroundY();

    // Obstacles
    public static native int   nativeGetMaxObstacles();
    public static native int   nativeGetObstacleActive(int i);
    public static native float nativeGetObstacleX(int i);
    public static native float nativeGetObstacleY(int i);
    public static native float nativeGetObstacleW(int i);
    public static native float nativeGetObstacleH(int i);
    public static native int   nativeGetObstacleType(int i);

    // Coins
    public static native int   nativeGetMaxCoins();
    public static native int   nativeGetCoinActive(int i);
    public static native float nativeGetCoinX(int i);
    public static native float nativeGetCoinY(int i);
    public static native float nativeGetCoinSize();

    // Encrypted game strings
    public static native String nativeGetString(int id);
}
