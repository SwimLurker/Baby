package org.slstudio.baby.game.puzzle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import org.slstudio.baby.game.GameException;
import org.slstudio.baby.game.TimeableGame;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.Log;

public class Puzzle extends TimeableGame<IPuzzleGameListener>{
	
	
	private static Paint pieceFramePaint = null;
	
	private PuzzlePiece[] pieceList = null;
	private Bitmap originalBitmap = null;
	private int dimension = 3;
	private int moveCount = 0;
	
	
	private String successString = null;

	
	static{
		pieceFramePaint = new Paint();
		pieceFramePaint.setStyle(Paint.Style.STROKE);
		pieceFramePaint.setStrokeWidth(1);
		pieceFramePaint.setColor(Color.BLACK);
		
	}
	
	public Puzzle(Bitmap originalBitmap, int dimension, int maxTime) {
		super(maxTime, 0);
		this.originalBitmap = originalBitmap;
		this.dimension = dimension;
	}


	public PuzzlePiece[] getPieceList() {
		return pieceList;
	}

	public int getMoveCount() {
		return moveCount;
	}


	public Bitmap getOriginalBitmap() {
		return originalBitmap;
	}


	public int getDimension() {
		return dimension;
	}

	@Override
	public void initGame() throws GameException{
		shufflePieces();
		successString = getSuccessString();
		moveCount = 0;
	}
	
	private List<Integer> getRandomImageIdList(){
		Random r = new Random();
		
		ArrayList<Integer> result = new ArrayList<Integer>();
		ArrayList<Integer> source = new ArrayList<Integer>();
		
		int piecesCount = dimension * dimension;
		for(int i = 0; i<piecesCount -1; i++){
			source.add(i);
		}
		
		for(int i = 0; i<piecesCount -1; i++){
			int index = r.nextInt(source.size());
			result.add(source.get(index));
			source.remove(index);
		}
		
		return result;
		
	}
	
	private String getPuzzleString(List<Integer> ids) {
		StringBuffer sb = new StringBuffer();
		for(int i=0; i< ids.size(); i++){
			sb.append(Integer.toString(ids.get(i)));
			if(i!= ids.size() -1){
				sb.append(",");
			}
		}
		return sb.toString();
	
	}

	private void shufflePieces() {
		
		List<Integer> imageIds = null;
		
		do{
			imageIds = getRandomImageIdList();
			Log.d("PuzzleGame", "image id list:" + getPuzzleString(imageIds));
		}while(!puzzleHasSolution(imageIds));
		
		int piecesCount = dimension * dimension;
		pieceList = new PuzzlePiece[piecesCount];
		for(int i = 0; i<piecesCount; i++){
			PuzzlePiece piece = null;	
			if(i == piecesCount - 1){
				piece = new BlankPuzzlePiece();
				piece.setIndex(i);
			}else{
				int imageId = imageIds.get(i);
				piece = new PuzzlePiece(imageId);
				piece.setIndex(i);
				Bitmap piecePic  = getPuzzlePiecePicture(imageId);
				piece.setPicture(piecePic);
			}
			
			pieceList[i] = piece;
		}
	}


	private boolean puzzleHasSolution(List<Integer> imageIds) {
		boolean result = false;
		
		int tSum = 0;
		
		for(int i = 0 ;i <imageIds.size(); i++){
			int ti = calculateTValue(imageIds, i);
			Log.d("PuzzleGame", "T" + i + " is " + ti);
			
			tSum += ti;
		}
		Log.d("PuzzleGame", "TSum is " + tSum);
		
		if(isEven(dimension)){
			//dimension is even number
			//blank piece is the last one, which means it is in odd line from below to above
			//so to have solution tSum must be even
			Log.d("PuzzleGame", "dimension(" + dimension +") is even number");
			result = isEven(tSum);
		}else{
			//dimension is odd number
			//so to have solution tSum must be even
			Log.d("PuzzleGame", "dimension(" + dimension +") is odd number");
			result = isEven(tSum);
		}
		if(result){
			Log.d("PuzzleGame", "Has solution");
		}else{
			Log.d("PuzzleGame", "No solution");
		}
		return result;
	}
	
	


	private int calculateTValue(List<Integer> ids, int index) {
		int currentId = ids.get(index);
		int result  = 0;
		for(int i = index+1; i<ids.size(); i++){
			int targetId = ids.get(i);
			if(targetId < currentId){
				result ++;
			}
		}
		return result;
	}


