package nvnteam.guessthesketch.bluetooth;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;

import nvnteam.guessthesketch.activity.FullScreenActivity;
import nvnteam.guessthesketch.activity.PrepareToGuessDialog;
import nvnteam.guessthesketch.activity.SDGameActivity;
import nvnteam.guessthesketch.activity.SDPreGameActivity;
import nvnteam.guessthesketch.activity.WordPickerDialog;
import nvnteam.guessthesketch.bluetooth.BluetoothProtocol;
import nvnteam.guessthesketch.dto.DrawingNode;
import nvnteam.guessthesketch.util.ActivityUtils;
import nvnteam.guessthesketch.util.GTSUtils;
import nvnteam.guessthesketch.util.HighScoreManager;
import nvnteam.guessthesketch.util.WordBase;
import nvnteam.guessthesketch.widget.DrawingView;
import nvnteam.guessthesketch.widget.LetterSpacingTextView;

import nvnteam.guessthesketch.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView.OnEditorActionListener;

/**
 * This is the main Activity that displays the current session.
 */
public class BTGameActivity extends FullScreenActivity
{
    // Debugging
    private static final String TAG = "BluetoothGame";
    private static final boolean D = true;

    // Key names received from the BluetoothService Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;
    
    // ViewFlipper views
    private ViewFlipper m_viewFlipper;
    private View m_connLayout;
    private View m_preLayout;
    private View m_gameLayout;
    private View m_infoLayout;

    // Connection views
    private TextView m_deviceTextView;
    private TextView m_statusTextView;
    private Button m_deviceConnectButton;
    private Button m_discoverableButton;
    private ListView m_conversationView;
    private EditText m_outEditText;
    private Button m_sendButton;
    private Button m_createGameButton;

    // Pre-game views
    private TextView m_titleText;
    private TextView m_teamNamesText;
    private TextView m_gameModesText;

    private EditText m_teamOneEditText;
    private EditText m_teamTwoEditText;

    private RadioButton m_fiveRoundsRadioButton;
    private RadioButton m_timedRadioButton;
    private RadioButton m_maxPointsRadioButton;

    private Button m_backBtn;
    private Button m_startBtn;

    // In-game views
    private DrawingView m_drawView;
    private ImageButton m_currentPaint;
    private ImageButton m_currentBrush;
    private LetterSpacingTextView m_mainWordTextView;
    private Button m_finishBtn;
    private Button m_undoBtn;
    private TextView m_countDownTextView;
    private TextView m_currentRoundTextView;
    private TextView m_globalCountDownTextView;
    private ViewFlipper m_paletteFlipper;
    private LinearLayout m_colorStrip;
    private LinearLayout m_brushStrip;
    private EditText m_guesserEditText;
    private TextView m_infoTextView;

    // Name of the connected device
    private String m_connectedDeviceName = null;
    // Array adapter for the conversation thread
    private ArrayAdapter<String> m_conversationArrayAdapter;
    // String buffer for outgoing messages
    private StringBuffer m_outStringBuffer;
    // Local Bluetooth adapter
    private BluetoothAdapter m_bluetoothAdapter = null;
    // Member object for the bluetooth services
    private BluetoothService m_bluetoothService = null;

    // Used in status bar text
    private String m_thisDeviceName;

    // Device that initiates the connection exposes server-like behavior
    private boolean m_isGameHost = false;

    // Game logic elements
    private String m_currentWord = new String("");
    private float m_wordPoints = 0;
    private int m_baseFactor = 5;
    private int m_difficultyFactor = 5;
    private float m_wordModifier = 1.0f;
    private boolean m_revealedLetter = false;

    private String[] m_teamNames = new String[2];
    private int m_currentTurn = 0;
    private static int MaxTeams = 2;
    private float[] m_currentScores = {0, 0};
    // private HighScoreManager m_highScoreManager = null;

    private int m_roundsPassed = 0;
    private int m_gameMode = 0;
    private enum State { Picking, Drawing, Guessing, Over };
    private State m_gameState = State.Picking;

    private long m_currentTimeLeft = 60000;

