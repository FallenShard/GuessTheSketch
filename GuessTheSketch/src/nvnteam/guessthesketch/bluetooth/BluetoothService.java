package nvnteam.guessthesketch.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * This class does all the work for setting up and managing Bluetooth
 * connections with other devices. It has a thread that listens for
 * incoming connections, a thread for connecting with a device, and a
 * thread for performing data transmissions when connected.
 */
public class BluetoothService 
{
    // Debugging
    private static final String TAG = "BluetoothGame";
    private static final boolean D = true;

    // Name for the SDP record when creating server socket
    private static final String NAME = "BluetoothGame";

    // Unique UUID for this application
    private static final UUID MY_UUID = UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    // Member fields
    private final BluetoothAdapter m_adapter;
    private final Handler m_handler;
    private AcceptThread m_acceptThread;
    private ConnectThread m_connectThread;
    private ConnectedThread m_connectedThread;
    private int m_state;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    /**
     * Constructor. Prepares a new BluetoothChat session.
     * @param context  The UI Activity Context
     * @param handler  A Handler to send messages back to the UI Activity
     */
    public BluetoothService(Context context, Handler handler)
    {
        m_adapter = BluetoothAdapter.getDefaultAdapter();
        m_state = STATE_NONE;
        m_handler = handler;
    }

    /**
     * Set the current state of the chat connection
     * @param state  An integer defining the current connection state
     */
    private synchronized void setState(int state)
    {
        if (D) Log.d(TAG, "setState() " + m_state + " -> " + state);
        m_state = state;

        // Give the new state to the Handler so the UI Activity can update
        m_handler.obtainMessage(BluetoothProtocol.MESSAGE_STATE_CHANGE, state, -1).sendToTarget();
    }

