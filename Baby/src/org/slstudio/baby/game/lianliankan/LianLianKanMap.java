package org.slstudio.baby.game.lianliankan;

import java.util.ArrayList;
import java.util.List;


public class LianLianKanMap {
	
	private Block[][] blockList;
	private int rows, cols;
	private LianLianKan game;
	
	
	public LianLianKanMap(LianLianKan game, int rows, int cols) {
		super();
		this.game = game;
		this.rows = rows;
		this.cols = cols;
		init();
	}
	
	public int getRows() {
		return rows;
	}
	
	public int getColumns() {
		return cols;
	}

	public LianLianKan getGame() {
		return game;
	}

	
	public Block getBlock(int row, int col){
		if(row < 0 || row > rows-1){
			return null;
		}
		
		if(col < 0 || col > cols -1){
			return null;
		}
		return blockList[row][col];
	}
	
	
	public void removeBlock(Block block) {
		block.setEmpty(true);
	}
	
	public Path findPath(Block start, Block end){	
		List<Path> paths = findPaths(start, end);
		return getShortestPath(paths);
	}
	
	protected void init(){
		blockList = new Block[rows][cols];
		for(int i=0; i<rows ;i++){
			for(int j=0; j<cols; j++){
				blockList[i][j] = new Block(i, j);
			}
		}
	}
	
	protected List<Path> findPaths(Block start, Block end){
		//first find empty blocks near start/end block
		List<Block> emptyBlocks_start = getEmptyBlocks(start);
		List<Block> emptyBlocks_end = getEmptyBlocks(end);
		
		return getPaths(emptyBlocks_start, emptyBlocks_end, start, end);
	}

	private List<Block> getEmptyBlocks(Block curBlock) {
		List<Block> result = new ArrayList<Block>();
		result.add(curBlock);
		result.addAll(getEmptyBlocksUpside(curBlock));
		result.addAll(getEmptyBlocksDownside(curBlock));
		result.addAll(getEmptyBlocksLeftside(curBlock));
		result.addAll(getEmptyBlocksRightside(curBlock));
		return result;
	}


	private List<Path> getPaths(List<Block> emptyBlocks_start, List<Block> emptyBlocks_end, Block start, Block end) {
		List<Path> result = new ArrayList<Path>();
		for(Block b1: emptyBlocks_start){
			for(Block b2: emptyBlocks_end){
				if(checkBlockReachable(b1, b2)){
					Path path = new Path();
					path.addToPath(start);
					if(!b1.sameAs(start)){
						for(Block b: getBlocksBetween(start, b1)){
							path.addToPath(b);
						}
						path.addToPath(b1);
					}
					if(!b1.sameAs(b2)){
						for(Block b: getBlocksBetween(b1, b2)){
							path.addToPath(b);
						}
						path.addToPath(b2);
					}
					if(!b2.sameAs(end)){
						for(Block b: getBlocksBetween(b2, end)){
							path.addToPath(b);
						}
						path.addToPath(end);
					}
					result.add(path);
				}
				
			}
		}
		
		return result;
	}
	
	private List<Block> getBlocksBetween(Block from, Block to) {
		List<Block> result = new ArrayList<Block>();
		if(to.upSide(from)){
			for(int i = from.getRow()-1; i>to.getRow(); i--){
				result.add(getBlock(i, from.getColumn()));
			}
		}
		if(to.downSide(from)){
			for(int i = from.getRow()+1; i<to.getRow(); i++){
				result.add(getBlock(i, from.getColumn()));
			}
		}
		if(to.leftSide(from)){
			for(int i = from.getColumn()-1; i>to.getColumn(); i--){
				result.add(getBlock(from.getRow(), i));
			}
		}
		if(to.rightSide(from)){
			for(int i = from.getColumn()+1; i<to.getColumn(); i++){
				result.add(getBlock(from.getRow(), i));
			}
		}
		
		return result;
		
	}



