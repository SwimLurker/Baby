package org.slstudio.baby.data;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.os.Environment;

public class DataPackageManager {
	private static DataPackageManager _instance = null;

	public static final String PACKAGE_NAME = "org.slstudio.baby";
	public static final String DATAPACKAGE_DIR = "/data"
			+ Environment.getDataDirectory().getAbsolutePath() + "/"
			+ PACKAGE_NAME + "/";

	public static final String DATA_PACKAGE_NAME = "data.zip";
	public static final String PHOTO_DIR = "photos";
	public static final String BLOCKIMAGE_DIR = "blockimages";
	public static final String BACKUP_DIR = "bak";
	public static final String PROPERTIES_FILENAME = "conf.properties";
	
	
	private DataPackageManager() {
	}

	public static DataPackageManager getInstance() {
		if (_instance == null) {
			_instance = new DataPackageManager();
		}
		return _instance;
	}

	
	public void unpackDataPackageFromAssets(Context context, boolean bOverwrite) throws IOException {
		unpackDataPackage(context.getResources().getAssets().open(DATA_PACKAGE_NAME), bOverwrite);
	}
	
	public void unpackDataPackageFromInputStream(InputStream dataPackageIS, boolean bOverwrite)throws Exception {
		backupDataFiles();
		try{
			unpackDataPackage(dataPackageIS, bOverwrite);		
		}catch(Exception exp){
			rollbackDataFiles();
			throw exp;
		}
	}
	
	private void backupDataFiles(){
		File f = new File(DATAPACKAGE_DIR);
		if(f.exists() && f.isDirectory()){
			File backDir = new File(DATAPACKAGE_DIR + BACKUP_DIR + File.separator);
			delete(backDir);
			backDir.mkdirs();
			
			File[] files = f.listFiles();
			for(File sf: files){
				if(sf.isDirectory()){
					if(!sf.getName().equalsIgnoreCase(BACKUP_DIR)){
						sf.renameTo(new File(DATAPACKAGE_DIR + BACKUP_DIR + File.separator + sf.getName()));
					}
					
				}else if(sf.isFile()){
					if(!sf.getName().equalsIgnoreCase(PROPERTIES_FILENAME)){
						sf.renameTo(new File(DATAPACKAGE_DIR + BACKUP_DIR + File.separator + sf.getName()));
					}
				}
			}
		}
		deleteDataFiles();
		
	}
	
	private void rollbackDataFiles(){
		File f = new File(DATAPACKAGE_DIR + BACKUP_DIR + File.separator);
		if(f.exists() && f.isDirectory()){
			deleteDataFiles();
			
			File[] files = f.listFiles();
			for(File sf: files){
				if(sf.isDirectory()){
					sf.renameTo(new File(DATAPACKAGE_DIR + sf.getName()));
				}else if(sf.isFile()){
					sf.renameTo(new File(DATAPACKAGE_DIR + sf.getName()));
				}
			}
			
			delete(f);
		}
	}
	
	private void deleteDataFiles(){
		File f = new File(DATAPACKAGE_DIR);
		if(f.exists() && f.isDirectory()){
			File[] files2 = f.listFiles();
			for(File sf: files2){
				if(sf.isDirectory()&&(sf.getName().equalsIgnoreCase(BACKUP_DIR))){
					continue;
				}
				if(sf.isFile() && (sf.getName().equalsIgnoreCase(PROPERTIES_FILENAME))){
					continue;
				}
				delete(sf);
			}
		}
	}
	
	private void delete(File f){
		if(f.isFile()){
			f.delete();
		}else if(f.isDirectory()){
			File[] childFiles = f.listFiles();
			if(childFiles == null || childFiles.length == 0){
				f.delete();
			}else{
				for(File ff:childFiles){
					delete(ff);
				}
				f.delete();
			}
			
		}
	}

	private void unpackDataPackage(InputStream dataPackageIS, boolean bOverwrite)
			throws IOException {
		
		File photoDir = new File(getPhotoDirAbsolutePath());
		
		if (photoDir.exists()&&(!bOverwrite)) {
			return;
		}

		File file = new File(DATAPACKAGE_DIR);
		
		if (!file.exists()) {
			file.mkdirs();
		}
		ZipInputStream zis = new ZipInputStream(dataPackageIS);

		ZipEntry zipEntry = zis.getNextEntry();

		byte[] buf = new byte[1024 * 32];

		int count = 0;
		while (zipEntry != null) {

			file = new File(DATAPACKAGE_DIR + zipEntry.getName());
			if (zipEntry.isDirectory()) {
				file.mkdirs();
			} else {
				int index = zipEntry.getName().lastIndexOf("/");
				if(index != -1){
					File df = new File(DATAPACKAGE_DIR + zipEntry.getName().substring(0, index));
					df.mkdirs();
				}
				file.createNewFile();
				FileOutputStream fos = new FileOutputStream(file);
				while ((count = zis.read(buf)) > 0) {
					fos.write(buf, 0, count);
				}
				fos.close();
			}

			zipEntry = zis.getNextEntry();
		}
		zis.close();
	}

	
	public String getPhotoDirAbsolutePath() {
		return DATAPACKAGE_DIR + PHOTO_DIR + File.separator;
	}
	
	public String getBlockImageDirAbsolutePath() {
		return DATAPACKAGE_DIR + BLOCKIMAGE_DIR + File.separator;
	}
	
	public String getPropertiesFilename(){
		return DATAPACKAGE_DIR + PROPERTIES_FILENAME;
	}
}
