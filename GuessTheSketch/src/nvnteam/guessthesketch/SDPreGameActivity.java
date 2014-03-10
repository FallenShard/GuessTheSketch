package nvnteam.guessthesketch;

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
               intent.putExtra("teamOneName", teamOneName);
               intent.putExtra("teamTwoName", teamTwoName);

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
}