    private CountDownTimer m_timer = new CountDownTimer(60000, 300)
    {
        public void onTick(long millisUntilFinished)
        {
            m_countDownTextView.setText(Long.toString(millisUntilFinished / 1000));
            m_currentTimeLeft = millisUntilFinished;
            int redComp = (millisUntilFinished > 30000 ? (255 * (30000 - (int)millisUntilFinished) / 30000) : 255) << 16;
            int greenComp = (millisUntilFinished < 30000 ? (255 * ((int)millisUntilFinished) / 30000) : 255) << 8;

            m_countDownTextView.setTextColor(0xFF000000 | redComp | greenComp);

            //if (m_gameState == State.Guessing && !m_revealedLetter && millisUntilFinished < 30000)
                //revealRandomLetter();
        }

        public void onFinish()
        {/*
            m_currentTimeLeft = 0;
            if (m_gameState == State.Drawing)
                prepareGuessingPhase();
            else if (m_gameState == State.Guessing)
                exitGuessingPhase();*/
        }
    };

    private CountDownTimer m_globalTimer = null;

    private SenderThread m_senderThread;
    private DecoderThread m_decoderThread;

    public void transferNodes(Deque<DrawingNode> deque)
    {
        Log.e(TAG, "NEW DEQUE SIZE: " + deque.size());

        m_senderThread = new SenderThread(deque);
        m_senderThread.run();
    }

    public void getDecodedNodes(Deque<DrawingNode> deque)
    {
        Log.e(TAG, "DECODED QUEUE SIZE: " + deque.size());
        m_drawView.drawFromDeque(deque);
    }

    private class SenderThread extends Thread
    {
        /* test purposes only */
        private final Deque<DrawingNode> q;
/*
        public SenderThread(int num)
        {
            q = new LinkedList<DrawingNode>();
            for (int i = 0; i < num; i++)
            {
                DrawingNode newnode = new DrawingNode();
                newnode.setAttrib(15.3f + i, 14.23f + i, 3 + i, 123123 + i, 909090 + i);
                q.add(newnode);
            }
            //sendNodes(BluetoothProtocol.DATA_DRAWING_NODE, q);
        }*/
        public SenderThread(Deque<DrawingNode> deque)
        {
           q = deque;
        }

        public void run()
        {
            sendNodes(BluetoothProtocol.DATA_DRAWING_NODE, q);
        }
    }

    private class DecoderThread extends Thread
    {
        /* test purposes only */
        private final Deque<DrawingNode> mm_queue;
        private final byte[] mm_array;

        public DecoderThread(byte[] array)
        {
            mm_queue = new LinkedList<DrawingNode>();
            mm_array = new byte[array.length];
            for (int i = 0; i < array.length; i++)
                mm_array[i] = array[i];
        }

