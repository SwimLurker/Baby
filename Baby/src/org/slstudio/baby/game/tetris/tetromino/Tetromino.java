package org.slstudio.baby.game.tetris.tetromino;

import org.slstudio.baby.game.tetris.TetrisMap;

public class Tetromino {
	public static final int TILE_BLANK = 0;
	public static final int TILE_BLOCK = 1;
	
	public static final int COLOR_RED = 1;
	public static final int COLOR_ORANGE = 2;
	public static final int COLOR_YELLOW = 3;
	public static final int COLOR_GREEN = 4;
	public static final int COLOR_DARKGREEN = 5;
	public static final int COLOR_CYAN = 6;
	public static final int COLOR_BLUE = 7;
	public static final int COLOR_DARKBLUE = 8;
	public static final int COLOR_PURPLE = 9;
	public static final int COLOR_BROWN = 10;
	public static final int NUMBER_OF_COLOR = 10;
	
	protected int size = 3;
	
	//x, y position in the map
	private int x, y;
	
	protected int[][] sharp = null;
	
	
	public Tetromino(int size){
		this.size = size;
		sharp = new int[size][size];
		initSharp();
	}
	
	protected void initSharp(){
		sharp = new int[size][size];
	}
	
	public int getX() {
		return x;
	}

	public void setX(int x) {
		this.x = x;
	}

	public int getY() {
		return y;
	}

	public void setY(int y) {
		this.y = y;
	}

	public void setColor(int color) {
		for(int row = 0; row < size; row++){
			for(int col = 0; col < size; col++){
				sharp[row][col] *= color;
			}
		}
	}

	public int[][] getSharp() {
		return sharp;
	}

	public int getSize() {
		return size;
	}

	public boolean rotate(TetrisMap map){
		int[][] temp = _rotate_clocksize(sharp);
		if(!isCollisionX(x, temp, map) && !isCollisionY(y, temp, map)){
			sharp = temp;
			return true;
		}else if(!isCollisionX(x - 1, temp, map) && !isCollisionY(y, temp, map)){
			x = x - 1;
			sharp = temp;
			return true;
		}else if(!isCollisionX(x + 1, temp, map) && !isCollisionY(y, temp, map)){
			x = x + 1;
			sharp = temp;
			return true;
		}
		return false;
	}
	
	public boolean moveDown(TetrisMap map){
		if(!isCollisionY(y + 1, sharp, map)){
			y = y + 1;
			return true;
		}
		return false;
	}
	
	public boolean moveLeft(TetrisMap map){
		if(!isCollisionX(x - 1, sharp, map)){
			x = x - 1;
			return true;
		}
		return false;
	}
	
	public boolean moveRight(TetrisMap map){
		if(!isCollisionX(x + 1, sharp, map)){
			x = x + 1;
			return true;
		}
		return false;
	}
	
	public boolean isAvailable(TetrisMap map){
		if((!isCollisionX(x, sharp, map))&&(!isCollisionY(y, sharp, map))){
			return true;
		}
		return false;
	}
	
	public boolean isCollisionX(int newX, int[][] newSharp, TetrisMap map){
		if(newX <= -size || newX >= map.getColNumber()){
			return true;
		}
		
		for(int row = 0; row<size; row++){
			for(int col = 0; col < size; col++){
				if(newSharp[row][col] != TILE_BLANK){
					if(newX + col <0 || newX + col >= map.getColNumber()){
						return true;
					}
					if(y + row < 0 || y + row >= map.getRowNumber()){
						return true;
					}
					if(map.getTile(y + row, newX + col)!= TILE_BLANK){
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	public boolean isCollisionY(int newY, int[][] newSharp, TetrisMap map){
		if(newY < -size || newY >= map.getRowNumber()){
			return true;
		}
		
		for(int row = 0; row<size; row++){
			for(int col = 0; col < size; col++){
				if(newSharp[row][col] != TILE_BLANK){
					if(newY + row < 0||newY + row >= map.getRowNumber()){
						return true;
					}
					if(x + col < 0||x + col >= map.getColNumber()){
						return true;
					}
					if(map.getTile(newY + row, x + col)!= TILE_BLANK){
						return true;
					}
				}
			}
		}
		return false;
	}

	public void putOnMap(TetrisMap map) {
		for(int row = 0; row < size; row++){
			for(int col = 0; col < size; col++){
				if(x + col >= 0 && x + col < map.getColNumber() && y + row >= 0 && y + row < map.getRowNumber()){
					if(sharp[row][col] != TILE_BLANK){
						map.setTile( y + row, x + col, sharp[row][col]);
					}
				}
			}
		}
	}
	
	private int[][] _rotate_counterclocksize(int[][] _b){
		int[][] b = new int[size][size];
		for(int row = 0; row < size; row++){
			for(int col = 0; col < size; col++){
				b[row][col] = _b[col][size -1 - row];
			}
		}
		return b;
	}
	
	private int[][] _rotate_clocksize(int[][] _b){
		int[][] b = new int[size][size];
		for(int row = 0; row < size; row++){
			for(int col = 0; col < size; col++){
				b[row][col] = _b[size -1 - col][row];
			}
		}
		return b;
	}

	public int getFirstBlockColIndex() {
		for(int col = 0; col < size; col++){
			for(int row = 0; row < size; row++){
				if(sharp[row][col] != TILE_BLANK){
					return x + col;
				}
			}
		}
		return x;
	}

	public int getLastBlockColIndex() {
		for(int col = size -1; col > 0; col--){
			for(int row = 0; row < size; row++){
				if(sharp[row][col] != TILE_BLANK){
					return x + col;
				}
			}
		}
		return x;
	}
	
	public int getFirstBlockRowIndex() {
		for(int row = 0; row < size; row++){
			for(int col = 0; col < size; col++){
				if(sharp[row][col] != TILE_BLANK){
					return y + row;
				}
			}
		}
		return y;
	}

	public int getLastBlockRowIndex() {
		for(int row = size -1; row > 0; row--){
			for(int col = 0; col < size; col++){
				if(sharp[row][col] != TILE_BLANK){
					return y + row;
				}
			}
		}
		return y;
	}
	
}
