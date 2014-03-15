package nvnteam.guessthesketch.dto;

public class DrawingNode
{
    private float m_x;
    private float m_y;
    private int m_actionType;
    private long m_timeStamp;
    private int m_color;

    public DrawingNode()
    {
        m_x = 0.f;
        m_y = 0.f;
        m_actionType = -1;
        m_timeStamp = 60000;
        m_color = 0x00000000;
    }
    
    public DrawingNode(DrawingNode dNode)
    {
        m_x = dNode.m_x;
        m_y = dNode.m_y;
        m_actionType = dNode.m_actionType;
        m_timeStamp = dNode.m_timeStamp;
        m_color = dNode.m_color;
    }
    
    public void setAttrib(float x, float y, int actionType, long time, int color)
    {
        m_x = x;
        m_y = y;
        m_actionType = actionType;
        m_timeStamp = time;
        m_color = color;
    }

    public void setTimeStamp(long timeStamp)
    {
        m_timeStamp = timeStamp;
    }
    
    public float getX()
    {
        return m_x;
    }
    
    public float getY()
    {
        return m_y;
    }
    
    public int getActionType()
    {
        return m_actionType;
    }
    
    public long getTimeStamp()
    {
        return m_timeStamp;
    }
    
    public int getColor()
    {
        return m_color;
    }
}
