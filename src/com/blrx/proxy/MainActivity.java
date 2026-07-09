package com.blrx.proxy;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private static final int REQ_OVERLAY = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button start = (Button) findViewById(R.id.btnStart);
        Button stop = (Button) findViewById(R.id.btnStop);
        ImageView telegram = (ImageView) findViewById(R.id.btnTelegram);

        start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startService();
            }
        });

        stop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopOverlayService();
            }
        });

        telegram.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                openTelegram();
            }
        });
    }

    private boolean hasOverlayPermission() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this);
    }

    private void requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivityForResult(intent, REQ_OVERLAY);
            Toast.makeText(this, R.string.grant_overlay, Toast.LENGTH_LONG).show();
        }
    }

    private void startService() {
        if (!hasOverlayPermission()) {
            requestOverlayPermission();
            return;
        }
        Intent intent = new Intent(this, OverlayService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
        Toast.makeText(this, R.string.service_started, Toast.LENGTH_SHORT).show();
    }

    private void stopOverlayService() {
        stopService(new Intent(this, OverlayService.class));
        Toast.makeText(this, R.string.service_stopped, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQ_OVERLAY && hasOverlayPermission()) {
            startService();
        }
    }

    private void openTelegram() {
        String link = Native.tgLink();
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(link)));
        } catch (Exception e) {
            Toast.makeText(this, link, Toast.LENGTH_LONG).show();
        }
    }
}
