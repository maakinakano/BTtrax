package jp.ac.titech.itpro.maaki.bttrax;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonReader;
import android.util.JsonWriter;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.UUID;
import android.os.Handler;

public class MainActivity extends AppCompatActivity {
   private boolean BTMode = false;
    private Colors playerColor = Colors.NONE;
    private static final int REQUEST_ENABLE_BT = 100;
    private static final int REQUEST_DISCOVERABLE = 101;
    private static final int REQUEST_GET_DEVICE = 102;
    private static final int SERVER_TIMEOUT_SEC = 20;
    private final static String SPP_UUID_STRING = "00001101-0000-1000-8000-00805F9B34FC";
    private static final UUID SPP_UUID = UUID.fromString(SPP_UUID_STRING);

    private BoardView boardView;
    private Toast toast;
    private TextView messageView;
    private Resources resources;
    private BluetoothAdapter btAdapter;
    private String devName;
    private ProgressBar connectionProgress;

    private ServerTask serverTask;
    private ClientTask clientTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_layout);
        resources = getResources();
        toast = Toast.makeText(this, resources.getString(R.string.illegal_move), Toast.LENGTH_SHORT);
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        connectionProgress = (ProgressBar)findViewById(R.id.connection_progress);
        if (btAdapter == null) {
            Toast.makeText(this, resources.getString(R.string.bluetooth_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    public void onClickPanel(View v) {
        int buttonNum = 0;
        if (v.getId() == R.id.button_panel1)
            buttonNum = 0;
        else if (v.getId() == R.id.button_panel2)
            buttonNum = 1;
        else if (v.getId() == R.id.button_panel3)
            buttonNum = 2;

        if(BTMode && boardView.getTurn()!=playerColor) return;
        if(boardView.getWinner() == Colors.NONE)
            if(!boardView.onClickPanel(buttonNum)) {
                toast.show();
            } else if(BTMode) {
                String content = boardView.getAddX()+","+boardView.getAddY()+","+buttonNum;
                Messenger message = new Messenger(content);
                gameThread.send(message);
            }
        textRedraw();
    }

    public void textRedraw() {
        if(BTMode) {
            BTTextRedraw();
            return;
        }
        String s="";
        if(boardView.getWinner() == Colors.RED)
            s = resources.getString(R.string.red_win);
        else if(boardView.getWinner() == Colors.WHITE)
            s = resources.getString(R.string.white_win);
        else if(boardView.getWinner() == Colors.DRAW)
            s = resources.getString(R.string.draw_game);
        else {
            if (boardView.getTurn() == Colors.RED)
                s = resources.getString(R.string.red_turn);
            else if (boardView.getTurn() == Colors.WHITE)
                s = resources.getString(R.string.white_turn);
        }
        messageView.setText(s);
    }

    private void BTTextRedraw() {
        String s = "";
        if (playerColor == Colors.RED)
            s += resources.getString(R.string.player_red);
        else if (playerColor == Colors.WHITE)
            s += resources.getString(R.string.player_white);

        if(boardView.getWinner() == playerColor)
            s += resources.getString(R.string.your_win);
        else if(boardView.getWinner() != playerColor && boardView.getWinner() != Colors.NONE)
            s += resources.getString(R.string.opponent_win);
        else if(boardView.getWinner() == Colors.DRAW)
            s += resources.getString(R.string.draw_game);
        else {
            if(boardView.getTurn() == playerColor)
                s += resources.getString(R.string.your_turn);
            else
                s += resources.getString(R.string.opponent_turn);
        }
        messageView.setText(s);
    }

    public void onClickPlayMode(View v) {
        stopServer();
        setContentView(R.layout.game_layout);
        boardView = (BoardView)findViewById(R.id.board_view);
        messageView = (TextView)findViewById(R.id.message_view);
    }

    public void onClickRule(View v) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.tantrix.jp/trax/trax_rule.htm")));
    }

    public void onClickBTMode(View v) {
        stopServer();
        if(!btAdapter.isEnabled())
            startActivityForResult(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), REQUEST_ENABLE_BT);
        else {
            setupBT();
            if(v.getId() == R.id.server_button) {
                startServer();
            }
            if(v.getId() == R.id.client_button) {
                connect();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if(resultCode == Activity.RESULT_OK)
                    setupBT();
                else
                    Toast.makeText(this, resources.getString(R.string.bluetooth_not_accept), Toast.LENGTH_SHORT).show();
                break;
            case REQUEST_DISCOVERABLE:
                startServer1();
                break;
            case REQUEST_GET_DEVICE:
                if (resultCode == Activity.RESULT_OK)
                    connect1((BluetoothDevice) data.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE));
                break;
        }
    }

    private void setupBT() {
        devName = btAdapter.getName();
    }

    private void startServer() {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        intent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, SERVER_TIMEOUT_SEC);
        startActivityForResult(intent, REQUEST_DISCOVERABLE);
    }

    private void startServer1() {
        serverTask = new ServerTask();
        serverTask.execute(SERVER_TIMEOUT_SEC);
    }

    private void stopServer() {
        if(serverTask != null)
            serverTask.stop();
    }

    private class ServerTask extends AsyncTask<Integer, Void, BluetoothSocket> {
        private BluetoothServerSocket serverSocket;
        @Override
        protected void onPreExecute() {
            connectionProgress.setIndeterminate(true);
        }

        @Override
        protected BluetoothSocket doInBackground(Integer... params) {
            BluetoothSocket socket = null;
            try {
                serverSocket = btAdapter.listenUsingRfcommWithServiceRecord(devName, SPP_UUID);
                socket = serverSocket.accept(params[0]*1000);
            } catch (IOException e) {
                socket = null;
            } finally {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return socket;
        }

        @Override
        protected void onPostExecute(BluetoothSocket socket) {
            connectionProgress.setIndeterminate(false);
            if(socket == null) {
                Toast.makeText(MainActivity.this, resources.getString(R.string.bluetooth_connection_failed), Toast.LENGTH_SHORT).show();
            } else {
                BTMode = true;
                try {
                    setContentView(R.layout.game_layout);
                    boardView = (BoardView)findViewById(R.id.board_view);
                    playerColor = Colors.RED;
                    messageView = (TextView)findViewById(R.id.message_view);
                    textRedraw();
                    gameThread = new GameThread(socket);
                    gameThread.start();
                } catch (IOException e) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            serverTask = null;
        }

        @Override
        protected void onCancelled() {
            connectionProgress.setIndeterminate(false);
            serverTask = null;
        }

        public void stop() {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            cancel(false);
        }
    }

    private void connect() {
        Intent intent = new Intent(this, BTScanActivity.class);
        startActivityForResult(intent, REQUEST_GET_DEVICE);
    }

    private void connect1(BluetoothDevice device) {
        clientTask = new ClientTask();
        clientTask.execute(device);
    }

    private class ClientTask extends AsyncTask<BluetoothDevice, Void, BluetoothSocket> {
        @Override
        protected void onPreExecute() {
            connectionProgress.setIndeterminate(true);
        }

        @Override
        protected BluetoothSocket doInBackground(BluetoothDevice... params) {
            BluetoothSocket socket = null;
            try {
                socket = params[0].createRfcommSocketToServiceRecord(SPP_UUID);
                socket.connect();
            } catch (IOException e) {
                if(socket != null) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    socket = null;
                }
            }
            return  socket;
        }

        @Override
        protected void onPostExecute(BluetoothSocket socket) {
            connectionProgress.setIndeterminate(false);
            if(socket == null) {
                Toast.makeText(MainActivity.this, resources.getString(R.string.bluetooth_connection_failed), Toast.LENGTH_SHORT).show();
            } else {
                BTMode = true;
                try {
                    setContentView(R.layout.game_layout);
                    boardView = (BoardView)findViewById(R.id.board_view);
                    playerColor = Colors.WHITE;
                    messageView = (TextView)findViewById(R.id.message_view);
                    textRedraw();
                    gameThread = new GameThread(socket);
                    gameThread.start();
                } catch (IOException e) {
                    try {
                        socket.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                }
            }
            clientTask = null;

        }
    }

    private final static int MESG_STARTED = 1000;
    private final static int MESG_RECEIVED = 1001;
    private final static int MESG_FINISHED = 1002;

    private GameThread gameThread;
    private Handler gameHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESG_RECEIVED:
                    Messenger messenger = (Messenger)msg.obj;
                    String[] temp = messenger.toString().split(",");
                    boardView.onClickPanel(Integer.parseInt(temp[2]), Integer.parseInt(temp[0]), Integer.parseInt(temp[1]));
                    textRedraw();
                    break;
            }
            return false;
        }
    });

    private class GameThread extends Thread {
        private final BluetoothSocket socket;
        private final Messenger.Reader reader;
        private final Messenger.Writer writer;

        public GameThread(BluetoothSocket socket) throws  IOException {
            if(!socket.isConnected())
                throw new IOException("Socket is not connected");
            this.socket = socket;
            reader = new Messenger.Reader(new JsonReader(new InputStreamReader(socket.getInputStream(), "UTF-8")));
            writer = new Messenger.Writer(new JsonWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8")));
        }

        public void run() {
            gameHandler.sendMessage(gameHandler.obtainMessage(MESG_STARTED, socket.getRemoteDevice()));
            try {
                writer.beginArray();
                reader.beginArray();
                while (reader.hasNext())
                    gameHandler.sendMessage(gameHandler.obtainMessage(MESG_RECEIVED, reader.read()));
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (socket.isConnected()) {
                    close();
                }
            }
            gameHandler.sendMessage(gameHandler.obtainMessage(MESG_FINISHED));
        }

        public void send(Messenger message) {
            try {
                writer.write(message);
                writer.flush();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }

        public void close() {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

