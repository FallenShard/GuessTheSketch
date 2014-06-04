package nvnteam.guessthesketch.dto;

import java.nio.ByteBuffer;

public class DrawingNode
{
    private float m_x;
    private float m_y;
    private int m_actionType;
    private long m_timeStamp;
    private int m_color;
    private float m_thickness;

    public DrawingNode()
    {
        m_x = 0.f;
        m_y = 0.f;
        m_actionType = -1;
        m_timeStamp = 60000;
        m_color = 0x00000000;
        m_thickness = 0;
    }

    public DrawingNode(DrawingNode dNode)
    {
        m_x = dNode.m_x;
        m_y = dNode.m_y;
        m_actionType = dNode.m_actionType;
        m_timeStamp = dNode.m_timeStamp;
        m_color = dNode.m_color;
        m_thickness = dNode.m_thickness;
    }

    public void setAttrib(float x, float y, int actionType, long time, int color, float thickness)
    {
        m_x = x;
        m_y = y;
        m_actionType = actionType;
        m_timeStamp = time;
        m_color = color;
        m_thickness = thickness;
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

    public void setActionType(int actionType)
    {
        m_actionType = actionType;
    }

    public long getTimeStamp()
    {
        return m_timeStamp;
    }

    public int getColor()
    {
        return m_color;
    }

    public float getBrushSize()
    {
        return m_thickness;
    }

    public static byte[] serialize(DrawingNode node)
    {
        ByteBuffer buffer = ByteBuffer.allocate(28);
        buffer.putFloat(node.m_x);
        buffer.putFloat(node.m_y);
        buffer.putInt(node.m_actionType);
        buffer.putLong(node.m_timeStamp);
        buffer.putInt(node.m_color);
        buffer.putFloat(node.m_thickness);
        return buffer.array();
    }

    public static DrawingNode deserialize(byte[] array)
    {
        DrawingNode node = new DrawingNode();
        node.setAttrib(ByteBuffer.wrap(array, 0, 4).getFloat(), 
                       ByteBuffer.wrap(array, 4, 4).getFloat(),
                       ByteBuffer.wrap(array, 8, 4).getInt(),
                       ByteBuffer.wrap(array, 12, 8).getLong(),
                       ByteBuffer.wrap(array, 20, 4).getInt(),
                       ByteBuffer.wrap(array, 24, 4).getFloat());
        return node;
    }
}