        public void run()
        {
            int i = 0;
            
            while (i < mm_array.length)
            {
                byte[] tempNode = new byte[28];
                int k;
                for (k = 0; k < 28 && i < mm_array.length; k++)
                {
                    tempNode[k] = mm_array[i];
                    i++;
                }

                if (k == 28)
                {
                    DrawingNode newNode = DrawingNode.deserialize(tempNode);
                    Log.i(TAG, "RECEIVED X: " + newNode.getX() +
                            "\nY: " + newNode.getY() +
                            "\nType: " + newNode.getActionType() + 
                            "\nTime: " + newNode.getTimeStamp() +
                            "\nColor: " + newNode.getColor());
                    mm_queue.add(newNode);
                }
            }
            Log.i(TAG, "Deserialized " + mm_queue.size() + " nodes!");
            Deque<DrawingNode> newDeque = new LinkedList<DrawingNode>(mm_queue);
            mm_queue.clear();
            getDecodedNodes(newDeque);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");
        setContentView(R.layout.view_flipper);

        // Get local Bluetooth adapter
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (m_bluetoothAdapter == null)
        {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }

        initUI();
        initListeners();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupBluetooth() will then be called during onActivityResult
        if (!m_bluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else if (m_bluetoothService == null) setupBluetooth();

        m_teamNames[0] = "TeamOne";
        m_teamNames[1] = "TeamTwo";
    }

    @Override
    public synchronized void onResume()
    {
        super.onResume();
        if(D) Log.e(TAG, "+ ON RESUME +");

        // Performing this check in onResume() covers the case in which BT was
        // not enabled during onStart(), so we were paused to enable it...
        // onResume() will be called when ACTION_REQUEST_ENABLE activity returns.
        if (m_bluetoothService != null)
        {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (m_bluetoothService.getState() == BluetoothService.STATE_NONE)
            {
                // Start the Bluetooth services
                m_bluetoothService.start();
            }
        }
    }

    @Override
    public synchronized void onPause()
    {
        super.onPause();
        if(D) Log.e(TAG, "- ON PAUSE -");
    }

    @Override
    public void onStop()
    {
        super.onStop();
        m_timer.cancel();
        if(D) Log.e(TAG, "-- ON STOP --");
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        // Stop the Bluetooth chat services
        if (m_bluetoothService != null) m_bluetoothService.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(D) Log.d(TAG, "onActivityResult " + resultCode);
        switch (requestCode)
        {
        case REQUEST_CONNECT_DEVICE:
            // When DeviceListActivity returns with a device to connect
            if (resultCode == Activity.RESULT_OK) 
            {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListDialogActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BluetoothDevice object
                BluetoothDevice device = m_bluetoothAdapter.getRemoteDevice(address);
                // Attempt to connect to the device
                m_isGameHost = true;
                m_bluetoothService.connect(device);
                m_deviceTextView.append(" as a server");
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK)
            {
                // Bluetooth is now enabled, so set up a chat session
                setupBluetooth();
            }
            else
            {
                // User did not enable Bluetooth or an error occured
                Log.d(TAG, "BT not enabled");
                Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private void setupBluetooth()
    {
        Log.d(TAG, "setupChat()");
        m_thisDeviceName = m_bluetoothAdapter.getName();
        m_deviceTextView.setText(m_thisDeviceName);
        // Initialize the array adapter for the conversation thread

        // Initialize the BluetoothChatService to perform bluetooth connections
        m_bluetoothService = new BluetoothService(this, mHandler);

        // Initialize the buffer for outgoing messages
        m_outStringBuffer = new StringBuffer("");
    }

    private void ensureDiscoverable()
    {
        if(D) Log.d(TAG, "ensure discoverable");
        if (m_bluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE)
        {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void preparePreGame()
    {
        if (m_isGameHost)
        {
            m_teamTwoEditText.setVisibility(View.INVISIBLE);
        }
        else
        {
            m_teamOneEditText.setVisibility(View.INVISIBLE);
            m_fiveRoundsRadioButton.setEnabled(false);
            m_timedRadioButton.setEnabled(false);
            m_maxPointsRadioButton.setEnabled(false);
        }

        m_viewFlipper.setDisplayedChild(m_viewFlipper.indexOfChild(m_preLayout));
    }

    public void prepareInGame()
    {
        if (m_isGameHost)
        {
            m_viewFlipper.setDisplayedChild(m_viewFlipper.indexOfChild(m_gameLayout));
            prepareDrawingPhase();
        }
        else
        {
            m_infoTextView.setText("Please wait while the other player picks a word"
                    + '\n' + "Team one: " + m_teamNames[0]
                    + '\n' + "Team two: " + m_teamNames[1]);
            m_viewFlipper.setDisplayedChild(m_viewFlipper.indexOfChild(m_infoLayout));
        }
    }

    /**
     * Sends a message.
     * @param message  A string of text to send.
     */
    private void sendMessage(String message)
    {
        // Check that we're actually connected before trying anything
        if (m_bluetoothService.getState() != BluetoothService.STATE_CONNECTED)
        {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        // Check that there's actually something to send
        if (message.length() > 0)
        {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            m_bluetoothService.write(send);

            // Reset out string buffer to zero and clear the edit text field
            m_outStringBuffer.setLength(0);
            m_outEditText.setText(m_outStringBuffer);
        }
    }
    
    private void sendCommandWithString(int value, String string)
    {
        if (m_bluetoothService.getState() != BluetoothService.STATE_CONNECTED)
        {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        byte[] stringBytes = string.getBytes();
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(value);
        byte[] intBytes = b.array();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try
        {
            outputStream.write(intBytes);
            outputStream.write(stringBytes);
        } 
        catch (IOException e)
        {
            e.printStackTrace();
        }

        byte[] send = outputStream.toByteArray();
        m_bluetoothService.write(send);
    }

    private void sendNodes(int code, Queue<DrawingNode> nodes)
    {
        if (m_bluetoothService.getState() != BluetoothService.STATE_CONNECTED)
        {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try 
        {
            ByteBuffer b = ByteBuffer.allocate(4);
            b.putInt(code);
            outputStream.write(b.array());
            while (nodes.peek() != null)
                outputStream.write(DrawingNode.serialize(nodes.poll()));
            byte[] send = outputStream.toByteArray();
            m_bluetoothService.write(send);
        } 
        catch (IOException e1) 
        {
            e1.printStackTrace();
        }
    }

    private void sendCommand(int value)
    {
        if (m_bluetoothService.getState() != BluetoothService.STATE_CONNECTED)
        {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }
        ByteBuffer b = ByteBuffer.allocate(4);
        b.putInt(value);

        byte[] send = b.array();
        m_bluetoothService.write(send);
    }

    // The Handler that gets information back from the BluetoothService
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
            case BluetoothProtocol.MESSAGE_STATE_CHANGE:
                if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                switch (msg.arg1)
                {
                case BluetoothService.STATE_CONNECTED:
                    m_statusTextView.setText(R.string.title_connected_to);
                    m_statusTextView.append(m_connectedDeviceName);
                    if (!m_isGameHost)
                        m_deviceTextView.append(" as a client");
                    m_conversationArrayAdapter.clear();
                    break;

                case BluetoothService.STATE_CONNECTING:
                    m_statusTextView.setText(R.string.title_connecting);
                    break;

                case BluetoothService.STATE_LISTEN:
                case BluetoothService.STATE_NONE:
                    m_statusTextView.setText(R.string.title_not_connected);
                    break;
                }
                break;

            case BluetoothProtocol.MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                m_conversationArrayAdapter.add("Me:  " + writeMessage);
                break;

            case BluetoothProtocol.MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                Log.e(TAG,  "$$$ MESSAGE LENGTH $$$ " + msg.arg1);
                //if (!handleCommandMessage(readBuf, msg.arg1))
                //{
                if (msg.arg2 == BluetoothProtocol.DATA_DRAWING_NODE)
                {
                    Log.i(TAG, "HAS ENTERED DRAWING NODE RECEIVE");
                    m_decoderThread = new DecoderThread(readBuf);
                    m_decoderThread.start();
                }
                else
                {
                    if (!handleCommandMessage(readBuf, msg.arg1))
                    {
                        String readMessage = new String(readBuf, 0, msg.arg1);
                        m_conversationArrayAdapter.add(m_connectedDeviceName+":  " + readMessage);
                    }
                }
                //}
                break;

            case BluetoothProtocol.MESSAGE_DEVICE_NAME:
                // save the connected device's name
                m_connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + m_connectedDeviceName, Toast.LENGTH_SHORT).show();
                break;

            case BluetoothProtocol.MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }

        public boolean handleCommandMessage(byte[] messageBytes, int arg1)
        {
            int code = messageBytes[0] << 24 | messageBytes[1] << 16
                     | messageBytes[2] << 8  | messageBytes[3];
            Log.e(TAG,  "RECEIVED MESSAGE CODE: ++++ " + code);
            switch (code)
            {
            case BluetoothProtocol.COMMAND_CREATE_GAME:
            {
                preparePreGame();
                return true;
            }
            case BluetoothProtocol.COMMAND_START_GAME:
            {
                prepareInGame();
                return true;
            }
            case BluetoothProtocol.COMMAND_EXCHANGE_TEAM_NAMES:
            {
                if (m_isGameHost)
                {
                    String teamTwoName = new String(messageBytes, 4, arg1 - 4);
                    m_teamNames[1] = teamTwoName;
                }
                else
                {
                    m_teamNames[1] = m_teamTwoEditText.getText().toString();
                    String teamOneName = new String(messageBytes, 4, arg1 - 4);
                    m_teamNames[0] = teamOneName;
                }
                return true;
            }
            case BluetoothProtocol.COMMAND_START_DRAWING:
            {
                prepareDrawingPhase();
                return true;
            }
            case BluetoothProtocol.COMMAND_START_GUESSING:
            {
                m_currentWord = new String(messageBytes, 4, arg1 - 4);
                m_viewFlipper.setDisplayedChild(m_viewFlipper.indexOfChild(m_gameLayout));
                prepareGuessingPhase();
                return true;
            }
            case BluetoothProtocol.COMMAND_SHOW_SCORES:
            {
                // showScores();
                return true;
            }
            case BluetoothProtocol.DATA_DRAWING_NODE:
            {/*
                Log.e(TAG, "DATA_DRAWING_NODE_MESSAGE_LENGTH: " + arg1);
                byte[] nodes = new byte[arg1 - 4];
                for (int i = 0; i < nodes.length; i++)
                    nodes[i] = messageBytes[i + 4];
                if (arg1 > 4)
                {
                    
                    m_decoderThread = new DecoderThread(nodes);
                    m_decoderThread.start();*/
                    /*
                    byte[] firstNode = new byte[24];
                    for (int i = 0; i < 24 || i < nodes.length; i++)
                        firstNode[i] = nodes[i];
                    try
                    {
                    DrawingNode newNode = new DrawingNode(DrawingNode.deserialize(firstNode));
                    Log.e(TAG, "RECEIVED X: " + newNode.getX() +
                            "\nY: " + newNode.getY() +
                            "\nType: " + newNode.getActionType() + 
                            "\nTime: " + newNode.getTimeStamp() +
                            "\nColor: " + newNode.getColor());
                    }
                    catch (Exception ex)
                    {
                        Log.e(TAG, "ERROR ON DESERIALIZATION");
                    }
                }*/
                return true;
            }
            }
            if (D) Log.d(TAG, "CODE GIVEN: " + code);
            return false;
        }
    };

    /**
     * This function gets executed before the drawing phase begins, by clearing
     * all the relevant text views, wiping the canvas and resetting the timer.
     * It pops a modal dialog where the user can select a word.
     */
    public void prepareDrawingPhase()
    {
        // Reset UI Text Views, color and brush size
        m_guesserEditText.setText("");
        m_countDownTextView.setText("");
        m_currentRoundTextView.setText("Current Round: " + ((m_roundsPassed + 2) >> 1));
        m_mainWordTextView.setText("");
        paintClicked((ImageButton) m_colorStrip.getChildAt(0));
        brushClicked((ImageButton) m_brushStrip.getChildAt(0));

        // Reset the timer
        m_timer.cancel();

        // Stop the playback and wipe the canvas
        m_drawView.startNew();

        // Change the backgrounds of buttons to corresponding team color
        if (m_currentTurn == 0)
        {
            m_undoBtn.setBackgroundResource(R.drawable.button_in_game_blue);
            m_currentRoundTextView.setTextColor(0xFF8080FF);
        }
        else
        {
            m_undoBtn.setBackgroundResource(R.drawable.button_in_game_red);
            m_currentRoundTextView.setTextColor(0xFFFF8080);
        }

        // Get the random words from the WordBase
        final String words[] = { WordBase.getEasyWord(),
                                 WordBase.getMediumWord(),
                                 WordBase.getHardWord(),
                                 WordBase.getReallyHardWord() };

        // Start the WordPickerDialog
        final WordPickerDialog wpd = new WordPickerDialog(BTGameActivity.this);
        wpd.show();
        wpd.setWords(words);
        wpd.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (wpd.getSelectedWord() != "")
                {
                    // Grab selected word and form the underscore string
                    m_currentWord = wpd.getSelectedWord();
                    StringBuffer outputBuffer = new StringBuffer(m_currentWord.length());
                    for (int i = 0; i < m_currentWord.length(); i++)
                       if (m_currentWord.charAt(i) != ' ')
                           outputBuffer.append('_');
                       else
                           outputBuffer.append(' ');
                    m_mainWordTextView.setText(outputBuffer.toString());

                    // Send the word across Bluetooth here
                    sendCommandWithString(BluetoothProtocol.COMMAND_START_GUESSING,
                            m_currentWord);

                    // Set base word points for selected word
                    m_wordModifier = wpd.getSelectedModifier();
                    m_wordPoints = m_baseFactor + m_difficultyFactor * wpd.getSelectedIndex();
                    m_drawView.startNew();
                    m_gameState = State.Drawing;
                    m_timer.start();
                    Toast.makeText(BTGameActivity.this, wpd.getSelectedWord(), Toast.LENGTH_LONG).show();
                    wpd.dismiss();
                }
                else
                    Toast.makeText(BTGameActivity.this, "Please select a word!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This function gets executed before the guessing phase begins, by clearing
     * all the relevant text views, wiping the canvas and resetting the timer.
     * It pops a modal dialog where the user can confirm he's ready to guess.
     */
    public void prepareGuessingPhase()
    {
        StringBuffer outputBuffer = new StringBuffer(m_currentWord.length());
        for (int i = 0; i < m_currentWord.length(); i++)
           if (m_currentWord.charAt(i) != ' ')
               outputBuffer.append('_');
           else
               outputBuffer.append(' ');
        m_mainWordTextView.setText(outputBuffer.toString());
        m_drawView.startNew();
        m_paletteFlipper.showNext();
        m_revealedLetter = false;
        m_gameState = State.Guessing;
        m_timer.cancel();
        m_timer.start();
    }

    /**
     * This function gets executed after the guessing phase begins, and evaluates
     * the current round.
     *//*
    public void exitGuessingPhase()
    {
        // Cancel the timer and stop playback
        m_timer.cancel();
        m_drawView.stopPlayback();

        // Reveal the word to the guesser
        m_mainWordTextView.setText(m_currentWord);
        Toast.makeText(BTGameActivity.this, "The word was " + m_currentWord, Toast.LENGTH_LONG).show();

        // Clean up the guessing TextView
        m_guesserEditText.setText("");

        // Hide soft keyboard
        InputMethodManager inputManager = (InputMethodManager) 
                this.getSystemService(SDPreGameActivity.INPUT_METHOD_SERVICE);
        View v = this.getCurrentFocus();
        if(v != null)
            inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

        // If time hasn't run out, add points to the current team, else set to zero
        if (m_currentTimeLeft != 0)
            m_wordPoints += m_currentTimeLeft / 10000;
        else
            m_wordPoints = 0;

        // Assert points and finish current round
        endRound();
    }*/
/*
    public void exitGuessingPhase()
    {
        m_timer.cancel();
        //m_drawView.setPlayback(false);
        m_mainWordTextView.setText(m_currentWord);
        m_guesserEditText.setText("");
        InputMethodManager inputManager = (InputMethodManager)
                           getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, 0);
        
        // Handle evaluation
        if (m_currentTimeLeft > 0)
            m_currentPoints[m_currentTurn] += m_wordPoints 
                                           + (float)m_currentTimeLeft / 6000;
        m_currentPoints[m_currentTurn] = GTSUtils.round(m_currentPoints[m_currentTurn], 2);

        if (m_currentPoints[m_currentTurn] > 100.f)
            m_gameState = State.Over;

        m_currentTurn = (m_currentTurn + 1) % MaxTeams;
        
        if (m_gameState == State.Over)
        {
            gameOver();
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(m_teamNames[0]+ ": " + m_currentPoints[0] + 
                           "     " + m_teamNames[1] + ": " + m_currentPoints[1]
                                   + "\nPrepare to Draw!")
               .setCancelable(false)
               .setPositiveButton(m_teamNames[m_currentTurn] + "'s turn",
                                  new DialogInterface.OnClickListener()
               {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                       m_drawView.startNew();
                       m_guesserEditText.animate().alpha(0).withLayer();
                       m_guesserEditText.setVisibility(View.GONE);
                       m_colorStrip.animate().alpha(1).withLayer();
                       m_colorStrip.setVisibility(View.VISIBLE);
                       m_gameState = State.Picking;
                       m_timer.start();
                       //startPickingPhase();
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
    }
    *//*
    public void gameOver()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(m_teamNames[m_currentTurn] + " wins!")
               .setCancelable(false)
               .setPositiveButton("Exit", new DialogInterface.OnClickListener() 
               {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                       BTGameActivity.this.finish();
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
    }*/

    @Override
    public void onBackPressed() 
    {
        new AlertDialog.Builder(this)
               .setMessage("Are you sure you want to exit?")
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
               {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                        BTGameActivity.super.onBackPressed();
                   }
               })
               .setNegativeButton("No", null)
               .show();
    }

    public void paintClicked(View view)
    {
        if (view != m_currentPaint)
        {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            m_drawView.setColor(color);

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.color_button_pressed));
            m_currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.color_button_normal));
            m_currentPaint = (ImageButton)view;
        }
    }

    public void initUI()
    {
        // Activity flipper setup
        m_viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        Animation inAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation outAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        m_viewFlipper.setInAnimation(inAnim);
        m_viewFlipper.setOutAnimation(outAnim);

        // Chat and connection views setup
        m_deviceTextView = (TextView) findViewById(R.id.device_text_view);
        m_statusTextView = (TextView) findViewById(R.id.status_text_view);
        m_deviceConnectButton = (Button) findViewById(R.id.connect_button);
        m_discoverableButton = (Button) findViewById(R.id.discoverable_button);
        m_createGameButton = (Button) findViewById(R.id.button_create_game);
        m_conversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        m_conversationView = (ListView) findViewById(R.id.in);
        m_conversationView.setAdapter(m_conversationArrayAdapter);
        m_outEditText = (EditText) findViewById(R.id.edit_text_out);
        m_sendButton = (Button) findViewById(R.id.button_send);
        TextView chatTitleView = (TextView) findViewById(R.id.chat_title_view);

        // Pre-game views setup
        m_titleText = (TextView) findViewById(R.id.text_view_pre_game_title);
        m_teamNamesText = (TextView) findViewById(R.id.text_view_pre_game_teams);
        m_gameModesText = (TextView) findViewById(R.id.text_view_pre_game_game_modes);
        m_teamOneEditText = (EditText) findViewById(R.id.edit_text_team_one);
        m_teamTwoEditText = (EditText) findViewById(R.id.edit_text_team_two);
        m_startBtn = (Button) findViewById(R.id.button_start_sd_pre_game);
        m_backBtn = (Button) findViewById(R.id.button_back_sd_pre_game);
        m_fiveRoundsRadioButton = (RadioButton) findViewById(R.id.radio_button_five_rounds);
        m_timedRadioButton = (RadioButton) findViewById(R.id.radio_button_timed);
        m_maxPointsRadioButton = (RadioButton) findViewById(R.id.radio_button_max_points);

        // In-game views setup
        m_mainWordTextView = (LetterSpacingTextView) findViewById(R.id.text_view_word_to_guess);
        m_drawView = (DrawingView) findViewById(R.id.drawing);
        m_colorStrip = (LinearLayout) findViewById(R.id.paint_colors);
        m_brushStrip = (LinearLayout) findViewById(R.id.brush_size_layout);
        m_currentRoundTextView = (TextView) findViewById(R.id.text_view_current_round);
        m_currentPaint = (ImageButton) (m_colorStrip).getChildAt(0);
        m_currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.color_button_pressed));
        m_currentBrush = (ImageButton) (m_brushStrip).getChildAt(0);
        m_currentBrush.setImageDrawable(getResources().getDrawable(R.drawable.medium_brush_pressed));
        m_finishBtn = (Button) findViewById(R.id.button_finish_drawing);
        m_finishBtn.setVisibility(View.GONE);
        m_undoBtn = (Button) findViewById(R.id.button_undo);
        m_globalCountDownTextView = (TextView) findViewById(R.id.text_view_global_count_down);
        m_countDownTextView = (TextView) findViewById(R.id.text_view_count_down);
        m_guesserEditText = (EditText) findViewById(R.id.edit_text_guesser);
        m_paletteFlipper = (ViewFlipper) findViewById(R.id.palette_flipper);
        m_paletteFlipper.setInAnimation(inAnim);
        m_paletteFlipper.setOutAnimation(outAnim);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Villa.ttf");

        m_deviceConnectButton.setTypeface(tf);
        m_discoverableButton.setTypeface(tf);
        m_createGameButton.setTypeface(tf);
        m_sendButton.setTypeface(tf);
        chatTitleView.setTypeface(tf);

        m_titleText.setTypeface(tf);
        m_backBtn.setTypeface(tf);
        m_startBtn.setTypeface(tf);
        m_teamOneEditText.setTypeface(tf);
        m_teamTwoEditText.setTypeface(tf);

        m_undoBtn.setTypeface(tf);
        m_mainWordTextView.setTypeface(tf);
        m_mainWordTextView.setLetterSpacing(1.3f);
        m_countDownTextView.setTypeface(tf);
        m_currentRoundTextView.setTypeface(tf);
        m_globalCountDownTextView.setTypeface(tf);

        tf = Typeface.createFromAsset(getAssets(), "fonts/Segoe.ttf");
        m_teamNamesText.setTypeface(tf);
        m_gameModesText.setTypeface(tf);
        m_fiveRoundsRadioButton.setTypeface(tf);
        m_timedRadioButton.setTypeface(tf);
        m_maxPointsRadioButton.setTypeface(tf);

        m_guesserEditText.setTypeface(tf);
        m_guesserEditText.setInputType(m_guesserEditText.getInputType()
                                     | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                                     | EditorInfo.TYPE_TEXT_VARIATION_FILTER);

        m_connLayout = findViewById(R.id.layout_connection);
        m_preLayout = findViewById(R.id.layout_pre_game);
        m_gameLayout = findViewById(R.id.layout_game);
        m_infoLayout = findViewById(R.id.layout_info);

        m_infoTextView = (TextView) findViewById(R.id.information_text_view);
        m_infoTextView.setText("Congratulations! You hacked your way to this page");
        m_infoTextView.setTypeface(tf);

        m_drawView.attachObserver(this);
    }

