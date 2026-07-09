package com.blrx.runner;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ShopActivity extends Activity {
    private String[] names  = { "NINJA", "ROBOT", "ATHLETE" };
    private int[]    prices = { 100, 250, 500 };

    @Override
    protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_shop);

        final SharedPreferences sp = getSharedPreferences("blrx", MODE_PRIVATE);
        final TextView coins = (TextView) findViewById(R.id.shopCoins);
        coins.setText("Coins: " + sp.getInt("coins", 0));

        LinearLayout list = (LinearLayout) findViewById(R.id.list);

        for (int i = 0; i < names.length; i++) {
            final int idx = i;
            Button btn = new Button(this);
            boolean owned = sp.getBoolean("char_" + i, i == 0);
            btn.setText(names[i] + (owned ? "  [SELECT]" : "  BUY " + prices[i]));
            btn.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    boolean own = sp.getBoolean("char_" + idx, idx == 0);
                    int c = sp.getInt("coins", 0);
                    if (own) {
                        sp.edit().putInt("selectedChar", idx).apply();
                        Toast.makeText(ShopActivity.this, "Selected " + names[idx], Toast.LENGTH_SHORT).show();
                    } else if (c >= prices[idx]) {
                        sp.edit().putInt("coins", c - prices[idx]).putBoolean("char_" + idx, true).apply();
                        coins.setText("Coins: " + sp.getInt("coins", 0));
                        ((Button) v).setText(names[idx] + "  [SELECT]");
                        Toast.makeText(ShopActivity.this, "Purchased " + names[idx], Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ShopActivity.this, "Not enough coins", Toast.LENGTH_SHORT).show();
                    }
                }
            });
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.topMargin = 24;
            list.addView(btn, lp);
        }
    }
}
