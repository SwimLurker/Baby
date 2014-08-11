package org.slstudio.baby.game;

import java.util.ArrayList;
import java.util.List;

import org.slstudio.baby.game.GameException;
import org.slstudio.baby.game.IGameListener;

public abstract class AbstractGame {
	public static final int GAME_NOTSTART = 0;
	public static final int GAME_FINISHED = 1;
	public static final int GAME_OVER=2;
	
	private boolean isPaused = false;
	private boolean isStarted = false;
	private int gameResult = GAME_NOTSTART;
	
	private List<IGameListener> listeners = new ArrayList<IGameListener>();
	
	
	public void initGame() throws GameException{
		isStarted = false;
		gameResult = GAME_NOTSTART;
		isPaused = false;
	}
	
	public boolean isStarted() {
		return isStarted;
	}
	
	public boolean isRunning() {
		return isStarted && !isPaused;
	}
	
	public boolean isPaused() {
		return isStarted && isPaused;
	}
	
	public boolean isGameOver(){
		return !isStarted && (gameResult == GAME_OVER);
	}
	
	public boolean isGameSucceed(){
		return !isStarted && (gameResult == GAME_FINISHED);
	}
	
	public void addListener(IGameListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(IGameListener listener){
		listeners.remove(listener);
	}
	
	public void pause() {
		if(isStarted){
			isPaused = true;
			for(IGameListener listener: listeners){
				listener.onPaused(this);
			}
		}
	}
	
	public void resume(){
		if(isStarted){
			isPaused = false;
			for(IGameListener listener: listeners){
				listener.onResumed(this);
			}
		}
	}
	
	public void gameOver() {
		isStarted = false;
		gameResult = GAME_OVER;
		
		for(IGameListener listener: listeners){
			listener.onGameOver(this);
		}
	}
	
	public void gameWin(){
		isStarted = false;
		gameResult = GAME_FINISHED;
		for(IGameListener listener: listeners){
			listener.onFinished(this);
		}
	}
	
	public void start(){
		isStarted = true;
		
		for(IGameListener listener: listeners){
			listener.onStarted(this);
		}
	}
	
	public void stop() {
		isStarted = false;
		gameResult = GAME_NOTSTART;
	
		for(IGameListener listener: listeners){
			listener.onStopped(this);
		}
	}
	
	public abstract void gameChecking();
	

}
