package jp.ac.titech.itpro.maaki.bttrax;

public class RUCurvePanel extends TraxPanel {

    public RUCurvePanel(Colors up, Colors down, int image) {
        super(up, down, down, up, image);
    }

    @Override
    public Direction connectUp() {
        return Direction.RIGHT;
    }

    @Override
    public Direction connectLeft() {
        return Direction.DOWN;
    }

    @Override
    public Direction connectDown() {
        return Direction.LEFT;
    }

    @Override
    public Direction connectRight() {
        return Direction.UP;
    }

}
