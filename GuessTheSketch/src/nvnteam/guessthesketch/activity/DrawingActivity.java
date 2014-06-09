package nvnteam.guessthesketch.activity;

import java.util.UUID;

import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.widget.DrawingView;


import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


public class DrawingActivity extends FullScreenActivity
{

	private DrawingView m_drawView;

	private ImageButton m_newButton;
	private ImageButton m_saveButton;
	private ImageButton m_undoButton;
	private ImageButton m_currentBrush;
	private ImageButton m_currentPaint;
	private ImageButton m_exitButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drawing);
		initUI();
		initListeners();

	}

	private void initUI()
	{
	    m_drawView = (DrawingView) findViewById(R.id.drawing);

	    m_newButton = (ImageButton) findViewById(R.id.button_new_canvas);
	    m_saveButton = (ImageButton) findViewById(R.id.button_save_drawing);
	    m_undoButton = (ImageButton) findViewById(R.id.button_undo);

	    LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
        m_currentPaint = (ImageButton) paintLayout.getChildAt(0);
        m_currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.color_button_pressed));

        LinearLayout brushLayout = (LinearLayout)findViewById(R.id.brush_size_layout);
        m_currentBrush = (ImageButton) brushLayout.getChildAt(0);
        m_currentBrush.setImageDrawable(getResources().getDrawable(R.drawable.small_brush_pressed));
        
        m_exitButton = (ImageButton) findViewById(R.id.button_exit);
	}

	private void initListeners()
	{
	    m_newButton.setOnClickListener(new OnClickListener()
	    {
	        public void onClick(View v)
	        {
	            final ConfirmationDialog cd = new ConfirmationDialog(DrawingActivity.this);
	            cd.show();
	            cd.setParam("New Drawing", "This command will erase the whole canvas. Do you wish to continue?");
	            cd.setYesOnClickListener("OK", new OnClickListener()
	            {
                    public void onClick(View v)
                    {
                        m_drawView.startNew();
                        cd.dismiss();
                    }
                });
	            cd.setNoOnClickListener("Cancel", new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        cd.dismiss();
                    }
                });
	        }
	    });

	    m_saveButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                final ConfirmationDialog cd = new ConfirmationDialog(DrawingActivity.this);
                cd.show();
                cd.setParam("Save Drawing", "This command will start saving the canvas" +
                " as a picture to the gallery. Do you wish to continue?");
                cd.setYesOnClickListener("OK", new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        m_drawView.setDrawingCacheEnabled(true);
                        String imgSaved = MediaStore.Images.Media.insertImage(
                                getContentResolver(), m_drawView.getDrawingCache(),
                                UUID.randomUUID().toString()+".png", "drawing");

                        if (imgSaved != null)
                        {
                            Toast savedToast = Toast.makeText(getApplicationContext(), 
                                    "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                            savedToast.show();
                        }
                        else
                        {
                            Toast unsavedToast = Toast.makeText(getApplicationContext(), 
                                    "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                            unsavedToast.show();
                        }
                        m_drawView.destroyDrawingCache();
                    }
                });
                cd.setNoOnClickListener("Cancel", new OnClickListener()
                {
                    public void onClick(View v)
                    {
                        cd.dismiss();
                    }
                });
            }
        });

	    m_undoButton.setOnClickListener(new OnClickListener()
        {
            public void onClick(View v)
            {
                m_drawView.undo();
            }
        });

	    m_exitButton.setOnClickListener(new OnClickListener()
	    {
	        public void onClick(View v)
            {
	            final ConfirmationDialog cd = new ConfirmationDialog(DrawingActivity.this);
	            cd.show();
	            cd.setParam("Exit", "Are you sure you want to exit?");
	            cd.setYesOnClickListener("Yes", new OnClickListener()
	            {
	                 public void onClick(View v) 
	                 {
	                     cd.dismiss();
	                     DrawingActivity.super.onBackPressed();
	                 }
	            });
	            cd.setNoOnClickListener("No", new OnClickListener()
	            {
	                 public void onClick(View v) 
	                 {
	                     cd.dismiss();
	                 }
	            });
            }
	    });
	}

	public void brushClicked(View view)
    {
        if (view != m_currentBrush)
        {
            ImageButton imgView = (ImageButton) view;
            String size = view.getTag().toString();
            if (size.compareTo("small_brush") == 0)
            {
                m_drawView.setBrushSize(10.0f);
                imgView.setImageDrawable(getResources().getDrawable(R.drawable.small_brush_pressed));
            }
            else if (size.compareTo("medium_brush") == 0)
            {
                m_drawView.setBrushSize(20.0f);
                imgView.setImageDrawable(getResources().getDrawable(R.drawable.medium_brush_pressed));
            }
            else
            {
                m_drawView.setBrushSize(30.0f);
                imgView.setImageDrawable(getResources().getDrawable(R.drawable.large_brush_pressed));
            }

            String currentSize = m_currentBrush.getTag().toString();
            if (currentSize.compareTo("small_brush") == 0)
                m_currentBrush.setImageDrawable(getResources().getDrawable(R.drawable.small_brush_normal));
            else if (currentSize.compareTo("medium_brush") == 0)
                m_currentBrush.setImageDrawable(getResources().getDrawable(R.drawable.medium_brush_normal));
            else
                m_currentBrush.setImageDrawable(getResources().getDrawable(R.drawable.large_brush_normal));

            m_currentBrush = (ImageButton) view;
        }
    }

    public void paintClicked(View view)
    {
        if (view != m_currentPaint)
        {
            ImageButton imgView = (ImageButton) view;
            String color = view.getTag().toString();
            m_drawView.setColor(color);

            imgView.setImageDrawable(getResources().getDrawable(R.drawable.color_button_pressed));
            m_currentPaint.setImageDrawable(getResources().getDrawable(R.drawable.color_button_normal));
            m_currentPaint = (ImageButton) view;
        }
    }
}
