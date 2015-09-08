package org.slstudio.baby.game.rsp;

import org.slstudio.baby.game.IGameProfile;

public class RSPProfile implements IGameProfile{
	public static final RSPProfile EASY = new RSPProfile(1);
	public static final RSPProfile NORMAL = new RSPProfile(3);
	public static final RSPProfile HARD = new RSPProfile(5);
	
	private int maxRound = 1;

	public RSPProfile(int maxRound) {
		super();
		this.maxRound = maxRound;
	}

	public int getMaxRound() {
		return maxRound;
	}
	

}
