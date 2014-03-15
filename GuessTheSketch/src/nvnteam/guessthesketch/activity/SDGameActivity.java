package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.WordBase;
import nvnteam.guessthesketch.widget.DrawingView;
import android.app.Activity;
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

public class SDGameActivity extends Activity implements OnClickListener
{
    private String[] m_teamNames;
    public static String LOG_TAG = "DEBUGGING";

    private DrawingView m_drawView;
    private ImageButton m_currPaint;
    private TextView m_textView;
    private Button m_finishButton;
    private TextView m_textViewCountDown;
    private LinearLayout m_colorLayout;
    private EditText m_guesserText;
    
    private String m_currentWord = new String("PLACEHOLDER");
    private int m_wordPoints = 0;
    private int m_baseFactor = 5;
    private int m_difficultyFactor = 3;
    
    private enum State { Picking, Drawing, Guessing, Paused };
    
    private int m_currentTurn = 0;
    private static int MaxTeams = 2;
    
    private int[] m_currentPoints = {0, 0};
    
    private State m_gameState = State.Picking;
    private long m_currentTimeLeft = 60000;
    
    private long m_currentTime = 0;
    private Animation m_scaleOut;
    private Animation m_scaleIn;

    private CountDownTimer m_timer = new CountDownTimer(60000, 100)
    {

        public void onTick(long millisUntilFinished) 
        {
            m_textViewCountDown.setText(Long.toString(millisUntilFinished / 1000));
            m_currentTimeLeft = millisUntilFinished;
            m_drawView.pullTimeStamp(millisUntilFinished);
        }

        public void onFinish() 
        {
            m_currentTimeLeft = 0;
            if (m_gameState == State.Drawing)
                popUpGuesserTurn();
            else if (m_gameState == State.Guessing)
                popUpEvaluator();
            else if (m_gameState == State.Picking)
                popUpWordPicker();
        }
    };
    
    @Override
    public void onClick(View v)
    {
        
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sd_game);

        m_scaleOut = AnimationUtils.loadAnimation(this, R.anim.scale_out_anim);
        m_scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in_anim);
        
        m_teamNames = new String[2];
        m_teamNames[0] = getIntent().getStringExtra("teamOneName");
        m_teamNames[1] = getIntent().getStringExtra("teamTwoName");
        
        m_textView = (TextView) findViewById(R.id.text_view_word_to_guess);
        m_drawView = (DrawingView) findViewById(R.id.drawing);
        
        m_colorLayout = (LinearLayout) findViewById(R.id.paint_colors);
        m_currPaint = (ImageButton) (m_colorLayout).getChildAt(0);
        m_currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
        
        m_finishButton = (Button) findViewById(R.id.button_finish_drawing);
        m_finishButton.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_timer.cancel();
                popUpGuesserTurn();
            }
        });

        m_textViewCountDown = (TextView) findViewById(R.id.text_view_count_down);
        
        m_guesserText = (EditText) findViewById(R.id.text_view_guesser);
        m_guesserText.setInputType(m_guesserText.getInputType()
                | EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                | EditorInfo.TYPE_TEXT_VARIATION_FILTER);
        m_guesserText.setOnEditorActionListener(new OnEditorActionListener()
        {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event)
            {
                if (actionId == EditorInfo.IME_ACTION_DONE) 
                   if (v.getText().toString().compareToIgnoreCase(m_currentWord) == 0)
                   {
                       cleanUpGuessingPhase();
                   }
                   else
                       v.setText("");
                return true;
            }
        });
    }

    public void paintClicked(View view)
    {
        //set erase false
        m_drawView.setErase(false);
        m_drawView.setBrushSize(m_drawView.getLastBrushSize());

        if(view != m_currPaint)
        {
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            m_drawView.setColor(color);
            //update ui
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            m_currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            m_currPaint = (ImageButton)view;
        }
    }

    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (m_gameState == State.Picking)
            popUpWordPicker();
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
    
    public void popUpWordPicker()
    {
        final String words[] ={ WordBase.getEasyWord(),
                                WordBase.getMediumWord(),
                                WordBase.getHardWord(),
                                WordBase.getReallyHardWord() };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Pick a Word!");
        builder.setItems(words, new DialogInterface.OnClickListener() 
        {
            public void onClick(DialogInterface dialog, int item)
            {
                m_drawView.startNew();
                m_currentWord = words[item];
                StringBuffer outputBuffer = new StringBuffer(m_currentWord.length());
                for (int i = 0; i < m_currentWord.length(); i++)
                {
                   if (m_currentWord.charAt(i) != ' ')
                       outputBuffer.append('_');
                   else
                       outputBuffer.append(' ');
                }
                m_textView.setText(outputBuffer.toString());
                m_wordPoints = m_baseFactor + m_difficultyFactor * item;
                Toast.makeText(SDGameActivity.this, words[item], Toast.LENGTH_SHORT).show();
                m_gameState = State.Drawing;
                m_timer.start();
                m_currentTime = SystemClock.uptimeMillis();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    public void popUpEvaluator()
    {
        handleEvaluation();
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("T1: " + m_currentPoints[0] + " T2: " + m_currentPoints[1])
               .setCancelable(false)
               .setPositiveButton(m_teamNames[m_currentTurn] + "'s turn", new DialogInterface.OnClickListener() 
               {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                       m_drawView.startNew();
                       m_guesserText.startAnimation(m_scaleOut);
                       m_guesserText.setVisibility(View.GONE);
                       m_scaleIn.setStartOffset(m_scaleOut.getDuration());
                       m_colorLayout.startAnimation(m_scaleIn);
                       m_colorLayout.setVisibility(View.VISIBLE);
                       m_scaleIn.setStartOffset(0);
                       m_gameState = State.Picking;
                       m_timer.start();
                       popUpWordPicker();
                   }
               });
        AlertDialog alert = builder.create();
        alert.show();
    }
    
    public void popUpGuesserTurn()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Prepare to guess")
               .setCancelable(false)
               .setPositiveButton("OK", new DialogInterface.OnClickListener() 
               {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                       m_drawView.startNew();
                       m_colorLayout.startAnimation(m_scaleOut);
                       m_colorLayout.setVisibility(View.GONE);
                       m_scaleIn.setStartOffset(m_scaleOut.getDuration());
                       m_guesserText.startAnimation(m_scaleIn);
                       m_guesserText.setVisibility(View.VISIBLE);
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
    
    public void handleEvaluation()
    {
        m_currentPoints[m_currentTurn] += m_wordPoints;
        m_currentTurn = (m_currentTurn + 1) % MaxTeams;
    }
    
    public void cleanUpGuessingPhase()
    {
        m_timer.cancel();
        m_textView.setText(m_currentWord);
        m_guesserText.setText("");
        m_drawView.setPlayback(false);
        InputMethodManager inputManager = (InputMethodManager)
        SDGameActivity.this.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.toggleSoftInput(0, 0);
        popUpEvaluator();
    }
    
    @Override
    public void onStop()
    {
        super.onStop();
        m_timer.cancel();
    }
}
