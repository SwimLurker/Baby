package org.slstudio.baby;

import org.slstudio.baby.ui.PictureView;
import org.slstudio.baby.util.BitmapUtil;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

public class PictureActivity extends Activity {
	
	private Resources resources = null;
	private String pictureFilename = null;
	
	private PictureView pictureView = null;
	private ImageButton zoominBtn = null;
	private ImageButton zoomoutBtn = null;
	private ImageButton closeBtn = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picture);
		resources = getResources();
		
		pictureView = (PictureView)findViewById(R.id.picture_view);
		zoominBtn = (ImageButton)findViewById(R.id.picture_zoomin);
		zoomoutBtn = (ImageButton)findViewById(R.id.picture_zoomout);
		closeBtn = (ImageButton)findViewById(R.id.picture_close);
		
		
		pictureFilename = this.getIntent().getExtras().getString(PictureListActivity.SELECTED_PICTURE);
		if(pictureFilename != null){
			Bitmap bitmap = BitmapUtil.getBitmapFromFile(pictureFilename);
			if(bitmap!=null){
				pictureView.setPicture(bitmap);
			}
		}
		
		zoominBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				pictureView.zoomIn();
			}
			
		});
		
		zoomoutBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				pictureView.zoomOut();
			}
			
		});
		
		closeBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View view) {
				PictureActivity.this.finish();
			}
			
		});
		
	}
}
