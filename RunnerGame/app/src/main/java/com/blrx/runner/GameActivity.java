package com.blrx.runner;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

public class GameActivity extends Activity {
    private GameView view;

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        view = new GameView(this);
        setContentView(view);
    }
}
