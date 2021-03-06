package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GameOverDialog extends AlertDialog 
{
    Context m_context;

    TextView m_title;
    TextView m_info;
    TextView m_teamName;
    TextView m_with;
    TextView m_number;
    TextView m_points;
    TextView m_congratulations;
    Button m_button;

    protected GameOverDialog(Context context) 
    {
        super(context);
        m_context = context;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_game_over);

        setCancelable(false);
        m_button = (Button) findViewById(R.id.dialog_guessing_button);
        m_title = (TextView) findViewById(R.id.dialog_game_over_title);
        m_info = (TextView) findViewById(R.id.dialog_game_over_text);
        m_teamName = (TextView) findViewById(R.id.dialog_game_over_team_name);
        m_with = (TextView) findViewById(R.id.dialog_game_over_with);
        m_number = (TextView) findViewById(R.id.dialog_game_over_number);
        m_points = (TextView) findViewById(R.id.dialog_game_over_points);
        m_congratulations = (TextView) findViewById(R.id.dialog_game_over_congratulations);

        Typeface tf = Typeface.createFromAsset(m_context.getAssets(), "fonts/Villa.ttf");
        m_button.setTypeface(tf);
        

        tf = Typeface.createFromAsset(m_context.getAssets(), "fonts/Segoe.ttf");
        m_title.setTypeface(tf);
        m_teamName.setTypeface(tf);
        m_number.setTypeface(tf);
        m_info.setTypeface(tf);
        m_with.setTypeface(tf);
        m_points.setTypeface(tf);
        m_congratulations.setTypeface(tf);
    }

    public void setParam(String teamName, int points, int color)
    {
        m_teamName.setText(teamName);
        m_number.setText("" + points);
        m_teamName.setTextColor(color);
        m_number.setTextColor(color);
    }

    public void setOnClickListener(View.OnClickListener lsnr)
    {
        m_button.setOnClickListener(lsnr);
    }
}
