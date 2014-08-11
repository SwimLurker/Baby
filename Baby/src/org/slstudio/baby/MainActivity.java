package org.slstudio.baby;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.slstudio.baby.data.DataPackageManager;
import org.slstudio.baby.data.PhotoManager;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.SimpleAdapter;

public class MainActivity extends Activity {

	private Resources resources = null;
	
	private String texts[] = null;
	private int images[] = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		resources = getResources();
		
		prepareData(this);
		
		images = new int[]{R.drawable.icon_pictures, R.drawable.icon_lianliankan, R.drawable.icon_puzzle};
		texts = new String[]{ resources.getString(R.string.gridview_icontext_pictures), 
				resources.getString(R.string.gridview_icontext_lianliankan),
				resources.getString(R.string.gridview_icontext_puzzle)};
		
		GridView gridView = (GridView)findViewById(R.id.main_gridview);
		
		ArrayList<HashMap<String, Object>> items = new ArrayList<HashMap<String, Object>>();
		for(int i=0; i<images.length; i++){
			HashMap<String, Object> item = new HashMap<String, Object>();
			item.put("itemImage", images[i]);
			item.put("itemText", texts[i]);
			items.add(item);
		}
		
		SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.gridview_item, 
				new String[]{"itemImage", "itemText"}, 
				new int[]{R.id.gridview_item_image, R.id.gridview_item_text});
		
		gridView.setAdapter(adapter);
		gridView.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long rowID) {
				//HashMap<String, Object> item = (HashMap<String, Object>)parent.getItemAtPosition(position);
				switch(images[position]){
				case R.drawable.icon_pictures:
					startPicture();
					break;
				case R.drawable.icon_lianliankan:
					startGame_Lianliankan();
					break;
				case R.drawable.icon_puzzle:
					startGame_Puzzle();
					break;
				}
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_about:
			showAboutDialog();
			break;
		}
		return true;
	}

	private void showAboutDialog() {
    	final View dialogView = getLayoutInflater().inflate(R.layout.dialog_about, null);
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.title_aboutdialog))
        	.setView(dialogView)
        	.setNegativeButton(resources.getString(R.string.lable_closebtn), new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			})
        	.show();
    }

	private void startPicture(){
		Intent picIntent = new Intent();
		picIntent.setAction("org.slstudio.baby.PictureListActivity");
		startActivity(picIntent);
	}
	
	private void startGame_Lianliankan() {
		Intent gameIntent = new Intent();
		gameIntent.setAction("org.slstudio.baby.LianLianKanGameActivity");
		startActivity(gameIntent);
	}
	
	private void startGame_Puzzle() {
		Intent gameIntent = new Intent();
		gameIntent.setAction("org.slstudio.baby.PuzzleGameActivity");
		startActivity(gameIntent);
	}
	
	private void prepareData(Context context){
		unpackDataPackage(context);
		createBlockImages(context);
		
	}
	private void unpackDataPackage(Context context) {
		try{
			DataPackageManager.getInstance().unpackDataPackageFromAssets(context, false);
		}catch(IOException e){
			Log.e("DataPackageManager", "Unpack data files error");
			e.printStackTrace();
		}
	}
	
	private void createBlockImages(Context context){
		try{
			PhotoManager.getInstance().createBlockImages();
		}catch(IOException e){
			Log.e("PhotoManager", "Create block images error");
			e.printStackTrace();
		}
	}
}