    public void initListeners()
    {
        m_deviceConnectButton.setOnClickListener(new OnClickListener()
        {
           public void onClick(View v)
           {
               Intent intent = new Intent(BTGameActivity.this, DeviceListDialogActivity.class);
               startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
           }
        });

        m_discoverableButton.setOnClickListener(new OnClickListener()
        {
           public void onClick(View v)
           {
               ensureDiscoverable();
           }
        });

        m_createGameButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (m_bluetoothService.getState() != BluetoothService.STATE_CONNECTED)
                {
                    Toast.makeText(BTGameActivity.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (m_isGameHost)
                {
                    sendCommand(BluetoothProtocol.COMMAND_CREATE_GAME);
                    preparePreGame();
                }
                else
                {
                    Toast.makeText(BTGameActivity.this, 
                            "Please wait for the server to create the game", Toast.LENGTH_LONG).show();
                }
            }
        });

        m_outEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() 
        {
            public boolean onEditorAction(TextView view, int actionId, KeyEvent event) 
            {
                // If the action is a key-up event on the return key, send the message
                if (actionId == EditorInfo.IME_NULL && event.getAction() == KeyEvent.ACTION_UP)
                {
                    String message = view.getText().toString();
                    sendMessage(message);
                }
                if(D) Log.i(TAG, "END onEditorAction");
                return true;
            }
        });

