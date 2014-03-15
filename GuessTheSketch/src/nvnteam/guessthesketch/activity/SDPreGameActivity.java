package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.R.id;
import nvnteam.guessthesketch.R.layout;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class SDPreGameActivity extends Activity 
{
    TextView m_textViewTeamOne;
    TextView m_textViewTeamTwo;
    
    Button m_backBtn;
    Button m_startBtn;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sd_pre_game);

        m_textViewTeamOne = (TextView) findViewById(R.id.text_view_team_one);
        m_textViewTeamTwo = (TextView) findViewById(R.id.text_view_team_two);
        
        m_startBtn = (Button) findViewById(R.id.button_start_sd_pre_game);
        m_startBtn.setOnClickListener(new OnClickListener()
        {
           @Override
           public void onClick(View v)
           {
               String teamOneName = m_textViewTeamOne.getText().toString();
               String teamTwoName = m_textViewTeamTwo.getText().toString();

               Intent intent = new Intent(SDPreGameActivity.this,
                                          SDGameActivity.class);
               
               intent.putExtra("teamOneName", "Team One");
               intent.putExtra("teamTwoName", "Team Two");

               startActivity(intent);
           }
        });
        
        m_backBtn = (Button) findViewById(R.id.button_back_sd_pre_game);
        m_backBtn.setOnClickListener(new OnClickListener()
        {
           @Override
           public void onClick(View v)
           {
               finish();
           }
        });
    }
    
    @Override
    public void onWindowFocusChanged(boolean hasFocus) 
    {
            super.onWindowFocusChanged(hasFocus);
        if (hasFocus) 
        {
            getWindow().getDecorView().setSystemUiVisibility(
                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }
}
