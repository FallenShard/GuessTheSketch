package nvnteam.guessthesketch.activity;

import nvnteam.guessthesketch.util.ActivityUtils;
import android.app.Activity;

public class FullScreenActivity extends Activity 
{
    @Override
    public void onWindowFocusChanged(boolean hasFocus) 
    {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus)
            ActivityUtils.hideSystemUI(this);
    }

}
