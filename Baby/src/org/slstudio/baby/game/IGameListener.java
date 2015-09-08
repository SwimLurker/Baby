package org.slstudio.baby.game;
import org.slstudio.baby.game.AbstractGame;

public interface IGameListener<T extends AbstractGame> {
	public void onFinished(T game);
	public void onPaused(T game);
	public void onResumed(T game);
	public void onStarted(T game);
	public void onStopped(T game);
	public void onGameOver(T game);
	
}
