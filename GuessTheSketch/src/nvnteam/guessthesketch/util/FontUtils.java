package nvnteam.guessthesketch.util;

import android.content.Context;
import android.graphics.Typeface;

public class FontUtils 
{

    private static Typeface m_titleFont;

    public static enum FontType
    {
        VILLA
        {
            public String toString() 
            {
                return "Villa.ttf";
            }
        },

        SEGOE
        {
            public String toString() 
            {
                return "Segoe.ttf";
            }
        }
    }

    public static Typeface getTypeface(Context context, String typefaceName) 
    {
        Typeface typeFace = null;

        try 
        {
            if (typefaceName.equals(FontType.VILLA.toString())) 
            {
                if (m_titleFont == null)
                    m_titleFont = Typeface.createFromAsset(
                            context.getAssets(), "fonts/" + typefaceName);

                typeFace = m_titleFont;
            }

            if (typefaceName.equals(FontType.SEGOE.toString())) 
            {
                if (m_titleFont == null)
                    m_titleFont = Typeface.createFromAsset(
                            context.getAssets(), "fonts/" + typefaceName);

                typeFace = m_titleFont;
            }
        }
        catch (Exception ex) 
        {
            typeFace = Typeface.DEFAULT;
        }

        return typeFace;
    }

    public static Typeface getTypeface(Context context, FontType typefaceName) 
    {
        return getTypeface(context, typefaceName.toString());
    }
}
