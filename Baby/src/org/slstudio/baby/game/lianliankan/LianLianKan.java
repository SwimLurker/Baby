package org.slstudio.baby.game.lianliankan;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import org.slstudio.baby.data.PhotoManager;
import org.slstudio.baby.game.GameException;
import org.slstudio.baby.game.TimeableGame;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;


public class LianLianKan extends TimeableGame{
	
	
	private LianLianKanMap map = null;
	
	private Block selectedBlock = null;
	
	private Path currentPath = null;
	
	private Path hintPath = null;
	
	private List<ILianLianKanListener> lianliankanListeners = new ArrayList<ILianLianKanListener>();
	
	private boolean showHint = false;
	
	private int mapRowNumber;
	private int mapColumnNumber;
	private int mapSameImageCount;
	private int maxHintNumber;
	private int hintNumber;
	
	
	private Map<String, Bitmap> bitmapCache = new HashMap<String, Bitmap>();
	
	public LianLianKan(int rows, int cols, int sameImageCount, int maxHintNumber, int maxTime, int bonusTime){
		super(maxTime, bonusTime);
		this.mapRowNumber = rows;
		this.mapColumnNumber = cols;
		this.mapSameImageCount = sameImageCount;
		this.maxHintNumber = maxHintNumber;
		
	}
	
	public int getMapRowNumber() {
		return mapRowNumber;
	}

	public int getMapColumnNumber() {
		return mapColumnNumber;
	}

	public int getMapSameImageCount() {
		return mapSameImageCount;
	}

	public int getMaxHintNumber() {
		return maxHintNumber;
	}

	
	public int getHintNumber() {
		return hintNumber;
	}

	public void setHintNumber(int hintNumber) {
		this.hintNumber = hintNumber;
	}
	

	public LianLianKanMap getLianLianKanMap(){
		return map;
	}
	
	public Block getSelectedBlock() {
		return selectedBlock;
	}

	public Path getCurrentPath() {
		return currentPath;
	}

	public Path getHintPath() {
		return hintPath;
	}
	
	public void addCustomizedListener(ILianLianKanListener listener){
		lianliankanListeners.add(listener);
	}
	
	public void removeCustomizedListener(ILianLianKanListener listener){
		lianliankanListeners.remove(listener);
	}

	@Override
	public void initGame() throws GameException{
		
		super.initGame();
		
		//first check parameter valid
		List<Integer> imageIds = new ArrayList<Integer>();
		
		int blockNumber = (mapRowNumber -2) * (mapColumnNumber - 2);
		
		if(blockNumber %2 != 0){
			throw new GameException("Invalid game settings: the rows x columns should be even");
		}
		
		if(mapSameImageCount %2 != 0){
			throw new GameException("Invalid game settings: the sameImageCount should be even");
		}
		
		map = new LianLianKanMap(this, mapRowNumber, mapColumnNumber);
		
		//prepare image ids
		int differentImageCount = blockNumber/mapSameImageCount;
		
		for(int i=0; i<differentImageCount; i++){
			for(int j = 0; j<mapSameImageCount; j++){
				imageIds.add(i);
			}
		}
		
		int imageIdSize = imageIds.size();
		int leftSize = blockNumber - imageIdSize;
		if(leftSize >0){
			for(int i=0; i< leftSize; i++){
				imageIds.add(differentImageCount);
			}
			differentImageCount++;
		}
		
		//get different iamge list
		List<String> imageList = getRandomImageList(differentImageCount);
		
		
		SecureRandom r = new SecureRandom();
		
		for(int i = 0; i < mapRowNumber; i++){
			for(int j = 0; j < mapColumnNumber; j++){
				Block b = map.getBlock(i, j);
				if(i == 0 || i == mapRowNumber-1 || j == 0|| j == mapColumnNumber-1){
					//for the invisible blocks
					b.setImageId(Block.EMPTY_IMAGEID);
					b.setEmpty(true);
				}else{
					r.setSeed(UUID.randomUUID().hashCode());
					
					int index = r.nextInt(imageIds.size());
					Bitmap bitmap = getBlockImage(imageList.get(imageIds.get(index)));
					if(bitmap == null){
						throw new GameException("Game initial fail: get bitmap failed");
					}
					b.setBitmap(bitmap);
					b.setImageId(imageIds.get(index));
					b.setEmpty(false);
					
					imageIds.remove(index);
					
				}
			}
		}
		
		hintNumber = maxHintNumber;
		
				
	}
	

	
	public boolean isShowHint(){
		return showHint;
	}
	
	@Override
	public void gameChecking() {
		if(map.isAllBlocksEmpty()){
			gameWin();
		}else if(map.isDeadLock()){
			gameDeadLock();
		}
	}
	
	
	public void rerange() {
		List<Bitmap> bitmaps = new ArrayList<Bitmap>();
		List<Integer> imageIDs = new ArrayList<Integer>();
		
		List<Block> blocks = map.getNotEmptyBlocks();
		for(Block b: blocks){
			bitmaps.add(b.getBitmap());
			imageIDs.add(b.getImageId());
		}
		
		Random r = new Random();
		
		for(Block b2: blocks){
			int index = r.nextInt(bitmaps.size());
			b2.setBitmap(bitmaps.get(index));
			b2.setImageId(imageIDs.get(index));
			bitmaps.remove(index);
			imageIDs.remove(index);
		}
		
		gameChecking();
	}
	
	
	public void gameDeadLock(){
		for(ILianLianKanListener listener: lianliankanListeners){
			listener.onDeadLock(this);
		}
	}
	