	private boolean checkBlockReachable(Block b1, Block b2) {
		if(b1.sameAs(b2)){
			return true;
		}
		
		if(b1.getRow() == b2.getRow()){
			int col_left = Math.min(b1.getColumn(), b2.getColumn());
			int col_right = Math.max(b1.getColumn(), b2.getColumn());
			for(int i= col_left+1 ;i < col_right; i++){
				Block b = getBlock(b1.getRow(), i);
				if(b==null ||!b.isEmpty()){
					return false;
				}
			}
			return true;
		}
		if(b1.getColumn() == b2.getColumn()){
			int row_up = Math.min(b1.getRow(), b2.getRow());
			int row_down = Math.max(b1.getRow(), b2.getRow());
			for(int i= row_up+1 ;i < row_down; i++){
				Block b = getBlock(i, b1.getColumn());
				if(b==null ||!b.isEmpty()){
					return false;
				}
			}
			return true;
		}
		return false;
	}



	private Path getShortestPath(List<Path> paths) {
		Path result = null;
		for(Path p:paths){
			if(result == null){
				result = p;
			}else{
				if(result.getPathLength() > p.getPathLength()){
					result = p;
				}
			}
		}
		
		return result;
	}
	
	private List<Block> getEmptyBlocksUpside(Block curBlock) {
		List<Block> result = new ArrayList<Block>();
		
		for(int i = curBlock.getRow()-1; i>=0; i--){
			Block b = getBlock(i, curBlock.getColumn());
			if(b!= null && b.isEmpty()){
				result.add(b);
			}else{
				break;
			}
		}
		
		return result;		
	}

	private List<Block> getEmptyBlocksDownside(Block curBlock) {
		List<Block> result = new ArrayList<Block>();
		
		for(int i = curBlock.getRow()+1; i<rows; i++){
			Block b = getBlock(i, curBlock.getColumn());
			if(b!= null && b.isEmpty()){
				result.add(b);
			}else{
				break;
			}
		}
		
		return result;		
	}
	
	private List<Block> getEmptyBlocksLeftside(Block curBlock) {
		List<Block> result = new ArrayList<Block>();
		
		for(int i = curBlock.getColumn()-1; i>=0; i--){
			Block b = getBlock(curBlock.getRow(),i);
			if(b!= null && b.isEmpty()){
				result.add(b);
			}else{
				break;
			}
		}
		
		return result;		
	}
	
	private List<Block> getEmptyBlocksRightside(Block curBlock) {
		List<Block> result = new ArrayList<Block>();
		
		for(int i = curBlock.getColumn()+1; i<cols; i++){
			Block b = getBlock(curBlock.getRow(), i);
			if(b!= null && b.isEmpty()){
				result.add(b);
			}else{
				break;
			}
		}
		
		return result;		
	}

	


	private List<Block> getBlocksByImageIds(int imageID) {
		List<Block> result = new ArrayList<Block>();
		
		for(int i = 0; i < rows; i++){
			for (int j = 0; j < cols; j++){
				Block b = getBlock(i, j);
				if(!b.isEmpty() && b.getImageId() == imageID ){
					result.add(b);
				}
			}
		}
		
		return result;
	}

	public boolean isAllBlocksEmpty() {
		for(int i = 0; i < rows; i++){
			for (int j = 0; j < cols; j++){
				if(!getBlock(i, j).isEmpty()){
					return false;
				}
			}
		}
		return true;
	}



	public boolean isDeadLock() {
		List<Block> blocks = getNotEmptyBlocks();
		for(Block block: blocks){
			int imageID = block.getImageId();
			List<Block> sameValueBlocks = getBlocksByImageIds(imageID);
			for(Block block2: sameValueBlocks){
				if(!block2.sameAs(block)){
					Path p = findPath(block, block2);
					if(p != null){
						return false;
					}
				}
			}
		}
		return true;
	}


	public List<Block> getNotEmptyBlocks() {
		List<Block> result = new ArrayList<Block>();
		
		for(int i = 0; i < rows; i++){
			for (int j = 0; j < cols; j++){
				if(!getBlock(i, j).isEmpty()){
					result.add(getBlock(i, j));
				}
			}
		}
		
		return result;
	}


	public Path findHintPath() {
		List<Block> blocks = getNotEmptyBlocks();
		for(Block block: blocks){
			int imageID = block.getImageId();
			List<Block> sameValueBlocks = getBlocksByImageIds(imageID);
			for(Block block2: sameValueBlocks){
				if(!block2.sameAs(block)){
					Path p = findPath(block, block2);
					if(p != null){
						return p;
					}
				}
			}
		}
		return null;
	}



	
}
