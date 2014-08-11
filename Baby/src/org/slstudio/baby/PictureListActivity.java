package org.slstudio.baby;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.slstudio.baby.data.PhotoManager;
import org.slstudio.baby.ui.PictureThumbnailDecorator;
import org.slstudio.baby.util.BitmapUtil;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.TextView;

public class PictureListActivity extends Activity {

	public static final int PHOTO_THUMBNAIL_WIDTH = 120;
	public static final int PHOTO_THUMBNAIL_HEIGHT = 90;
	
	public static final String SELECTED_PICTURE = "org.slstudio.baby.SELECTED_PICTURE";
	
	private Resources resources = null;
	
	private List<String> photoFilenames = null;
	private HashMap<String, Bitmap> photoCache = new HashMap<String, Bitmap>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_picturelist);
		resources = getResources();
		
		photoFilenames = PhotoManager.getInstance().getAllPhotos(); 
		
		GridView gridView = (GridView)findViewById(R.id.picture_gridview);
		
		ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
		for(int i=0; i<photoFilenames.size(); i++){
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put("itemImage", photoFilenames.get(i));
			//item.put("itemImage", texts[i]);
			items.add(item);
		}
		
		SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.picture_gridview_item, 
				new String[]{"itemImage"}, 
				new int[]{R.id.picture_gridview_item_image});
		
		adapter.setViewBinder(new ViewBinder(){

			@Override
			public boolean setViewValue(View view, Object data,	String textRepresentation) {
				if(view instanceof ImageView && data instanceof String){
					ImageView iv = (ImageView)view;
					String filename = (String)data;
					Bitmap bitmap = getPhotoThumbnailBitmap(filename);
					iv.setImageBitmap(bitmap);
					return true;
				}
				return false;
			}
			
		});
		
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowID) {
				HashMap<String, Object> item = (HashMap<String, Object>) parent.getItemAtPosition(position);
				String filename = (String)item.get("itemImage");
				Intent intent = new Intent();
				intent.putExtra(SELECTED_PICTURE, filename);
				intent.setAction("org.slstudio.baby.PictureActivity");
				startActivity(intent);
			}
		});
	}

	private Bitmap getPhotoThumbnailBitmap(String filename) {
		
		if(!photoCache.containsKey(filename)){

			PictureThumbnailDecorator decorator = new PictureThumbnailDecorator();
			
			Bitmap pic = BitmapUtil.getImageThumbnail(filename, PHOTO_THUMBNAIL_WIDTH, PHOTO_THUMBNAIL_HEIGHT);
			
			if(pic!=null){
				Bitmap decoPic = decorator.decorateImage(pic);
				if(decoPic != null){
					photoCache.put(filename, decoPic);
				}
			}
		}
		return photoCache.get(filename);
		
	}

}