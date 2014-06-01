package nvnteam.guessthesketch.activity;


import nvnteam.guessthesketch.R;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

public class SDPreGameActivity extends FullScreenActivity 
{
    public static String TeamOneNameTag = "TEAM_ONE_NAME_TAG";
    public static String TeamTwoNameTag = "TEAM_TWO_NAME_TAG";
    public static String gameModeTag = "GAME_MODE_TAG";



    // 0 is for 5 Rounds, 1 is for 200 points, 2 is for 10 minutes 
    private int m_gameMode = 0;

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

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sd_pre_game);
        initUI();
        initListeners();

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Villa.ttf");
        m_backBtn.setTypeface(tf);
        m_startBtn.setTypeface(tf);
        m_teamOneEditText.setTypeface(tf);
        m_teamTwoEditText.setTypeface(tf);
        m_titleText.setTypeface(tf);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        InputMethodManager inputManager = (InputMethodManager) 
                this.getSystemService(SDPreGameActivity.INPUT_METHOD_SERVICE);

        View v = this.getCurrentFocus();
        if(v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
    }

    private void initUI()
    {
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

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Villa.ttf");
        m_titleText.setTypeface(tf);
        m_backBtn.setTypeface(tf);
        m_startBtn.setTypeface(tf);
        m_teamOneEditText.setTypeface(tf);
        m_teamTwoEditText.setTypeface(tf);

        tf = Typeface.createFromAsset(getAssets(), "fonts/Segoe.ttf");
        m_teamNamesText.setTypeface(tf);
        m_gameModesText.setTypeface(tf);
        m_fiveRoundsRadioButton.setTypeface(tf);
        m_timedRadioButton.setTypeface(tf);
        m_maxPointsRadioButton.setTypeface(tf);
    }

    private void initListeners()
    {
        m_startBtn.setOnClickListener(new OnClickListener()
        {
           @Override
           public void onClick(View v)
           {
               String teamOneName = m_teamOneEditText.getText().toString();
               String teamTwoName = m_teamTwoEditText.getText().toString();

               Intent intent = new Intent(SDPreGameActivity.this,
                                          SDGameActivity.class);

               intent.putExtra(TeamOneNameTag, teamOneName);
               intent.putExtra(TeamTwoNameTag, teamTwoName);
               intent.putExtra(gameModeTag, m_gameMode);

               startActivity(intent);
           }
        });

        m_backBtn.setOnClickListener(new OnClickListener()
        {
           @Override
           public void onClick(View v)
           {
               finish();
           }
        });
    }

    public void onRadioButtonClicked(View v)
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