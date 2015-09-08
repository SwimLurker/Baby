package org.slstudio.baby.game.rsp;

import org.slstudio.baby.game.ICustomizedGameListener;

public interface IRSPListener extends ICustomizedGameListener{
	public void onRoundStarted(int roundNumber);

	public void onP1Ready();
	
	public void onP2Ready();
	
	public void onFight();

	public void onRoundFinished(int roundNumber, RSPResult result);
	
	
}
