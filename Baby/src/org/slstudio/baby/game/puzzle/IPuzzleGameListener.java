package org.slstudio.baby.game.puzzle;

import org.slstudio.baby.game.ICustomizedGameListener;

public interface IPuzzleGameListener extends ICustomizedGameListener {
	public void onPieceMoved(Puzzle puzzle, int from, int to);
}
