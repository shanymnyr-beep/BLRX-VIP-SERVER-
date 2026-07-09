package com.blrx.runner;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.InputStream;

public class GameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private Thread thread;
    private volatile boolean running = false;
    private final SurfaceHolder holder;
    private final Paint paint = new Paint();

    private volatile int input = 0; // 0 none,1 up,2 down,3 left,4 right
    private long lastTime;
    private int animTick = 0;
    private float bgScroll = 0f;
    private boolean savedThisRun = false;

    // sprite sheet frame counts (match the GPT-generated sheets)
    private static final int RUN_FRAMES   = 8;
    private static final int JUMP_FRAMES  = 4;
    private static final int SLIDE_FRAMES = 4;
    private static final int OBS_TYPES    = 6;

    private Bitmap bg, playerRun, playerJump, playerSlide, coinBmp, obstaclesBmp, arrowsBmp;

    private RectF btnUp, btnDown, btnLeft, btnRight, btnPause;

    public GameView(Context c) {
        super(c);
        holder = getHolder();
        holder.addCallback(this);
        paint.setAntiAlias(true);
        setFocusable(true);
        loadAssets();
    }

    private Bitmap load(String name) {
        try {
            AssetManager am = getContext().getAssets();
            InputStream is = am.open("gfx/" + name);
            Bitmap b = BitmapFactory.decodeStream(is);
            is.close();
            return b;
        } catch (Throwable t) {
            return null; // fallback shapes will be drawn instead
        }
    }

    private void loadAssets() {
        bg          = load("bg.png");
        playerRun   = load("player_run.png");
        playerJump  = load("player_jump.png");
        playerSlide = load("player_slide.png");
        coinBmp     = load("coin.png");
        obstaclesBmp= load("obstacles.png");
        arrowsBmp   = load("arrows.png");
    }

    private void setupButtons(int w, int h) {
        float s = w * 0.13f;
        float m = w * 0.04f;
        float baseY = h - s * 2 - m * 2;
        btnUp    = new RectF(m + s, baseY, m + s * 2, baseY + s);
        btnDown  = new RectF(m + s, baseY + s + m, m + s * 2, baseY + s * 2 + m);
        btnLeft  = new RectF(m, baseY + s * 0.5f + m * 0.5f, m + s, baseY + s * 1.5f + m * 0.5f);
        btnRight = new RectF(m + s * 2, baseY + s * 0.5f + m * 0.5f, m + s * 3, baseY + s * 1.5f + m * 0.5f);
        btnPause = new RectF(w - s - m, m, w - m, m + s * 0.7f);
    }

    public void surfaceCreated(SurfaceHolder h) {
        int w = getWidth(), hh = getHeight();
        NativeBridge.nativeInit(w, hh);
        setupButtons(w, hh);
        running = true;
        thread = new Thread(this);
        thread.start();
    }

    public void surfaceChanged(SurfaceHolder h, int f, int w, int hh) {
        setupButtons(w, hh);
    }

    public void surfaceDestroyed(SurfaceHolder h) {
        running = false;
        try { if (thread != null) thread.join(); } catch (Exception e) { }
    }

    public boolean onTouchEvent(MotionEvent e) {
        int action = e.getActionMasked();
        if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
            input = 0;
            return true;
        }
        float x = e.getX(), y = e.getY();
        if (NativeBridge.nativeIsGameOver() == 1) {
            NativeBridge.nativeReset();
            input = 0;
            return true;
        }
        if (btnUp != null && btnUp.contains(x, y)) input = 1;
        else if (btnDown != null && btnDown.contains(x, y)) input = 2;
        else if (btnLeft != null && btnLeft.contains(x, y)) input = 3;
        else if (btnRight != null && btnRight.contains(x, y)) input = 4;
        else input = 0;
        return true;
    }

    public void run() {
        lastTime = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float dt = (now - lastTime) / 1000000000f;
            lastTime = now;
            if (dt > 0.05f) dt = 0.05f;

            NativeBridge.nativeUpdate(dt, input);

            Canvas c = holder.lockCanvas();
            if (c != null) {
                try { drawGame(c); } finally { holder.unlockCanvasAndPost(c); }
            }
            animTick++;
            try { Thread.sleep(16); } catch (Exception ex) { }
        }
    }

    private void drawSheet(Canvas c, Bitmap sheet, int frames, float dx, float dy, float dw, float dh, int frameIndex, int fallbackColor) {
        if (sheet == null) {
            paint.setColor(fallbackColor);
            c.drawRoundRect(new RectF(dx, dy, dx + dw, dy + dh), 14, 14, paint);
            return;
        }
        int fw = sheet.getWidth() / frames;
        if (fw <= 0) fw = sheet.getWidth();
        int fi = frames > 0 ? (frameIndex % frames) : 0;
        Rect src = new Rect(fi * fw, 0, fi * fw + fw, sheet.getHeight());
        RectF dst = new RectF(dx, dy, dx + dw, dy + dh);
        c.drawBitmap(sheet, src, dst, paint);
    }

    private void drawGame(Canvas c) {
        int w = getWidth(), h = getHeight();

        // Background (scrolling)
        if (bg != null) {
            bgScroll += NativeBridge.nativeGetSpeed() * 0.016f;
            float off = bgScroll % w;
            Rect src = new Rect(0, 0, bg.getWidth(), bg.getHeight());
            c.drawBitmap(bg, src, new RectF(-off, 0, w - off, h), paint);
            c.drawBitmap(bg, src, new RectF(w - off, 0, 2 * w - off, h), paint);
        } else {
            c.drawColor(0xFF88C6FF);
            paint.setColor(0xFF6B8E23);
            c.drawRect(0, NativeBridge.nativeGetGroundY(), w, h, paint);
        }

        // Coins
        float cs = NativeBridge.nativeGetCoinSize();
        int maxC = NativeBridge.nativeGetMaxCoins();
        for (int i = 0; i < maxC; i++) {
            if (NativeBridge.nativeGetCoinActive(i) == 1) {
                float cx = NativeBridge.nativeGetCoinX(i);
                float cy = NativeBridge.nativeGetCoinY(i);
                if (coinBmp != null) {
                    c.drawBitmap(coinBmp, null, new RectF(cx, cy, cx + cs, cy + cs), paint);
                } else {
                    paint.setColor(0xFFFFD54F);
                    c.drawOval(new RectF(cx, cy, cx + cs, cy + cs), paint);
                }
            }
        }

        // Obstacles
        int maxO = NativeBridge.nativeGetMaxObstacles();
        for (int i = 0; i < maxO; i++) {
            if (NativeBridge.nativeGetObstacleActive(i) == 1) {
                float ox = NativeBridge.nativeGetObstacleX(i);
                float oy = NativeBridge.nativeGetObstacleY(i);
                float ow = NativeBridge.nativeGetObstacleW(i);
                float oh = NativeBridge.nativeGetObstacleH(i);
                int type = NativeBridge.nativeGetObstacleType(i);
                drawSheet(c, obstaclesBmp, OBS_TYPES, ox, oy, ow, oh, type, 0xFFD84315);
            }
        }

        // Player
        float px = NativeBridge.nativeGetPlayerX();
        float py = NativeBridge.nativeGetPlayerY();
        float pw = NativeBridge.nativeGetPlayerW();
        float ph = NativeBridge.nativeGetPlayerH();
        int state = NativeBridge.nativeGetPlayerState();
        Bitmap sheet; int frames;
        if (state == 1) { sheet = playerJump; frames = JUMP_FRAMES; }
        else if (state == 2) { sheet = playerSlide; frames = SLIDE_FRAMES; }
        else { sheet = playerRun; frames = RUN_FRAMES; }
        drawSheet(c, sheet, frames, px, py, pw, ph, animTick / 4, 0xFF3AA0FF);

        // HUD
        paint.setColor(Color.WHITE);
        paint.setTextSize(w * 0.05f);
        paint.setTextAlign(Paint.Align.LEFT);
        c.drawText(NativeBridge.nativeGetString(4) + " " + NativeBridge.nativeGetScore(), w * 0.04f, w * 0.10f, paint);
        c.drawText(NativeBridge.nativeGetString(5) + " " + NativeBridge.nativeGetCoins(), w * 0.04f, w * 0.17f, paint);

        // Lives
        int lives = NativeBridge.nativeGetLives();
        float hs = w * 0.05f;
        for (int i = 0; i < lives; i++) {
            paint.setColor(0xFFE53935);
            float lx = w - (i + 1) * (hs + w * 0.02f);
            c.drawRect(lx, w * 0.05f, lx + hs, w * 0.05f + hs, paint);
        }

        // Control buttons
        drawButton(c, btnUp, "U", 0);
        drawButton(c, btnDown, "D", 1);
        drawButton(c, btnLeft, "L", 2);
        drawButton(c, btnRight, "R", 3);

        // Game over overlay
        if (NativeBridge.nativeIsGameOver() == 1) {
            if (!savedThisRun) {
                savedThisRun = true;
                SharedPreferences sp = getContext().getSharedPreferences("blrx", Context.MODE_PRIVATE);
                int total = sp.getInt("coins", 0) + NativeBridge.nativeGetCoins();
                sp.edit().putInt("coins", total).apply();
            }
            paint.setColor(0xCC000000);
            c.drawRect(0, 0, w, h, paint);
            paint.setColor(Color.WHITE);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(w * 0.11f);
            c.drawText(NativeBridge.nativeGetString(6), w / 2f, h * 0.38f, paint);
            paint.setTextSize(w * 0.06f);
            c.drawText(NativeBridge.nativeGetString(4) + " " + NativeBridge.nativeGetScore(), w / 2f, h * 0.48f, paint);
            c.drawText(NativeBridge.nativeGetString(7) + " " + NativeBridge.nativeGetBest(), w / 2f, h * 0.55f, paint);
            c.drawText(NativeBridge.nativeGetString(10), w / 2f, h * 0.70f, paint);
            paint.setTextAlign(Paint.Align.LEFT);
        } else {
            savedThisRun = false;
        }
    }

    private void drawButton(Canvas c, RectF r, String label, int frameIndex) {
        if (r == null) return;
        if (arrowsBmp != null) {
            int frames = 4;
            int fw = arrowsBmp.getWidth() / frames;
            Rect src = new Rect(frameIndex * fw, 0, frameIndex * fw + fw, arrowsBmp.getHeight());
            c.drawBitmap(arrowsBmp, src, r, paint);
        } else {
            paint.setColor(0x99FFFFFF);
            c.drawOval(r, paint);
            paint.setColor(0xFF102030);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(r.height() * 0.5f);
            c.drawText(label, r.centerX(), r.centerY() + r.height() * 0.18f, paint);
            paint.setTextAlign(Paint.Align.LEFT);
        }
    }
}
