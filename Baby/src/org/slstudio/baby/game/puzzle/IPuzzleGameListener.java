package org.slstudio.baby.game.puzzle;

public interface IPuzzleGameListener {
	public void onPieceMoved(Puzzle puzzle, int from, int to);
}
