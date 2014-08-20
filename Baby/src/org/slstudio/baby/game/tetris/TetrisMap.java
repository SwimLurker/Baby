package org.slstudio.baby.game.tetris;

import java.util.Random;

import org.slstudio.baby.game.tetris.tetromino.Tetromino;

public class TetrisMap {
	private int rowNumber, colNumber;
	private int[][] tiles;
	
	public TetrisMap(int rowNumber, int colNumber){
		this.rowNumber = rowNumber;
		this.colNumber = colNumber;
		
		tiles = new int[rowNumber][colNumber];
		initTiles();
	}

	public int getRowNumber() {
		return rowNumber;
	}
	
	public int getColNumber() {
		return colNumber;
	}

	public int[][] getTiles() {
		return tiles;
	}
	
	public int getTile(int row, int col){
		return tiles[row][col];
	}

	
	public void setTile(int row, int col, int tileValue){
		tiles[row][col] = tileValue;
	}

	private void initTiles() {
		Random r = new Random();
		for(int row = 0 ;row < rowNumber; row++){
			for(int col = 0; col < colNumber; col++){
				tiles[row][col] = Tetromino.TILE_BLANK;
			}
		}
	}


	public void copyFrom(TetrisMap currentMap) {
		for(int row = 0 ;row < rowNumber; row++){
			for(int col = 0; col < colNumber; col++){
				tiles[row][col] = currentMap.getTile(row, col);
			}
		}
	}


	public int clearLine() {
		for(int row = rowNumber - 1; row >= 0 ; row--){
			boolean canClear = true;
			for(int col = 0; col < colNumber; col++){
				if(tiles[row][col] == Tetromino.TILE_BLANK){
					canClear = false;
					break;
				}
			}
			if(canClear){
				doClearLine(row);
				return row;
			}
		}
		return -1;
	}


	private void doClearLine(int lineNumber) {
		for(int row = rowNumber - 1 ;row >= 0; row--){
			if(lineNumber < row){
				continue;
			}
			if(row > 0){
				for(int col = 0; col < colNumber; col++){
					tiles[row][col] = tiles[row-1][col];
				}
			}else{
				for(int col = 0; col < colNumber; col++){
					tiles[row][col] = Tetromino.TILE_BLANK;
				}
			}
		}
	}
	
	
}
