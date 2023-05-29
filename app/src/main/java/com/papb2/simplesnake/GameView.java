package com.papb2.simplesnake;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.LinkedList;
import java.util.Random;

public class GameView extends View {
    private static final int MAP_SIZE = 20;
    private static final int START_X = 5;
    private static final int START_Y = 10;

    private final Point[][] points = new Point[MAP_SIZE][MAP_SIZE];
    private final LinkedList<Point> snake = new LinkedList<>();
    private Direction dir;

    private ScoreUpdatedListener scoreUpdatedListener;

    private boolean gameOver = false;

    private int boxSize;
    private int boxPadding;

    private final Paint paint = new Paint();

    public GameView(Context context) {
        super(context);
    }

    public GameView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public GameView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void init() {
        boxSize = getContext().getResources()
                .getDimensionPixelSize(R.dimen.game_size) / MAP_SIZE;
        boxPadding = boxSize / 10;
    }

    public void newGame() {
        gameOver = false;
        dir = Direction.RIGHT;
        initMap();
        updateScore();
    }

    public void setGameScoreUpdatedListener(ScoreUpdatedListener scoreUpdatedListener) {
        this.scoreUpdatedListener = scoreUpdatedListener;
    }

    private void initMap() {
        for (int i = 0; i < MAP_SIZE; i++) {
            for (int j = 0; j < MAP_SIZE; j++) {
                points[i][j] = new Point(j, i);
            }
        }
        snake.clear();
        for (int i = 0; i < 3; i++) {
            Point point = getPoint(START_X + i, START_Y);
            point.type = PointType.SNAKE;
            snake.addFirst(point);
        }
        randomApple();
    }

    private void randomApple() {
        Random random = new Random();
        while (true) {
            Point point = getPoint(random.nextInt(MAP_SIZE),
                    random.nextInt(MAP_SIZE));
            if (point.type == PointType.EMPTY) {
                point.type = PointType.APPLE;
                break;
            }
        }
    }

    private Point getPoint(int x, int y) {
        return points[y][x];
    }

    public void next() {
        Point first = snake.getFirst();
        Point next = getNext(first);

        switch (next.type) {
            case EMPTY:
                next.type = PointType.SNAKE;
                snake.addFirst(next);
                snake.getLast().type = PointType.EMPTY;
                snake.removeLast();
                break;
            case APPLE:
                next.type = PointType.SNAKE;
                snake.addFirst(next);
                randomApple();
                updateScore();
                break;
            case SNAKE:
                gameOver = true;
                break;
        }
    }

    public void updateScore() {
        if (scoreUpdatedListener != null) {
            int score = snake.size() - 3;
            scoreUpdatedListener.onScoreUpdated(score);
        }
    }

    public void setDirection(Direction dir) {
        if ((dir == Direction.LEFT || dir == Direction.RIGHT) &&
                (this.dir == Direction.LEFT || this.dir == Direction.RIGHT)) {
            return;
        }
        if ((dir == Direction.UP || dir == Direction.DOWN) &&
                (this.dir == Direction.UP || this.dir == Direction.DOWN)) {
            return;
        }
        this.dir = dir;
    }

    private Point getNext(Point point) {
        int x = point.x;
        int y = point.y;

        switch (dir) {
            case UP:
                y = y == 0 ? MAP_SIZE - 1 : y - 1;
                break;
            case DOWN:
                y = y == MAP_SIZE - 1 ? 0 : y + 1;
                break;
            case LEFT:
                x = x == 0 ? MAP_SIZE - 1 : x - 1;
                break;
            case RIGHT:
                x = x == MAP_SIZE - 1 ? 0 : x + 1;
                break;
        }
        return getPoint(x, y);
    }

    public boolean isGameOver() {
        return gameOver;
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        for (int y = 0; y < MAP_SIZE; y++) {
            for (int x = 0; x < MAP_SIZE; x++) {
                int left = boxSize * x;
                int right = left + boxSize;
                int top = boxSize * y;
                int bottom = top + boxSize;
                switch (getPoint(x, y).type) {
                    case APPLE:
                        paint.setColor(Color.RED);
                        break;
                    case SNAKE:
                        paint.setColor(Color.BLACK);
                        canvas.drawRect(left, top, right, bottom, paint);
                        paint.setColor(Color.WHITE);
                        left += boxPadding;
                        right -= boxPadding;
                        top += boxPadding;
                        bottom -= boxPadding;
                        break;
                    case EMPTY:
                        paint.setColor(Color.BLACK);
                        break;
                }
                canvas.drawRect(left, top, right, bottom, paint);
            }
        }
    }
}
