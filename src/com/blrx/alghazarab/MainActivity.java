package com.blrx.alghazarab;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity {
    private GameData data;
    private LinearLayout root;
    private TextView coins;

    protected void onCreate(Bundle b){
        super.onCreate(b);
        data = new GameData(this);
        build();
    }

    protected void onResume(){
        super.onResume();
        build();
    }

    private void build(){
        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(18), dp(28), dp(18), dp(22));
        root.setBackgroundColor(Color.rgb(12,18,38));
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("ألغاز العرب");
        title.setTextColor(Color.WHITE);
        title.setTextSize(34);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1, -2));

        TextView sub = new TextView(this);
        sub.setText("اختبر ذكاءك وافتح المراحل واحدة وراء الثانية");
        sub.setTextColor(Color.rgb(160,180,210));
        sub.setTextSize(15);
        sub.setGravity(Gravity.CENTER);
        sub.setPadding(0, dp(8), 0, dp(18));
        root.addView(sub);

        coins = pill("💰 " + data.coins() + " عملة");
        root.addView(coins);

        Button play = bigButton("ابدأ اللعب ✅", Color.rgb(50,214,255));
        play.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){ openLevel(data.unlocked()); }
        });
        root.addView(play);

        TextView map = new TextView(this);
        map.setText("المراحل");
        map.setTextColor(Color.WHITE);
        map.setTextSize(22);
        map.setPadding(0, dp(22), 0, dp(12));
        root.addView(map);

        GridLayout grid = new GridLayout(this);
        grid.setColumnCount(3);
        Level[] levels = Levels.all();
        for(int i=1;i<=levels.length;i++){
            final int lv = i;
            Button btn = new Button(this);
            btn.setAllCaps(false);
            btn.setText(i <= data.unlocked() ? "مرحلة " + i : "🔒 " + i);
            btn.setTextColor(Color.WHITE);
            btn.setTextSize(14);
            btn.setBackgroundColor(i <= data.unlocked() ? Color.rgb(33,52,88) : Color.rgb(25,32,50));
            GridLayout.LayoutParams gp = new GridLayout.LayoutParams();
            gp.width = dp(96);
            gp.height = dp(54);
            gp.setMargins(dp(5), dp(5), dp(5), dp(5));
            btn.setLayoutParams(gp);
            btn.setOnClickListener(new View.OnClickListener(){
                public void onClick(View v){ if(lv <= data.unlocked()) openLevel(lv); }
            });
            grid.addView(btn);
        }
        root.addView(grid);
        setContentView(scroll);
    }

    private void openLevel(int lv){
        Intent i = new Intent(this, GameActivity.class);
        i.putExtra("level", lv);
        startActivity(i);
    }

    private TextView pill(String s){
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextColor(Color.rgb(255,197,66));
        v.setTextSize(18);
        v.setGravity(Gravity.CENTER);
        v.setPadding(dp(16), dp(12), dp(16), dp(12));
        v.setBackgroundColor(Color.rgb(22,35,59));
        return v;
    }

    private Button bigButton(String s, int c){
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(19);
        b.setAllCaps(false);
        b.setBackgroundColor(c);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(60));
        lp.setMargins(0, dp(18), 0, 0);
        b.setLayoutParams(lp);
        return b;
    }

    private int dp(int v){ return (int)(v * getResources().getDisplayMetrics().density); }
}
