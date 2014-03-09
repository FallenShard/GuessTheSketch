package nvnteam.guessthesketch;

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
    Button m_highScoresBtn;
    Button m_quitBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) 
    {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/goodfoot.ttf");

        m_playBtn = (Button) findViewById(R.id.button_play);
        m_playBtn.setTypeface(tf);

        m_highScoresBtn = (Button) findViewById(R.id.button_high_scores);
        m_highScoresBtn.setTypeface(tf);

        m_freeDrawingBtn = (Button) findViewById(R.id.button_free_drawing);
        m_freeDrawingBtn.setTypeface(tf);

        m_quitBtn = (Button) findViewById(R.id.button_quit);
        m_quitBtn.setTypeface(tf);

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

    @Override
    protected void onStart()
    {
        super.onStart();
        final View l1 = findViewById(R.id.main_menu_linear_layout);
        final View l2 = findViewById(R.id.logo_view);
        final Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.layout_fade_in);
        final Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_anim);
        l1.startAnimation(fadeOut);
        l2.startAnimation(logoAnim);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                      | View.SYSTEM_UI_FLAG_FULLSCREEN
                      | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        getWindow().getDecorView().setSystemUiVisibility(uiOptions);
    }

}
