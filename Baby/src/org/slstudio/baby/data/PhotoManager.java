package org.slstudio.baby.data;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.slstudio.baby.data.DataPackageManager;
import org.slstudio.baby.util.BitmapUtil;

import android.graphics.Bitmap;


public class PhotoManager {
	public static final int BLOCKIMAGE_WIDTH = 64;
	public static final int BLOCKIMAGE_HEIGHT = 64;
	
	
	
	public static PhotoManager _instance = null;

	private boolean bPhotoLoaded = false;
	
	private boolean bBlockImageLoaded = false;
	
	private static Object lock = new Object();
	
	private HashMap<String, String> photos = new HashMap<String, String>();
	
	private HashMap<String, String> blockImages = new HashMap<String, String>();
	
	private PhotoManager(){
	}
	
	public static PhotoManager getInstance(){
		if(_instance == null){
			_instance = new PhotoManager();
		}
		return _instance;
	}
	
	public void reload(){
		synchronized(lock){
			photos.clear();
			blockImages.clear();
			bPhotoLoaded = false;
			bBlockImageLoaded = false;
		}
	}
	
	protected boolean loadPhotosInfo(){
		synchronized(lock){			
			bPhotoLoaded =  loadImagesInfo(new File(DataPackageManager.getInstance().getPhotoDirAbsolutePath()), photos);
		}
		return bPhotoLoaded;
	}
	
	protected boolean loadBlockImagesInfo(){
		synchronized(lock){			
			bBlockImageLoaded =  loadImagesInfo(new File(DataPackageManager.getInstance().getBlockImageDirAbsolutePath()), blockImages);
		}
		return bBlockImageLoaded;
	}


	private boolean loadImagesInfo(File rootDir, HashMap<String, String> imageMap) {
		if(!rootDir.exists()||(!rootDir.isDirectory())){
			return false;
		}
		
		File[] subFiles = rootDir.listFiles();
		for(File f: subFiles){
			if(f.isDirectory()){
				loadImagesInfo(f, imageMap);
			}else if(f.isFile()){
				String filename = getFilename(f);
				if(filename != null){
					imageMap.put(filename.toLowerCase(), f.getAbsolutePath());
				}
			}	
		}
		
		return true;
	}
	
	private String getFilename(File file){
		int pos = -1;
		String filename = file.getName();
		if((pos = filename.lastIndexOf(".")) != -1){
			String fname = filename.substring(0, pos);
			return fname;
		}else{
			return filename;
		}
		
	}

	public List<String> getAllPhotos() {
		
		if(!bPhotoLoaded){
			loadPhotosInfo();
		}
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(photos.values());
		return result;
	}

	public void createBlockImages() throws IOException {
		if(!bPhotoLoaded){
			loadPhotosInfo();
		}
		
		String blockImageDir = DataPackageManager.getInstance().getBlockImageDirAbsolutePath();
		File dir = new File(blockImageDir);
		if(!dir.exists()){
			dir.mkdirs();
		}
		Set<String> keys = photos.keySet();
		for(String filename:keys){
			String originalFullname = photos.get(filename);
			String targetFullname= blockImageDir + filename + ".png";
			createBlockImage(originalFullname, targetFullname);		
			blockImages.put(filename, targetFullname);
		}
		bBlockImageLoaded = true;
		
	}
	
	
	public List<String> getAllBlockImages(){
		if(!bBlockImageLoaded){
			loadBlockImagesInfo();
		}
		ArrayList<String> result = new ArrayList<String>();
		result.addAll(blockImages.values());
		return result;
	}
	
	private void createBlockImage(String originalFullname, String targetFullname) throws IOException {
		
		File f = new File(targetFullname);
		if(!f.exists()){
			Bitmap blockBitmap = BitmapUtil.getImageThumbnail(originalFullname, BLOCKIMAGE_WIDTH, BLOCKIMAGE_HEIGHT);
			if(blockBitmap == null){
				throw new IOException("create block image failed");
			}
			BitmapUtil.writeBitmapToFile(blockBitmap, targetFullname);
		}
	}

}
