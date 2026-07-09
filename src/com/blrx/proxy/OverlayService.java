package com.blrx.proxy;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.IBinder;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

public class OverlayService extends Service {

    private static final String CHANNEL_ID = "blrx_proxy";

    private WindowManager wm;
    private View widget;
    private View panel;
    private WindowManager.LayoutParams widgetParams;
    private WindowManager.LayoutParams panelParams;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startAsForeground();
        wm = (WindowManager) getSystemService(WINDOW_SERVICE);
        Native.init();
        addWidget();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private int overlayType() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        }
        return WindowManager.LayoutParams.TYPE_PHONE;
    }

    private void addWidget() {
        ImageView iv = new ImageView(this);
        GradientDrawable bg = new GradientDrawable();
        bg.setShape(GradientDrawable.OVAL);
        bg.setColor(Color.parseColor("#1E88E5"));
        bg.setStroke(dp(2), Color.parseColor("#0D47A1"));
        iv.setBackground(bg);
        iv.setImageResource(R.drawable.ic_widget);
        int pad = dp(12);
        iv.setPadding(pad, pad, pad, pad);
        widget = iv;

        widgetParams = new WindowManager.LayoutParams(
                dp(56), dp(56), overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        widgetParams.gravity = Gravity.TOP | Gravity.START;
        widgetParams.x = dp(12);
        widgetParams.y = dp(120);

        widget.setOnTouchListener(new DragTouch());
        wm.addView(widget, widgetParams);
    }

    private final class DragTouch implements View.OnTouchListener {
        private int initialX;
        private int initialY;
        private float touchX;
        private float touchY;
        private boolean moved;

        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    initialX = widgetParams.x;
                    initialY = widgetParams.y;
                    touchX = event.getRawX();
                    touchY = event.getRawY();
                    moved = false;
                    return true;
                case MotionEvent.ACTION_MOVE:
                    int newX = initialX + (int) (event.getRawX() - touchX);
                    int newY = initialY + (int) (event.getRawY() - touchY);
                    if (Math.abs(newX - initialX) > dp(4) || Math.abs(newY - initialY) > dp(4)) {
                        moved = true;
                    }
                    widgetParams.x = newX;
                    widgetParams.y = newY;
                    wm.updateViewLayout(widget, widgetParams);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (!moved) {
                        togglePanel();
                    }
                    return true;
                default:
                    return false;
            }
        }
    }

    private void togglePanel() {
        if (panel != null) {
            closePanel();
        } else {
            openPanel();
        }
    }

    private void openPanel() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        GradientDrawable panelBg = new GradientDrawable();
        panelBg.setColor(Color.parseColor("#EE10151C"));
        panelBg.setCornerRadius(dp(14));
        panelBg.setStroke(dp(1), Color.parseColor("#1E88E5"));
        root.setBackground(panelBg);
        int p = dp(14);
        root.setPadding(p, p, p, p);

        LinearLayout header = new LinearLayout(this);
        header.setOrientation(LinearLayout.HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);

        TextView title = new TextView(this);
        title.setText(Native.buildInfo());
        title.setTextColor(Color.WHITE);
        title.setTextSize(18);
        title.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        header.addView(title, titleParams);

        TextView close = new TextView(this);
        close.setText("X");
        close.setTextColor(Color.parseColor("#FF5252"));
        close.setTextSize(18);
        close.setPadding(dp(10), 0, dp(6), 0);
        close.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                closePanel();
            }
        });
        header.addView(close);
        root.addView(header);

        View divider = new View(this);
        LinearLayout.LayoutParams divParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, dp(1));
        divParams.topMargin = dp(10);
        divParams.bottomMargin = dp(6);
        divider.setBackgroundColor(Color.parseColor("#33FFFFFF"));
        root.addView(divider, divParams);

        String[] names = getResources().getStringArray(R.array.features);
        for (int i = 0; i < names.length; i++) {
            root.addView(buildToggle(i, names[i]));
        }

        panel = root;
        panelParams = new WindowManager.LayoutParams(
                dp(268), ViewGroup.LayoutParams.WRAP_CONTENT, overlayType(),
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
        panelParams.gravity = Gravity.TOP | Gravity.START;
        panelParams.x = widgetParams.x;
        panelParams.y = widgetParams.y + dp(64);
        wm.addView(panel, panelParams);
    }

    private View buildToggle(final int id, String name) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        int vp = dp(6);
        row.setPadding(0, vp, 0, vp);

        TextView label = new TextView(this);
        label.setText(name);
        label.setTextColor(Color.parseColor("#E0E6ED"));
        label.setTextSize(15);
        LinearLayout.LayoutParams labelParams = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(label, labelParams);

        Switch sw = new Switch(this);
        sw.setChecked(Native.getFeature(id));
        sw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Native.setFeature(id, isChecked);
            }
        });
        row.addView(sw);
        return row;
    }

    private void closePanel() {
        if (panel != null) {
            wm.removeView(panel);
            panel = null;
        }
    }

    private void startAsForeground() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, "BLRX PROXY", NotificationManager.IMPORTANCE_LOW);
            nm.createNotificationChannel(channel);
            Notification notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentTitle("BLRX PROXY")
                    .setContentText("Service running")
                    .setSmallIcon(R.drawable.ic_widget)
                    .build();
            startForeground(1, notification);
        }
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    @Override
    public void onDestroy() {
        closePanel();
        if (widget != null) {
            wm.removeView(widget);
            widget = null;
        }
        stopForeground(true);
        super.onDestroy();
    }
}
