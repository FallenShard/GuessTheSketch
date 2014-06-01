package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.util.GTSUtils;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class WordPickerDialog extends AlertDialog 
{
    Context m_context;

    private TextView m_title;
    private TextView m_words[] = new TextView[4];
    private TextView m_modifiers[] = new TextView[4];
    private float m_rawModifiers[] = new float[4];
    private Button m_button;
    private String m_selectedWord = "";
    private int m_selectedIndex = -1;

    public WordPickerDialog(Context context) 
    {
        super(context);
        m_context = context;
    }

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_word_picker);

        setCancelable(false);
        m_button = (Button) findViewById(R.id.dialog_word_picker_button);
        m_title = (TextView) findViewById(R.id.dialog_word_picker_title);
        m_words[0] = (TextView) findViewById(R.id.dialog_word_picker_easy_word);
        m_words[1] = (TextView) findViewById(R.id.dialog_word_picker_medium_word);
        m_words[2] = (TextView) findViewById(R.id.dialog_word_picker_hard_word);
        m_words[3] = (TextView) findViewById(R.id.dialog_word_picker_very_hard_word);

        m_modifiers[0] = (TextView) findViewById(R.id.dialog_word_picker_easy_modifier);
        m_modifiers[1] = (TextView) findViewById(R.id.dialog_word_picker_medium_modifier);
        m_modifiers[2] = (TextView) findViewById(R.id.dialog_word_picker_hard_modifier);
        m_modifiers[3] = (TextView) findViewById(R.id.dialog_word_picker_very_hard_modifier);

        Typeface tf = Typeface.createFromAsset(m_context.getAssets(), "fonts/Villa.ttf");
        m_button.setTypeface(tf);
        m_title.setTypeface(tf);
        m_modifiers[0].setTypeface(tf);
        m_modifiers[1].setTypeface(tf);
        m_modifiers[2].setTypeface(tf);
        m_modifiers[3].setTypeface(tf);

        tf = Typeface.createFromAsset(m_context.getAssets(), "fonts/Segoe.ttf");
        m_words[0].setTypeface(tf);
        m_words[1].setTypeface(tf);
        m_words[2].setTypeface(tf);
        m_words[3].setTypeface(tf);

        for (int k = 0; k < 4; k++)
            m_words[k].setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v) 
                {
                    for (int i = 0; i < 4; i++)
                    {
                        if (v.getId() == m_words[i].getId())
                        {
                            m_words[i].setTextColor(0xFFFFFF00);
                            m_selectedWord = m_words[i].getText().toString();
                            m_selectedIndex = i;
                        }
                        else
                            m_words[i].setTextColor(0xFFFFFFFF);
                    }
                }
            });
        
        for (int i = 0; i < 4; i++)
        {
            float randVal = GTSUtils.round((float) Math.random(), 1);
            m_rawModifiers[i] = randVal + 1;
            m_modifiers[i].setText("" + m_rawModifiers[i] + "x");

            int redComp = (int)((1 - randVal) * 255) << 16;
            int blueComp = (int)((1 - randVal) * 255);
            m_modifiers[i].setTextColor(0xFF00FF00 | redComp | blueComp);
        }
    }

    public void setOnClickListener(View.OnClickListener lsnr)
    {
        m_button.setOnClickListener(lsnr);
    }

    public String getSelectedWord()
    {
        return m_selectedWord;
    }

    public int getSelectedIndex()
    {
        return m_selectedIndex;
    }

    public float getSelectedModifier()
    {
        return m_rawModifiers[m_selectedIndex];
    }

    public void setWords(String[] words)
    {
        String[] localWords = new String[4];
        for (int i = 0; i < words.length; i++)
            localWords[i] = new String(words[i]);

        for (int i = 0; i < words.length; i++)
            m_words[i].setText(localWords[i]);
    }
}
