package org.slstudio.baby.game.rsp;

import org.slstudio.baby.game.IGameProfile;
import org.slstudio.baby.game.IGameProfileFactory;

public class RSPProfileFactory implements IGameProfileFactory<RSPProfile>{

	@Override
	public RSPProfile getProfile(int level) {
		switch(level){
		case IGameProfile.LEVEL_EASY:
			return RSPProfile.EASY;
		case IGameProfile.LEVEL_NORMAL:
			return RSPProfile.NORMAL;
		case IGameProfile.LEVEL_HARD:
			return RSPProfile.HARD;
		default:
			return null;
		}		
	}

}
