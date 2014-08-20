package org.slstudio.baby.game.tetris;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slstudio.baby.game.AbstractGame;
import org.slstudio.baby.game.GameException;
import org.slstudio.baby.game.tetris.tetromino.ITetromino;
import org.slstudio.baby.game.tetris.tetromino.JTetromino;
import org.slstudio.baby.game.tetris.tetromino.LTetromino;
import org.slstudio.baby.game.tetris.tetromino.OTetromino;
import org.slstudio.baby.game.tetris.tetromino.STetromino;
import org.slstudio.baby.game.tetris.tetromino.TTetromino;
import org.slstudio.baby.game.tetris.tetromino.Tetromino;
import org.slstudio.baby.game.tetris.tetromino.ZTetromino;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class TetrisGame extends AbstractGame{
	public static final String TAG = "TetrisGame";
	
	public static final int MAP_COL_SIZE = 10;
	public static final int MAP_ROW_SIZE = 20;
	
	public static final int MSG_NEW_TETROMINO = 0;
	public static final int MSG_TETROMINO_MOVE = 1;
	
	public static final long TETROMINO_NEW_INTERVAL = 1000;
	public static final long TETROMINO_MOVE_INTERVAL = 1200;
	public static final long TETROMINO_MOVE_INTERVAL_RESEND = 400;
	
	public static final int TETROMINO_L = 0;
	public static final int TETROMINO_J = 1;
	public static final int TETROMINO_S = 2;
	public static final int TETROMINO_Z = 3;
	public static final int TETROMINO_T = 4;
	public static final int TETROMINO_O = 5;
	public static final int TETROMINO_I = 6;
	public static final int NUMBER_OF_TETROMINO_TYPE = 7;
	
	
	
	private TetrisMap currentMap = null;
	private TetrisMap lastMoveMap = null;
	private TetrisMap oldMap = null;
	
	private Tetromino currentTetromino = null;
	
	private Tetromino nextTetromino = null;
	
	private Random random = new Random();
	
	private int tetrominoCount = 200;
	
	private List<ITetrisListener> tetrisListener = new ArrayList<ITetrisListener>();
	
	
	private Object lockObj = new Object();
	
	private Handler handler = new Handler(){

		@Override
		public void handleMessage(Message msg) {
			if(msg.what == MSG_TETROMINO_MOVE){
				Log.d(TAG, "receive move tetromino message");
				lastMoveMap.copyFrom(currentMap);
				gameMove();
				
			}else if (msg.what == MSG_NEW_TETROMINO){
				Log.d(TAG, "receive new tetromino message");
				
				currentMap.copyFrom(lastMoveMap);
				gameChecking();
				if(isRunning()){
					oldMap.copyFrom(currentMap);
					
					currentTetromino = getNewTetromino();
					if(currentTetromino.isAvailable(currentMap)){
						currentTetromino.putOnMap(currentMap);
						
						for(ITetrisListener listener: tetrisListener){
							listener.onNewTetromino();
						}
						this.removeMessages(MSG_TETROMINO_MOVE);
						this.sendEmptyMessageDelayed(MSG_TETROMINO_MOVE, TETROMINO_MOVE_INTERVAL);
						
					}else{
						gameOver();
					}
				}
				
			}
		}
		
	};
	
	public void addCustomizedListener(ITetrisListener listener){
		tetrisListener.add(listener);
	}
	
	public void removeCustomizedListener(ITetrisListener listener){
		tetrisListener.remove(listener);
	}
	
	public TetrisMap getCurrentMap() {
		return currentMap;
	}

	
	public Tetromino getCurrentTetromino() {
		return currentTetromino;
	}

	
	public Tetromino getNextTetromino() {
		return nextTetromino;
	}

	@Override
	public void initGame() throws GameException{
		currentMap = new TetrisMap(MAP_ROW_SIZE, MAP_COL_SIZE);
		lastMoveMap = new TetrisMap(MAP_ROW_SIZE, MAP_COL_SIZE);
		oldMap = new TetrisMap(MAP_ROW_SIZE, MAP_COL_SIZE);
	}

	@Override
	public void gameChecking() {
		clearLines();
		if(tetrominoCount == 0){
			gameWin();
		}
	}
	
	private void clearLines() {
		int cleanedLine = -1;
		while((cleanedLine = currentMap.clearLine())!= -1){
			for(ITetrisListener listener: tetrisListener){
				listener.onLineCleaned(cleanedLine);
			}
		}
		oldMap.copyFrom(currentMap);
		lastMoveMap.copyFrom(currentMap);
		
	}

	@Override
	public void start(){
		super.start();
		handler.sendEmptyMessage(MSG_NEW_TETROMINO);
	}
	
	private Tetromino getNewTetromino(){
		
		Tetromino current = null;
		if(nextTetromino == null){
			current = createRandomTetromino();
		}else{
			current = nextTetromino;
		}
		nextTetromino = createRandomTetromino();
		tetrominoCount--;
		
		return current;
	}
	
	private Tetromino createRandomTetromino() {
		Tetromino result = null;
		
		int type = random.nextInt(NUMBER_OF_TETROMINO_TYPE);
		switch(type){
		case TETROMINO_L:
			result = new LTetromino();
			break;
		case TETROMINO_J:
			result = new JTetromino();
			break;
		case TETROMINO_S:
			result = new STetromino();
			break;
		case TETROMINO_Z:
			result = new ZTetromino();
			break;
		case TETROMINO_T:
			result = new TTetromino();
			break;
		case TETROMINO_O:
			result = new OTetromino();
			break;
		case TETROMINO_I:
			result = new ITetromino();
			break;	
		}
		
		
		int color = random.nextInt(Tetromino.NUMBER_OF_COLOR) + 1;
		result.setColor(color);
		result.setX(0);
		result.setY(0);
		
		int col = result.getFirstBlockColIndex();
		int row = result.getFirstBlockRowIndex();
		
		result.setX(4 - col);
		result.setY(0 - row);
		
		return result;
	}

	public boolean rotateTetromino() {
		Log.d(TAG, "rotate tetromino");
		synchronized(lockObj){
			if(currentTetromino.rotate(oldMap)){
				currentMap.copyFrom(oldMap);
				currentTetromino.putOnMap(currentMap);
				lastMoveMap.copyFrom(currentMap);
		
				for(ITetrisListener listener: tetrisListener){
					listener.onTetrominoRotated();
				}
				return true;
			}
			return false;
		}
	}

	public boolean moveTetrominoLeft() {
		Log.d(TAG, "move tetromino to left");
		synchronized(lockObj){
			if(currentTetromino.moveLeft(oldMap)){
				currentMap.copyFrom(oldMap);
				currentTetromino.putOnMap(currentMap);
				lastMoveMap.copyFrom(currentMap);
				
				//if old position will collision but new moved position will not, then need remove the new tetromino message and send tetromino move message=
				if(!currentTetromino.isCollisionY(currentTetromino.getY() + 1, currentTetromino.getSharp(), currentMap)){
					if(handler.hasMessages(MSG_NEW_TETROMINO)){
						handler.removeMessages(MSG_NEW_TETROMINO);
						handler.sendEmptyMessageDelayed(MSG_TETROMINO_MOVE, TETROMINO_MOVE_INTERVAL_RESEND);
					}
				}
				
				for(ITetrisListener listener: tetrisListener){
					listener.onTetrominoMove();;
				}
				return true;
			}
			return false;
		}
	}
	
	public boolean moveTetrominoRight() {
		Log.d(TAG, "move tetromino to right");
		synchronized(lockObj){
			if(currentTetromino.moveRight(oldMap)){
				currentMap.copyFrom(oldMap);
				currentTetromino.putOnMap(currentMap);
				lastMoveMap.copyFrom(currentMap);
				
				//if old position will collision but new moved position will not, then need remove the new tetromino message and send tetromino move message=
				if(!currentTetromino.isCollisionY(currentTetromino.getY() + 1, currentTetromino.getSharp(), currentMap)){
					if(handler.hasMessages(MSG_NEW_TETROMINO)){
						handler.removeMessages(MSG_NEW_TETROMINO);
						handler.sendEmptyMessageDelayed(MSG_TETROMINO_MOVE, TETROMINO_MOVE_INTERVAL_RESEND);
					}
				}
				
				for(ITetrisListener listener: tetrisListener){
					listener.onTetrominoMove();;
				}
				return true;
			}
			return false;
		}
	}
	
	public boolean moveTetrominoDown() {
		Log.d(TAG, "move tetromino down");
		synchronized(lockObj){
			if(currentTetromino.moveDown(oldMap)){
				currentMap.copyFrom(oldMap);
				currentTetromino.putOnMap(currentMap);
				lastMoveMap.copyFrom(currentMap);
				
				for(ITetrisListener listener: tetrisListener){
					listener.onTetrominoMove();;
				}
				return true;
			}else{
				return false;
			}
		}
	}
	
	private void gameMove(){
		if(moveTetrominoDown()){
			handler.removeMessages(MSG_TETROMINO_MOVE);
			handler.sendEmptyMessageDelayed(MSG_TETROMINO_MOVE, TETROMINO_NEW_INTERVAL);
		}else{
			handler.sendEmptyMessageDelayed(MSG_NEW_TETROMINO, TETROMINO_NEW_INTERVAL);
		}
	}

	public void dropTetromino() {
		Log.d(TAG, "drop tetromino down");
		synchronized(lockObj){
			while(currentTetromino.moveDown(oldMap)){
				currentMap.copyFrom(oldMap);
				currentTetromino.putOnMap(currentMap);
				lastMoveMap.copyFrom(currentMap);
				
				for(ITetrisListener listener: tetrisListener){
					listener.onTetrominoMove();;
				}
			}
		}
	}
}
