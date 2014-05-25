package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ConfirmationDialog extends AlertDialog 
{
    Context m_context;

    TextView m_title;
    TextView m_info;
    Button m_yesButton;
    Button m_nobutton;

    protected ConfirmationDialog(Context context) 
    {
        super(context);
        m_context = context;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_confirm);

        setCancelable(false);
        m_yesButton = (Button) findViewById(R.id.dialog_confirm_button_ok);
        m_nobutton = (Button) findViewById(R.id.dialog_confirm_button_cancel);
        m_title = (TextView) findViewById(R.id.dialog_confirm_title);
        m_info = (TextView) findViewById(R.id.dialog_confirm_text);

        Typeface tf = Typeface.createFromAsset(m_context.getAssets(), "fonts/Villa.ttf");
        m_yesButton.setTypeface(tf);
        m_nobutton.setTypeface(tf);
        m_title.setTypeface(tf);

        tf = Typeface.createFromAsset(m_context.getAssets(), "fonts/Segoe.ttf");
        m_info.setTypeface(tf);
    }

    public void setParam(String title, String message)
    {
        m_title.setText(title);
        m_info.setText(message);
    }

    public void setYesOnClickListener(View.OnClickListener lsnr)
    {
        m_yesButton.setOnClickListener(lsnr);
    }

    public void setNoOnClickListener(View.OnClickListener lsnr)
    {
        m_nobutton.setOnClickListener(lsnr);
    }
}
