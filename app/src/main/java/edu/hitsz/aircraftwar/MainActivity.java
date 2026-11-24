package edu.hitsz.aircraftwar;

import android.app.Activity;
import android.os.Bundle;

public final class MainActivity extends Activity {
    private BaseGame game;
    @Override public void onCreate(Bundle state) { super.onCreate(state); game = new BaseGame(this); setContentView(game); }
    @Override protected void onPause() { super.onPause(); game.pause(); }
    @Override protected void onResume() { super.onResume(); if (game != null) game.resume(); }
}
