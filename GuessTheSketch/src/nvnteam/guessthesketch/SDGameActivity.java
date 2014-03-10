package nvnteam.guessthesketch;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

public class SDGameActivity extends Activity 
{
    private String m_teamOne;
    private String m_teamTwo;
    
    private DrawingView m_drawView;
    private ImageButton m_currPaint;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sd_game);

        m_teamOne = getIntent().getExtras().getString("teamOneName");
        m_teamTwo = getIntent().getExtras().getString("teamTwoName");
        
    }

    public void paintClicked(View view){
        //use chosen color

        //set erase false
        m_drawView.setErase(false);
        m_drawView.setBrushSize(m_drawView.getLastBrushSize());

        if(view != m_currPaint){
            ImageButton imgView = (ImageButton)view;
            String color = view.getTag().toString();
            m_drawView.setColor(color);
            //update ui
            imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
            m_currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
            m_currPaint = (ImageButton)view;
        }
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
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);}
        }
}
