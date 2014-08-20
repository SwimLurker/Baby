package org.slstudio.baby.game.tetris.ui;

import org.slstudio.baby.game.tetris.TetrisGame;
import org.slstudio.baby.game.tetris.tetromino.Tetromino;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;

public class TetrisMapView extends TileView {
	
	
	public static final int DEFAULT_MOVE_SENSITIVITY = 20;
	public static final int DEFAULT_DROP_SENSITIVITY = 30;
	public static final int DEFAULT_ROTATE_SENSITIVITY = 10;
	
	public static final long DROP_TIME_DELTA = 300;
	
	private static Paint framePaint = null;
	
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
	}
	
	public TetrisMapView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		setMarginWidth(20);
		setMarginHeight(20);
	}

	public TetrisMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		setMarginWidth(20);
		setMarginHeight(20);
	}

	public TetrisMapView(Context context) {
		super(context);
		setMarginWidth(20);
		setMarginHeight(20);
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
		if(game != null && game.isRunning()){
			setTiles(game.getCurrentMap().getTiles());
		}
		super.onDraw(canvas);	
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
	
	
}
