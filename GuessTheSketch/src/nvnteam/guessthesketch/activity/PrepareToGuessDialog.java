package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class PrepareToGuessDialog extends AlertDialog 
{
    Context m_context;

    TextView m_title;
    TextView m_info;
    Button m_button;

    protected PrepareToGuessDialog(Context context) 
    {
        super(context);
        m_context = context;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_prepare_to_guess);

        setCancelable(false);
        m_button = (Button) findViewById(R.id.dialog_guessing_button);
        m_title = (TextView) findViewById(R.id.dialog_guessing_title);
        m_info = (TextView) findViewById(R.id.dialog_guessing_text);

        Typeface tf = Typeface.createFromAsset(m_context.getAssets(), "fonts/Villa.ttf");
        m_button.setTypeface(tf);
        m_title.setTypeface(tf);

        tf = Typeface.createFromAsset(m_context.getAssets(), "fonts/Segoe.ttf");
        m_info.setTypeface(tf);
    }

    public void setOnClickListener(View.OnClickListener lsnr)
    {
        m_button.setOnClickListener(lsnr);
    }
}
