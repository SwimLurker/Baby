package org.slstudio.baby.game.tetris.ui;

import org.slstudio.baby.game.tetris.tetromino.Tetromino;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;

public class TetrominoView extends TileView{
	
	private static Paint framePaint = null;
	
	private Tetromino tetromino = null;
	

	static{
		framePaint = new Paint();
		framePaint.setStyle(Paint.Style.STROKE);
		framePaint.setStrokeWidth(2);
		framePaint.setColor(Color.GRAY);
	}
	
	public TetrominoView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setMarginWidth(2);
		setMarginHeight(2);
	}

	public TetrominoView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setMarginWidth(2);
		setMarginHeight(2);
	}

	public TetrominoView(Context context) {
		super(context);
		setMarginWidth(2);
		setMarginHeight(2);
	}

	public Tetromino getTetromino() {
		return tetromino;
	}

	public void setTetromino(Tetromino tetromino) {
		this.tetromino = tetromino;
		if(tetromino != null){
			setColNumber(tetromino.getSize());
			setRowNumber(tetromino.getSize());
			setTiles(tetromino.getSharp());
			calculateSize();
		}
	}
	

	@Override
	protected void onDraw(Canvas canvas){
		Rect frameRect = new Rect(getMarginWidth(), getMarginHeight(), getViewWidth() - getMarginWidth(), getViewHeight() - getMarginHeight());
		canvas.drawRect(frameRect, framePaint);
		super.onDraw(canvas);	
	}
}
