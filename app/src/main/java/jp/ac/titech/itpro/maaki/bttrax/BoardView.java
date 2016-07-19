package jp.ac.titech.itpro.maaki.bttrax;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class BoardView extends View {
    private Paint p = new Paint();
    private int field[][];
    private int size;
    private float width;
    private static final int BOARD_SIZE = 200;
    private int orientX;
    private int orientY;
    private int touchX, touchY;
    private int addX, addY;
    private Colors turn = Colors.WHITE;
    private Colors winner = Colors.NONE;

    public BoardView(Context context) {
        this(context, null);
    }

    public BoardView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BoardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        size = 1;
        field = new int[BOARD_SIZE][BOARD_SIZE];
        for(int i=0; i<BOARD_SIZE; i++)
            for(int j=0; j<BOARD_SIZE; j++)
                field[i][j] = 0;
        touchX = -1;
        touchY = -1;
        orientX = 100;
        orientY = 100;
    }

    private void turnFlip() {
        switch (turn) {
            case RED:
                turn = Colors.WHITE;
                return;
            case WHITE:
                turn = Colors.RED;
                return;
            default:
                throw new RuntimeException();
        }
    }

    public Colors getTurn() {
        return turn;
    }

    public Colors getWinner() {
        return winner;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                touchX = (int)(event.getX()/width);
                touchY = (int)(event.getY()/width);
                if(touchX < 0) touchX = 0;
                if(touchY < 0) touchY = 0;
                if(touchX >= size) touchX = size-1;
                if(touchY >= size) touchY = size-1;
                invalidate();
                break;
        }
        return true;
    }

    public boolean onClickPanel(int buttonNum) {
        if(size == 1){
            addPanel(100, 100, buttonNum*2+1);
            return true;
        }
        return onClickPanel(buttonNum, orientX - size / 2 + touchX, orientY - size / 2 + touchY);
    }

    public boolean onClickPanel(int buttonNum, int x, int y) {
        if(size == 1) {
            addPanel(100, 100, buttonNum * 2 + 1);
            return true;
        }
        int tempID;
        Colors neighbor = Colors.NONE;
        Direction neigD = Direction.UP;
        //already exist
        if (field[x][y] != 0)
            return false;
        //get neighborhood panel
        for (Direction d : Direction.values()) {
            int neigID = field[x + d.dx][y + d.dy];
            if (neigID == 0) continue;
            neighbor = TraxPanelUtil.of(neigID).getDirection(d.getReverse());
            neigD = d;
            break;
        }
        //no neighborhood
        if (neighbor == Colors.NONE)
            return false;
        //get available panel
        if (TraxPanelUtil.of(buttonNum * 2 + 1).getDirection(neigD) == neighbor)
            tempID = buttonNum * 2 + 1;
        else
            tempID = buttonNum * 2 + 2;
        //check availability
        for (Direction d : Direction.values()) {
            TraxPanel tempPanel = TraxPanelUtil.of(field[x + d.dx][y + d.dy]);
            if (tempPanel.getDirection(d.getReverse()) != Colors.NONE && tempPanel.getDirection(d.getReverse()) != TraxPanelUtil.of(tempID).getDirection(d))
                return false;
        }
        //add panel
        addPanel(x, y, tempID);
        return true;
    }

    public void addPanel(int x, int y, int ID) {
        field[x][y] = ID;
        turnFlip();
        autoAdd(x, y);
        sizeChange();
        winCheck();
        invalidate();
        addX = x;
        addY = y;
        touchX = -1;
        touchY = -1;
    }

    public void winCheck() {
        //white roop check
        boolean redF = roopCheck(Colors.RED) || lineCheck(Colors.RED);
        boolean whiteF = roopCheck(Colors.WHITE) || lineCheck(Colors.WHITE);
        if(redF & !whiteF) winner = Colors.RED;
        if(!redF & whiteF) winner = Colors.WHITE;
        if(redF & whiteF) winner = Colors.DRAW;
    }

    public int getAddX() {
        return addX;
    }

    public int getAddY() {
        return addY;
    }

    private boolean roopCheck(Colors colors) {
        boolean f = false;
        boolean checkField[][] = new boolean[size][size];
        for(int i=0; i<size; i++)
            for(int j=0; j<size; j++)
                checkField[i][j] = false;

        for(int i=0; i<size; i++)
            for(int j=0; j<size; j++) {
                if(checkField[i][j]) continue;
                if(field[orientX-size/2+i][orientY-size/2+j] == 0) continue;
                f|=roopCheckSub(checkField, i, j, TraxPanelUtil.of(field[orientX-size/2+i][orientY-size/2+j]).colorDirection(colors).getReverse());
            }
        return f;
    }

    private boolean roopCheckSub(boolean[][] checkField, int x, int y, Direction d) {
        int tempX = orientX-size/2+x;
        int tempY = orientY-size/2+y;
        if(field[tempX][tempY] == 0)
            return false;
        if(checkField[x][y])
            return true;
        checkField[x][y] = true;
        Direction connectTo = TraxPanelUtil.of(field[tempX][tempY]).connect(d.getReverse());
        return roopCheckSub(checkField, x+connectTo.dx, y+connectTo.dy, connectTo);
    }

    private boolean lineCheck(Colors colors) {
        if(size < 10)
            return false;
        int tempMaxX = 0, tempMaxY = 0;
        int tempMinX = BOARD_SIZE, tempMinY = BOARD_SIZE;
        for(int i=0; i<BOARD_SIZE; i++)
            for(int j=0; j<BOARD_SIZE; j++) {
                if(field[i][j] == 0) continue;
                if(tempMaxX < i) tempMaxX = i;
                if(tempMaxY < j) tempMaxY = j;
                if(tempMinX > i) tempMinX = i;
                if(tempMinY > j) tempMinY = j;
            }
        //vertical victory line
        for(int i=0; i<size; i++) {
            int tempX = orientX-size/2+i;
            if(field[tempX][tempMinY] == 0) continue;
            if(TraxPanelUtil.of(field[tempX][tempMinY]).getDirection(Direction.UP) != colors) continue;
            int j = tempMinY;
            Direction d = Direction.DOWN;
            for(;;) {
                if(field[tempX][j] == 0) break;
                TraxPanel tempPanel = TraxPanelUtil.of(field[tempX][j]);
                d = tempPanel.connect(d.getReverse());
                tempX+=d.dx;
                j+=d.dy;
            }
            if(j == tempMaxY+1 && d == Direction.DOWN && tempMaxY-tempMinY+1 >= 8) return true;
        }
        // horizon victory line
        for(int j=0; j<size; j++) {
            int tempY = orientY-size/2+j;
            if(field[tempMinX][tempY] == 0) continue;
            if(TraxPanelUtil.of(field[tempMinX][tempY]).getDirection(Direction.LEFT) != colors) continue;
            int i = tempMinX;
            Direction d = Direction.RIGHT;
            for(;;) {
                if(field[i][tempY] == 0) break;
                TraxPanel tempPanel = TraxPanelUtil.of(field[i][tempY]);
                d = tempPanel.connect(d.getReverse());
                i+=d.dx;
                tempY+=d.dy;
            }
            if(i == tempMaxX+1 && d == Direction.RIGHT && tempMaxX-tempMinX+1 >= 8) return true;
        }
        return false;
    }

    private void sizeChange() {
        int tempMaxX = 0, tempMaxY = 0;
        int tempMinX = BOARD_SIZE, tempMinY = BOARD_SIZE;
        for(int i=0; i<BOARD_SIZE; i++)
            for(int j=0; j<BOARD_SIZE; j++) {
                if(field[i][j] == 0) continue;
                if(tempMaxX < i) tempMaxX = i;
                if(tempMaxY < j) tempMaxY = j;
                if(tempMinX > i) tempMinX = i;
                if(tempMinY > j) tempMinY = j;
            }
        size = (tempMaxX-tempMinX < tempMaxY-tempMinY) ? tempMaxY-tempMinY+3 : tempMaxX-tempMinX+3;
        orientX = (tempMaxX+tempMinX)/2-(size%2)+1;
        orientY = (tempMaxY+tempMinY)/2-(size%2)+1;
    }

    private void autoAdd(int nowX, int nowY) {
        for(Direction d1 : Direction.values()) {
            TraxPanel tempPanel = TraxPanelUtil.of(field[nowX+d1.dx][nowY+d1.dy]);
            if(tempPanel.getDirection(d1.getReverse()) != Colors.NONE)
                continue;
            int tempX = nowX+d1.dx;
            int tempY = nowY+d1.dy;
            int redN = 0;
            int whiteN = 0;
            for(Direction d2 : Direction.values()) {
                TraxPanel tempPanel2 = TraxPanelUtil.of(field[tempX+d2.dx][tempY+d2.dy]);
                if(tempPanel2.getDirection(d2.getReverse()) == Colors.RED) redN++;
                if(tempPanel2.getDirection(d2.getReverse()) == Colors.WHITE) whiteN++;
            }
            if(redN >= 2 || whiteN >=2) {
                for(int i=1; i<=6; i++) {
                    TraxPanel tempPanel3 = TraxPanelUtil.of(i);
                    int agree = 0;
                    for(Direction d3 : Direction.values())
                        if(field[tempX+d3.dx][tempY+d3.dy] == 0 || TraxPanelUtil.of(field[tempX+d3.dx][tempY+d3.dy]).getDirection(d3.getReverse()) == tempPanel3.getDirection(d3))
                            agree++;
                    if(agree == 4) {
                        field[tempX][tempY] = i;
                        autoAdd(tempX, tempY);
                        break;
                    }
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        width = (canvas.getWidth() < canvas.getHeight()) ? canvas.getWidth()/this.size : canvas.getHeight()/this.size;

        if(winner == Colors.NONE) {
            p.setColor(Color.argb(255, 220, 220, 220));
            canvas.drawRect(touchX * width, touchY * width, (touchX + 1) * width, (touchY + 1) * width, p);
        }

        for(int i=0; i<size; i++)
            for(int j=0; j<size; j++) {
                int temp = field[orientX -size/2+i][orientY -size/2+j];
                TraxPanelUtil.of(temp).draw(getContext(), canvas, i*width, j*width, width);
            }
    }
}
