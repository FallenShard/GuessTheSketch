package nvnteam.guessthesketch.dialog;

import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.activity.SDGameActivity;
import nvnteam.guessthesketch.util.FontUtils;
import nvnteam.guessthesketch.util.FontUtils.FontType;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GuessingDialog extends Dialog
{
    public GuessingDialog(Context context) 
    {
        super(context);
        final SDGameActivity parent = (SDGameActivity) context;
        setContentView(R.layout.dialog_guessing);
        setCancelable(false);
        Typeface tf = FontUtils.getTypeface(context, FontType.MAIN_FONT);

        TextView title = (TextView) findViewById(R.id.dialog_guessing_title);
        TextView text = (TextView) findViewById(R.id.dialog_guessing_text);
        title.setTypeface(tf);
        text.setTypeface(tf);

        Button okButton = (Button) findViewById(R.id.dialog_guessing_button);
        okButton.setTypeface(tf);
        okButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) 
            {
                parent.startGuessingPhase();
                GuessingDialog.this.dismiss();
            }
        });
    }

    @Override
    public void dismiss() 
    {
        super.dismiss();
    }
}
