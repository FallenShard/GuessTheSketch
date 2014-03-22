package nvnteam.guessthesketch.widget;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ScaleXSpan;
import android.util.AttributeSet;
import android.widget.TextView;

public class LetterSpacingTextView extends TextView 
{
    private float m_letterSpacing = LetterSpacing.NORMAL;
    private CharSequence m_originalText = "";


    public LetterSpacingTextView(Context context)
    {
        super(context);
    }

    public LetterSpacingTextView(Context context, AttributeSet attrs)
    {
        super(context, attrs);
    }

    public LetterSpacingTextView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);
    }

    public float getLetterSpacing()
    {
        return m_letterSpacing;
    }

    public void setLetterSpacing(float letterSpacing)
    {
        this.m_letterSpacing = letterSpacing;
        applyLetterSpacing();
    }

    @Override
    public void setText(CharSequence text, BufferType type)
    {
        m_originalText = text;
        applyLetterSpacing();
    }

    @Override
    public CharSequence getText()
    {
        return m_originalText;
    }

    private void applyLetterSpacing()
    {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < m_originalText.length(); i++)
        {
            builder.append(m_originalText.charAt(i));
            if (i + 1 < m_originalText.length())
                builder.append("\u00A0");
        }

        SpannableString finalText = new SpannableString(builder.toString());
        if (builder.toString().length() > 1)
            for (int i = 1; i < builder.toString().length(); i+=2)
                finalText.setSpan(new ScaleXSpan((m_letterSpacing + 1) / 10), 
                                  i, i+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        super.setText(finalText, BufferType.SPANNABLE);
    }

    public class LetterSpacing
    {
        public final static float NORMAL = 0;
    }
}