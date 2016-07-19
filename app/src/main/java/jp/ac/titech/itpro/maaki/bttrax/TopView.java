package jp.ac.titech.itpro.maaki.bttrax;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import java.util.Random;

public class TopView extends RelativeLayout {
    private static final int SIZE = 12;
    private Random random = new Random();
    private int field[][];

    public TopView(Context context) {
        this(context, null);
    }

    public TopView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TopView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TopView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setWillNotDraw(false);
        field = new int[SIZE][SIZE];
        randomFieldMake();
    }

    private void randomFieldMake() {
        int temp;
        for(int i=0; i<SIZE; i++)
            for(int j=0; j<SIZE; j++) {
                if(i == 0 && j == 0)
                    field[i][j] = random.nextInt(6)+1;
                else if(i == 0) {
                    temp = random.nextInt(3);
                    if(TraxPanelUtil.of(field[i][j-1]).getDirection(Direction.DOWN) == TraxPanelUtil.of(temp*2+1).getDirection(Direction.UP))
                        field[i][j] = temp*2+1;
                    else
                        field[i][j] = temp*2+2;
                }
                else if(j == 0) {
                    temp = random.nextInt(3);
                    if(TraxPanelUtil.of(field[i-1][j]).getDirection(Direction.RIGHT) == TraxPanelUtil.of(temp*2+1).getDirection(Direction.LEFT))
                        field[i][j] = temp*2+1;
                    else
                        field[i][j] = temp*2+2;
                }
                else {
                    for(;;) {
                        temp = random.nextInt(6)+1;
                        if(TraxPanelUtil.of(field[i][j-1]).getDirection(Direction.DOWN) != TraxPanelUtil.of(temp).getDirection(Direction.UP)) continue;
                        if(TraxPanelUtil.of(field[i-1][j]).getDirection(Direction.RIGHT) != TraxPanelUtil.of(temp).getDirection(Direction.LEFT)) continue;
                        break;
                    }
                    field[i][j] = temp;
                }
            }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int width = (canvas.getWidth() < canvas.getHeight()) ? canvas.getWidth()/ SIZE : canvas.getHeight()/ SIZE;

        for(int i = 0; i< SIZE; i++)
            for(int j = 0; j< SIZE; j++) {
                TraxPanelUtil.of(field[i][j]).draw(getContext(), canvas, i*width, j*width, width);
            }
    }
}
