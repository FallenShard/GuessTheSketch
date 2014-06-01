package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.util.GTSUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ScoreDialog extends AlertDialog 
{
    Context m_context;

    TextView m_title;
    TextView m_currentRound;
    TextView m_currentScore;
    TextView m_teamOneName;
    TextView m_teamOneScore;
    TextView m_teamTwoName;
    TextView m_teamTwoScore;
    TextView m_proceedText;

    Button m_button;

    public ScoreDialog(Context context) 
    {
        super(context);
        m_context = context;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_scores);

        setCancelable(false);
        m_button = (Button) findViewById(R.id.dialog_scores_button);
        m_title = (TextView) findViewById(R.id.dialog_scores_title);
        m_currentRound = (TextView) findViewById(R.id.dialog_scores_current_round);
        m_currentScore = (TextView) findViewById(R.id.dialog_scores_current_score);
        m_teamOneName = (TextView) findViewById(R.id.dialog_scores_team_one_name);
        m_teamOneScore = (TextView) findViewById(R.id.dialog_scores_team_one_score);
        m_teamTwoName = (TextView) findViewById(R.id.dialog_scores_team_two_name);
        m_teamTwoScore = (TextView) findViewById(R.id.dialog_scores_team_two_score);
        m_proceedText = (TextView) findViewById(R.id.dialog_scores_proceed);

        Typeface tf = Typeface.createFromAsset(m_context.getAssets(), "fonts/Villa.ttf");
        m_button.setTypeface(tf);
        m_title.setTypeface(tf);
        m_teamOneName.setTypeface(tf);
        m_teamOneScore.setTypeface(tf);
        m_teamTwoName.setTypeface(tf);
        m_teamTwoScore.setTypeface(tf);

        tf = Typeface.createFromAsset(m_context.getAssets(), "fonts/Segoe.ttf");
        m_currentRound.setTypeface(tf);
        m_currentScore.setTypeface(tf);
        m_proceedText.setTypeface(tf);
    }

    public void setOnClickListener(View.OnClickListener lsnr)
    {
        m_button.setOnClickListener(lsnr);
    }

    public void setParam(String gameMode, String round, String teamOne, float scoreOne, String teamTwo, float scoreTwo, int turn)
    {
        m_title.setText(gameMode);
        m_currentRound.setText(round);
        m_teamOneName.setText(teamOne);
        m_teamOneScore.setText("" + GTSUtils.round(scoreOne, 2));
        m_teamTwoName.setText(teamTwo);
        m_teamTwoScore.setText("" + GTSUtils.round(scoreTwo, 2));
        if (turn == 0)
            m_proceedText.setText("It's " + teamOne + "'s turn!");
        else
            m_proceedText.setText("It's " + teamTwo + "'s turn!");
    }
}
