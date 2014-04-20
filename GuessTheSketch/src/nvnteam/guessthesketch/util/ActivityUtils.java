package nvnteam.guessthesketch.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.*;
import android.view.View;

public class ActivityUtils 
{
    @SuppressLint("InlinedApi")
    public static void hideSystemUI(Activity activity)
    {
        int currentApiVersion = android.os.Build.VERSION.SDK_INT;
        
        if (currentApiVersion >= Build.VERSION_CODES.KITKAT)
        {
            activity.getWindow().getDecorView().setSystemUiVisibility
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
           | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
           | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
           | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
           | View.SYSTEM_UI_FLAG_FULLSCREEN
           | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
        else
        {
            activity.getWindow().getDecorView().setSystemUiVisibility
            (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
           | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
           | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
           | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
           | View.SYSTEM_UI_FLAG_FULLSCREEN);
        }
    }

}