	public void selectBlock(int row, int column) {
		
		Block newSelectedBlock = map.getBlock(row, column);
		Block oldSelectedBlock = selectedBlock;
		
		if(newSelectedBlock ==null || newSelectedBlock.isEmpty()){
			return;
		}
		showHint = false;
		
		if(selectedBlock == null){
			//select new block
			selectedBlock = newSelectedBlock;
			for(ILianLianKanListener l: lianliankanListeners){
				l.onBlockStateChanged(this, ILianLianKanListener.BLOCK_SELECTED, newSelectedBlock);
			}
		}else{
			if(selectedBlock.sameAs(newSelectedBlock)){
				selectedBlock = null;
				for(ILianLianKanListener l: lianliankanListeners){
					l.onBlockStateChanged(this, ILianLianKanListener.BLOCK_UNSELECTED, oldSelectedBlock);
				}
			}else{
				//first unselected old block
				selectedBlock = null;
				
				for(ILianLianKanListener l: lianliankanListeners){
					l.onBlockStateChanged(this, ILianLianKanListener.BLOCK_UNSELECTED, oldSelectedBlock);
				}
				
				//calculate path
				if(newSelectedBlock.getImageId() == oldSelectedBlock.getImageId()){
					currentPath = map.findPath(oldSelectedBlock, newSelectedBlock);
				}
				
				
				//select new block
				selectedBlock = newSelectedBlock;
				for(ILianLianKanListener l: lianliankanListeners){
					l.onBlockStateChanged(this, ILianLianKanListener.BLOCK_SELECTED, newSelectedBlock);
				}
				
			}
			
		}
	}

	public void handleRemoveBlocks() {
		if(currentPath != null){
			Block startBlock = currentPath.getStartBlock();
			Block endBlock = currentPath.getEndBlock();
			currentPath = null;
			selectedBlock = null;
			
			map.removeBlock(startBlock);
			for(ILianLianKanListener l: lianliankanListeners){
				l.onBlockStateChanged(this, ILianLianKanListener.BLOCK_REMOVED, startBlock);
			}
			map.removeBlock(endBlock);
			for(ILianLianKanListener l: lianliankanListeners){
				l.onBlockStateChanged(this, ILianLianKanListener.BLOCK_REMOVED, endBlock);
			}
			
			timeBonus();
			
			//check game finished
			gameChecking();
		}
	}
	
	public void testPath() {
		Block start = map.getBlock(2, 1);
		Block end = map.getBlock(1, 2);
		
		Log.d("Game","Start Block -- " + start.toString());
		Log.d("Game","End Block -- " + end.toString());
		List<Path> paths = map.findPaths(start, end);
		if(paths.size()>0){
			for(int i=0; i<paths.size(); i++){
				Log.d("Game","Path"+i +":");
				Log.d("Game",paths.get(i).toString());
			}
		}else{
			Log.d("Game","find path failed");
		}
	}

	private Bitmap getBlockImage(String imageFilename) {
		
		if(!bitmapCache.containsKey(imageFilename.toUpperCase())){
			FileInputStream fis = null;
			try{
				File f = new File(imageFilename);
			
				if(f.exists() && f.isFile()){
					fis = new FileInputStream(f);
					Bitmap bitmap = BitmapFactory.decodeStream(fis);
					bitmapCache.put(imageFilename.toUpperCase(), bitmap);
				}		
				
			}catch(Exception exp){
				exp.printStackTrace();
				return null;
			}finally{
				if(fis != null){
					try {
						fis.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return bitmapCache.get(imageFilename.toUpperCase());
	}

	private List<String> getRandomImageList(int size) throws GameException{
		List<String> result = new ArrayList<String>();
		
		List<String> images = PhotoManager.getInstance().getAllBlockImages();
		
		if(images.size()<size){
			throw new GameException("Not enough image types");
		}
		
		Random r = new Random();
		int loopCount = size;
		while(loopCount != 0){
			int index = r.nextInt(images.size());
			String imageFilename = images.get(index);
			if(!result.contains(imageFilename)){
				result.add(imageFilename);
				loopCount--;
			}
		}
		
		return result;
	}


	

	
	public void getHint() {
		showHint = true;
		
		if(hintNumber == -1){
			//no limited
			selectedBlock = null;
			if(hintPath==null || hintPath.getStartBlock().isEmpty() || hintPath.getEndBlock().isEmpty()){
				hintPath = map.findHintPath();
				for(ILianLianKanListener listener: lianliankanListeners){
					listener.onNewHintPathFound(this, hintPath);
				}
			}
		}else if(hintNumber>0){
			selectedBlock = null;
			if(hintPath==null || hintPath.getStartBlock().isEmpty() || hintPath.getEndBlock().isEmpty()){
				hintPath = map.findHintPath();
				hintNumber--;
				for(ILianLianKanListener listener: lianliankanListeners){
					listener.onNewHintPathFound(this, hintPath);
				}
			}
		}
		
		for(ILianLianKanListener listener: lianliankanListeners){
			listener.onGetHintPath(this, hintPath);
		}
	}

	

	
}
