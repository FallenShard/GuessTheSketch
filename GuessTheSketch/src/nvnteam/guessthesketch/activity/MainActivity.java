package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends Activity 
{
    ImageButton m_playBtn;
    ImageButton m_freeDrawingBtn;
    ImageButton m_tutorialBtn;
    ImageButton m_highScoresBtn;
    ImageButton m_quitBtn;
    
    Button m_singleDeviceBtn;
    Button m_viaBluetoothBtn;
    Button m_backBtn;
    
    View m_mainLayout;
    View m_playLayout;
    View m_logo;
    
    View m_upperLeft;
    View m_lowerLeft;
    View m_devLogo;
    
    Animation m_scaleIn;
    Animation m_scaleOut;
    Animation m_fadeIn;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/goodfoot.ttf");

        m_playBtn = (ImageButton) findViewById(R.id.button_play);
        m_freeDrawingBtn = (ImageButton) findViewById(R.id.button_free_drawing);
        m_tutorialBtn = (ImageButton) findViewById(R.id.button_tutorial);
        m_highScoresBtn = (ImageButton) findViewById(R.id.button_high_scores);
        m_quitBtn = (ImageButton) findViewById(R.id.button_quit);
        m_singleDeviceBtn = (Button) findViewById(R.id.button_single_device);
        m_viaBluetoothBtn = (Button) findViewById(R.id.button_via_bluetooth);
        m_backBtn = (Button) findViewById(R.id.button_back_play_menu);
        
        m_mainLayout = findViewById(R.id.main_menu_linear_layout);
        m_playLayout = findViewById(R.id.play_menu_linear_layout);
        m_logo = findViewById(R.id.logo_view);
        
        m_devLogo = findViewById(R.id.dev_logo_view);
        m_upperLeft = findViewById(R.id.upper_left_view);
        m_lowerLeft = findViewById(R.id.lower_left_view);
        
        m_scaleOut = AnimationUtils.loadAnimation(this, R.anim.scale_out_anim);
        m_scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in_anim);
        m_fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in_anim);

        m_singleDeviceBtn.setTypeface(tf);
        m_viaBluetoothBtn.setTypeface(tf);
        m_backBtn.setTypeface(tf);

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
        
        m_playBtn.setOnClickListener(new OnClickListener()
        {
           @Override
           public void onClick(View v)
           {
               m_mainLayout.startAnimation(m_scaleOut);
               m_mainLayout.setVisibility(View.GONE);
               m_scaleIn.setStartOffset(m_scaleOut.getDuration());
               m_playLayout.startAnimation(m_scaleIn);
               m_playLayout.setVisibility(View.VISIBLE);
               m_scaleIn.setStartOffset(0);
           }
        });
        
        m_backBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_playLayout.startAnimation(m_scaleOut);
                m_playLayout.setVisibility(View.GONE);
                m_scaleIn.setStartOffset(m_scaleOut.getDuration());
                m_mainLayout.startAnimation(m_scaleIn);
                m_mainLayout.setVisibility(View.VISIBLE);
                m_scaleIn.setStartOffset(0);
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
        m_mainLayout.startAnimation(m_scaleIn);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        
        m_logo.startAnimation(m_scaleIn);
        m_devLogo.startAnimation(m_fadeIn);
        m_upperLeft.startAnimation(m_fadeIn);
        m_lowerLeft.startAnimation(m_fadeIn);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
            super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
    }
}
