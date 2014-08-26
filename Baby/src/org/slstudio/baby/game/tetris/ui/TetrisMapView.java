package org.slstudio.baby.game.tetris.ui;

import org.slstudio.baby.R;
import org.slstudio.baby.game.tetris.TetrisGame;
import org.slstudio.baby.game.tetris.tetromino.Tetromino;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TetrisMapView extends TileView {
	
	
	public static final int DEFAULT_MOVE_SENSITIVITY = 20;
	public static final int DEFAULT_DROP_SENSITIVITY = 30;
	public static final int DEFAULT_ROTATE_SENSITIVITY = 10;
	
	public static final long DROP_TIME_DELTA = 300;
	
	private static Paint framePaint, frameBKPaint, infoBoxPaint, textPaint;
	
	private TetrisGame game = null;
	
	private int initX, initY, dropInitY;
	private long initTime;
	
	private boolean isMoved = false;

	private int moveSensitivity = DEFAULT_MOVE_SENSITIVITY;
	private int dropSensitivity = DEFAULT_DROP_SENSITIVITY;
	private int rotateSensitivity = DEFAULT_ROTATE_SENSITIVITY;
	
	
	
	static{
		framePaint = new Paint();
		framePaint.setStyle(Paint.Style.STROKE);
		framePaint.setStrokeWidth(2);
		framePaint.setColor(Color.GRAY);
		frameBKPaint = new Paint();
		frameBKPaint.setStyle(Paint.Style.FILL);
		frameBKPaint.setColor(Color.BLACK);
		frameBKPaint.setAlpha(128);
		
		infoBoxPaint = new Paint();
		infoBoxPaint.setStyle(Paint.Style.FILL);
		infoBoxPaint.setColor(Color.WHITE);
		infoBoxPaint.setAlpha(100);
		
		textPaint = new Paint();
		textPaint.setTextSize(30);
		textPaint.setTextScaleX(1);
		textPaint.setTextSkewX(0);
		textPaint.setColor(Color.BLACK);
	}
	
	public TetrisMapView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setMarginWidth(0);
		setMarginHeight(0);
	}

	public TetrisMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setMarginWidth(0);
		setMarginHeight(0);
	}

	public TetrisMapView(Context context) {
		super(context);
		setMarginWidth(0);
		setMarginHeight(0);
	}
	
	public void setGame(TetrisGame game) {
		this.game = game;
		setTiles(game.getCurrentMap().getTiles());
		setColNumber(game.getCurrentMap().getColNumber());
		setRowNumber(game.getCurrentMap().getRowNumber());
	}
	
	@Override
	protected void onDraw(Canvas canvas){
		Rect frameRect = new Rect(getMarginWidth(), getMarginHeight(), getViewWidth() - getMarginWidth(), getViewHeight() - getMarginHeight());
		canvas.drawRect(frameRect, framePaint);
		canvas.drawRect(frameRect, frameBKPaint);
		if(game != null && game.isRunning()){
			setTiles(game.getCurrentMap().getTiles());
		}
		
		super.onDraw(canvas);	
	}

	@Override   
	protected void dispatchDraw(Canvas canvas) { 
		super.dispatchDraw(canvas);
		if(game != null){
			if(game.isPaused() || game.isGameOver()){
				String infoText = null;	
				if(game.isPaused()){
					infoText = getContext().getResources().getString(R.string.game_info_gamepaused);
				}else if(game.isGameOver()){
					infoText = getContext().getResources().getString(R.string.game_info_gameover);
				}
				drawInfoBox(canvas, infoText);
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		synchronized(event){
			try {
				event.wait(16);
				
				if(game!=null && game.isRunning()){
					int action = event.getAction() & MotionEvent.ACTION_MASK;
					if(action == MotionEvent.ACTION_DOWN){
						initTime = System.currentTimeMillis();
						initX = (int)Math.floor(event.getX());
						initY = (int)Math.floor(event.getY());
						dropInitY = initY;	
						isMoved = false;
					}else if(action == MotionEvent.ACTION_MOVE){
						int currentX = (int)Math.floor(event.getX());
						int currentY = (int)Math.floor(event.getY());
						
						if((initX - currentX) > moveSensitivity && Math.abs(initY - currentY) < dropSensitivity){
							int moveCount = (initX - currentX) / moveSensitivity;
							initX = currentX;
							isMoved = true;
							for(int i = 0; i < moveCount; i++){
								game.moveTetrominoLeft();
							}
						}else if((currentX - initX) > moveSensitivity && Math.abs(initY - currentY) < dropSensitivity){
							int moveCount = (currentX - initX) / moveSensitivity;
							initX = currentX;
							isMoved = true;
							for(int i = 0; i < moveCount; i++){
								game.moveTetrominoRight();
							}
						}
						
						if((currentY - initY) > moveSensitivity){
							long timeDelta = Math.abs(initTime - System.currentTimeMillis());
							if(timeDelta > DROP_TIME_DELTA){
								dropInitY = currentY;
								initTime = System.currentTimeMillis();
							}
							initY = currentY;
							isMoved = true;
							game.moveTetrominoDown();
						}
					}else if(action == MotionEvent.ACTION_UP){
						long timeDelta = Math.abs(initTime - System.currentTimeMillis());
						int currentY = (int)Math.floor(event.getY());
						if((currentY - dropInitY) > dropSensitivity && timeDelta < DROP_TIME_DELTA){
							game.dropTetromino();
						}else if(!isMoved && Math.abs(currentY - initY)< rotateSensitivity){
							game.rotateTetromino();
						}
					}
				}			
			} catch (InterruptedException e) {
				return true;
			}
			
		}
		return true;
	}
	
	private void drawInfoBox(Canvas canvas, String infoText){
		if(infoText!= null){
			
			//calculate box and draw box
			FontMetrics fontMetrics = textPaint.getFontMetrics(); 
			float fontHeight = fontMetrics.ascent + fontMetrics.descent;
			float textWidth = textPaint.measureText(infoText);
			
			int infoBoxHeight = (int)fontHeight * 6;
			int infoBoxWidth = (int)(textWidth * 1.5f);
					
			Rect pauseRect = new Rect(getViewWidth()/2 - infoBoxWidth/2,
					getViewHeight()/2 - infoBoxHeight /2, 
									getViewWidth()/2 + infoBoxWidth/2,
									getViewHeight()/2 + infoBoxHeight /2);
			canvas.drawRect(pauseRect, infoBoxPaint);
			
			canvas.drawText(infoText, getViewWidth()/2 - textWidth/2, getViewHeight()/2 - fontHeight/2, textPaint);
			
		}
	}
	
}
