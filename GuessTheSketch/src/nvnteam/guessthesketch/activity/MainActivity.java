package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.R.anim;
import nvnteam.guessthesketch.R.id;
import nvnteam.guessthesketch.R.layout;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

public class MainActivity extends Activity 
{
    Button m_playBtn;
    Button m_freeDrawingBtn;
    Button m_tutorialBtn;
    Button m_highScoresBtn;
    Button m_quitBtn;
    
    Button m_singleDeviceBtn;
    Button m_viaBluetoothBtn;
    Button m_backBtn;
    
    View m_mainLayout;
    View m_playLayout;
    View m_logo;
    
    Animation m_scaleIn;
    Animation m_scaleOut;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/goodfoot.ttf");

        m_playBtn = (Button) findViewById(R.id.button_play);
        m_freeDrawingBtn = (Button) findViewById(R.id.button_free_drawing);
        m_tutorialBtn = (Button) findViewById(R.id.button_tutorial);
        m_highScoresBtn = (Button) findViewById(R.id.button_high_scores);
        m_quitBtn = (Button) findViewById(R.id.button_quit);
        m_singleDeviceBtn = (Button) findViewById(R.id.button_single_device);
        m_viaBluetoothBtn = (Button) findViewById(R.id.button_via_bluetooth);
        m_backBtn = (Button) findViewById(R.id.button_back_play_menu);
        
        m_mainLayout = findViewById(R.id.main_menu_linear_layout);
        m_playLayout = findViewById(R.id.play_menu_linear_layout);
        m_logo = findViewById(R.id.logo_view);
        
        m_scaleOut = AnimationUtils.loadAnimation(this, R.anim.scale_out_anim);
        m_scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in_anim);

        m_playBtn.setTypeface(tf);
        m_highScoresBtn.setTypeface(tf);
        m_tutorialBtn.setTypeface(tf);
        m_freeDrawingBtn.setTypeface(tf);
        m_quitBtn.setTypeface(tf);
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
