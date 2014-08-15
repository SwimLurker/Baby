package org.slstudio.baby.game.puzzle;

import java.util.List;

import org.slstudio.baby.game.GameException;

public interface IPuzzleAlgorithm {
	public List<DIRECTION> calculate(IProgressListener listener) throws GameException;
	
	public void cancel();
	
	public boolean isCancelled();
}
