package org.slstudio.baby.game.puzzle;


public class PuzzleProfile {
	public static final PuzzleProfile EASY = new PuzzleProfile(2, 100);
	public static final PuzzleProfile NORMAL = new PuzzleProfile(3, 200);
	public static final PuzzleProfile HARD = new PuzzleProfile(4, 300);
	
	private int dimension = 4;
	private int maxTime = 100;
	
	public PuzzleProfile(int dimension, int maxTime) {
		super();
		this.dimension = dimension;
		this.maxTime = maxTime;
	}

	public int getDimension() {
		return dimension;
	}

	public int getMaxTime() {
		return maxTime;
	}
	
	
	
}
