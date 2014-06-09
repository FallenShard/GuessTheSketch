package nvnteam.guessthesketch.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.Toast;

public class HighScoreManager 
{
    public static final String ROUND_PREFS_NAME = "RoundPrefs";
    public static final String AVG_PREFS_NAME = "AvgPrefs";
    public static final String FIVE_PREFS_NAME = "FivePrefs";

    private SharedPreferences m_roundPrefs;
    private SharedPreferences m_avgPrefs;
    private SharedPreferences m_fivePrefs;

    private String[] m_roundScoresNames = new String[5];
    private float[] m_roundScoresVals = new float[5];
    private String[] m_avgScoresNames = new String[5];
    private float[] m_avgScoresVals = new float[5];
    private String[] m_fiveScoresNames = new String[5];
    private float[] m_fiveScoresVals = new float[5];
    
    private Context m_context;

    public HighScoreManager(Context context)
    {
        m_context = context;
        m_roundPrefs = context.getSharedPreferences(ROUND_PREFS_NAME, Context.MODE_PRIVATE);
        m_avgPrefs = context.getSharedPreferences(AVG_PREFS_NAME, Context.MODE_PRIVATE);
        m_fivePrefs = context.getSharedPreferences(FIVE_PREFS_NAME, Context.MODE_PRIVATE);
        for (int i = 0; i < 5; i++)
        {
            m_roundScoresNames[i] = m_roundPrefs.getString("roundName" + i, "-r-");
            m_roundScoresVals[i] = m_roundPrefs.getFloat("roundVal" + i, 0);
            m_avgScoresNames[i] = m_avgPrefs.getString("avgName" + i, "-a-");
            m_avgScoresVals[i] = m_avgPrefs.getFloat("avgVal" + i, 1.0f);
            m_fiveScoresNames[i] = m_fivePrefs.getString("fiveName" + i, "-f-");
            m_fiveScoresVals[i] = m_fivePrefs.getFloat("fiveVal" + i, 5);
        }
    }

    public void saveScore(String teamName, float points, String fileName)
    {
        if (fileName == ROUND_PREFS_NAME)
        {
            int i;
            for (i = 0; i < 5 && points < m_roundScoresVals[i]; i++);

            if (i == 5) return;

            for (int j = 4; j > i; j--)
            {
                m_roundScoresVals[j] = m_roundScoresVals[j - 1];
                m_roundScoresNames[j] = m_roundScoresNames[j - 1];
            }

            m_roundScoresVals[i] = GTSUtils.round(points, 2);
            m_roundScoresNames[i] = new String(teamName);

            SharedPreferences.Editor editor = m_roundPrefs.edit();
            for (int x = 0; x < 5; x++)
            {
                editor.putFloat("roundVal" + x, m_roundScoresVals[x]);
                editor.putString("roundName" + x, m_roundScoresNames[x]);
            }
            editor.commit();
        }
        else if (fileName == AVG_PREFS_NAME)
        {
            int i;
            for (i = 0; i < 5 && points < m_avgScoresVals[i]; i++);

            if (i == 5) return;

            for (int j = 4; j > i; j--)
            {
                m_avgScoresVals[j] = m_avgScoresVals[j - 1];
                m_avgScoresNames[j] = m_avgScoresNames[j - 1];
            }

            m_avgScoresVals[i] = GTSUtils.round(points, 2);
            m_avgScoresNames[i] = new String(teamName);

            SharedPreferences.Editor editor = m_avgPrefs.edit();
            for (int x = 0; x < 5; x++)
            {
                editor.putFloat("avgVal" + x, m_avgScoresVals[x]);
                editor.putString("avgName" + x, m_avgScoresNames[x]);
            }
            editor.commit();
        }
        else
        {
            int i;
            for (i = 0; i < 5 && points < m_fiveScoresVals[i]; i++);

            if (i == 5) return;

            for (int j = 4; j > i; j--)
            {
                m_fiveScoresVals[j] = m_fiveScoresVals[j - 1];
                m_fiveScoresNames[j] = m_fiveScoresNames[j - 1];
            }

            m_fiveScoresVals[i] = GTSUtils.round(points, 2);
            m_fiveScoresNames[i] = new String(teamName);

            SharedPreferences.Editor editor = m_fivePrefs.edit();
            for (int x = 0; x < 5; x++)
            {
                editor.putFloat("fiveVal" + x, m_fiveScoresVals[x]);
                editor.putString("fiveName" + x, m_fiveScoresNames[x]);
            }
            editor.commit();
        }
        Toast.makeText(m_context, "New high score: " + teamName + " - " + points, Toast.LENGTH_SHORT).show();
    }

    public String getScoresName(int pos, String fileName)
    {
        if (pos < 0 || pos >= 5) return "";

        if (fileName == ROUND_PREFS_NAME)
            return m_roundScoresNames[pos];
        else if (fileName == AVG_PREFS_NAME)
            return m_avgScoresNames[pos];
        else
            return m_fiveScoresNames[pos];
    }

    public float getScoresVal(int pos, String fileName)
    {
        if (pos < 0 || pos >= 5) return 0;

        if (fileName == ROUND_PREFS_NAME)
            return m_roundScoresVals[pos];
        else if (fileName == AVG_PREFS_NAME)
            return m_avgScoresVals[pos];
        else
            return m_fiveScoresVals[pos];
    }
}
