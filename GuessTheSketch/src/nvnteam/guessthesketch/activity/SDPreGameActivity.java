package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class SDPreGameActivity extends FullScreenActivity 
{
    public static String TeamOneNameTag = "TEAM_ONE_NAME_TAG";
    public static String TeamTwoNameTag = "TEAM_TWO_NAME_TAG";

    EditText m_teamOneEditText;
    EditText m_teamTwoEditText;

    Button m_backBtn;
    Button m_startBtn;

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
    }

    private void initUI()
    {
        m_teamOneEditText = (EditText) findViewById(R.id.edit_text_team_one);
        m_teamTwoEditText = (EditText) findViewById(R.id.edit_text_team_two);

        m_startBtn = (Button) findViewById(R.id.button_start_sd_pre_game);
        m_backBtn = (Button) findViewById(R.id.button_back_sd_pre_game);
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
}
