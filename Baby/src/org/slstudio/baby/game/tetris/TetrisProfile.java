package org.slstudio.baby.game.tetris;

import org.slstudio.baby.game.IGameProfile;


public class TetrisProfile implements IGameProfile {
	public static final TetrisProfile EASY = new TetrisProfile(1.0f, 50);
	public static final TetrisProfile NORMAL = new TetrisProfile(1f, 200);
	public static final TetrisProfile HARD = new TetrisProfile(2f, 500);
	
	private float moveSpeed = 1.0f;
	private int tetrominoNumber = 200;

	public TetrisProfile(float moveSpeed, int tetrominoNumber) {
		super();
		this.moveSpeed = moveSpeed;
		this.tetrominoNumber = tetrominoNumber;
	}

	public float getMoveSpeed() {
		return moveSpeed;
	}

	public int getTetrominoNumber() {
		return tetrominoNumber;
	}
	
	
}
