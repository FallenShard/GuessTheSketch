package nvnteam.guessthesketch.widget;


import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;


import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.bluetooth.BTGameActivity;
import nvnteam.guessthesketch.dto.DrawingNode;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;


public class DrawingView extends View 
{
	private Path m_drawPath;
	private Paint m_drawPaint; 
	private Paint m_canvasPaint;
	private int m_paintColor = 0xFF660000;
	private Canvas m_drawCanvas;
	private Bitmap m_canvasBitmap;

	private float m_brushSize; 
	private float m_lastBrushSize;

	private volatile boolean m_shouldPlayback = false;
	private DrawingNode m_currentNode = new DrawingNode();
	private Deque<DrawingNode> m_playbackQueue = new LinkedList<DrawingNode>();
	
	private Stack<DrawingNode> m_undoStack = new Stack<DrawingNode>();
	
	private BTGameActivity m_observer = null;
	private int m_screenX;
	private int m_screenY;

	public DrawingView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setupDrawing();
		
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		m_screenX = size.x;
		m_screenY = size.y;
	}

	private void setupDrawing()
	{
		//prepare for drawing and setup paint stroke properties
		m_brushSize = getResources().getInteger(R.integer.medium_size);
		m_lastBrushSize = m_brushSize;
		m_drawPath = new Path();
		m_drawPaint = new Paint();
		m_drawPaint.setColor(m_paintColor);
		m_drawPaint.setAntiAlias(true);
		m_drawPaint.setStrokeWidth(m_brushSize);
		m_drawPaint.setStyle(Paint.Style.STROKE);
		m_drawPaint.setStrokeJoin(Paint.Join.ROUND);
		m_drawPaint.setStrokeCap(Paint.Cap.ROUND);
		m_canvasPaint = new Paint(Paint.DITHER_FLAG);
	}
	
	public void attachObserver(BTGameActivity act)
	{
	    m_observer = act;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		m_canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		m_drawCanvas = new Canvas(m_canvasBitmap);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawBitmap(m_canvasBitmap, 0, 0, m_canvasPaint);
		canvas.drawPath(m_drawPath, m_drawPaint);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
	    if (!m_shouldPlayback)
	    {
    		float touchX = event.getX();
    		float touchY = event.getY();
    		float normX = touchX / m_screenX;
    		float normY = touchY / m_screenY;
    		m_currentNode.setAttrib(normX, normY, event.getAction(), SystemClock.uptimeMillis(),
    		        m_drawPaint.getColor(), m_drawPaint.getStrokeWidth());
    		//respond to down, move and up events
    		switch (event.getAction()) 
    		{
        		case MotionEvent.ACTION_DOWN:
        		    m_drawPath.moveTo(touchX, touchY);
        			break;
        		case MotionEvent.ACTION_MOVE:
        		    m_drawPath.lineTo(touchX, touchY);
        			break;
        		case MotionEvent.ACTION_UP:
        		    m_drawPath.lineTo(touchX, touchY);
        		    m_drawCanvas.drawPath(m_drawPath, m_drawPaint);
        		    m_drawPath.reset();
        			break;
        		default:
        			return false;
    		}
    		m_undoStack.add(new DrawingNode(m_currentNode));
    		m_playbackQueue.add(new DrawingNode(m_currentNode));
    		
    		if (m_observer != null)
    		{
    		    if (m_playbackQueue.size() >= 30 || event.getAction() == MotionEvent.ACTION_UP)
    		    {
    		        Deque<DrawingNode> newDeque = new LinkedList<DrawingNode>(m_playbackQueue);
    		        m_observer.transferNodes(newDeque);
    		        m_playbackQueue.clear();
    		    }
    		}
    		invalidate();
    		return true;
	    }
	    return false;
	}

	public void setColor(String newColor)
	{
		invalidate();
		m_paintColor = Color.parseColor(newColor);
		m_drawPaint.setColor(m_paintColor);
	}

	public void setBrushSize(float newSize)
	{
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
				newSize, getResources().getDisplayMetrics());
		m_brushSize = pixelAmount;
		m_drawPaint.setStrokeWidth(m_brushSize);
	}

	public void setLastBrushSize(float lastSize)
	{
	    m_lastBrushSize = lastSize;
	}
	public float getLastBrushSize()
	{
		return m_lastBrushSize;
	}

	public void startNew()
	{
	    if (m_drawCanvas != null)
	        m_drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
	    m_undoStack.clear();
		invalidate();
	}

	public void clearQueue()
	{
	    m_playbackQueue.clear();
	}
	
	public void setPlayback(boolean playback)
	{
	    m_shouldPlayback = playback;
	}
	
	public boolean getPlayback()
    {
        return m_shouldPlayback;
    }
	
	public Deque<DrawingNode> getPlaybackDeque()
	{
	    return m_playbackQueue;
	}
	
	public void putNode(DrawingNode node)
	{
	    m_playbackQueue.add(node);
	}
	
	public void playBack(final long milliSec)
	{
	    if (!m_playbackQueue.isEmpty())
	    new Thread(new Runnable()
	    {
	        public void run()
	        {
	            long diff = SystemClock.uptimeMillis() - milliSec;

	            while (!m_playbackQueue.isEmpty() && m_shouldPlayback)
	            {
	                while ((SystemClock.uptimeMillis() < m_playbackQueue.peek().getTimeStamp() + diff));
	                DrawingNode firstNode = m_playbackQueue.remove();
	                m_drawPaint.setColor(firstNode.getColor());

	                switch (firstNode.getActionType())
	                {
	                    case MotionEvent.ACTION_DOWN:
	                        m_drawPath.moveTo(firstNode.getX() * m_screenX, firstNode.getY() * m_screenY);
	                        break;
	                    case MotionEvent.ACTION_MOVE:
	                        m_drawPath.lineTo(firstNode.getX() * m_screenX, firstNode.getY() * m_screenY);
	                        break;
	                    case MotionEvent.ACTION_UP:
	                        m_drawPath.lineTo(firstNode.getX() * m_screenX, firstNode.getY() * m_screenY);
	                        m_drawCanvas.drawPath(m_drawPath, m_drawPaint);
	                        m_drawPath.reset();
	                        break;
	                }
	                if (m_shouldPlayback)
	                    postInvalidate();
	            }
	            if (!m_playbackQueue.isEmpty())
	                m_playbackQueue.clear();
	        }
	    }).start();
	}
	
	public void drawFromDeque(final Deque<DrawingNode> deque)
    {
        if (!deque.isEmpty())
        new Thread(new Runnable()
        {
            public void run()
            {
                Deque<DrawingNode> localDeque = new LinkedList<DrawingNode>(deque);
                while (!deque.isEmpty())
                {
                    DrawingNode node = deque.pollFirst();
                    m_playbackQueue.add(new DrawingNode(node));
                    m_undoStack.add(new DrawingNode(node));
                }

                while (!localDeque.isEmpty())
                {
                    DrawingNode firstNode = localDeque.poll();
                    m_drawPaint.setColor(firstNode.getColor());

                    switch (firstNode.getActionType()) 
                    {
                        case MotionEvent.ACTION_DOWN:
                            m_drawPath.moveTo(firstNode.getX() * m_screenX, firstNode.getY() * m_screenY);
                            break;
                        case MotionEvent.ACTION_MOVE:
                            m_drawPath.lineTo(firstNode.getX() * m_screenX, firstNode.getY() * m_screenY);
                            break;
                        case MotionEvent.ACTION_UP:
                            m_drawPath.lineTo(firstNode.getX() * m_screenX, firstNode.getY() * m_screenY);
                            m_drawCanvas.drawPath(m_drawPath, m_drawPaint);
                            m_drawPath.reset();
                            break;
                    }
                    postInvalidate();
                }
            }
        }).start();
    }
	
	public void undo()
	{
	    try
	    {
	    while (m_undoStack.peek().getActionType() != MotionEvent.ACTION_DOWN)
	    {
	        m_undoStack.pop();
	        m_playbackQueue.pollLast();
	    }
	    m_undoStack.pop();
	    m_playbackQueue.pollLast();
	    m_drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
	    int previousColor = m_paintColor;
	    for (DrawingNode node : m_undoStack)
	    {
	        m_drawPaint.setColor(node.getColor());
	        switch (node.getActionType())
            {
                case MotionEvent.ACTION_DOWN:
                    m_drawPath.moveTo(node.getX() * m_screenX, node.getY() * m_screenY);
                    break;
                case MotionEvent.ACTION_MOVE:
                    m_drawPath.lineTo(node.getX() * m_screenX, node.getY() * m_screenY);
                    break;
                case MotionEvent.ACTION_UP:
                    m_drawPath.lineTo(node.getX() * m_screenX, node.getY() * m_screenY);
                    m_drawCanvas.drawPath(m_drawPath, m_drawPaint);
                    m_drawPath.reset();
                    break;
            }
	    }
	    m_drawPaint.setColor(previousColor);
	    m_paintColor = previousColor;
	    invalidate();
	    }
	    catch (Exception ex)
	    {

	    }
	}
}
