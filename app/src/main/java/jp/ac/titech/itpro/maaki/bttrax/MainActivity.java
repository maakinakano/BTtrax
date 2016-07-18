package jp.ac.titech.itpro.maaki.bttrax;

import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private BoardView boardView;
    private Toast toast;
    private TextView messageView;
    private Resources resources;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        boardView = (BoardView)findViewById(R.id.board_view);
        messageView = (TextView) findViewById(R.id.message_view);
        resources = getResources();
        toast = Toast.makeText(this, resources.getString(R.string.illegal_move), Toast.LENGTH_SHORT);
    }

    public void onClickPanel(View v) {
        int buttonNum = 0;
        if (v.getId() == R.id.button_panel1)
            buttonNum = 0;
        else if (v.getId() == R.id.button_panel2)
            buttonNum = 1;
        else if (v.getId() == R.id.button_panel3)
            buttonNum = 2;

        if(boardView.getWinner() == Colors.NONE)
            if(!boardView.onClickPanel(buttonNum))
                toast.show();

        if(boardView.getWinner() == Colors.RED)
            messageView.setText(resources.getString(R.string.red_win));
        else if(boardView.getWinner() == Colors.WHITE)
            messageView.setText(resources.getString(R.string.white_win));
        else if(boardView.getWinner() == Colors.DRAW)
            messageView.setText(resources.getString(R.string.draw_game));
        else {
            if (boardView.getTurn() == Colors.RED)
                messageView.setText(resources.getString(R.string.red_turn));
            if (boardView.getTurn() == Colors.WHITE)
                messageView.setText(resources.getString(R.string.white_turn));
        }
    }

}
