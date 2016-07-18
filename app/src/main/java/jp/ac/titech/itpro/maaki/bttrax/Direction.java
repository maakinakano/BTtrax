package jp.ac.titech.itpro.maaki.bttrax;

import java.util.Arrays;

public enum Direction {
    UP(0, -1),
    LEFT(-1, 0),
    DOWN(0, 1),
    RIGHT(1, 0);

    public final int dx;
    public final int dy;
    Direction(int dx, int dy) {
        this.dx = dx;
        this.dy = dy;

    }

    public Direction getReverse() {
        switch (this) {
            case UP:
                return DOWN;
            case LEFT:
                return RIGHT;
            case DOWN:
                return UP;
            case RIGHT:
                return LEFT;
        }
        throw new RuntimeException();
    }

}