        m_sendButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
                ActivityUtils.hideSystemUI(BTGameActivity.this);
                /*if (m_isServer)
                {
                    m_senderThread = new SenderThread((new Random().nextInt()) % 15);
                    m_senderThread.start();
                }*/
            }
        });

        m_teamOneEditText.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    sendCommandWithString(BluetoothProtocol.COMMAND_EXCHANGE_TEAM_NAMES,
                            m_teamOneEditText.getText().toString());
                    m_teamNames[0] = m_teamOneEditText.getText().toString();
                }
                return true;
            }
        });

        m_teamTwoEditText.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE)
                {
                    sendCommandWithString(BluetoothProtocol.COMMAND_EXCHANGE_TEAM_NAMES,
                            m_teamTwoEditText.getText().toString());
                    m_teamNames[1] = m_teamTwoEditText.getText().toString();
                }
                return true;
            }
        });

        m_backBtn.setOnClickListener(new OnClickListener()
        {
           @Override
           public void onClick(View v)
           {
               new AlertDialog.Builder(BTGameActivity.this)
               .setMessage("Are you sure you want to exit?")
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() 
               {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                        BTGameActivity.this.finish();
                   }
               })
               .setNegativeButton("No", null)
               .show();
           }
        });

        m_startBtn.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (m_bluetoothService.getState() != BluetoothService.STATE_CONNECTED)
                {
                    Toast.makeText(BTGameActivity.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (m_isGameHost)
                {
                    sendCommand(BluetoothProtocol.COMMAND_START_GAME);
                    prepareInGame();
                    m_viewFlipper.setDisplayedChild(m_viewFlipper.indexOfChild(m_gameLayout));
                }
                else
                {
                    Toast.makeText(BTGameActivity.this, 
                            "Please wait for the server to start the game", Toast.LENGTH_LONG).show();
                }
            }
        });

        m_undoBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_drawView.undo();
                sendCommand(BluetoothProtocol.COMMAND_UNDO);
            }
        });
    }

    public void onRadioButtonClicked(View v)
    {
        if (m_isGameHost)
        {
            boolean checked = ((RadioButton) v).isChecked();

            switch(v.getId()) 
            {
                case R.id.radio_button_five_rounds:
                    if (checked)
                        m_gameMode = 0;
                    break;
                case R.id.radio_button_max_points:
                    if (checked)
                        m_gameMode = 1;
                    break;
                case R.id.radio_button_timed:
                    if (checked)
                        m_gameMode = 2;
                    break;
            }
        }
    }

    public void brushClicked(View view)
    {
        if (view != m_currentBrush)
        {
            ImageButton imgView = (ImageButton) view;
            String size = view.getTag().toString();
            if (size.compareTo("small_brush") == 0)
            {
                m_drawView.setBrushSize(10.0f);
                imgView.setImageDrawable(getResources().getDrawable(R.drawable.small_brush_pressed));
            }
            else if (size.compareTo("medium_brush") == 0)
            {
                m_drawView.setBrushSize(20.0f);
                imgView.setImageDrawable(getResources().getDrawable(R.drawable.medium_brush_pressed));
            }
            else
            {
                m_drawView.setBrushSize(30.0f);
                imgView.setImageDrawable(getResources().getDrawable(R.drawable.large_brush_pressed));
            }

            String currentSize = m_currentBrush.getTag().toString();
            if (currentSize.compareTo("small_brush") == 0)
                m_currentBrush.setImageDrawable(getResources().getDrawable(R.drawable.small_brush_normal));
            else if (currentSize.compareTo("medium_brush") == 0)
                m_currentBrush.setImageDrawable(getResources().getDrawable(R.drawable.medium_brush_normal));
            else
                m_currentBrush.setImageDrawable(getResources().getDrawable(R.drawable.large_brush_normal));

            m_currentBrush = (ImageButton) view;
        }
    }
}