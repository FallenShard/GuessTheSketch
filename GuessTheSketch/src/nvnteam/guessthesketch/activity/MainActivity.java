package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;

public class MainActivity extends FullScreenActivity
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
    View m_gamelogo;

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
        initUI();
        initListeners();

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/goodfoot.ttf");
        m_singleDeviceBtn.setTypeface(tf);
        m_viaBluetoothBtn.setTypeface(tf);
        m_backBtn.setTypeface(tf);

        m_mainLayout.startAnimation(m_scaleIn);
        m_gamelogo.startAnimation(m_scaleIn);
        m_devLogo.startAnimation(m_fadeIn);
        m_upperLeft.startAnimation(m_fadeIn);
        m_lowerLeft.startAnimation(m_fadeIn);
    }

    private void initUI()
    {
        m_playBtn = (ImageButton) findViewById(R.id.button_main_menu_play);
        m_freeDrawingBtn = (ImageButton) findViewById(R.id.button_main_menu_free_drawing);
        m_tutorialBtn = (ImageButton) findViewById(R.id.button_main_menu_tutorial);
        m_highScoresBtn = (ImageButton) findViewById(R.id.button_main_menu_high_scores);
        m_quitBtn = (ImageButton) findViewById(R.id.button_main_menu_quit);
        m_singleDeviceBtn = (Button) findViewById(R.id.button_single_device);
        m_viaBluetoothBtn = (Button) findViewById(R.id.button_via_bluetooth);
        m_backBtn = (Button) findViewById(R.id.button_back_play_menu);

        m_mainLayout = findViewById(R.id.linear_layout_main_menu);
        m_playLayout = findViewById(R.id.linear_layout_play_menu);
        m_gamelogo = findViewById(R.id.image_view_logo_game);

        m_devLogo = findViewById(R.id.image_view_logo_team);
        m_upperLeft = findViewById(R.id.image_view_upper_left);
        m_lowerLeft = findViewById(R.id.image_view_lower_left);

        m_scaleOut = AnimationUtils.loadAnimation(this, R.anim.scale_out);
        m_scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        m_fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
    }

    private void initListeners()
    {
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
    }
}
