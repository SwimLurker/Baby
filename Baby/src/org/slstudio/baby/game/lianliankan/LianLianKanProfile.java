package org.slstudio.baby.game.lianliankan;

import org.slstudio.baby.game.IGameProfile;

public class LianLianKanProfile implements IGameProfile{
	public static final LianLianKanProfile EASY = new LianLianKanProfile(8, 6, 4, -1, 30, 5);
	public static final LianLianKanProfile NORMAL = new LianLianKanProfile(10, 8, 4, 5, 30, 5);
	public static final LianLianKanProfile HARD = new LianLianKanProfile(12, 10, 4, 3, 50, 3);
	
	private int rowNumber = 10;
	private int columnNumber = 6;
	private int sameImageCount = 4;
	private int maxHintNumber = 3;
	private int maxTime = 100;
	private int bonusTime = 3;
	
	public LianLianKanProfile(int rowNumber, int columnNumber, int sameImageCount,
			int maxHintNumber, int maxTime, int bonusTime) {
		super();
		this.rowNumber = rowNumber;
		this.columnNumber = columnNumber;
		this.sameImageCount = sameImageCount;
		this.maxHintNumber = maxHintNumber;
		this.maxTime = maxTime;
		this.bonusTime = bonusTime;
	}
	
	public int getRowNumber() {
		return rowNumber;
	}
	public int getColumnNumber() {
		return columnNumber;
	}
	public int getSameImageCount() {
		return sameImageCount;
	}
	public int getMaxHintNumber() {
		return maxHintNumber;
	}
	public int getMaxTime() {
		return maxTime;
	}
	public int getBonusTime() {
		return bonusTime;
	}
	
}
