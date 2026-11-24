package edu.hitsz.aircraftwar;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Easy-mode rules adapted from the original Game, factories, shooting strategy and
 * AbstractFlyingObject. One tick corresponds to the desktop game's 40 ms timer.
 * This class has no Android or Java AWT dependency.
 */
public final class GameWorld {
    public static final int WIDTH = 512, HEIGHT = 768;
    private static final float STEP = 0.04f;
    public static final int HERO = 0, MOB = 1, ELITE = 2, ELITE_PLUS = 3,
            HERO_BULLET = 4, ENEMY_BULLET = 5;

    public static final class Object {
        public float x, y, vx, vy;
        public final int kind;
        public final float w, h;
        public int hp, power;
        Object(int kind, float x, float y, float vx, float vy,
               float w, float h, int hp, int power) {
            this.kind = kind; this.x = x; this.y = y; this.vx = vx; this.vy = vy;
            this.w = w; this.h = h; this.hp = hp; this.power = power;
        }
        boolean aircraft() { return kind == HERO || kind == MOB || kind == ELITE || kind == ELITE_PLUS; }
        /** Same center-based overlap and reduced aircraft height as original crash(). */
        boolean hits(Object other) {
            return Math.abs(x - other.x) < (w + other.w) / 2f
                    && Math.abs(y - other.y) < (h / (aircraft() ? 2f : 1f)
                    + other.h / (other.aircraft() ? 2f : 1f)) / 2f;
        }
        void forward() {
            x += vx; y += vy;
            if (x <= 0 || x >= WIDTH) vx = -vx;
        }
    }

    public final Object hero = new Object(HERO, WIDTH / 2f, HEIGHT - 83, 0, 0, 100, 83, 100, 30);
    public final List<Object> enemies = new ArrayList<>();
    public final List<Object> heroBullets = new ArrayList<>();
    public final List<Object> enemyBullets = new ArrayList<>();
    public int score;
    public boolean over;
    public float background;
    private int spawnCounter, heroShootCounter, enemyShootCounter, comboCount;
    private float accumulator, lastKillAt, elapsed;
    private final Random random;

    public GameWorld() { this(new Random()); }
    GameWorld(Random random) { this.random = random; }

    public void moveHero(float x, float y) {
        hero.x = Math.max(hero.w / 2, Math.min(WIDTH - hero.w / 2, x));
        hero.y = Math.max(hero.h / 2, Math.min(HEIGHT - hero.h / 2, y));
    }

    public void update(float dt) {
        if (over) return;
        accumulator += Math.max(0, Math.min(dt, 0.2f));
        while (accumulator >= STEP && !over) { tick(); accumulator -= STEP; }
    }

    private void tick() {
        elapsed += STEP;
        background = (background + 1) % HEIGHT;
        if (++spawnCounter >= 28) {
            spawnCounter = 0;
            if (enemies.size() < 4) spawnEnemy();
        }
        if (++heroShootCounter >= 18) {
            heroShootCounter = 0;
            heroBullets.add(new Object(HERO_BULLET, hero.x, hero.y - 2, 0, -5, 10, 25, 1, 30));
        }
        if (++enemyShootCounter >= 28) {
            enemyShootCounter = 0;
            for (Object enemy : enemies) {
                if (enemy.kind == ELITE || enemy.kind == ELITE_PLUS) {
                    int count = enemy.kind == ELITE ? 3 : 2;
                    int spacing = enemy.kind == ELITE ? 10 : 15;
                    for (int i = 0; i < count; i++) {
                        int offset = (i * 2 - count + 1) * spacing;
                        enemyBullets.add(new Object(ENEMY_BULLET, enemy.x + offset,
                                enemy.y + 2, 0, enemy.vy + 5, 10, 18, 1, 20));
                    }
                }
            }
        }
        for (Object e : enemies) e.forward();
        for (Object b : heroBullets) b.forward();
        for (Object b : enemyBullets) b.forward();
        resolveHits();
    }

    private void spawnEnemy() {
        int x = random.nextInt(WIDTH - 75);
        int y = random.nextInt((int) (HEIGHT * .05f) + 1);
        double roll = random.nextDouble();
        if (roll < .70) enemies.add(new Object(MOB, x, y, 0, 10, 75, 50, 30, 0));
        else if (roll < .90) enemies.add(new Object(ELITE, x, y, 0, 12, 105, 68, 30, 20));
        else enemies.add(new Object(ELITE_PLUS, x, y, 2, 12, 105, 68, 40, 20));
    }

    private void resolveHits() {
        Iterator<Object> eb = enemyBullets.iterator();
        while (eb.hasNext()) {
            Object b = eb.next();
            if (b.x <= 0 || b.x >= WIDTH || b.y >= HEIGHT) { eb.remove(); continue; }
            if (hero.hits(b)) { hero.hp -= b.power; eb.remove(); }
        }
        Iterator<Object> hb = heroBullets.iterator();
        while (hb.hasNext()) {
            Object b = hb.next();
            if (b.x <= 0 || b.x >= WIDTH || b.y <= 0) { hb.remove(); continue; }
            for (Object e : enemies) {
                if (e.hp > 0 && e.hits(b)) {
                    e.hp -= b.power; hb.remove();
                    if (e.hp <= 0) addScore();
                    break;
                }
            }
        }
        Iterator<Object> es = enemies.iterator();
        while (es.hasNext()) {
            Object e = es.next();
            if (e.hp <= 0 || e.y >= HEIGHT) { es.remove(); continue; }
            if (e.hits(hero)) { es.remove(); hero.hp = 0; }
        }
        if (hero.hp <= 0) { hero.hp = 0; over = true; }
    }

    private void addScore() {
        if (elapsed - lastKillAt > 3) comboCount = 0;
        comboCount++;
        lastKillAt = elapsed;
        score += Math.round(10 * (1f + Math.min(comboCount - 1, 20) * .05f));
    }
}
