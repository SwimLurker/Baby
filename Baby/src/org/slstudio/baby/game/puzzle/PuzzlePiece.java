package org.slstudio.baby.game.puzzle;

import android.graphics.Bitmap;

public class PuzzlePiece {
	public static final int INDEX_BLANKPIECE = -1;
	
	private int index = -1;
	private int imageIndex = -1;
	private Bitmap picture = null;
	
	
	public PuzzlePiece(int imageIndex) {
		super();
		this.imageIndex = imageIndex;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}

	public int getIndex() {
		return index;
	}
	
	public Bitmap getPicture() {
		return picture;
	}
	
	public void setPicture(Bitmap picture) {
		this.picture = picture;
	}

	public int getImageIndex() {
		return imageIndex;
	}

	
}
