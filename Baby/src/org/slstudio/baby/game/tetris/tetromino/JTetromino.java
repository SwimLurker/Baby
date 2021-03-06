package org.slstudio.baby.game.tetris.tetromino;

public class JTetromino extends Tetromino{
	private static final int SIZE = 3;
	
	public JTetromino(){
		super(SIZE);
	}
	
	@Override
	protected void initSharp(){
		sharp[0][0] = TILE_BLANK;
		sharp[0][1] = TILE_BLANK;
		sharp[0][2] = TILE_BLOCK;
		sharp[1][0] = TILE_BLANK;
		sharp[1][1] = TILE_BLANK;
		sharp[1][2] = TILE_BLOCK;
		sharp[2][0] = TILE_BLANK;
		sharp[2][1] = TILE_BLOCK;
		sharp[2][2] = TILE_BLOCK;
	}
}