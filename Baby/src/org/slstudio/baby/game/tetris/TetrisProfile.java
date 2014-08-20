package org.slstudio.baby.game.tetris;


public class TetrisProfile {
	public static final TetrisProfile EASY = new TetrisProfile(1.0f);
	public static final TetrisProfile NORMAL = new TetrisProfile(1.2f);
	public static final TetrisProfile HARD = new TetrisProfile(1.5f);
	
	private float moveSpeed = 1.0f;

	public TetrisProfile(float moveSpeed) {
		super();
		this.moveSpeed = moveSpeed;
	}

	public float getMoveSpeed() {
		return moveSpeed;
	}
	
}
