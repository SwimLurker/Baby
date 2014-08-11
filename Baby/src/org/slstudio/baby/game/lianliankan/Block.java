package org.slstudio.baby.game.lianliankan;

import android.graphics.Bitmap;

public class Block {
	public static final int EMPTY_IMAGEID = -1;
	
	private int row, col;
	private int imageId;
	private Bitmap bitmap;

	private boolean isEmpty = false;
	
	
	public Block(int row, int col) {
		super();
		this.row = row;
		this.col = col;
	}
	public int getRow() {
		return row;
	}
	public void setRow(int row) {
		this.row = row;
	}
	public int getColumn() {
		return col;
	}
	public void setColumn(int col) {
		this.col = col;
	}
	public int getImageId() {
		return imageId;
	}
	public void setImageId(int imageId) {
		this.imageId = imageId;
	}
	
	public Bitmap getBitmap() {
		return bitmap;
	}

	public void setBitmap(Bitmap bitmap) {
		this.bitmap = bitmap;
	}
	
	public void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}
	
	public boolean isEmpty(){
		return isEmpty;
	}
	

	public String toString(){
		StringBuffer sb = new StringBuffer();
		sb.append("Block -- row:");
		sb.append(row);
		sb.append(",column:");
		sb.append(col);
		sb.append(",image:");
		sb.append(imageId);
		sb.append(",status:");
		sb.append(isEmpty()?"Empty":"Not Empty");
		return sb.toString();
	}
	
	public boolean upSide(Block block) {
		if(block.getColumn() == col && block.getRow()>row){
			return true;
		}
		return false;
	}
	
	public boolean downSide(Block block) {
		if(block.getColumn() == col && block.getRow()<row){
			return true;
		}
		return false;
	}
	
	public boolean leftSide(Block block) {
		if(block.getRow() == row && block.getColumn()>col){
			return true;
		}
		return false;
	}
	
	public boolean rightSide(Block block) {
		if(block.getRow() == row && block.getColumn()<col){
			return true;
		}
		return false;
		
	}
	
	public boolean sameAs(Block block) {
		return row == block.getRow() && col == block.getColumn();
	}
	
}
