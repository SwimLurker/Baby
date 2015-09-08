package org.slstudio.baby.game.tetris;

import org.slstudio.baby.game.ICustomizedGameListener;

public interface ITetrisListener extends ICustomizedGameListener{
	public void onNewTetromino();
	public void onTetrominoMove();
	public void onTetrominoRotated();
	public void onLineCleaned(int lineNumber);
	public void onLinesCleaned(int cleanedLineNumber);
}
