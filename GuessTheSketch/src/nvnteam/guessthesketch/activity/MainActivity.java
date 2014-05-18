package nvnteam.guessthesketch.activity;


import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.bluetooth.BTGameActivity;
import nvnteam.guessthesketch.util.FontUtils;
import nvnteam.guessthesketch.util.FontUtils.FontType;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ViewFlipper;

public class MainActivity extends FullScreenActivity
{
    private Button m_newGameBtn;
    private Button m_freeDrawingBtn;
    private Button m_tutorialBtn;
    private Button m_highScoresBtn;
    private Button m_quitBtn;

    private Button m_singleDeviceBtn;
    private Button m_viaBluetoothBtn;
    private Button m_backBtn;

    private LinearLayout m_mainLayout;
    private LinearLayout m_playLayout;

    private ViewFlipper m_menuFlipper;

    private View m_gamelogo;
    private View m_upperLeft;
    private View m_lowerLeft;
    private View m_devLogo;

    private Animation m_scaleIn;
    private Animation m_fadeIn;
    private Animation m_flipperInAnim;
    private Animation m_flipperOutAnim;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initUI();
        initListeners();
        

        Typeface tf = FontUtils.getTypeface(this, FontType.VILLA);
        m_singleDeviceBtn.setTypeface(tf);
        m_viaBluetoothBtn.setTypeface(tf);
        m_backBtn.setTypeface(tf);
        m_newGameBtn.setTypeface(tf);
        m_freeDrawingBtn.setTypeface(tf);
        m_tutorialBtn.setTypeface(tf);
        m_highScoresBtn.setTypeface(tf);
        m_quitBtn.setTypeface(tf);

        m_mainLayout.startAnimation(m_scaleIn);
        m_gamelogo.startAnimation(m_scaleIn);
        m_devLogo.startAnimation(m_fadeIn);
        m_upperLeft.startAnimation(m_fadeIn);
        m_lowerLeft.startAnimation(m_fadeIn);
    }

    private void initUI()
    {
        m_newGameBtn = (Button) findViewById(R.id.button_main_menu_new_game);
        m_freeDrawingBtn = (Button) findViewById(R.id.button_main_menu_free_drawing);
        m_tutorialBtn = (Button) findViewById(R.id.button_main_menu_tutorial);
        m_highScoresBtn = (Button) findViewById(R.id.button_main_menu_high_scores);
        m_quitBtn = (Button) findViewById(R.id.button_main_menu_quit);
        m_singleDeviceBtn = (Button) findViewById(R.id.button_single_device);
        m_viaBluetoothBtn = (Button) findViewById(R.id.button_via_bluetooth);
        m_backBtn = (Button) findViewById(R.id.button_back_play_menu);

        m_mainLayout = (LinearLayout) findViewById(R.id.linear_layout_main_menu);
        m_playLayout = (LinearLayout) findViewById(R.id.linear_layout_play_menu);
        m_menuFlipper = (ViewFlipper) findViewById(R.id.menu_flipper);

        m_gamelogo = findViewById(R.id.image_view_logo_game);
        m_devLogo = findViewById(R.id.image_view_logo_team);
        m_upperLeft = findViewById(R.id.image_view_upper_left);
        m_lowerLeft = findViewById(R.id.image_view_lower_left);

        m_scaleIn = AnimationUtils.loadAnimation(this, R.anim.scale_in);
        m_fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        
        m_flipperInAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        m_flipperOutAnim = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        m_menuFlipper.setInAnimation(m_flipperInAnim);
        m_menuFlipper.setOutAnimation(m_flipperOutAnim);

        Typeface tf = FontUtils.getTypeface(this, FontType.VILLA);
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
        m_newGameBtn.setOnClickListener(new OnClickListener()
        {
           @Override
           public void onClick(View v)
           {
               m_menuFlipper.showNext();
               /*m_mainLayout.startAnimation(m_scaleOut);
               m_mainLayout.setVisibility(View.GONE);
               m_scaleIn.setStartOffset(m_scaleOut.getDuration());
               m_playLayout.startAnimation(m_scaleIn);
               m_playLayout.setVisibility(View.VISIBLE);
               m_scaleIn.setStartOffset(0);*//*
               m_mainLayout.animate().alpha(0).withEndAction(new Runnable()
               {
                   public void run()
                   {
                       ViewGroup vg = (ViewGroup)m_mainLayout.getParent();
                       vg.removeView(m_mainLayout);
                       vg.addView(m_playLayout);
                       m_playLayout.animate().alpha(1);
                   }
               }); */
           }
        });

        m_backBtn.setOnClickListener(new OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                m_menuFlipper.showPrevious();
                /*m_playLayout.startAnimation(m_scaleOut);
                m_playLayout.setVisibility(View.GONE);
                m_scaleIn.setStartOffset(m_scaleOut.getDuration());
                m_mainLayout.startAnimation(m_scaleIn);
                m_mainLayout.setVisibility(View.VISIBLE);
                m_scaleIn.setStartOffset(0);*//*
                m_playLayout.animate().alpha(0).withEndAction(new Runnable()
                {
                    public void run()
                    {
                        ViewGroup vg = (ViewGroup)m_playLayout.getParent();
                        vg.removeView(m_playLayout);
                        vg.addView(m_mainLayout);
                        m_mainLayout.animate().alpha(1);
                    }
                }); */
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
           {
           }
        });
    }
}
