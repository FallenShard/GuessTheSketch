package nvnteam.guessthesketch;


import nvnteam.guessthesketch.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;


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

	private boolean erase = false;

	public DrawingView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		setupDrawing();
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

	//size assigned to view
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		m_canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		m_drawCanvas = new Canvas(m_canvasBitmap);
	}

	//draw the view - will be called after touch event
	@Override
	protected void onDraw(Canvas canvas)
	{
		canvas.drawBitmap(m_canvasBitmap, 0, 0, m_canvasPaint);
		canvas.drawPath(m_drawPath, m_drawPaint);
	}

	//register user touches as drawing action
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		float touchX = event.getX();
		float touchY = event.getY();
		//respond to down, move and up events
		switch (event.getAction()) {
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
		//redraw
		invalidate();
		return true;

	}

	//update color
	public void setColor(String newColor)
	{
		invalidate();
		m_paintColor = Color.parseColor(newColor);
		m_drawPaint.setColor(m_paintColor);
	}

	//set brush size
	public void setBrushSize(float newSize)
	{
		float pixelAmount = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 
				newSize, getResources().getDisplayMetrics());
		m_brushSize=pixelAmount;
		m_drawPaint.setStrokeWidth(m_brushSize);
	}

	//get and set last brush size
	public void setLastBrushSize(float lastSize)
	{
	    m_lastBrushSize=lastSize;
	}
	public float getLastBrushSize(){
		return m_lastBrushSize;
	}

	//set erase true or false
	public void setErase(boolean isErase)
	{
		erase=isErase;
		if (erase) m_drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		else m_drawPaint.setXfermode(null);
	}

	//start new drawing
	public void startNew()
	{
	    m_drawCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
		invalidate();
	}
}
