package org.slstudio.baby.game;


public interface IGameTimerListener<T extends TimeableGame> {
	public void onTimeLeftChanged(T timeableGame, int timeLeft);
	
}
