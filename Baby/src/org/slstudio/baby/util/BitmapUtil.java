package org.slstudio.baby.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.os.Environment;
import android.os.StatFs;

public class BitmapUtil {
	
	private static int FREE_SD_SPACE_NEEDED_TO_CACHE = 1;
	private static int MB = 1024 * 1024;
	
	public static Bitmap getBitmapFromFile(String filename){
		FileInputStream fis = null;
		try{
			if(filename == null){
				return null;
			}
			File f = new File(filename);
		
			if(f.exists() && f.isFile()){
				fis = new FileInputStream(f);
				return BitmapFactory.decodeStream(fis);
			}else{
				return null; 
			}			
			
		}catch(Exception exp){
			exp.printStackTrace();
			return null;
		}finally{
			if(fis != null){
				try {
					fis.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	public static Bitmap getBitmapFromResource(Resources resouces, int resId){
		return BitmapFactory.decodeResource(resouces, resId);
	}
	
	public static Bitmap getImageThumbnail(String imagePath, int width, int height){
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		
		options.inJustDecodeBounds = false;
		
		int h = options.outHeight;
		int w = options.outWidth;
		
		if(width<=0){
			width = w;
		}
		if(height<=0){
			height = h;
		}
		
		int beWidth = w / width;
		int beHeight = h / height;
		
		int be = 1;
		if(beWidth < beHeight){
			be = beWidth;
		}else{
			be = beHeight;
		}
		
		if(be <= 0){
			be = 1;
		}
		options.inSampleSize = be;
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, width, height, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
		
	}
	
	public static Bitmap getImageThumbnailWithProportion(String imagePath, int width, int height){
		Bitmap bitmap = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		
		options.inJustDecodeBounds = false;
		
		int h = options.outHeight;
		int w = options.outWidth;
		
		int newWidth = width;
		int newHeight = height;
		
		if(width<=0){
			width = w;
		}
		if(height<=0){
			height = h;
		}
		
		float beWidth = (float)w / (float)width;
		float beHeight = (float)h / (float)height;
		
		float be = 1f;
		if(beWidth < beHeight){
			be = beWidth;
			newHeight = (int)((float)h/be);
		}else{
			be = beHeight;
			newWidth = (int)((float)w/be);
		}
		
		if(be <= 0){
			be = 1;
		}
		options.inSampleSize = (int)be;
		bitmap = BitmapFactory.decodeFile(imagePath, options);
		
		bitmap = ThumbnailUtils.extractThumbnail(bitmap, newWidth, newHeight, ThumbnailUtils.OPTIONS_RECYCLE_INPUT);
		return bitmap;
		
	}

	public static void writeBitmapToFile(Bitmap bitmap, String filename) {
		if(FREE_SD_SPACE_NEEDED_TO_CACHE > freeSpaceOnSd()){
			return;
		}
		if(!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())){
			return;
		}
		
		File file = new File(filename);
		try{
			file.createNewFile();
			OutputStream os = new FileOutputStream(file);
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
			os.flush();
			os.close();
		}catch(FileNotFoundException fnfExp){
			
		}catch(IOException ioExp){
			ioExp.printStackTrace();
		}
	}
	
	private static int freeSpaceOnSd(){
		StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
		double sdFreeMB = ((double)stat.getAvailableBlocks() * (double)stat.getBlockSize()) / MB;
		
		return (int)sdFreeMB;
	}
}
