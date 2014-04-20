package nvnteam.guessthesketch.util;

import android.content.Context;
import android.graphics.Typeface;

public class FontUtils 
{

    private static Typeface mTitleFont;

    public static enum FontType
    {
        MAIN_FONT
        {
            public String toString() 
            {
                return "Villa.ttf";
            }
        }
    }

    public static Typeface getTypeface(Context context, String typefaceName) 
    {
        Typeface typeFace = null;

        try 
        {
            if (typefaceName.equals(FontType.MAIN_FONT.toString())) 
            {
                if (mTitleFont == null)
                    mTitleFont = Typeface.createFromAsset(
                            context.getAssets(), "fonts/" + typefaceName);

                typeFace = mTitleFont;
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
