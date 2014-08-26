package org.slstudio.baby.game.lianliankan;

import org.slstudio.baby.game.IGameProfile;
import org.slstudio.baby.game.IGameProfileFactory;

public class LianLianKanProfileFactory implements IGameProfileFactory{
	
	@Override
	public IGameProfile getProfile(int level) {
		switch(level){
		case IGameProfile.LEVEL_EASY:
			return LianLianKanProfile.EASY;
		case IGameProfile.LEVEL_NORMAL:
			return LianLianKanProfile.NORMAL;
		case IGameProfile.LEVEL_HARD:
			return LianLianKanProfile.HARD;
		default:
			return null;
		}		
	}
}
