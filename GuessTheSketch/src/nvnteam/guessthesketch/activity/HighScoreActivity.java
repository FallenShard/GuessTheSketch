package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.util.HighScoreManager;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class HighScoreActivity extends FullScreenActivity 
{
    ViewFlipper m_scoreFlipper;

    Button m_goLeftBtn;
    Button m_goRightBtn;
    Button m_backBtn;

    TextView m_highScoresTitle;
    TextView m_flipperPage;

    private TextView[] m_roundScoresNames = new TextView[5];
    private TextView[] m_roundScoresVals = new TextView[5];
    private TextView[] m_avgScoresNames = new TextView[5];
    private TextView[] m_avgScoresVals = new TextView[5];
    private TextView[] m_fiveScoresNames = new TextView[5];
    private TextView[] m_fiveScoresVals  = new TextView[5];

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);
        initUI();
        initListeners();

        HighScoreManager hsm = new HighScoreManager(this);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Segoe.ttf");
        m_flipperPage.setTypeface(tf);
        for (int i = 0; i < 5; i++)
        {
            m_roundScoresNames[i].setTypeface(tf);
            m_roundScoresNames[i].setText(hsm.getScoresName(i, HighScoreManager.ROUND_PREFS_NAME));
            m_roundScoresVals[i].setTypeface(tf);
            m_roundScoresVals[i].setText("" + hsm.getScoresVal(i, HighScoreManager.ROUND_PREFS_NAME));
        }

        for (int i = 0; i < 5; i++)
        {
            m_avgScoresNames[i].setTypeface(tf);
            m_avgScoresNames[i].setText(hsm.getScoresName(i, HighScoreManager.AVG_PREFS_NAME));
            m_avgScoresVals[i].setTypeface(tf);
            m_avgScoresVals[i].setText("" + hsm.getScoresVal(i, HighScoreManager.AVG_PREFS_NAME));
        }

        for (int i = 0; i < 5; i++)
        {
            m_fiveScoresNames[i].setTypeface(tf);
            m_fiveScoresNames[i].setText(hsm.getScoresName(i, HighScoreManager.FIVE_PREFS_NAME));
            m_fiveScoresVals[i].setTypeface(tf);
            m_fiveScoresVals[i].setText("" + hsm.getScoresVal(i, HighScoreManager.FIVE_PREFS_NAME));
        }
    }

    private void initUI()
    {
        m_scoreFlipper = (ViewFlipper) findViewById(R.id.high_score_flipper);
        m_highScoresTitle = (TextView) findViewById(R.id.text_view_high_scores);
        m_flipperPage = (TextView) findViewById(R.id.text_view_flipper_page);

        m_goLeftBtn = (Button) findViewById(R.id.btn_left);
        m_goRightBtn = (Button) findViewById(R.id.btn_right);
        m_backBtn = (Button) findViewById(R.id.button_back_high_scores);

        m_roundScoresNames[0] = (TextView) findViewById(R.id.round_score_name_one);
        m_roundScoresNames[1] = (TextView) findViewById(R.id.round_score_name_two);
        m_roundScoresNames[2] = (TextView) findViewById(R.id.round_score_name_three);
        m_roundScoresNames[3] = (TextView) findViewById(R.id.round_score_name_four);
        m_roundScoresNames[4] = (TextView) findViewById(R.id.round_score_name_five);

        m_roundScoresVals[0] = (TextView) findViewById(R.id.round_score_value_one);
        m_roundScoresVals[1] = (TextView) findViewById(R.id.round_score_value_two);
        m_roundScoresVals[2] = (TextView) findViewById(R.id.round_score_value_three);
        m_roundScoresVals[3] = (TextView) findViewById(R.id.round_score_value_four);
        m_roundScoresVals[4] = (TextView) findViewById(R.id.round_score_value_five);

        m_avgScoresNames[0] = (TextView) findViewById(R.id.avg_score_name_one);
        m_avgScoresNames[1] = (TextView) findViewById(R.id.avg_score_name_two);
        m_avgScoresNames[2] = (TextView) findViewById(R.id.avg_score_name_three);
        m_avgScoresNames[3] = (TextView) findViewById(R.id.avg_score_name_four);
        m_avgScoresNames[4] = (TextView) findViewById(R.id.avg_score_name_five);

        m_avgScoresVals[0] = (TextView) findViewById(R.id.avg_score_value_one);
        m_avgScoresVals[1] = (TextView) findViewById(R.id.avg_score_value_two);
        m_avgScoresVals[2] = (TextView) findViewById(R.id.avg_score_value_three);
        m_avgScoresVals[3] = (TextView) findViewById(R.id.avg_score_value_four);
        m_avgScoresVals[4] = (TextView) findViewById(R.id.avg_score_value_five);

        m_fiveScoresNames[0] = (TextView) findViewById(R.id.five_score_name_one);
        m_fiveScoresNames[1] = (TextView) findViewById(R.id.five_score_name_two);
        m_fiveScoresNames[2] = (TextView) findViewById(R.id.five_score_name_three);
        m_fiveScoresNames[3] = (TextView) findViewById(R.id.five_score_name_four);
        m_fiveScoresNames[4] = (TextView) findViewById(R.id.five_score_name_five);

        m_fiveScoresVals[0] = (TextView) findViewById(R.id.five_score_value_one);
        m_fiveScoresVals[1] = (TextView) findViewById(R.id.five_score_value_two);
        m_fiveScoresVals[2] = (TextView) findViewById(R.id.five_score_value_three);
        m_fiveScoresVals[3] = (TextView) findViewById(R.id.five_score_value_four);
        m_fiveScoresVals[4] = (TextView) findViewById(R.id.five_score_value_five);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Villa.ttf");
        m_highScoresTitle.setTypeface(tf);
        m_goLeftBtn.setTypeface(tf);
        m_goRightBtn.setTypeface(tf);
        m_backBtn.setTypeface(tf);

        Animation inAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation outAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        m_scoreFlipper.setInAnimation(inAnim);
        m_scoreFlipper.setOutAnimation(outAnim);
        m_flipperPage.setText(m_scoreFlipper.getCurrentView().getTag().toString());
    }

    private void initListeners()
    {
        m_goLeftBtn.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v) 
            {
                m_scoreFlipper.showPrevious();
                m_flipperPage.setText(m_scoreFlipper.getCurrentView().getTag().toString());
            }
        });

        m_goRightBtn.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v) 
            {
                m_scoreFlipper.showNext();
                m_flipperPage.setText(m_scoreFlipper.getCurrentView().getTag().toString());
            }
        });

        m_backBtn.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v) 
            {
                finish();
            }
        });
    }
}
