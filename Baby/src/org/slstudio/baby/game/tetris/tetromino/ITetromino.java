package org.slstudio.baby.game.tetris.tetromino;

public class ITetromino extends Tetromino{
	private static final int SIZE = 4;
	
	public ITetromino(){
		super(SIZE);
	}
	
	@Override
	protected void initSharp(){
		sharp[0][0] = TILE_BLANK;
		sharp[0][1] = TILE_BLOCK;
		sharp[0][2] = TILE_BLANK;
		sharp[0][3] = TILE_BLANK;
		sharp[1][0] = TILE_BLANK;
		sharp[1][1] = TILE_BLOCK;
		sharp[1][2] = TILE_BLANK;
		sharp[1][3] = TILE_BLANK;
		sharp[2][0] = TILE_BLANK;
		sharp[2][1] = TILE_BLOCK;
		sharp[2][2] = TILE_BLANK;
		sharp[2][3] = TILE_BLANK;
		sharp[3][0] = TILE_BLANK;
		sharp[3][1] = TILE_BLOCK;
		sharp[3][2] = TILE_BLANK;
		sharp[3][3] = TILE_BLANK;
	}
	
}