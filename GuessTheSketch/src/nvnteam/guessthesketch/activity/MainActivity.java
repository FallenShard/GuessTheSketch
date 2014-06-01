package nvnteam.guessthesketch.activity;


import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.bluetooth.BTGameActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ViewFlipper;

public class MainActivity extends FullScreenActivity
{
    // ViewFlipper to switch between main menu and new game menu
    private ViewFlipper m_menuFlipper;

    // GUI Button handlers to set up listeners
    private Button m_newGameBtn;
    private Button m_freeDrawingBtn;
    private Button m_tutorialBtn;
    private Button m_highScoresBtn;
    private Button m_quitBtn;

    private Button m_singleDeviceBtn;
    private Button m_viaBluetoothBtn;
    private Button m_backBtn;

    // GUI View handlers to start up animations
    private View m_gamelogo;
    private View m_upperLeft;
    private View m_lowerLeft;
    private View m_devLogo;

    // Animation handlers
    private Animation m_scaleIn;
    private Animation m_fadeIn;
    private Animation m_fadeOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        // Required by every life-cycle activity
        super.onCreate(savedInstanceState);

        // Main layout file for this activity
        setContentView(R.layout.activity_main);

        // Initializes GUI handlers
        initUI();

        // Initializes listeners for buttons
        initListeners();

        // Plays animation here, so that they are played only once
        m_menuFlipper.startAnimation(m_scaleIn);
        m_gamelogo.startAnimation(m_scaleIn);
        m_upperLeft.startAnimation(m_fadeIn);
        m_lowerLeft.startAnimation(m_fadeIn);
        m_devLogo.startAnimation(m_fadeIn);
    }


    private void initUI()
    {
        // Set up GUI handlers
        m_menuFlipper = (ViewFlipper) findViewById(R.id.menu_flipper);

        m_newGameBtn = (Button) findViewById(R.id.button_main_menu_new_game);
        m_freeDrawingBtn = (Button) findViewById(R.id.button_main_menu_free_drawing);
        m_tutorialBtn = (Button) findViewById(R.id.button_main_menu_tutorial);
        m_highScoresBtn = (Button) findViewById(R.id.button_main_menu_high_scores);
        m_quitBtn = (Button) findViewById(R.id.button_main_menu_quit);
        m_singleDeviceBtn = (Button) findViewById(R.id.button_single_device);
        m_viaBluetoothBtn = (Button) findViewById(R.id.button_via_bluetooth);
        m_backBtn = (Button) findViewById(R.id.button_back_play_menu);

        m_gamelogo = findViewById(R.id.image_view_logo_game);
        m_devLogo = findViewById(R.id.image_view_logo_team);
        m_upperLeft = findViewById(R.id.image_view_upper_left);
        m_lowerLeft = findViewById(R.id.image_view_lower_left);

        m_scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        m_fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        m_fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        m_menuFlipper.setInAnimation(m_fadeIn);
        m_menuFlipper.setOutAnimation(m_fadeOut);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Villa.ttf");
        m_singleDeviceBtn.setTypeface(tf);
        m_viaBluetoothBtn.setTypeface(tf);
        m_backBtn.setTypeface(tf);
        m_newGameBtn.setTypeface(tf);
        m_freeDrawingBtn.setTypeface(tf);
        m_tutorialBtn.setTypeface(tf);
        m_highScoresBtn.setTypeface(tf);
        m_quitBtn.setTypeface(tf);
    }

    private void initListeners()
    {
        // Sets up listeners for buttons
        m_newGameBtn.setOnClickListener(new OnClickListener()
        {
           @Override
           public void onClick(View v)
           {
               m_menuFlipper.showNext();
           }
        });

        m_backBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_menuFlipper.showPrevious();
            }
         });

        m_singleDeviceBtn.setOnClickListener(new OnClickListener()
        {
           @Override
           public void onClick(View v)
           {
               Intent intent = new Intent(MainActivity.this,
                                          SDPreGameActivity.class);
               startActivity(intent);
           }
        });

        m_freeDrawingBtn.setOnClickListener(new OnClickListener() 
        {
            @Override
            public void onClick(View v) 
            {
                Intent intent = new Intent(MainActivity.this, 
                                           DrawingActivity.class);
                startActivity(intent);
            }
        });

        m_quitBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });

        m_viaBluetoothBtn.setOnClickListener(new OnClickListener()
        {
           @Override
           public void onClick(View v)
           {
               Intent intent = new Intent(MainActivity.this,
                                          BTGameActivity.class);
               startActivity(intent);
           }
        });

        m_tutorialBtn.setOnClickListener(new OnClickListener()
        {
           public void onClick(View v)
           {/*
               SharedPreferences settings = MainActivity.this.getSharedPreferences("RoundPrefs", Context.MODE_PRIVATE);
               settings.edit().clear().commit();
               settings = MainActivity.this.getSharedPreferences("AvgPrefs", Context.MODE_PRIVATE);
               settings.edit().clear().commit();
               settings = MainActivity.this.getSharedPreferences("FivePrefs", Context.MODE_PRIVATE);
               settings.edit().clear().commit();*/
           }
        });

        m_highScoresBtn.setOnClickListener(new OnClickListener()
        {
           public void onClick(View v)
           {
               Intent intent = new Intent(MainActivity.this,
                       HighScoreActivity.class);
               startActivity(intent);
           }
        });
    }
}
