package nvnteam.guessthesketch.activity;

import java.util.UUID;

import nvnteam.guessthesketch.R;
import nvnteam.guessthesketch.R.drawable;
import nvnteam.guessthesketch.R.id;
import nvnteam.guessthesketch.R.integer;
import nvnteam.guessthesketch.R.layout;
import nvnteam.guessthesketch.R.menu;
import nvnteam.guessthesketch.widget.DrawingView;


import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;


public class DrawingActivity extends Activity implements OnClickListener 
{
	//custom drawing view
	private DrawingView m_drawView;
	//buttons
	private ImageButton m_currPaint;
	private ImageButton m_drawBtn; 
	private ImageButton m_eraseBtn; 
	private ImageButton m_newBtn; 
	private ImageButton m_saveBtn;
	//sizes
	private float m_smallBrush;
	private float m_mediumBrush;
	private float m_largeBrush;
	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_drawing);

		//get drawing view
		m_drawView = (DrawingView)findViewById(R.id.drawing);

		//get the palette and first color button
		LinearLayout paintLayout = (LinearLayout)findViewById(R.id.paint_colors);
		m_currPaint = (ImageButton)paintLayout.getChildAt(0);
		m_currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));

		//sizes from dimensions
		m_smallBrush = getResources().getInteger(R.integer.small_size);
		m_mediumBrush = getResources().getInteger(R.integer.medium_size);
		m_largeBrush = getResources().getInteger(R.integer.large_size);

		//draw button
		m_drawBtn = (ImageButton)findViewById(R.id.draw_btn);
		m_drawBtn.setOnClickListener(this);

		//set initial size
		m_drawView.setBrushSize(m_mediumBrush);

		//erase button
		m_eraseBtn = (ImageButton)findViewById(R.id.erase_btn);
		m_eraseBtn.setOnClickListener(this);

		//new button
		m_newBtn = (ImageButton)findViewById(R.id.new_btn);
		m_newBtn.setOnClickListener(this);

		//save button
		m_saveBtn = (ImageButton)findViewById(R.id.save_btn);
		m_saveBtn.setOnClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	//user clicked paint
	public void paintClicked(View view){
		//use chosen color

		//set erase false
	    m_drawView.setErase(false);
	    m_drawView.setBrushSize(m_drawView.getLastBrushSize());

		if(view != m_currPaint){
			ImageButton imgView = (ImageButton)view;
			String color = view.getTag().toString();
			m_drawView.setColor(color);
			//update ui
			imgView.setImageDrawable(getResources().getDrawable(R.drawable.paint_pressed));
			m_currPaint.setImageDrawable(getResources().getDrawable(R.drawable.paint));
			m_currPaint = (ImageButton)view;
		}
	}

	@Override
	public void onClick(View view){

		if(view.getId()==R.id.draw_btn){
			//draw button clicked
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Brush size:");
			brushDialog.setContentView(R.layout.brush_chooser);
			//listen for clicks on size buttons
			ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
				    m_drawView.setErase(false);
				    m_drawView.setBrushSize(m_smallBrush);
				    m_drawView.setLastBrushSize(m_smallBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
				    m_drawView.setErase(false);
				    m_drawView.setBrushSize(m_mediumBrush);
				    m_drawView.setLastBrushSize(m_mediumBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
				    m_drawView.setErase(false);
				    m_drawView.setBrushSize(m_largeBrush);
				    m_drawView.setLastBrushSize(m_largeBrush);
					brushDialog.dismiss();
				}
			});
			//show and wait for user interaction
			brushDialog.show();
		}
		else if(view.getId()==R.id.erase_btn){
			//switch to erase - choose size
			final Dialog brushDialog = new Dialog(this);
			brushDialog.setTitle("Eraser size:");
			brushDialog.setContentView(R.layout.brush_chooser);
			//size buttons
			ImageButton smallBtn = (ImageButton)brushDialog.findViewById(R.id.small_brush);
			smallBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
				    m_drawView.setErase(true);
				    m_drawView.setBrushSize(m_smallBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton mediumBtn = (ImageButton)brushDialog.findViewById(R.id.medium_brush);
			mediumBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
				    m_drawView.setErase(true);
				    m_drawView.setBrushSize(m_mediumBrush);
					brushDialog.dismiss();
				}
			});
			ImageButton largeBtn = (ImageButton)brushDialog.findViewById(R.id.large_brush);
			largeBtn.setOnClickListener(new OnClickListener(){
				@Override
				public void onClick(View v) {
				    m_drawView.setErase(true);
				    m_drawView.setBrushSize(m_largeBrush);
					brushDialog.dismiss();
				}
			});
			brushDialog.show();
		}
		else if(view.getId()==R.id.new_btn){
			//new button
			AlertDialog.Builder newDialog = new AlertDialog.Builder(this);
			newDialog.setTitle("New drawing");
			newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
			newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
				    m_drawView.startNew();
					dialog.dismiss();
				}
			});
			newDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
			newDialog.show();
		}
		else if(view.getId()==R.id.save_btn){
			//save drawing
			AlertDialog.Builder saveDialog = new AlertDialog.Builder(this);
			saveDialog.setTitle("Save drawing");
			saveDialog.setMessage("Save drawing to device Gallery?");
			saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					//save drawing
				    m_drawView.setDrawingCacheEnabled(true);
					//attempt to save
					String imgSaved = MediaStore.Images.Media.insertImage(
							getContentResolver(), m_drawView.getDrawingCache(),
							UUID.randomUUID().toString()+".png", "drawing");
					//feedback
					if(imgSaved!=null){
						Toast savedToast = Toast.makeText(getApplicationContext(), 
								"Drawing saved to Gallery!", Toast.LENGTH_SHORT);
						savedToast.show();
					}
					else{
						Toast unsavedToast = Toast.makeText(getApplicationContext(), 
								"Oops! Image could not be saved.", Toast.LENGTH_SHORT);
						unsavedToast.show();
					}
					m_drawView.destroyDrawingCache();
				}
			});
			saveDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener(){
				public void onClick(DialogInterface dialog, int which){
					dialog.cancel();
				}
			});
			saveDialog.show();
		}
	}

}
