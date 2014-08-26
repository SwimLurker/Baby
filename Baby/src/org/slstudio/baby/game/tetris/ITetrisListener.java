package org.slstudio.baby.game.tetris;

public interface ITetrisListener {
	public void onNewTetromino();
	public void onTetrominoMove();
	public void onTetrominoRotated();
	public void onLineCleaned(int lineNumber);
	public void onLinesCleaned(int cleanedLineNumber);
}
