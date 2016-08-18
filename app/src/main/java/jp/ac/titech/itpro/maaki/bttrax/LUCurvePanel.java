package jp.ac.titech.itpro.maaki.bttrax;

public class LUCurvePanel extends TraxPanel{

    public LUCurvePanel(Colors up, Colors down, int image) {
        super(up, up, down, down, image);
    }

    @Override
    public Direction connectUp() {
        return Direction.LEFT;
    }

    @Override
    public Direction connectLeft() {
        return Direction.UP;
    }

    @Override
    public Direction connectDown() {
        return Direction.RIGHT;
    }

    @Override
    public Direction connectRight() {
        return Direction.DOWN;
    }

}