    /**
     * Return the current connection state. */
    public synchronized int getState()
    {
        return m_state;
    }

    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume() */
    public synchronized void start()
    {
        if (D) Log.d(TAG, "start");

        // Cancel any thread attempting to make a connection
        if (m_connectThread != null)
        {
            m_connectThread.cancel();
            m_connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (m_connectedThread != null) 
        {
            m_connectedThread.cancel(); 
            m_connectedThread = null;
        }

        // Start the thread to listen on a BluetoothServerSocket
        if (m_acceptThread == null) 
        {
            m_acceptThread = new AcceptThread();
            m_acceptThread.start();
        }

        setState(STATE_LISTEN);
    }

    /**
     * Start the ConnectThread to initiate a connection to a remote device.
     * @param device  The BluetoothDevice to connect
     */
    public synchronized void connect(BluetoothDevice device)
    {
        if (D) Log.d(TAG, "connect to: " + device);

        // Cancel any thread attempting to make a connection
        if (m_state == STATE_CONNECTING)
            if (m_connectThread != null)
            {
                m_connectThread.cancel();
                m_connectThread = null;
            }

        // Cancel any thread currently running a connection
        if (m_connectedThread != null)
        {
            m_connectedThread.cancel();
            m_connectedThread = null;
        }

        // Start the thread to connect with the given device
        m_connectThread = new ConnectThread(device);
        m_connectThread.start();

        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     * @param socket  The BluetoothSocket on which the connection was made
     * @param device  The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device)
    {
        if (D) Log.d(TAG, "connected");

        // Cancel the thread that completed the connection
        if (m_connectThread != null)
        {
            m_connectThread.cancel();
            m_connectThread = null;
        }

        // Cancel any thread currently running a connection
        if (m_connectedThread != null)
        {
            m_connectedThread.cancel();
            m_connectedThread = null;
        }

        // Cancel the accept thread because we only want to connect to one device
        if (m_acceptThread != null)
        {
            m_acceptThread.cancel();
            m_acceptThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        m_connectedThread = new ConnectedThread(socket);
        m_connectedThread.start();

        // Send the name of the connected device back to the UI Activity
        Message msg = m_handler.obtainMessage(BluetoothProtocol.MESSAGE_DEVICE_NAME);
        Bundle bundle = new Bundle();
        bundle.putString(BTGameActivity.DEVICE_NAME, device.getName());
        msg.setData(bundle);
        m_handler.sendMessage(msg);

        setState(STATE_CONNECTED);
    }

    /**
     * Stop all threads
     */
    public synchronized void stop() 
    {
        if (D) Log.d(TAG, "stop");
        if (m_connectThread != null)
        {
            m_connectThread.cancel();
            m_connectThread = null;
        }
        if (m_connectedThread != null)
        {
            m_connectedThread.cancel();
            m_connectedThread = null;
        }

        if (m_acceptThread != null)
        {
            m_acceptThread.cancel();
            m_acceptThread = null;
        }

        setState(STATE_NONE);
    }

    /**
     * Write to the ConnectedThread in an unsynchronized manner
     * @param out The bytes to write
     * @see ConnectedThread#write(byte[])
     */
    public void write(byte[] out)
    {
        // Create temporary object
        ConnectedThread temp;
        // Synchronize a copy of the ConnectedThread
        synchronized (this) 
        {
            if (m_state != STATE_CONNECTED) return;
            temp = m_connectedThread;
        }
        // Perform the write unsynchronized
        temp.write(out);
    }

    /**
     * Indicate that the connection attempt failed and notify the UI Activity.
     */
    private void connectionFailed()
    {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = m_handler.obtainMessage(BluetoothProtocol.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BTGameActivity.TOAST, "Unable to connect device");
        msg.setData(bundle);
        m_handler.sendMessage(msg);
    }

    /**
     * Indicate that the connection was lost and notify the UI Activity.
     */
    private void connectionLost()
    {
        setState(STATE_LISTEN);

        // Send a failure message back to the Activity
        Message msg = m_handler.obtainMessage(BluetoothProtocol.MESSAGE_TOAST);
        Bundle bundle = new Bundle();
        bundle.putString(BTGameActivity.TOAST, "Device connection was lost");
        msg.setData(bundle);
        m_handler.sendMessage(msg);
    }

    /**
     * This thread runs while listening for incoming connections. It behaves
     * like a server-side client. It runs until a connection is accepted
     * (or until cancelled).
     */
    private class AcceptThread extends Thread
    {
        // The local server socket
        private final BluetoothServerSocket mm_serverSocket;

        public AcceptThread()
        {
            BluetoothServerSocket tmp = null;

            // Create a new listening server socket
            try
            {
                tmp = m_adapter.listenUsingRfcommWithServiceRecord(NAME, MY_UUID);
            }
            catch (IOException e) 
            {
                Log.e(TAG, "listen() failed", e);
            }
            mm_serverSocket = tmp;
        }

        public void run()
        {
            if (D) Log.d(TAG, "BEGIN m_acceptThread" + this);
            setName("AcceptThread");
            BluetoothSocket socket = null;

            // Listen to the server socket if we're not connected
            while (m_state != STATE_CONNECTED)
            {
                try
                {
                    // This is a blocking call and will only return on a
                    // successful connection or an exception
                    socket = mm_serverSocket.accept();
                }
                catch (IOException e)
                {
                    Log.e(TAG, "accept() failed", e);
                    break;
                }

                // If a connection was accepted
                if (socket != null)
                {
                    synchronized (BluetoothService.this)
                    {
                        switch (m_state)
                        {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            // Situation normal. Start the connected thread.
                            connected(socket, socket.getRemoteDevice());
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            // Either not ready or already connected. Terminate new socket.
                            try 
                            {
                                socket.close();
                            } 
                            catch (IOException e) 
                            {
                                Log.e(TAG, "Could not close unwanted socket", e);
                            }
                            break;
                        }
                    }
                }
            }
            if (D) Log.i(TAG, "END m_acceptThread");
        }

        public void cancel()
        {
            if (D) Log.d(TAG, "cancel " + this);
            try
            {
                mm_serverSocket.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "close() of server failed", e);
            }
        }
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mm_socket;
        private final BluetoothDevice mm_device;

        public ConnectThread(BluetoothDevice device)
        {
            mm_device = device;
            BluetoothSocket tmp = null;

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try
            {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            }
            catch (IOException e)
            {
                Log.e(TAG, "create() failed", e);
            }
            mm_socket = tmp;
        }

        public void run()
        {
            Log.i(TAG, "BEGIN m_connectThread");
            setName("ConnectThread");

            // Always cancel discovery because it will slow down a connection
            m_adapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try
            {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mm_socket.connect();
            } 
            catch (IOException e)
            {
                connectionFailed();
                // Close the socket
                try
                {
                    mm_socket.close();
                }
                catch (IOException e2)
                {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                // Start the service over to restart listening mode
                BluetoothService.this.start();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (BluetoothService.this)
            {
                m_connectThread = null;
            }

            // Start the connected thread
            connected(mm_socket, mm_device);
        }

        public void cancel()
        {
            try
            {
                mm_socket.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mm_socket;
        private final InputStream mm_inStream;
        private final OutputStream mm_outStream;

        public ConnectedThread(BluetoothSocket socket)
        {
            Log.d(TAG, "create ConnectedThread");
            mm_socket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try
            {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            }
            catch (IOException e)
            {
                Log.e(TAG, "temp streams not created", e);
            }

            mm_inStream = tmpIn;
            mm_outStream = tmpOut;
        }

        public void run()
        {
            Log.i(TAG, "BEGIN m_connectedThread");
            byte[] buffer = new byte[4096];
            int bytes;

            // Keep listening to the InputStream while connected
            while (true)
            {
                try
                {
                    // Read from the InputStream
                    bytes = mm_inStream.read(buffer);
                    Log.i("THREADCOMM", "BYTES READ: " + bytes);
                    if (bytes > 4)
                    {
                        byte[] messageCode = new byte[4];
                        messageCode[0] = buffer[0];
                        messageCode[1] = buffer[1];
                        messageCode[2] = buffer[2];
                        messageCode[3] = buffer[3];
                        byte[] byteMessage = new byte[bytes - 4];
                        for (int i = 0; i < bytes - 4; i++)
                            byteMessage[i] = buffer[i + 4];
                        
                        int code = messageCode[0] << 24 | messageCode[1] << 16
                                | messageCode[2] << 8  | messageCode[3];
                        Log.i(TAG, "CODE READ: " + code);
                        
                        if (code == BluetoothProtocol.DATA_DRAWING_NODE)
                            m_handler.obtainMessage(BluetoothProtocol.MESSAGE_READ, bytes - 4, code, byteMessage)
                            .sendToTarget();
                        else
                            m_handler.obtainMessage(BluetoothProtocol.MESSAGE_READ, bytes, -1, buffer)
                            .sendToTarget();
                        
                        
                    }
                    else
                     // Send the obtained bytes to the UI Activity
                        m_handler.obtainMessage(BluetoothProtocol.MESSAGE_READ, bytes, -1, buffer)
                        .sendToTarget();
                    
                    
                    
                    
                }
                catch (IOException e)
                {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }

        /**
         * Write to the connected OutStream.
         * @param buffer  The bytes to write
         */
        public void write(byte[] buffer)
        {
            try
            {
                mm_outStream.write(buffer);

                // Share the sent message back to the UI Activity
                m_handler.obtainMessage(BluetoothProtocol.MESSAGE_WRITE, -1, -1, buffer)
                         .sendToTarget();
            }
            catch (IOException e)
            {
                Log.e(TAG, "Exception during write", e);
            }
        }

        public void cancel()
        {
            try
            {
                mm_socket.close();
            }
            catch (IOException e)
            {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }
}
