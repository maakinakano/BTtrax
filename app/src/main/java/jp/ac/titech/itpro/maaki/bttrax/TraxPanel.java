package jp.ac.titech.itpro.maaki.bttrax;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;

public abstract class TraxPanel {
    protected Colors up, left, down, right;
    protected int image;

    public final static TraxPanel nonePanel = new TraxPanel() {
        @Override
        public Direction connectUp() {
            return Direction.UP;
        }

        @Override
        public Direction connectLeft() {
            return Direction.LEFT;
        }

        @Override
        public Direction connectDown() {
            return Direction.DOWN;
        }

        @Override
        public Direction connectRight() {
            return Direction.RIGHT;
        }
    };

    public TraxPanel(Colors up, Colors left, Colors down, Colors right, int image) {
        this.up = up;
        this.left = left;
        this.down = down;
        this.right = right;
        this.image = image;
    }

    public TraxPanel() {
        this.up = Colors.NONE;
        this.left = Colors.NONE;
        this.down = Colors.NONE;
        this.right = Colors.NONE;
    }

    public abstract Direction connectUp();
    public abstract Direction connectLeft();
    public abstract Direction connectDown();
    public abstract Direction connectRight();

    public Direction colorDirection(Colors c) {
        for(Direction d : Direction.values())
            if(getDirection(d) == c) return d;
        throw new RuntimeException();
    }

    public Direction connect(Direction d) {
        switch (d) {
            case UP:
                return connectUp();
            case LEFT:
                return connectLeft();
            case DOWN:
                return connectDown();
            case RIGHT:
                return connectRight();
        }
        throw new RuntimeException();
    }

    public Colors getDirection(Direction d) {
        switch (d) {
            case UP:
                return up;
            case LEFT:
                return left;
            case DOWN:
                return down;
            case RIGHT:
                return right;
            default:
                throw new RuntimeException();
        }
    }

    public void draw(Context context, Canvas canvas, float x, float y, float width) {
        if(up == Colors.NONE || left == Colors.NONE || down == Colors.NONE || right == Colors.NONE)
            return;
        Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), image);
        Matrix matrix = new Matrix();
        matrix.postScale(width/bmp.getHeight(), width/bmp.getWidth());
        Bitmap bmp2 = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        canvas.drawBitmap(bmp2, x, y, null);
    }
}
