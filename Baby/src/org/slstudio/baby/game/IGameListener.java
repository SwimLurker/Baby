package org.slstudio.baby.game;
import org.slstudio.baby.game.AbstractGame;

public interface IGameListener {
	public void onFinished(AbstractGame game);
	public void onPaused(AbstractGame game);
	public void onResumed(AbstractGame game);
	public void onStarted(AbstractGame game);
	public void onStopped(AbstractGame game);
	public void onGameOver(AbstractGame game);
	
}
