package jp.ac.titech.itpro.maaki.bttrax;

public class CrossPanel extends TraxPanel{

    public CrossPanel(Colors up, Colors left, int image) {
        super(up, left, up, left, image);
    }

    @Override
    public Direction connectUp() {
        return Direction.DOWN;
    }

    @Override
    public Direction connectLeft() {
        return Direction.RIGHT;
    }

    @Override
    public Direction connectDown() {
        return Direction.UP;
    }

    @Override
    public Direction connectRight() {
        return Direction.LEFT;
    }
}
