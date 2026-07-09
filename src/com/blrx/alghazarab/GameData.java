package com.blrx.alghazarab;

import android.content.Context;
import android.content.SharedPreferences;

public class GameData {
    private static final String PREF = "alghaz_save";
    private static final String K_UNLOCKED = "unlocked";
    private static final String K_COINS = "coins";
    private final SharedPreferences sp;

    public GameData(Context c){
        sp = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        if(!sp.contains(K_UNLOCKED)){
            sp.edit().putInt(K_UNLOCKED, 1).putInt(K_COINS, 25).apply();
        }
    }

    public int unlocked(){ return sp.getInt(K_UNLOCKED, 1); }
    public int coins(){ return sp.getInt(K_COINS, 0); }
    public void addCoins(int v){ sp.edit().putInt(K_COINS, coins()+v).apply(); }

    public boolean spend(int v){
        if(coins() < v) return false;
        sp.edit().putInt(K_COINS, coins()-v).apply();
        return true;
    }

    public void unlockNext(int level){
        if(level >= unlocked()) sp.edit().putInt(K_UNLOCKED, level+1).apply();
    }
}
