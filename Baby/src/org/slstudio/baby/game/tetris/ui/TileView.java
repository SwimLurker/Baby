package org.slstudio.baby.game.tetris.ui;

import org.slstudio.baby.R;
import org.slstudio.baby.game.tetris.TetrisGame;
import org.slstudio.baby.game.tetris.TetrisMap;
import org.slstudio.baby.game.tetris.tetromino.Tetromino;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

public class TileView extends View {
	
	private int viewWidth, viewHeight;
	
	private int tileWidth,tileHeight;
	
	private int marginWidth, marginHeight;
	
	private int rowNumber, colNumber;
	
	private int[][] tiles ;
	
	public TileView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public TileView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public TileView(Context context) {
		super(context);
	}
	
	public int getViewWidth() {
		return viewWidth;
	}

	public void setViewWidth(int width) {
		this.viewWidth = width;
	}

	public int getViewHeight() {
		return viewHeight;
	}

	public void setViewHeight(int height) {
		this.viewHeight = height;
	}

	public int getTileWidth() {
		return tileWidth;
	}

	public void setTileWidth(int tileWidth) {
		this.tileWidth = tileWidth;
	}

	public int getTileHeight() {
		return tileHeight;
	}

	public void setTileHeight(int tileHeight) {
		this.tileHeight = tileHeight;
	}

	public int getMarginWidth() {
		return marginWidth;
	}

	public void setMarginWidth(int marginWidth) {
		this.marginWidth = marginWidth;
	}

	public int getMarginHeight() {
		return marginHeight;
	}

	public void setMarginHeight(int marginHeight) {
		this.marginHeight = marginHeight;
	}

	public int[][] getTiles() {
		return tiles;
	}

	public void setTiles(int[][] tiles) {
		this.tiles = tiles;
	}

	public int getRowNumber() {
		return rowNumber;
	}

	public void setRowNumber(int rowNumber) {
		this.rowNumber = rowNumber;
	}

	public int getColNumber() {
		return colNumber;
	}

	public void setColNumber(int colNumber) {
		this.colNumber = colNumber;
	}

	public void calculateSize(){
		if(colNumber > 0 && rowNumber > 0){
			tileWidth = (viewWidth - marginWidth) / colNumber;
			tileHeight = (viewHeight - marginHeight) / rowNumber;
			
			tileWidth = Math.min(tileWidth, tileHeight);
			tileHeight = Math.min(tileWidth, tileHeight);
			
			marginWidth = (viewWidth - tileWidth * colNumber) /2;
			marginHeight = (viewHeight - tileHeight * rowNumber) /2;
		}
	}
	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		super.onSizeChanged(w, h, oldw, oldh);
		viewWidth = w;
		viewHeight = h;
		calculateSize();
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		
		for(int row = 0; row < rowNumber; row++){
			for(int col = 0; col < colNumber; col++){
				int tileValue = tiles[row][col];
				if(tileValue!= Tetromino.TILE_BLANK){
					Drawable drawable= getTileDrawable(tileValue);
					if(drawable != null){
						drawable.setBounds(marginWidth + col *tileWidth, marginHeight + row *tileHeight, marginWidth + col *tileWidth + tileWidth, marginHeight + row *tileHeight + tileHeight);
						drawable.draw(canvas);
					}
				}
			}	
		}
	}
	
	private Drawable getTileDrawable(int tileValue) {
		Drawable result = null;
		Resources resources = getContext().getResources();
		switch(tileValue){
		case Tetromino.COLOR_RED:
			result = resources.getDrawable(R.drawable.block_red);
			break;
		case Tetromino.COLOR_ORANGE:
			result = resources.getDrawable(R.drawable.block_orange);
			break;
		case Tetromino.COLOR_YELLOW:
			result = resources.getDrawable(R.drawable.block_yellow);
			break;
		case Tetromino.COLOR_GREEN:
			result = resources.getDrawable(R.drawable.block_green);
			break;
		case Tetromino.COLOR_DARKGREEN:
			result = resources.getDrawable(R.drawable.block_darkgreen);
			break;
		case Tetromino.COLOR_CYAN:
			result = resources.getDrawable(R.drawable.block_cyan);
			break;
		case Tetromino.COLOR_BLUE:
			result = resources.getDrawable(R.drawable.block_blue);
			break;
		case Tetromino.COLOR_DARKBLUE:
			result = resources.getDrawable(R.drawable.block_darkblue);
			break;
		case Tetromino.COLOR_PURPLE:
			result = resources.getDrawable(R.drawable.block_purple);
			break;
		case Tetromino.COLOR_BROWN:
			result = resources.getDrawable(R.drawable.block_brown);
			break;
		
		}
			
		return result;
	}
	
}
