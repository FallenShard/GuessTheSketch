package nvnteam.guessthesketch.bluetooth;

public class BluetoothProtocol
{
    // Message types sent from the BluetoothService Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final int COMMAND_CREATE_GAME = 6;
    public static final int COMMAND_START_GAME = 7;
    public static final int COMMAND_EXCHANGE_TEAM_NAMES = 8;
    public static final int COMMAND_START_DRAWING = 9;
    public static final int COMMAND_START_GUESSING = 10;
    public static final int COMMAND_SHOW_SCORES = 11;
    public static final int COMMAND_UNDO = 12;
    public static final int COMMAND_PROCEED = 13;
    public static final int COMMAND_GAME_MODE = 14;

    public static final int DATA_DRAWING_NODE = 20;

    public static final int IS_COMMAND = 100;
}
