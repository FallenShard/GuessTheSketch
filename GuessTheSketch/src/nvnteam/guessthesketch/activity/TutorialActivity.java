package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ViewFlipper;

public class TutorialActivity extends FullScreenActivity 
{
    private ViewFlipper m_rootFlipper;
    private ViewFlipper m_activeFlipper;
    private ViewFlipper m_basicFlipper;
    private ViewFlipper m_advancedFlipper;
    
    private Button m_rightButton;
    private Button m_leftButton;
    private Button m_changerButton;
    private Button m_backButton;
    
    private TextView m_subTitleTextView;
    private TextView m_pageNumber;
    

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);
        initUI();
        initListeners();
    }
    
    protected void onStart()
    {
        super.onStart();
        adjustPageText();

        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        m_rootFlipper.setInAnimation(fadeIn);
        m_rootFlipper.setOutAnimation(fadeOut);
        m_basicFlipper.setInAnimation(fadeIn);
        m_basicFlipper.setOutAnimation(fadeOut);
        m_advancedFlipper.setInAnimation(fadeIn);
        m_advancedFlipper.setOutAnimation(fadeOut);
        m_changerButton.setText("Advanced");
    }

    private void initUI()
    {
        m_rootFlipper = (ViewFlipper) findViewById(R.id.root_flipper);
        m_basicFlipper = (ViewFlipper) findViewById(R.id.basic_flipper);
        m_advancedFlipper = (ViewFlipper) findViewById(R.id.advanced_flipper);
        m_activeFlipper = m_basicFlipper;

        m_rightButton = (Button) findViewById(R.id.btn_right);
        m_leftButton = (Button) findViewById(R.id.btn_left);
        m_changerButton = (Button) findViewById(R.id.button_changer);
        m_backButton = (Button) findViewById(R.id.button_back_tutorial);

        m_subTitleTextView = (TextView) findViewById(R.id.text_view_tutorial_page_title);
        m_pageNumber = (TextView) findViewById(R.id.text_view_tutorial_page_number);

        TextView title = (TextView) findViewById(R.id.text_view_tutorial);

        Typeface tf = Typeface.createFromAsset(getAssets(), "fonts/Villa.ttf");
        m_rightButton.setTypeface(tf);
        m_leftButton.setTypeface(tf);
        m_changerButton.setTypeface(tf);
        m_backButton.setTypeface(tf);
        title.setTypeface(tf);

        tf = Typeface.createFromAsset(getAssets(), "fonts/Segoe.ttf");
        m_subTitleTextView.setTypeface(tf);
        m_pageNumber.setTypeface(tf);

        TextView[] views = new TextView[10];
        views[0] = (TextView) findViewById(R.id.text_view_basic_1);
        views[1] = (TextView) findViewById(R.id.text_view_basic_2);
        views[2] = (TextView) findViewById(R.id.text_view_basic_3);
        views[3] = (TextView) findViewById(R.id.text_view_basic_4);
        views[4] = (TextView) findViewById(R.id.text_view_basic_5);
        views[5] = (TextView) findViewById(R.id.text_view_basic_6);
        views[6] = (TextView) findViewById(R.id.text_view_basic_7);
        views[7] = (TextView) findViewById(R.id.text_view_basic_8);
        views[8] = (TextView) findViewById(R.id.text_view_basic_9);
        views[9] = (TextView) findViewById(R.id.text_view_advanced_1);
        for (TextView v : views)
        {
            v.setTypeface(tf);
        }
    }

    private void initListeners()
    {
        m_rightButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                m_activeFlipper.showNext();
                adjustPageText();
                m_subTitleTextView.setText(m_activeFlipper.getCurrentView().getTag().toString());
            }
        });

        m_leftButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                m_activeFlipper.showPrevious();
                adjustPageText();
            }
        });

        m_changerButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                m_rootFlipper.showNext();
                if (m_activeFlipper == m_basicFlipper)
                {
                    m_activeFlipper = m_advancedFlipper;
                    m_changerButton.setText("Basic");
                }
                else
                {
                    m_activeFlipper = m_basicFlipper;
                    m_changerButton.setText("Advanced");
                }
                adjustPageText();
            }
        });

        m_backButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                TutorialActivity.this.finish();
            }
        });
    }

    private void adjustPageText()
    {
        m_pageNumber.setText("" + (m_activeFlipper.getDisplayedChild() + 1) + "/" + m_activeFlipper.getChildCount());
        m_subTitleTextView.setText(m_activeFlipper.getCurrentView().getTag().toString());
    }
}
