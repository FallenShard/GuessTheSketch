package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.WordBase;
import nvnteam.guessthesketch.widget.DrawingView;
import nvnteam.guessthesketch.widget.LetterSpacingTextView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.view.KeyEvent;

public class SDGameActivity extends FullScreenActivity
{
    /* LogCat debugging purposes */
    public static String LOG_TAG = "DEBUGGING";

    /* GUI handlers */
    private DrawingView m_drawView;
    private ImageButton m_currentPaint;
    private LetterSpacingTextView m_mainWordTextView;
    private Button m_finishBtn;
    private TextView m_countDownTextView;
    private LinearLayout m_colorStrip;
    private EditText m_guesserEditText;

    private Animation m_scaleOut;
    private Animation m_scaleIn;

    /* Game logic elements */
    private String[] m_teamNames;
    private String m_currentWord = new String("?");

    private int m_currentTurn = 0;
    private static int MaxTeams = 2;

    private int m_wordPoints = 0;
    private int m_baseFactor = 5;
    private int m_difficultyFactor = 3;
    private float[] m_currentPoints = {0.f, 0.f};
    private enum State { Picking, Drawing, Guessing, Paused, Over };
    private State m_gameState = State.Picking;

    private long m_currentTimeLeft = 60000;
    private long m_currentTime = 0;             // Used to capture system time

    private CountDownTimer m_timer = new CountDownTimer(60000, 100)
    {
        public void onTick(long millisUntilFinished)
        {
            m_countDownTextView.setText(Long.toString(millisUntilFinished / 1000));
            m_currentTimeLeft = millisUntilFinished;
            m_drawView.pullTimeStamp(millisUntilFinished);
        }

        public void onFinish()
        {
            m_currentTimeLeft = 0;
            if (m_gameState == State.Drawing)
                startGuessingPhase();
            else if (m_gameState == State.Guessing)
                startEvaluationPhase();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sd_game);
        initUI();
        initListeners();

        m_mainWordTextView.setLetterSpacing(1.3f);

        m_teamNames = new String[2];
        m_teamNames[0] = getIntent().getStringExtra(SDPreGameActivity.TeamOneNameTag);
        m_teamNames[1] = getIntent().getStringExtra(SDPreGameActivity.TeamTwoNameTag);

        m_guesserEditText.setInputType(m_guesserEditText.getInputType()
                                     | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                                     | EditorInfo.TYPE_TEXT_VARIATION_FILTER);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (m_gameState == State.Picking)
            startPickingPhase();
    }

    @Override
    public void onStop()
    {
        super.onStop();
        m_timer.cancel();
    }

    public void startPickingPhase()
    {
        final String words[] ={ WordBase.getEasyWord(),
                                WordBase.getMediumWord(),
                                WordBase.getHardWord(),
                                WordBase.getReallyHardWord() };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.text_dialog_pick_a_word);
        builder.setItems(words, new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int item)
            {
                m_drawView.startNew();
                m_currentWord = words[item];
                StringBuffer outputBuffer = new StringBuffer(m_currentWord.length());
                for (int i = 0; i < m_currentWord.length(); i++)
                   if (m_currentWord.charAt(i) != ' ')
                       outputBuffer.append('_');
                   else
                       outputBuffer.append(' ');
                m_mainWordTextView.setText(outputBuffer.toString());
                m_wordPoints = m_baseFactor + m_difficultyFactor * item;
                Toast.makeText(SDGameActivity.this, words[item], Toast.LENGTH_LONG).show();
                m_gameState = State.Drawing;
                m_timer.start();
                m_currentTime = SystemClock.uptimeMillis();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void startGuessingPhase()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Prepare to guess")
               .setCancelable(false)
               .setPositiveButton("OK", new DialogInterface.OnClickListener() 
               {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                       m_drawView.startNew();
                       m_colorStrip.startAnimation(m_scaleOut);
                       m_colorStrip.setVisibility(View.GONE);
                       m_scaleIn.setStartOffset(m_scaleOut.getDuration());
                       m_guesserEditText.startAnimation(m_scaleIn);
                       m_guesserEditText.setVisibility(View.VISIBLE);
                       m_scaleIn.setStartOffset(0);
                       m_gameState = State.Guessing;
                       m_timer.start();
                       m_drawView.setPlayback(true);
                       m_drawView.playBack(m_currentTime);
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
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
        startEvaluationPhase();
    }

    public void startEvaluationPhase()
    {
        handleEvaluation();
        if (m_gameState == State.Over)
                return;
        
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
                       m_guesserEditText.startAnimation(m_scaleOut);
                       m_guesserEditText.setVisibility(View.GONE);
                       m_scaleIn.setStartOffset(m_scaleOut.getDuration());
                       m_colorStrip.startAnimation(m_scaleIn);
                       m_colorStrip.setVisibility(View.VISIBLE);
                       m_scaleIn.setStartOffset(0);
                       m_gameState = State.Picking;
                       m_timer.start();
                       startPickingPhase();
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void handleEvaluation()
    {
        if (m_currentTimeLeft > 0)
            m_currentPoints[m_currentTurn] += m_wordPoints 
                                           + (float)m_currentTimeLeft / 2000;

        if (m_currentPoints[m_currentTurn] > 100.f)
        {
            m_gameState = State.Over;
            gameOver();
        }

        m_currentTurn = (m_currentTurn + 1) % MaxTeams;
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
                       SDGameActivity.this.finish();
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void initUI()
    {
        m_mainWordTextView = (LetterSpacingTextView) findViewById(R.id.text_view_word_to_guess);
        m_drawView = (DrawingView) findViewById(R.id.drawing);
        m_colorStrip = (LinearLayout) findViewById(R.id.paint_colors);
        m_currentPaint = (ImageButton) (m_colorStrip).getChildAt(0);
        m_currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        m_finishBtn = (Button) findViewById(R.id.button_finish_drawing);
        m_countDownTextView = (TextView) findViewById(R.id.text_view_count_down);
        m_guesserEditText = (EditText) findViewById(R.id.edit_text_guesser);

        m_scaleOut = AnimationUtils.loadAnimation(this, R.anim.scale_out);
        m_scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
    }

    private void initListeners()
    {
        m_finishBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_timer.cancel();
                startGuessingPhase();
            }
        });

        m_guesserEditText.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_GO) 
                   if (v.getText().toString().compareToIgnoreCase(m_currentWord) == 0)
                       exitGuessingPhase();
                   else
                       v.setText("");
                return true;
            }
        });
    }

    public void paintClicked(View view)
    {
        if (view != m_currentPaint)
        {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            m_drawView.setColor(color);

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            m_currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            m_currentPaint = (ImageButton)view;
        }
    }
}
