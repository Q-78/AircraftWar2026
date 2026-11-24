package edu.hitsz.aircraftwar;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public final class BaseGame extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private final SurfaceHolder holder;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private final GameWorld world = new GameWorld();
    private final Bitmap bg, hero, mob, elite, elitePlus, heroBullet, enemyBullet;
    private volatile boolean surfaceReady, activityReady = true, running;
    private volatile float desiredX = Float.NaN, desiredY = Float.NaN;
    private Thread thread;

    public BaseGame(Context context) {
        super(context);
        holder = getHolder(); holder.addCallback(this);
        bg = BitmapFactory.decodeResource(getResources(), R.drawable.bg);
        hero = BitmapFactory.decodeResource(getResources(), R.drawable.hero);
        mob = BitmapFactory.decodeResource(getResources(), R.drawable.mob);
        elite = BitmapFactory.decodeResource(getResources(), R.drawable.elite);
        elitePlus = BitmapFactory.decodeResource(getResources(), R.drawable.eliteplus);
        heroBullet = BitmapFactory.decodeResource(getResources(), R.drawable.bullet_hero);
        enemyBullet = BitmapFactory.decodeResource(getResources(), R.drawable.bullet_enemy);
        setFocusable(true);
    }

    @Override public void surfaceCreated(SurfaceHolder h) { surfaceReady = true; start(); }
    @Override public void surfaceChanged(SurfaceHolder h, int format, int width, int height) { }
    @Override public void surfaceDestroyed(SurfaceHolder h) { surfaceReady = false; stop(); }
    public void pause() { activityReady = false; stop(); }
    public void resume() { activityReady = true; start(); }
    private synchronized void start() {
        if (!surfaceReady || !activityReady || running) return;
        running = true; thread = new Thread(this, "GameLoop"); thread.start();
    }
    private void stop() {
        Thread old;
        synchronized (this) { running = false; old = thread; thread = null; }
        if (old != null && old != Thread.currentThread()) {
            old.interrupt();
            try { old.join(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        }
    }

    @Override public void run() {
        long previous = System.nanoTime();
        while (running) {
            long now = System.nanoTime();
            float dt = (now - previous) / 1_000_000_000f; previous = now;
            if (!Float.isNaN(desiredX)) world.moveHero(desiredX, desiredY);
            world.update(dt);
            Canvas canvas = null;
            try { canvas = holder.lockCanvas(); if (canvas != null) drawWorld(canvas); }
            catch (IllegalArgumentException ignored) { }
            finally { if (canvas != null) holder.unlockCanvasAndPost(canvas); }
            long sleep = (16_666_667 - (System.nanoTime() - now)) / 1_000_000;
            if (sleep > 0) try { Thread.sleep(sleep); } catch (InterruptedException e) { break; }
        }
    }

    private void drawWorld(Canvas c) {
        float scale = Math.min(c.getWidth() / (float) GameWorld.WIDTH,
                c.getHeight() / (float) GameWorld.HEIGHT);
        float left = (c.getWidth() - GameWorld.WIDTH * scale) / 2f;
        float top = (c.getHeight() - GameWorld.HEIGHT * scale) / 2f;
        c.drawColor(Color.BLACK); c.save(); c.translate(left, top); c.scale(scale, scale);
        paint.setColor(Color.WHITE);
        float shift = world.background;
        c.drawBitmap(bg, null, new RectF(0, shift - GameWorld.HEIGHT, GameWorld.WIDTH, shift), paint);
        c.drawBitmap(bg, null, new RectF(0, shift, GameWorld.WIDTH, shift + GameWorld.HEIGHT), paint);
        for (GameWorld.Object e : world.enemies) drawBitmap(c, bitmapFor(e.kind), e);
        for (GameWorld.Object b : world.heroBullets) drawBitmap(c, heroBullet, b);
        for (GameWorld.Object b : world.enemyBullets) drawBitmap(c, enemyBullet, b);
        drawBitmap(c, hero, world.hero);
        paint.setTextSize(24); c.drawText("SCORE " + world.score, 12, 32, paint);
        c.drawText("HP " + world.hero.hp, 385, 32, paint);
        if (world.over) { paint.setTextSize(42); c.drawText("GAME OVER", 125, 370, paint); }
        c.restore();
    }
    private Bitmap bitmapFor(int kind) {
        if (kind == GameWorld.ELITE) return elite;
        if (kind == GameWorld.ELITE_PLUS) return elitePlus;
        return mob;
    }
    private void drawBitmap(Canvas c, Bitmap image, GameWorld.Object o) {
        paint.setColor(Color.WHITE);
        c.drawBitmap(image, null, new RectF(o.x - o.w / 2, o.y - o.h / 2,
                o.x + o.w / 2, o.y + o.h / 2), paint);
    }
    @Override public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        if (action == MotionEvent.ACTION_DOWN || action == MotionEvent.ACTION_MOVE) {
            float scale = Math.min(getWidth() / (float) GameWorld.WIDTH,
                    getHeight() / (float) GameWorld.HEIGHT);
            if (scale <= 0) return true;
            desiredX = (event.getX() - (getWidth() - GameWorld.WIDTH * scale) / 2f) / scale;
            desiredY = (event.getY() - (getHeight() - GameWorld.HEIGHT * scale) / 2f) / scale;
            return true;
        }
        return action == MotionEvent.ACTION_UP || super.onTouchEvent(event);
    }
}
