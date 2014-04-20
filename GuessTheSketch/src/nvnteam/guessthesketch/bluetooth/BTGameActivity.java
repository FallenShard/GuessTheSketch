package nvnteam.guessthesketch.bluetooth;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Set;

import nvnteam.guessthesketch.activity.FullScreenActivity;
import nvnteam.guessthesketch.activity.SDGameActivity;
import nvnteam.guessthesketch.activity.SDPreGameActivity;
import nvnteam.guessthesketch.bluetooth.BluetoothProtocol;
import nvnteam.guessthesketch.util.ActivityUtils;
import nvnteam.guessthesketch.util.FontUtils;
import nvnteam.guessthesketch.util.GTSUtils;
import nvnteam.guessthesketch.util.FontUtils.FontType;
import nvnteam.guessthesketch.widget.DrawingView;
import nvnteam.guessthesketch.widget.LetterSpacingTextView;

import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.WordBase;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
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
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;
import android.widget.AdapterView.OnItemClickListener;

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
    
    // View flipper animations
    private Animation m_inAnim;
    private Animation m_outAnim;

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
    private EditText m_teamOneEditText;
    private EditText m_teamTwoEditText;
    private Button m_backButton;
    private Button m_startButton;

    // In-game views
    private Button m_finishButton;
    private Button m_undoButton;
    private LetterSpacingTextView m_mainWordTextView;
    private LinearLayout m_colorStrip;
    private ImageButton m_currentPaint;
    private TextView m_countDownTextView;
    private EditText m_guesserEditText;
    private DrawingView m_drawView;
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
    private boolean m_isServer = false;

    // Game logic elements
    private String[] m_teamNames = new String[2];;
    private String m_currentWord = new String("");

    private int m_currentTurn = 0;
    private static int MaxTeams = 2;

    private int m_wordPoints = 0;
    private int m_baseFactor = 5;
    private int m_difficultyFactor = 3;
    private float[] m_currentPoints = {0.f, 0.f};
    private enum State { Picking, Drawing, Guessing, Paused, Over };
    private State m_gameState = State.Picking;

    private long m_currentTimeLeft = 60000;
    private CountDownTimer m_timer = new CountDownTimer(60000, 300)
    {
        public void onTick(long millisUntilFinished)
        {
            m_countDownTextView.setText(Long.toString(millisUntilFinished / 1000));
            m_currentTimeLeft = millisUntilFinished;
            int redComp = (255 * (60000 - (int)millisUntilFinished) / 60000) << 16;
            int greenComp = (255 * ((int)millisUntilFinished) / 60000) << 8;
            m_countDownTextView.setTextColor(0xFF000000 | redComp | greenComp);
        }

        public void onFinish()
        {
            m_currentTimeLeft = 0;
            if (m_gameState == State.Drawing)
                startGuessingPhase();
            else if (m_gameState == State.Guessing);
                //startEvaluationPhase();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "+++ ON CREATE +++");

        setContentView(R.layout.view_flipper);
        setupMyUI();

        // Get local Bluetooth adapter
        m_bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        if (m_bluetoothAdapter == null)
        {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
        }
    }
    
    @Override
    public void onStart()
    {
        super.onStart();
        if(D) Log.e(TAG, "++ ON START ++");

        // If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!m_bluetoothAdapter.isEnabled())
        {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
        else
        // Otherwise, setup the chat session
            if (m_bluetoothService == null) setupChat();
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
                // Start the Bluetooth chat services
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

    public void setupMyUI()
    {
        m_viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        m_inAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        m_outAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        m_viewFlipper.setInAnimation(m_inAnim);
        m_viewFlipper.setOutAnimation(m_outAnim);
        Typeface tf = FontUtils.getTypeface(this, FontType.MAIN_FONT);

        // Chat and connection views setup
        m_deviceTextView = (TextView) findViewById(R.id.device_text_view);
        m_statusTextView = (TextView) findViewById(R.id.status_text_view);

        m_deviceConnectButton = (Button) findViewById(R.id.connect_button);
        m_deviceConnectButton.setTypeface(tf);
        m_deviceConnectButton.setOnClickListener(new OnClickListener()
        {
           public void onClick(View v)
           {
               Intent intent = new Intent(BTGameActivity.this, DeviceListDialogActivity.class);
               startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
           }
        });

        m_discoverableButton = (Button) findViewById(R.id.discoverable_button);
        m_discoverableButton.setTypeface(tf);
        m_discoverableButton.setOnClickListener(new OnClickListener()
        {
           public void onClick(View v)
           {
               ensureDiscoverable();
           }
        });

        m_createGameButton = (Button) findViewById(R.id.button_create_game);
        m_createGameButton.setTypeface(tf);
        m_createGameButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (m_bluetoothService.getState() != BluetoothService.STATE_CONNECTED)
                {
                    Toast.makeText(BTGameActivity.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (m_isServer)
                {
                    sendCommand(BluetoothProtocol.COMMAND_CREATE_GAME);
                    enableConfigPreGame();
                    m_viewFlipper.setDisplayedChild(m_viewFlipper.indexOfChild(m_preLayout));
                }
                else
                {
                    Toast.makeText(BTGameActivity.this, 
                            "Please wait for the server to create the game", Toast.LENGTH_LONG).show();
                }
            }
        });

        m_conversationArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        m_conversationView = (ListView) findViewById(R.id.in);
        m_conversationView.setAdapter(m_conversationArrayAdapter);

        m_outEditText = (EditText) findViewById(R.id.edit_text_out);
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

        m_sendButton = (Button) findViewById(R.id.button_send);
        m_sendButton.setTypeface(tf);
        m_sendButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                TextView view = (TextView) findViewById(R.id.edit_text_out);
                String message = view.getText().toString();
                sendMessage(message);
                ActivityUtils.hideSystemUI(BTGameActivity.this);
            }
        });

        TextView chatTitleView = (TextView) findViewById(R.id.chat_title_view);
        chatTitleView.setTypeface(tf);

        // Pre-game views setup
        m_teamOneEditText = (EditText) findViewById(R.id.edit_text_team_one);
        m_teamOneEditText.setTypeface(tf);
        m_teamTwoEditText = (EditText) findViewById(R.id.edit_text_team_two);
        m_teamTwoEditText.setTypeface(tf);
        m_backButton = (Button) findViewById(R.id.button_back_sd_pre_game);
        m_backButton.setTypeface(tf);
        m_backButton.setOnClickListener(new OnClickListener()
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

        m_startButton = (Button) findViewById(R.id.button_start_sd_pre_game);
        m_startButton.setTypeface(tf);
        m_startButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (m_bluetoothService.getState() != BluetoothService.STATE_CONNECTED)
                {
                    Toast.makeText(BTGameActivity.this, R.string.not_connected, Toast.LENGTH_SHORT).show();
                    return;
                }

                if (m_isServer)
                {
                    enableConfigInGame();
                    sendCommand(BluetoothProtocol.COMMAND_START_GAME);
                    m_viewFlipper.setDisplayedChild(m_viewFlipper.indexOfChild(m_gameLayout));
                }
                else
                {
                    Toast.makeText(BTGameActivity.this, 
                            "Please wait for the server to start the game", Toast.LENGTH_LONG).show();
                }
            }
        });

        // In-game views setup
        m_finishButton = (Button) findViewById(R.id.button_finish_drawing);
        m_finishButton.setVisibility(View.GONE);

        m_undoButton = (Button) findViewById(R.id.button_undo);
        m_undoButton.setTypeface(tf);
        m_undoButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_drawView.undo();
                //sendCommand(COMMAND_UNDO);
            }
        });

        m_mainWordTextView = (LetterSpacingTextView) findViewById(R.id.text_view_word_to_guess);
        m_mainWordTextView.setTypeface(tf);
        m_mainWordTextView.setLetterSpacing(1.3f);

        m_colorStrip = (LinearLayout) findViewById(R.id.paint_colors);
        m_currentPaint = (ImageButton) (m_colorStrip).getChildAt(0);
        m_currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.color_button_pressed));

        m_countDownTextView = (TextView) findViewById(R.id.text_view_count_down);
        m_countDownTextView.setTypeface(tf);
        m_guesserEditText = (EditText) findViewById(R.id.edit_text_guesser);
        m_drawView = (DrawingView) findViewById(R.id.drawing);

        m_mainWordTextView.setTypeface(tf);
        m_mainWordTextView.setLetterSpacing(1.3f);

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
    }

    private void setupChat()
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

    // The Handler that gets information back from the BluetoothChatService
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
                    if (!m_isServer)
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
                if (!handleCommandMessage(readBuf, msg.arg1))
                {
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    m_conversationArrayAdapter.add(m_connectedDeviceName+":  " + readMessage);
                }
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
    };

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
                m_isServer = true;
                m_bluetoothService.connect(device);
                m_deviceTextView.append(" as a server");
            }
            break;
        case REQUEST_ENABLE_BT:
            // When the request to enable Bluetooth returns
            if (resultCode == Activity.RESULT_OK)
            {
                // Bluetooth is now enabled, so set up a chat session
                setupChat();
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

    public boolean handleCommandMessage(byte[] messageBytes, int arg1)
    {
        int code = messageBytes[0] << 24 | messageBytes[1] << 16
                 | messageBytes[2] << 8  | messageBytes[3];

        switch (code)
        {
        case BluetoothProtocol.COMMAND_CREATE_GAME:
        {
            enableConfigPreGame();
            m_viewFlipper.setDisplayedChild(m_viewFlipper.indexOfChild(m_preLayout));
            return true;
        }
        case BluetoothProtocol.COMMAND_START_GAME:
        {
            enableConfigInGame();
            m_viewFlipper.setDisplayedChild(m_viewFlipper.indexOfChild(m_infoLayout));
            return true;
        }
        case BluetoothProtocol.COMMAND_EXCHANGE_TEAM_NAMES:
        {
            if (m_isServer)
            {
                m_teamNames[0] = m_teamOneEditText.getText().toString();
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
            startDrawingPhase();
            return true;
        }
        case BluetoothProtocol.COMMAND_START_GUESSING:
        {
            m_currentWord = new String(messageBytes, 4, arg1 - 4);
            m_viewFlipper.showPrevious();
            startGuessingPhase();
            return true;
        }
        case BluetoothProtocol.COMMAND_SHOW_SCORES:
        {
            showScores();
            return true;
        }
        case BluetoothProtocol.DATA_DRAWING_NODE:
        {
            return true;
        }
        }
        if (D) Log.d(TAG, "CODE GIVEN: " + code);
        return false;
    }

    public void enableConfigPreGame()
    {
        if (m_isServer)
        {
            m_teamTwoEditText.setVisibility(View.INVISIBLE);
        }
        else
        {
            m_teamOneEditText.setVisibility(View.INVISIBLE);
        }
    }
    public void enableConfigInGame()
    {
        if (m_isServer)
        {
            sendCommandWithString(BluetoothProtocol.COMMAND_EXCHANGE_TEAM_NAMES,
                    m_teamOneEditText.getText().toString());
            startDrawingPhase();
        }
        else
        {
            sendCommandWithString(BluetoothProtocol.COMMAND_EXCHANGE_TEAM_NAMES,
                    m_teamTwoEditText.getText().toString());
            m_infoTextView.setText("Please wait while the other player picks a word"
                    + '\n' + "Team one: " + m_teamNames[0]
                    + '\n' + "Team two: " + m_teamNames[1]);
        }
    }

    public void startDrawingPhase()
    {
        m_guesserEditText.setText("");
        m_countDownTextView.setText("");
        m_mainWordTextView.setText("");
        m_currentPaint = (ImageButton) (m_colorStrip).getChildAt(0);
        paintClicked(m_currentPaint);
        m_timer.cancel();
        m_drawView.setPlayback(false);
        if (m_currentTurn == 0)
        {
            m_finishButton.setBackgroundResource(R.drawable.button_in_game_blue);
            m_undoButton.setBackgroundResource(R.drawable.button_in_game_blue);
        }
        else
        {
            m_finishButton.setBackgroundResource(R.drawable.button_in_game_red);
            m_undoButton.setBackgroundResource(R.drawable.button_in_game_red);
        }
        m_finishButton.animate().alpha(1).withLayer();
        m_undoButton.animate().alpha(1).withLayer();

        final String words[] = { WordBase.getEasyWord(),
                                 WordBase.getMediumWord(),
                                 WordBase.getHardWord(),
                                 WordBase.getReallyHardWord() };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.text_dialog_pick_a_word);
        builder.setCancelable(false);
        builder.setItems(words, new DialogInterface.OnClickListener()
        {
            public void onClick(DialogInterface dialog, int item)
            {
                m_drawView.startNew();
                m_currentWord = words[item];
                sendCommandWithString(BluetoothProtocol.COMMAND_START_GUESSING, m_currentWord);
                m_mainWordTextView.setText(m_currentWord);
                m_wordPoints = m_baseFactor + m_difficultyFactor * item;
                //Toast.makeText(BTGameActivity.this, words[item], Toast.LENGTH_LONG).show();
                m_gameState = State.Drawing;
                m_timer.start();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void startGuessingPhase()
    {
        /*
        StringBuffer outputBuffer = new StringBuffer(m_currentWord.length());
        for (int i = 0; i < m_currentWord.length(); i++)
           if (m_currentWord.charAt(i) != ' ')
               outputBuffer.append('_');
           else
               outputBuffer.append(' ');
        m_mainWordTextView.setText(outputBuffer.toString());*/
        m_mainWordTextView.setText(m_currentWord);
        m_timer.cancel();
        m_drawView.startNew();
        m_finishButton.animate().alpha(0).withLayer();
        m_undoButton.animate().alpha(0).withLayer();
        m_colorStrip.animate().alpha(0).withLayer();
        m_colorStrip.setVisibility(View.GONE);
        m_guesserEditText.animate().alpha(1).withLayer();
        m_guesserEditText.setVisibility(View.VISIBLE);
        m_gameState = State.Guessing;
        m_timer.start();
        m_drawView.setPlayback(true);
/*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Prepare to guess")
               .setCancelable(false)
               .setPositiveButton("OK", new DialogInterface.OnClickListener() 
               {
                   public void onClick(DialogInterface dialog, int id)
                   {
                       m_drawView.startNew();
                       m_finishButton.animate().alpha(0).withLayer();
                       m_undoButton.animate().alpha(0).withLayer();
                       m_colorStrip.animate().alpha(0).withLayer();
                       m_colorStrip.setVisibility(View.GONE);
                       m_guesserEditText.animate().alpha(1).withLayer();
                       m_guesserEditText.setVisibility(View.VISIBLE);
                       m_gameState = State.Guessing;
                       m_timer.start();
                       m_drawView.setPlayback(true);
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();*/
    }
    
    public void showScores()
    {
        
    }
    
    public void exitGuessingPhase()
    {
        m_timer.cancel();
        m_drawView.setPlayback(false);
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
    }

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
}