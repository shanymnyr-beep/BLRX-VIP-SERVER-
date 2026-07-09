# BLRX Runner

A 2D chase / endless-runner game for **AIDE Pro** using the built-in **NDK**.

## Build rules (as required)
- Built entirely inside AIDE Pro, no Android Studio / Gradle / CMake.
- Native code uses **Android.mk + Application.mk** only.
- All C++ sources live in `app/src/main/jni/`.
- Game/UI display strings are XOR-obfuscated inside the native `.so`
  (`strings.cpp`), not in Java. Only error/notification text is in Java resources.

## Structure
```
RunnerGame/
  app/src/main/
    AndroidManifest.xml
    java/com/blrx/runner/
      MainActivity.java     (main menu: money bar, PLAY, SHOP, SKILLS)
      GameActivity.java     (hosts the game view)
      GameView.java         (SurfaceView render loop + on-screen arrow controls + HUD)
      ShopActivity.java     (buy / select characters with coins)
      NativeBridge.java     (JNI declarations)
    jni/
      Android.mk
      Application.mk
      main.cpp              (JNI_OnLoad + bridge only)
      game.h / game.cpp     (player physics, obstacles, coins, collision, score)
      strings.h / strings.cpp (encrypted strings)
      includes.h, log.h
    res/layout/  res/values/
    assets/gfx/  (put your images here, see assets/gfx/README.txt)
```

## Controls
On-screen D-pad (bottom-left): UP = jump, DOWN = slide, LEFT/RIGHT = move.
Tap anywhere on the Game Over screen to retry.

## Gameplay
Auto-runner: dodge obstacles (jump over ground ones, slide under high ones),
collect coins, survive with 3 lives. Speed and obstacle density ramp up with score.
Coins persist between runs and are spent in the Shop.
