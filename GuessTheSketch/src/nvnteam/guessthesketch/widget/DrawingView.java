package nvnteam.guessthesketch.widget;

import java.util.Deque;
import java.util.LinkedList;
import java.util.Stack;


import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.bluetooth.BTGameActivity;
import nvnteam.guessthesketch.bluetooth.BluetoothProtocol;
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
import android.util.Log;
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

	private PlaybackThread m_playbackThread;

	private volatile boolean m_enableDrawing = true;
	private DrawingNode m_currentNode = new DrawingNode();
	private Deque<DrawingNode> m_playbackDeque = new LinkedList<DrawingNode>();

	private Stack<DrawingNode> m_undoStack = new Stack<DrawingNode>();

	private BTGameActivity m_observer = null;
	private int m_screenX;
	private int m_screenY;
	
	private boolean m_lineDrawn = false;

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
		// Prepare for drawing and setup paint stroke properties
		m_brushSize = getResources().getInteger(R.integer.small_size);
		m_drawPath = new Path();
		m_drawPaint = new Paint();
		m_drawPaint.setColor(m_paintColor);
		m_drawPaint.setAntiAlias(true);
		setBrushSize(m_brushSize);
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
	    if (m_enableDrawing)
	    {
    		float touchX = event.getX();
    		float touchY = event.getY();
    		float normX = touchX / m_screenX;
    		float normY = touchY / m_screenY;
    		m_currentNode.setAttrib(normX, normY, event.getAction(), SystemClock.uptimeMillis(),
    		        m_drawPaint.getColor(), m_brushSize);
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
    		m_playbackDeque.add(new DrawingNode(m_currentNode));

    		if (m_observer != null)
    		{
    		    m_observer.sendNode(BluetoothProtocol.DATA_DRAWING_NODE, m_currentNode);
    		    Log.d("NODESENDS", "ACTION SENT: " + m_currentNode.getActionType());
    		}
    		/*if (m_observer != null)
    		{
    		    if (m_playbackDeque.size() > 15 || event.getAction() == MotionEvent.ACTION_UP)
    		    {
    		        SenderThread senderThread = new SenderThread(m_playbackDeque);
    		        senderThread.start();
    		        m_playbackDeque.clear();
    		    }
    		}*/
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
		m_brushSize = newSize;
		m_drawPaint.setStrokeWidth(pixelAmount);
	}

	public void startNew()
	{
	    if (m_drawCanvas != null)
	        m_drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
	    m_drawPath.reset();
	    undo();
	    m_undoStack.clear();
		invalidate();
		m_lineDrawn = false;
	}

	public void clearQueue()
	{
	    m_playbackDeque.clear();
	}

	public Deque<DrawingNode> getPlaybackDeque()
	{
	    return m_playbackDeque;
	}

	public void putNode(DrawingNode node)
	{
	    m_playbackDeque.add(node);
	}

	public void startPlayback(final long milliSec)
	{
	    m_enableDrawing = false;
	    m_playbackThread = new PlaybackThread(milliSec);
	    m_playbackThread.start();
	}
	
	public void setDrawing(boolean drawing)
	{
	    m_enableDrawing = drawing;
	}

	public void stopPlayback()
	{
	    m_enableDrawing = true;
	}
	
	public synchronized void drawNode(DrawingNode node)
	{
	    float oldBrush = m_brushSize;
	    int oldColor = m_paintColor;
        m_drawPaint.setColor(node.getColor());
        setBrushSize(node.getBrushSize());
        int type = node.getActionType();
        boolean lineDrawn = m_lineDrawn;
        switch (node.getActionType()) 
        {
            case MotionEvent.ACTION_DOWN:
            {
                if (!m_lineDrawn)
                {
                    m_drawPath.moveTo(node.getX() * m_screenX, node.getY() * m_screenY);
                    m_lineDrawn = true;
                }
                else
                {
                    m_drawPath.lineTo(node.getX() * m_screenX, node.getY() * m_screenY);
                    m_drawCanvas.drawPath(m_drawPath, m_drawPaint);
                    m_drawPath.reset();
                    m_lineDrawn = false;
                    node.setActionType(MotionEvent.ACTION_UP);
                }
                break;
            }
            case MotionEvent.ACTION_MOVE:
            {
                if (!m_lineDrawn)
                {
                    m_drawPath.moveTo(node.getX() * m_screenX, node.getY() * m_screenY);
                    m_lineDrawn = true;
                    node.setActionType(MotionEvent.ACTION_DOWN);
                }
                else
                {
                    m_drawPath.lineTo(node.getX() * m_screenX, node.getY() * m_screenY);
                }
                break;
            }
            case MotionEvent.ACTION_UP:
            {
                if (!m_lineDrawn)
                {
                    m_paintColor = oldColor;
                    m_drawPaint.setColor(oldColor);
                    setBrushSize(oldBrush);
                    Log.w("NODESTUFF", "SKIPPED ONE UP NODE");
                    return;
                }
                else
                {
                    m_drawPath.lineTo(node.getX() * m_screenX, node.getY() * m_screenY);
                    m_drawCanvas.drawPath(m_drawPath, m_drawPaint);
                    m_drawPath.reset();
                    m_lineDrawn = false;
                }
                break;
            }
        }
        Log.w("NODESTUFF", "ACTION: " + type + " START: " + lineDrawn + " MUTATED ACTION: " + node.getActionType());
        postInvalidate();
        m_undoStack.add(new DrawingNode(node));
        m_playbackDeque.add(new DrawingNode(node));
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
                    m_playbackDeque.add(new DrawingNode(node));
                    m_undoStack.add(new DrawingNode(node));
                }

                while (!localDeque.isEmpty())
                {
                    DrawingNode firstNode = localDeque.poll();
                    m_drawPaint.setColor(firstNode.getColor());
                    setBrushSize(firstNode.getBrushSize());

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
                }
                postInvalidate();
            }
        }).start();
    }

	public void undo()
	{
	    new Thread(new Runnable()
        {
            public void run()
            {
        	    try
        	    {
            	    while (m_undoStack.peek().getActionType() != MotionEvent.ACTION_DOWN)
            	    {
            	        m_undoStack.pop();
            	        m_playbackDeque.pollLast();
            	    }
            	    m_undoStack.pop();
            	    m_playbackDeque.pollLast();
            	    m_drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            	    int previousColor = m_paintColor;
            	    float prevBrush = m_brushSize;
            	    for (DrawingNode node : m_undoStack)
            	    {
            	        m_drawPaint.setColor(node.getColor());
            	        setBrushSize(node.getBrushSize());
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
            	    m_paintColor = previousColor;
            	    m_drawPaint.setColor(previousColor);
            	    setBrushSize(prevBrush);
            	    postInvalidate();
        	    }
        	    catch (Exception ex)
        	    {

        	    }
            }
        }).start();
	}

	private class SenderThread extends Thread
	{
	    private Deque<DrawingNode> mm_deque;

	    public SenderThread (Deque<DrawingNode> deque)
	    {
	        mm_deque = new LinkedList<DrawingNode>(deque);
	    }

	    public void run()
	    {
	        m_observer.sendNodes(BluetoothProtocol.DATA_DRAWING_NODE, mm_deque);
	    }
	}

	private class PlaybackThread extends Thread
    {
        private long mm_timeDelay;

        public PlaybackThread(final long milliSec)
        {
            mm_timeDelay = SystemClock.uptimeMillis() - milliSec;
        }

        public void run()
        {
            while (!m_playbackDeque.isEmpty() && !m_enableDrawing)
            {
                while ((SystemClock.uptimeMillis() < m_playbackDeque.peek().getTimeStamp() + mm_timeDelay));
                DrawingNode firstNode = m_playbackDeque.remove();
                m_drawPaint.setColor(firstNode.getColor());
                setBrushSize(firstNode.getBrushSize());

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
                if (!m_enableDrawing)
                    postInvalidate();
            }
            if (!m_playbackDeque.isEmpty())
            {
                m_playbackDeque.clear();
                m_drawPath.reset();
            }
            if (!m_undoStack.isEmpty())
                m_undoStack.clear();
        }
    }
}
