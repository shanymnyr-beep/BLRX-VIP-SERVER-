package com.blrx.runner;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);

        TextView title  = (TextView) findViewById(R.id.title);
        Button   play   = (Button) findViewById(R.id.btnPlay);
        Button   shop   = (Button) findViewById(R.id.btnShop);
        Button   skills = (Button) findViewById(R.id.btnSkills);

        // Titles/labels come from the encrypted native strings.
        try {
            title.setText(NativeBridge.nativeGetString(0));
            play.setText(NativeBridge.nativeGetString(1));
            shop.setText(NativeBridge.nativeGetString(2));
            skills.setText(NativeBridge.nativeGetString(3));
        } catch (Throwable t) {
            // native not ready: keep XML defaults
        }

        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, GameActivity.class));
            }
        });
        shop.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ShopActivity.class));
            }
        });
        skills.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ShopActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("blrx", MODE_PRIVATE);
        TextView coins = (TextView) findViewById(R.id.coins);
        coins.setText("Coins: " + sp.getInt("coins", 0));
    }
}
