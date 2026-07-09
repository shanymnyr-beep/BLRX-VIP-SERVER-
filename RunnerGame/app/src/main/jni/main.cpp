#include "includes.h"
#include "game.h"
#include "strings.h"

// JNI bridge between Java (rendering + input) and native C++ (game logic).
// Package: com.blrx.runner  Class: NativeBridge

extern "C" {

JNIEXPORT void JNICALL
Java_com_blrx_runner_NativeBridge_nativeInit(JNIEnv* env, jclass clazz, jint w, jint h) {
    g_game.init((int)w, (int)h);
}

JNIEXPORT void JNICALL
Java_com_blrx_runner_NativeBridge_nativeReset(JNIEnv* env, jclass clazz) {
    g_game.reset();
}

JNIEXPORT void JNICALL
Java_com_blrx_runner_NativeBridge_nativeUpdate(JNIEnv* env, jclass clazz, jfloat dt, jint input) {
    g_game.update((float)dt, (int)input);
}

JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetPlayerX(JNIEnv* env, jclass clazz) { return g_game.playerX; }
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetPlayerY(JNIEnv* env, jclass clazz) { return g_game.effY(); }
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetPlayerW(JNIEnv* env, jclass clazz) { return g_game.playerW; }
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetPlayerH(JNIEnv* env, jclass clazz) { return g_game.effH(); }
JNIEXPORT jint JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetPlayerState(JNIEnv* env, jclass clazz) { return g_game.playerState; }

JNIEXPORT jint JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetScore(JNIEnv* env, jclass clazz) { return g_game.score; }
JNIEXPORT jint JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetCoins(JNIEnv* env, jclass clazz) { return g_game.coinCount; }
JNIEXPORT jint JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetLives(JNIEnv* env, jclass clazz) { return g_game.lives; }
JNIEXPORT jint JNICALL
Java_com_blrx_runner_NativeBridge_nativeIsGameOver(JNIEnv* env, jclass clazz) { return g_game.gameOver ? 1 : 0; }
JNIEXPORT jint JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetBest(JNIEnv* env, jclass clazz) { return g_game.bestScore; }
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetSpeed(JNIEnv* env, jclass clazz) { return g_game.speed; }
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetGroundY(JNIEnv* env, jclass clazz) { return g_game.groundY; }

JNIEXPORT jint JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetMaxObstacles(JNIEnv* env, jclass clazz) { return Game::MAX_OBS; }
JNIEXPORT jint JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetObstacleActive(JNIEnv* env, jclass clazz, jint i) {
    if (i < 0 || i >= Game::MAX_OBS) return 0;
    return g_game.obs[i].active ? 1 : 0;
}
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetObstacleX(JNIEnv* env, jclass clazz, jint i) { return (i>=0&&i<Game::MAX_OBS)?g_game.obs[i].x:0; }
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetObstacleY(JNIEnv* env, jclass clazz, jint i) { return (i>=0&&i<Game::MAX_OBS)?g_game.obs[i].y:0; }
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetObstacleW(JNIEnv* env, jclass clazz, jint i) { return (i>=0&&i<Game::MAX_OBS)?g_game.obs[i].w:0; }
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetObstacleH(JNIEnv* env, jclass clazz, jint i) { return (i>=0&&i<Game::MAX_OBS)?g_game.obs[i].h:0; }
JNIEXPORT jint JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetObstacleType(JNIEnv* env, jclass clazz, jint i) { return (i>=0&&i<Game::MAX_OBS)?g_game.obs[i].type:0; }

JNIEXPORT jint JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetMaxCoins(JNIEnv* env, jclass clazz) { return Game::MAX_COINS; }
JNIEXPORT jint JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetCoinActive(JNIEnv* env, jclass clazz, jint i) {
    if (i < 0 || i >= Game::MAX_COINS) return 0;
    return g_game.coins[i].active ? 1 : 0;
}
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetCoinX(JNIEnv* env, jclass clazz, jint i) { return (i>=0&&i<Game::MAX_COINS)?g_game.coins[i].x:0; }
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetCoinY(JNIEnv* env, jclass clazz, jint i) { return (i>=0&&i<Game::MAX_COINS)?g_game.coins[i].y:0; }
JNIEXPORT jfloat JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetCoinSize(JNIEnv* env, jclass clazz) { return g_game.coinSize(); }

JNIEXPORT jstring JNICALL
Java_com_blrx_runner_NativeBridge_nativeGetString(JNIEnv* env, jclass clazz, jint id) {
    std::string s = decryptString((int)id);
    return env->NewStringUTF(s.c_str());
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM* vm, void* reserved) {
    LOGI("BLRX Runner native library loaded");
    return JNI_VERSION_1_6;
}

} // extern "C"
