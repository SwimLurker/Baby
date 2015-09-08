package org.slstudio.baby.game.tetris;

import org.slstudio.baby.game.IGameProfile;
import org.slstudio.baby.game.IGameProfileFactory;

public class TetrisProfileFactory implements IGameProfileFactory<TetrisProfile>{
	
	@Override
	public TetrisProfile getProfile(int level) {
		switch(level){
		case IGameProfile.LEVEL_EASY:
			return TetrisProfile.EASY;
		case IGameProfile.LEVEL_NORMAL:
			return TetrisProfile.NORMAL;
		case IGameProfile.LEVEL_HARD:
			return TetrisProfile.HARD;
		default:
			return null;
		}		
	}
}
