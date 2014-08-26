package org.slstudio.baby.game.puzzle;

import org.slstudio.baby.game.IGameProfile;
import org.slstudio.baby.game.IGameProfileFactory;

public class PuzzleProfileFactory implements IGameProfileFactory{

	@Override
	public IGameProfile getProfile(int level) {
		switch(level){
		case IGameProfile.LEVEL_EASY:
			return PuzzleProfile.EASY;
		case IGameProfile.LEVEL_NORMAL:
			return PuzzleProfile.NORMAL;
		case IGameProfile.LEVEL_HARD:
			return PuzzleProfile.HARD;
		default:
			return null;
		}		
	}

}
