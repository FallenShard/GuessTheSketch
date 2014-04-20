package nvnteam.guessthesketch.dto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.nio.ByteBuffer;

public class DrawingNode implements Serializable
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
    
    public static byte[] serialize(DrawingNode node)
    {/*
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(node);
        byte[] retVal = bos.toByteArray();
        out.close();
        bos.close();*/
        ByteBuffer buffer = ByteBuffer.allocate(24);
        buffer.putFloat(node.m_x);
        buffer.putFloat(node.m_y);
        buffer.putInt(node.m_actionType);
        buffer.putLong(node.m_timeStamp);
        buffer.putInt(node.m_color);
        return buffer.array();
    }

    public static DrawingNode deserialize(byte[] array)
    {/*
        ByteArrayInputStream bis = new ByteArrayInputStream(array);
        ObjectInput in = new ObjectInputStream(bis);
        DrawingNode node = (DrawingNode) in.readObject();*/
        DrawingNode node = new DrawingNode();
        node.setAttrib(ByteBuffer.wrap(array, 0, 4).getFloat(), 
                       ByteBuffer.wrap(array, 4, 4).getFloat(),
                       ByteBuffer.wrap(array, 8, 4).getInt(),
                       ByteBuffer.wrap(array, 12, 8).getLong(),
                       ByteBuffer.wrap(array, 20, 4).getInt());
        return node;
    }
}