	private boolean isEven(int number) {
		return (number & 1) == 0;
	} 


	private Bitmap getPuzzlePiecePicture(int index) {
		int row = index / dimension;
		int col = index % dimension;
		
		int bitmapWidth = originalBitmap.getWidth();
		int bitmapHeight = originalBitmap.getHeight();
		
		int pieceWidth = bitmapWidth /dimension;
		int pieceHeight = bitmapHeight /dimension;
		
		Bitmap result = Bitmap.createBitmap(pieceWidth, pieceHeight, Bitmap.Config.RGB_565);
		
		Canvas canvas = new Canvas(result);
		Rect src = new Rect(col * pieceWidth, row * pieceHeight, (col+1) * pieceWidth, (row+1) * pieceHeight);
		Rect des = new Rect(0, 0, pieceWidth, pieceHeight);
		canvas.drawBitmap(originalBitmap, src, des, null);
		
		canvas.drawRect(des, pieceFramePaint);
		
		return result;
	}

	@Override
	public void gameChecking() {
		String result = getPieceListString();
		
		if(successString.equals(result)){
			gameWin();
		}
		
	}

	private String getPieceListString() {
		StringBuffer result = new StringBuffer();
		for(int i = 0; i<pieceList.length; i++){
			PuzzlePiece piece = pieceList[i];
			result.append(Integer.toString(piece.getImageIndex()));
			result.append(",");
		}
		return result.toString();
		
	}

	private String getSuccessString() {
		StringBuffer result = new StringBuffer();
		for(int i = 0; i<dimension * dimension-1; i++){
			result.append(Integer.toString(i));
			result.append(",");
		}
		result.append("-1");
		result.append(",");
		
		return result.toString();
	}


	public PuzzlePiece getPuzzlePiece(int index){
		if(index < 0 || index >= pieceList.length){
			return null;
		}
		return pieceList[index];
	}
	

	public void selectPiece(int index) {
		
		PuzzlePiece selected = getPuzzlePiece(index);
		if(selected == null){
			return;
		}
		
		PuzzlePiece up = getUpsidePiece(selected.getIndex());
		if(up!=null && up instanceof BlankPuzzlePiece){
			movePiece(selected.getIndex(), up.getIndex());
			return;
		}
		
		PuzzlePiece down = getDownsidePiece(selected.getIndex());
		if(down!=null && down instanceof BlankPuzzlePiece){
			movePiece(selected.getIndex(), down.getIndex());
			return;
		}
		
		PuzzlePiece left = getLeftsidePiece(selected.getIndex());
		if(left !=null && left instanceof BlankPuzzlePiece){
			movePiece(selected.getIndex(), left.getIndex());
			return;
		}
		
		PuzzlePiece right = getRightsidePiece(selected.getIndex());
		if(right!=null && right instanceof BlankPuzzlePiece){
			movePiece(selected.getIndex(), right.getIndex());
			return;
		}
	}


	public void movePiece(int fromIndex, int toIndex) {
		PuzzlePiece from = getPuzzlePiece(fromIndex);
		PuzzlePiece to = getPuzzlePiece(toIndex);
		
		if(from == null || to == null){
			return;
		}
		
		from.setIndex(toIndex);
		to.setIndex(fromIndex);
		pieceList[fromIndex] = to;
		pieceList[toIndex] = from;
		
		moveCount ++;
		
		for(IPuzzleGameListener listener: customizedListeners){
			listener.onPieceMoved(this, fromIndex, toIndex);
		}
		
	}

	public int getBlankPieceIndex() {
		for(int i = 0; i<pieceList.length; i++){
			if(pieceList[i] instanceof BlankPuzzlePiece){
				return i;
			}
		}
		return -1;
	}
	
	private PuzzlePiece getUpsidePiece(int index) {
		int targetIndex = index - dimension;
		return getPuzzlePiece(targetIndex);
	}
	
	private PuzzlePiece getDownsidePiece(int index) {
		int targetIndex = index + dimension;
		return getPuzzlePiece(targetIndex);
	}
	
	private PuzzlePiece getLeftsidePiece(int index) {
		if(index % dimension == 0){
			return null;
		}
		int targetIndex = index - 1;
		return getPuzzlePiece(targetIndex);
	}
	
	private PuzzlePiece getRightsidePiece(int index) {
		if(index % dimension == dimension - 1){
			return null;
		}
		int targetIndex = index + 1;
		return getPuzzlePiece(targetIndex);
	}

	
}
