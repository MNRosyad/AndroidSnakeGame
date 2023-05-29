package com.papb2.simplesnake;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.atomic.AtomicInteger;

public class MainActivity extends AppCompatActivity {
    private static final int FPS = 60;
    private static final int SPEED = 25;

    private static final int STATUS_PAUSED = 1;
    private static final int STATUS_START = 2;
    private static final int STATUS_OVER = 3;
    private static final int STATUS_PLAYING = 4;

    private GameView gameView;
    private TextView gameStatusText;
    private TextView gameScoreText;
    private Button gameBtn;

    private final AtomicInteger gameStatus = new AtomicInteger(STATUS_START);
    private final Handler handler = new Handler();

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gameView = findViewById(R.id.gameLayout);
        gameStatusText = findViewById(R.id.gameStatus);
        gameScoreText = findViewById(R.id.scoreText);
        gameBtn = findViewById(R.id.startButton);

        gameView.init();
        gameView.setGameScoreUpdatedListener(score -> {
            handler.post(() -> gameScoreText.setText("Score: " + score));
        });

        findViewById(R.id.upButton).setOnClickListener(v -> {
            if (gameStatus.get() == STATUS_PLAYING) {
                gameView.setDirection(Direction.UP);
            }
        });
        findViewById(R.id.downButton).setOnClickListener(v -> {
            if (gameStatus.get() == STATUS_PLAYING) {
                gameView.setDirection(Direction.DOWN);
            }
        });
        findViewById(R.id.leftButton).setOnClickListener(v -> {
            if (gameStatus.get() == STATUS_PLAYING) {
                gameView.setDirection(Direction.LEFT);
            }
        });
        findViewById(R.id.rightButton).setOnClickListener(v -> {
            if (gameStatus.get() == STATUS_PLAYING) {
                gameView.setDirection(Direction.RIGHT);
            }
        });

        gameBtn.setOnClickListener(v -> {
            if (gameStatus.get() == STATUS_PLAYING) {
                setGameStatus(STATUS_PAUSED);
            } else {
                setGameStatus(STATUS_PLAYING);
            }
        });

        setGameStatus(STATUS_START);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (gameStatus.get() == STATUS_PLAYING) {
            setGameStatus(STATUS_PAUSED);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setGameStatus(int gameStatus) {
        int prevStatus = this.gameStatus.get();
        gameStatusText.setVisibility(View.VISIBLE);
        gameBtn.setText("start");
        this.gameStatus.set(gameStatus);
        switch (gameStatus) {
            case STATUS_OVER:
                gameStatusText.setText("GAME OVER");
                break;
            case STATUS_START:
                gameView.newGame();
                gameStatusText.setText("START GAME");
                break;
            case STATUS_PAUSED:
                gameStatusText.setText("GAME PAUSED");
                break;
            case STATUS_PLAYING:
                if (prevStatus == STATUS_OVER) {
                    gameView.newGame();
                }
                startGame();
                gameStatusText.setVisibility(View.INVISIBLE);
                gameBtn.setText("pause");
                break;
        }
    }

    private void startGame() {
        final int delay = 1000 / FPS;
        new Thread(() -> {
            int count = 0;
            while (!gameView.isGameOver() && gameStatus.get() != STATUS_PAUSED) {
                try {
                    Thread.sleep(delay);
                    if (count % SPEED == 0) {
                        gameView.next();
                        handler.post(gameView::invalidate);
                    }
                    count++;
                } catch (InterruptedException ignored) {
                }
            }
            if (gameView.isGameOver()) {
                handler.post(() -> setGameStatus(STATUS_OVER));
            }
        }).start();
    }
}