package nvnteam.guessthesketch;

import java.util.Random;

public class WordBase 
{
    private final static Random m_randGen = new Random();
    private static String[] m_easyWords = new String[] 
            {
                "CAT",
                "SUN",
                "CUP",
                "GHOST",
                "FLOWER",
                "PIE",
                "COW",
                "BANANA",
                "SNOWFLAKE",
                "BUG",
                "BOOK",
                "JAR"
            };
    
    private static String[] m_mediumWords = new String[]
            {
                "PASSWORD"
            };
    
    private static String[] m_hardWords = new String[]
            {
                "LANDSCAPE"
            };
    
    public static String getEasyWord()
    {
        return m_easyWords[m_randGen.nextInt(m_easyWords.length)];
    }
    
    public static String getMediumWord()
    {
        return m_mediumWords[m_randGen.nextInt(m_easyWords.length)];
    }
    
    public static String getHardWord()
    {
        return m_hardWords[m_randGen.nextInt(m_easyWords.length)];
    }
}
