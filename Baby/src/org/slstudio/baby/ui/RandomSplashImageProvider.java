package org.slstudio.baby.ui;

import java.util.Random;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;

public class RandomSplashImageProvider implements ISplashImageProvider {
	
	private Resources resources = null;
	private int[] imageIDs = null;
	private Random r = new Random();
	
	public RandomSplashImageProvider(Resources resources, int[] imageIDs) {
		super();
		this.imageIDs = imageIDs;
		this.resources = resources;
		r.setSeed(System.currentTimeMillis());
	}


	public int[] getImageIDs() {
		return imageIDs;
	}

	public void setImageIDs(int[] imageIDs) {
		this.imageIDs = imageIDs;
	}


	@Override
	public Drawable getSplashImage() {
		int imageSize = imageIDs.length;
		int index = ((r.nextInt()%imageSize) + imageSize)%imageSize;
		int imageID = imageIDs[index];
		return resources.getDrawable(imageID);
		
	}

}
