package nvnteam.guessthesketch.activity;


import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.util.HighScoreManager;
import nvnteam.guessthesketch.util.WordBase;
import nvnteam.guessthesketch.widget.DrawingView;
import nvnteam.guessthesketch.widget.LetterSpacingTextView;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.util.Log;
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
import android.widget.ViewFlipper;
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
    private Button m_undoBtn;
    private TextView m_countDownTextView;
    private TextView m_currentRoundTextView;
    private TextView m_globalCountDownTextView;
    private ViewFlipper m_paletteFlipper;
    private LinearLayout m_colorStrip;
    private EditText m_guesserEditText;

    /* Game logic elements */
    private String m_currentWord = new String("");
    private int m_wordPoints = 0;
    private int m_baseFactor = 5;
    private int m_difficultyFactor = 5;
    private float m_wordModifier = 1.0f;

    private String[] m_teamNames;
    private int m_currentTurn = 0;
    private static int MaxTeams = 2;
    private int[] m_currentScores = {0, 0};
    private HighScoreManager m_highScoreManager = null;

    private int m_roundsPassed = 0;
    private int m_gameMode = 0;

    private enum State { Picking, Drawing, Guessing, Over };
    private State m_gameState = State.Picking;

    private long m_currentTimeLeft = 60000;
    private long m_currentTime = 0;             // Used to capture system time

    private CountDownTimer m_timer = new CountDownTimer(60000, 300)
    {
        public void onTick(long millisUntilFinished)
        {
            m_countDownTextView.setText(Long.toString(millisUntilFinished / 1000));
            m_currentTimeLeft = millisUntilFinished;
            int redComp = (millisUntilFinished > 30000 ? (255 * (30000 - (int)millisUntilFinished) / 30000) : 255) << 16;
            int greenComp = (millisUntilFinished < 30000 ? (255 * ((int)millisUntilFinished) / 30000) : 255) << 8;

            m_countDownTextView.setTextColor(0xFF000000 | redComp | greenComp);
        }

        public void onFinish()
        {
            m_currentTimeLeft = 0;
            if (m_gameState == State.Drawing)
                prepareGuessingPhase();
            else if (m_gameState == State.Guessing)
                exitGuessingPhase();
        }
    };

    private CountDownTimer m_globalTimer = null;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sd_game);
        initUI();
        initListeners();

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Villa.ttf");
        m_mainWordTextView.setTypeface(tf);
        m_mainWordTextView.setLetterSpacing(1.3f);

        m_countDownTextView.setTypeface(tf);
        m_currentRoundTextView.setTypeface(tf);
        m_globalCountDownTextView.setTypeface(tf);
        m_finishBtn.setTypeface(tf);
        m_undoBtn.setTypeface(tf);

        m_teamNames = new String[2];
        m_teamNames[0] = getIntent().getStringExtra(SDPreGameActivity.TeamOneNameTag);
        m_teamNames[1] = getIntent().getStringExtra(SDPreGameActivity.TeamTwoNameTag);
        m_gameMode = getIntent().getIntExtra(SDPreGameActivity.gameModeTag, 1);

        Log.e("GAME MODE", "GAME MODE IS " + m_gameMode);

        if (m_gameMode == 2)
        {
            m_globalTimer = new CountDownTimer(600000, 1000)
            {
                public void onTick(long millisUntilFinished)
                {
                    m_globalCountDownTextView.setText("" + 
                            millisUntilFinished / 60000 + ":" +
                            millisUntilFinished % 60000 / 1000);
                }

                public void onFinish()
                {
                    gameOver();
                }
            };
            m_globalCountDownTextView.setVisibility(View.VISIBLE);
            m_globalTimer.start();
        }

        m_guesserEditText.setTypeface(tf);
        m_guesserEditText.setInputType(m_guesserEditText.getInputType()
                                     | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                                     | EditorInfo.TYPE_TEXT_VARIATION_FILTER);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        m_highScoreManager = new HighScoreManager(this);

        if (m_gameState == State.Picking)
            prepareDrawingPhase();
    }

    public void onDestroy()
    {
        m_timer.cancel();
        if (m_globalTimer != null)
            m_globalTimer.cancel();

        super.onDestroy();
    }

    /**
     * This function gets executed before the drawing phase begins, by clearing
     * all the relevant text views, wiping the canvas and resetting the timer.
     * It pops a modal dialog where the user can select a word.
     */
    public void prepareDrawingPhase()
    {
        // Check to see if the game is over
        if (gameOverCondition())
            gameOver();

        // Reset UI Text Views
        m_guesserEditText.setText("");
        m_countDownTextView.setText("");
        m_currentRoundTextView.setText("Current Round: " + ((m_roundsPassed + 2) >> 1));
        m_mainWordTextView.setText("");

        // Reset the timer
        m_timer.cancel();

        // Stop the playback and wipe the canvas
        m_drawView.setPlayback(false);
        m_drawView.startNew();

        // Change the backgrounds of buttons to corresponding team color
        if (m_currentTurn == 0)
        {
            m_finishBtn.setBackgroundResource(R.drawable.button_in_game_blue);
            m_undoBtn.setBackgroundResource(R.drawable.button_in_game_blue);
        }
        else
        {
            m_finishBtn.setBackgroundResource(R.drawable.button_in_game_red);
            m_undoBtn.setBackgroundResource(R.drawable.button_in_game_red);
        }

        // Get the random words from the WordBase
        final String words[] = { WordBase.getEasyWord(),
                                 WordBase.getMediumWord(),
                                 WordBase.getHardWord(),
                                 WordBase.getReallyHardWord() };

        // Start the WordPickerDialog
        final WordPickerDialog wpd = new WordPickerDialog(SDGameActivity.this);
        wpd.show();
        wpd.setWords(words);
        wpd.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                if (wpd.getSelectedWord() != "")
                {
                    // Grab selected word and form the underscore string
                    m_currentWord = wpd.getSelectedWord();
                    StringBuffer outputBuffer = new StringBuffer(m_currentWord.length());
                    for (int i = 0; i < m_currentWord.length(); i++)
                       if (m_currentWord.charAt(i) != ' ')
                           outputBuffer.append('_');
                       else
                           outputBuffer.append(' ');
                    m_mainWordTextView.setText(outputBuffer.toString());

                    // Set base word points for selected word
                    m_wordPoints = m_baseFactor + m_difficultyFactor * wpd.getSelectedIndex();
                    m_gameState = State.Drawing;
                    m_timer.start();
                    m_currentTime = SystemClock.uptimeMillis();
                    Toast.makeText(SDGameActivity.this, wpd.getSelectedWord(), Toast.LENGTH_LONG).show();
                    wpd.dismiss();
                }
                else
                    Toast.makeText(SDGameActivity.this, "Please select a word!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * This function gets executed before the guessing phase begins, by clearing
     * all the relevant text views, wiping the canvas and resetting the timer.
     * It pops a modal dialog where the user can confirm he's ready to guess.
     */
    public void prepareGuessingPhase()
    {
        m_wordPoints += (int) (m_currentTimeLeft / 10000);
        m_drawView.startNew();
        m_paletteFlipper.showNext();
        m_gameState = State.Guessing;

        final PrepareToGuessDialog ptgd = new PrepareToGuessDialog(SDGameActivity.this);
        ptgd.show();
        ptgd.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v) 
            {
                m_drawView.setPlayback(true);
                m_drawView.playBack(m_currentTime);
                m_timer.start();
                ptgd.dismiss();
            }
        });
    }

    /**
     * This function gets executed after the guessing phase begins, and evaluates
     * the current round.
     */
    public void exitGuessingPhase()
    {
        // Cancel the timer and stop playback
        m_timer.cancel();
        m_drawView.setPlayback(false);

        // Reveal the word to the guesser
        m_mainWordTextView.setText(m_currentWord);
        Toast.makeText(SDGameActivity.this, "The word was " + m_currentWord, Toast.LENGTH_LONG).show();

        // Clean up the guessing TextView and hide soft keyboard
        m_guesserEditText.setText("");
        InputMethodManager inputManager = (InputMethodManager)
                           getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, 0);

        // If time hasn't run out, add points to the current team, else set to zero
        if (m_currentTimeLeft != 0)
        {
            m_wordPoints += (int) (m_currentTimeLeft / 10000);
            m_highScoreManager.saveScore(m_teamNames[m_currentTurn], 
                    m_wordPoints, HighScoreManager.ROUND_PREFS_NAME);
        }
        else
            m_wordPoints = 0;

        // Assert points and finish current round
        endRound();
    }

    private String getGameMode()
    {
        switch (m_gameMode)
        {
        case 0:
            return "Five Rounds Mode";

        case 1:
            return "Maximum Points Mode";

        case 2:
            return "Timed Mode";
        }

        return "ERROR_MODE";
    }

    public void endRound()
    {
        // Add the points to the current team
        m_currentScores[m_currentTurn] += (int) (m_wordPoints * m_wordModifier);

        m_currentTurn = (m_currentTurn + 1) % MaxTeams;

        // Increase the current round counter
        ++m_roundsPassed;

        String currentRound = "";
        if (m_roundsPassed % 2 == 0)
            currentRound = "End of Round " + (m_roundsPassed >> 1);
        else
            currentRound = "Round " + (m_roundsPassed + 1 >> 1) + " Half"; 

        // Pop the round evaluation dialog and prepare the user for the next round
        final ScoreDialog sd = new ScoreDialog(SDGameActivity.this);
        sd.show();
        sd.setParam(getGameMode(), currentRound, m_teamNames[0], m_currentScores[0],
                    m_teamNames[1], m_currentScores[1], m_currentTurn);
        sd.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                m_paletteFlipper.showNext();
                prepareDrawingPhase();
                sd.dismiss();
            }
        });
    }

    public boolean gameOverCondition()
    {
        switch (m_gameMode)
        {
        case 0:
            if (m_roundsPassed >= 10)
                return true;
            break;

        case 1:
            if (m_currentScores[0] >= 200 || m_currentScores[1] >= 200)
                return true;
            break;
        }

        return false;
    }

    public void gameOver()
    {
        int winner = m_currentScores[0] > m_currentScores[1] ? 0 : 1;
        int color = winner == 0 ? 0xFF8080FF : 0xFFFF8080;

        if (m_roundsPassed == 10)
        {
            m_highScoreManager.saveScore(m_teamNames[0], 
                    m_currentScores[0], HighScoreManager.FIVE_PREFS_NAME);
            m_highScoreManager.saveScore(m_teamNames[1], 
                    m_currentScores[1], HighScoreManager.FIVE_PREFS_NAME);
        }
        else if (m_roundsPassed >= 6)
        {
            m_highScoreManager.saveScore(m_teamNames[0], 
                    m_currentScores[0] / (m_roundsPassed >> 1), HighScoreManager.AVG_PREFS_NAME);
            m_highScoreManager.saveScore(m_teamNames[1], 
                    m_currentScores[1] / (m_roundsPassed >> 1), HighScoreManager.AVG_PREFS_NAME);
        }

        final GameOverDialog god = new GameOverDialog(SDGameActivity.this);
        god.show();
        god.setParam(m_teamNames[winner], m_currentScores[winner], color);
        god.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                god.dismiss();
                SDGameActivity.this.finish();
            }
        });
    }

    @Override
    public void onBackPressed() 
    {
        final ConfirmationDialog cd = new ConfirmationDialog(SDGameActivity.this);
        cd.show();
        cd.setParam("Exit", "Are you sure you want to exit?");
        cd.setYesOnClickListener(new OnClickListener()
        {
             public void onClick(View v) 
             {
                 cd.dismiss();
                 SDGameActivity.super.onBackPressed();
             }
        });
        cd.setNoOnClickListener(new OnClickListener()
        {
             public void onClick(View v) 
             {
                 cd.dismiss();
             }
        });
    }

    private void initUI()
    {
        m_mainWordTextView = (LetterSpacingTextView) findViewById(R.id.text_view_word_to_guess);
        m_drawView = (DrawingView) findViewById(R.id.drawing);
        m_colorStrip = (LinearLayout) findViewById(R.id.paint_colors);
        m_currentRoundTextView = (TextView) findViewById(R.id.text_view_current_round);
        m_currentPaint = (ImageButton) (m_colorStrip).getChildAt(0);
        m_currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.color_button_pressed));
        m_finishBtn = (Button) findViewById(R.id.button_finish_drawing);
        m_undoBtn = (Button) findViewById(R.id.button_undo);
        m_globalCountDownTextView = (TextView) findViewById(R.id.text_view_global_count_down);
        m_countDownTextView = (TextView) findViewById(R.id.text_view_count_down);
        m_guesserEditText = (EditText) findViewById(R.id.edit_text_guesser);
        m_paletteFlipper = (ViewFlipper) findViewById(R.id.palette_flipper);

        Animation inAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_in_left);
        Animation outAnim = AnimationUtils.loadAnimation(this, android.R.anim.slide_out_right);
        m_paletteFlipper.setInAnimation(inAnim);
        m_paletteFlipper.setOutAnimation(outAnim);
    }

    private void initListeners()
    {
        m_finishBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final ConfirmationDialog cd = new ConfirmationDialog(SDGameActivity.this);
                cd.show();
                cd.setCancelable(true);
                cd.setParam("Finish", "Are you sure you want to finish?");
                cd.setYesOnClickListener(new OnClickListener()
                {
                     public void onClick(View v) 
                     {
                         cd.dismiss();
                         m_timer.cancel();
                         prepareGuessingPhase();
                     }
                });
                cd.setNoOnClickListener(new OnClickListener()
                {
                     public void onClick(View v) 
                     {
                         cd.dismiss();
                     }
                });
            }
        });

        m_undoBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_drawView.undo();
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

    public void brushClicked(View view)
    {
        Toast.makeText(SDGameActivity.this, "Hello from brushes!", Toast.LENGTH_SHORT).show();
    }

    public void paintClicked(View view)
    {
        if (view != m_currentPaint)
        {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            m_drawView.setColor(color);

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.color_button_pressed));
            m_currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.color_button_normal));
            m_currentPaint = (ImageButton) view;
        }
    }
}
