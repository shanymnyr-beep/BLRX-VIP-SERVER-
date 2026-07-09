package com.blrx.alghazarab;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class GameActivity extends Activity {
    private GameData data;
    private Level level;
    private int levelNo;
    private LinearLayout root, foundBox;
    private TextView current, coins;
    private GridLayout lettersGrid;
    private ArrayList<Button> selectedBtns = new ArrayList<Button>();
    private ArrayList<String> found = new ArrayList<String>();
    private String typed = "";

    protected void onCreate(Bundle b){
        super.onCreate(b);
        data = new GameData(this);
        levelNo = getIntent().getIntExtra("level", 1);
        Level[] levels = Levels.all();
        if(levelNo < 1) levelNo = 1;
        if(levelNo > levels.length) levelNo = levels.length;
        level = levels[levelNo-1];
        build();
    }

    private void build(){
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(16), dp(26), dp(16), dp(16));
        root.setBackgroundColor(Color.rgb(10,16,34));

        TextView title = new TextView(this);
        title.setText(level.title);
        title.setTextColor(Color.WHITE);
        title.setTextSize(27);
        title.setGravity(Gravity.CENTER);
        root.addView(title, new LinearLayout.LayoutParams(-1,-2));

        coins = label("💰 " + data.coins() + " عملة", 17, Color.rgb(255,197,66));
        root.addView(coins);

        TextView hint = label("التلميح: " + level.hint, 18, Color.rgb(51,214,255));
        hint.setPadding(0, dp(14), 0, dp(10));
        root.addView(hint);

        foundBox = new LinearLayout(this);
        foundBox.setOrientation(LinearLayout.VERTICAL);
        foundBox.setGravity(Gravity.CENTER);
        root.addView(foundBox, new LinearLayout.LayoutParams(-1, dp(115)));
        refreshFound();

        current = label("", 28, Color.WHITE);
        current.setBackgroundColor(Color.rgb(22,35,59));
        current.setPadding(dp(10), dp(12), dp(10), dp(12));
        root.addView(current, new LinearLayout.LayoutParams(-1, dp(62)));

        lettersGrid = new GridLayout(this);
        lettersGrid.setColumnCount(4);
        LinearLayout.LayoutParams glp = new LinearLayout.LayoutParams(-2, -2);
        glp.setMargins(0, dp(18), 0, dp(14));
        root.addView(lettersGrid, glp);
        buildLetters();

        LinearLayout controls = new LinearLayout(this);
        controls.setOrientation(LinearLayout.HORIZONTAL);
        controls.setGravity(Gravity.CENTER);
        controls.addView(control("مسح", Color.rgb(90,105,135), new View.OnClickListener(){ public void onClick(View v){ clearPick(); }}));
        controls.addView(control("تلميح 10💰", Color.rgb(124,92,255), new View.OnClickListener(){ public void onClick(View v){ useHint(); }}));
        controls.addView(control("رجوع", Color.rgb(70,80,105), new View.OnClickListener(){ public void onClick(View v){ finish(); }}));
        root.addView(controls);

        setContentView(root);
    }

    private void buildLetters(){
        lettersGrid.removeAllViews();
        ArrayList<String> chars = new ArrayList<String>();
        for(int i=0;i<level.words.length;i++){
            String w = level.words[i];
            for(int j=0;j<w.length();j++) chars.add(w.substring(j,j+1));
        }
        while(chars.size() < 16){ chars.add(extraLetter()); }
        Collections.shuffle(chars, new Random(System.currentTimeMillis()));

        for(int i=0;i<chars.size();i++){
            final Button btn = new Button(this);
            btn.setText(chars.get(i));
            btn.setTextSize(24);
            btn.setTextColor(Color.WHITE);
            btn.setAllCaps(false);
            btn.setBackgroundColor(Color.rgb(33,52,88));
            GridLayout.LayoutParams gp = new GridLayout.LayoutParams();
            gp.width = dp(68);
            gp.height = dp(62);
            gp.setMargins(dp(5), dp(5), dp(5), dp(5));
            btn.setLayoutParams(gp);
            btn.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){ pick(btn); }});
            lettersGrid.addView(btn);
        }
    }

    private String extraLetter(){
        String pool = "ابتثجحخدذرزسشصضطظعغفقكلمنهوي";
        int i = new Random().nextInt(pool.length());
        return pool.substring(i, i+1);
    }

    private void pick(Button b){
        if(!b.isEnabled()) return;
        typed += b.getText().toString();
        selectedBtns.add(b);
        b.setEnabled(false);
        b.setBackgroundColor(Color.rgb(51,214,255));
        current.setText(typed);
        checkWord();
    }

    private void checkWord(){
        for(int i=0;i<level.words.length;i++){
            if(typed.equals(level.words[i]) && !found.contains(typed)){
                found.add(typed);
                for(int j=0;j<selectedBtns.size();j++) selectedBtns.get(j).setVisibility(View.INVISIBLE);
                Toast.makeText(this, "ممتاز ✅", Toast.LENGTH_SHORT).show();
                clearPickOnlyText();
                refreshFound();
                if(found.size() == level.words.length) win();
                return;
            }
        }
    }

    private void refreshFound(){
        foundBox.removeAllViews();
        for(int i=0;i<level.words.length;i++){
            String w = level.words[i];
            TextView row = label(found.contains(w) ? "✅ " + w : blanks(w), 20, found.contains(w) ? Color.rgb(90,230,150) : Color.rgb(180,195,220));
            foundBox.addView(row);
        }
    }

    private String blanks(String w){
        String s = "";
        for(int i=0;i<w.length();i++) s += "ـ ";
        return s;
    }

    private void clearPick(){
        for(int i=0;i<selectedBtns.size();i++){
            Button b = selectedBtns.get(i);
            b.setEnabled(true);
            b.setBackgroundColor(Color.rgb(33,52,88));
        }
        clearPickOnlyText();
    }

    private void clearPickOnlyText(){
        selectedBtns.clear();
        typed = "";
        current.setText("");
    }

    private void useHint(){
        if(!data.spend(10)){
            Toast.makeText(this, "تحتاج 10 عملات", Toast.LENGTH_SHORT).show();
            return;
        }
        coins.setText("💰 " + data.coins() + " عملة");
        for(int i=0;i<level.words.length;i++){
            if(!found.contains(level.words[i])){
                Toast.makeText(this, "كلمة تبدأ بـ: " + level.words[i].substring(0,1), Toast.LENGTH_LONG).show();
                return;
            }
        }
    }

    private void win(){
        data.addCoins(15);
        data.unlockNext(levelNo);
        Toast.makeText(this, "فزت! فتحت المرحلة التالية ✅", Toast.LENGTH_LONG).show();
        root.removeAllViews();
        root.setGravity(Gravity.CENTER);
        TextView done = label("فوز رائع ✅\nربحت 15 عملة\nتم فتح المرحلة التالية", 26, Color.WHITE);
        done.setGravity(Gravity.CENTER);
        root.addView(done);
        Button next = new Button(this);
        next.setText(levelNo < Levels.all().length ? "المرحلة التالية" : "أنهيت كل المراحل");
        next.setTextSize(19);
        next.setTextColor(Color.WHITE);
        next.setAllCaps(false);
        next.setBackgroundColor(Color.rgb(50,214,255));
        next.setOnClickListener(new View.OnClickListener(){ public void onClick(View v){
            if(levelNo < Levels.all().length){
                getIntent().putExtra("level", levelNo+1);
                recreate();
            } else {
                finish();
            }
        }});
        root.addView(next, new LinearLayout.LayoutParams(-1, dp(62)));
    }

    private TextView label(String s, int size, int color){
        TextView v = new TextView(this);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(color);
        v.setGravity(Gravity.CENTER);
        return v;
    }

    private Button control(String s, int color, View.OnClickListener l){
        Button b = new Button(this);
        b.setText(s);
        b.setTextColor(Color.WHITE);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setBackgroundColor(color);
        b.setOnClickListener(l);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(105), dp(50));
        lp.setMargins(dp(4),0,dp(4),0);
        b.setLayoutParams(lp);
        return b;
    }

    private int dp(int v){ return (int)(v * getResources().getDisplayMetrics().density); }
}
