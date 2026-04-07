package com.example.minigtarace;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.graphics.*;

import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new GameView(this));
    }
}

class GameView extends SurfaceView implements SurfaceHolder.Callback {
    GameThread thread;
    float carX = 500;

    ArrayList<Rect> enemies = new ArrayList<>();
    ArrayList<Rect> coins = new ArrayList<>();

    Random random = new Random();

    int score = 0;
    int money = 0;
    boolean gameOver = false;

    public GameView(android.content.Context context) {
        super(context);
        getHolder().addCallback(this);
        thread = new GameThread(getHolder(), this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        thread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {}

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!gameOver) carX = event.getX();
        else resetGame();
        return true;
    }

    void resetGame() {
        enemies.clear();
        coins.clear();
        score = 0;
        money = 0;
        gameOver = false;
    }

    public void update() {
        if (gameOver) return;

        // spawn enemies
        if (random.nextInt(25) == 0) {
            int x = 200 + random.nextInt(600);
            enemies.add(new Rect(x, -100, x + 100, 0));
        }

        // spawn coins
        if (random.nextInt(20) == 0) {
            int x = 200 + random.nextInt(600);
            coins.add(new Rect(x, -60, x + 60, 0));
        }

        Rect player = new Rect((int)carX - 50, getHeight() - 200, (int)carX + 50, getHeight() - 100);

        // enemies
        for (int i = 0; i < enemies.size(); i++) {
            Rect e = enemies.get(i);
            e.offset(0, 15);

            if (Rect.intersects(player, e)) {
                gameOver = true;
            }

            if (e.top > getHeight()) {
                enemies.remove(i);
                score++;
                i--;
            }
        }

        // coins
        for (int i = 0; i < coins.size(); i++) {
            Rect c = coins.get(i);
            c.offset(0, 12);

            if (Rect.intersects(player, c)) {
                coins.remove(i);
                money++;
                i--;
                continue;
            }

            if (c.top > getHeight()) {
                coins.remove(i);
                i--;
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (canvas == null) return;

        Paint paint = new Paint();

        // фон
        canvas.drawColor(Color.GRAY);

        // дорога
        paint.setColor(Color.DKGRAY);
        canvas.drawRect(200, 0, 800, getHeight(), paint);

        // машина
        paint.setColor(Color.RED);
        Rect player = new Rect((int)carX - 50, getHeight() - 200, (int)carX + 50, getHeight() - 100);
        canvas.drawRect(player, paint);

        // враги
        paint.setColor(Color.BLUE);
        for (Rect e : enemies) canvas.drawRect(e, paint);

        // монеты
        paint.setColor(Color.YELLOW);
        for (Rect c : coins) canvas.drawOval(new RectF(c), paint);

        // текст
        paint.setColor(Color.WHITE);
        paint.setTextSize(50);
        canvas.drawText("Score: " + score, 40, 70, paint);
        canvas.drawText("Coins: " + money, 40, 130, paint);

        if (gameOver) {
            paint.setTextSize(100);
            canvas.drawText("GAME OVER", 120, getHeight()/2, paint);
            paint.setTextSize(50);
            canvas.drawText("Tap to restart", 180, getHeight()/2 + 80, paint);
        }
    }
}

class GameThread extends Thread {
    private SurfaceHolder holder;
    private GameView game;
    private boolean running;

    public GameThread(SurfaceHolder holder, GameView game) {
        this.holder = holder;
        this.game = game;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    @Override
    public void run() {
        while (running) {
            Canvas canvas = null;
            try {
                canvas = holder.lockCanvas();
                synchronized (holder) {
                    game.update();
                    game.onDraw(canvas);
                }
            } finally {
                if (canvas != null) holder.unlockCanvasAndPost(canvas);
            }
        }
    }
}