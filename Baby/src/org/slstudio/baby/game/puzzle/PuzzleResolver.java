package org.slstudio.baby.game.puzzle;

import java.util.ArrayList;
import java.util.List;

import org.slstudio.baby.game.GameException;

import android.util.Log;

public class PuzzleResolver {
	public static final String TAG = "PuzzleResolver";
	
	private IPuzzleAlgorithm algorithm = null;
	
	public PuzzleResolver(Puzzle puzzle){
		int dimension = puzzle.getDimension();
		
		byte[] startState = getStartState(puzzle.getPieceList());
		
		//int[] startState = new int[]{7,8,11,4,13,12,5,14,1,0,10,6,3,9,2,-1};
		
		Log.d(TAG, "Start state:" + getStateString(startState));
		
		byte[] targetState = getTargetState(dimension);
		
		Log.d(TAG, "Target state:" + getStateString(targetState));
		
		if(dimension ==4){
			algorithm = new IDAStarWithWDAlgorithm(startState);
			
		}else{
			algorithm = new IDAStarAlgorithm(startState, targetState, dimension);
		}
	}
	
	public List<DIRECTION> resolvePuzzle() throws GameException{
		return resolvePuzzle(null);
	}
	
	
	public List<DIRECTION> resolvePuzzle(IProgressListener listener) throws GameException{
		
		long beginTime = System.currentTimeMillis();
		
		List<DIRECTION> result = null;
		
		try{
			result = algorithm.calculate(listener);
				
		}catch(GameException exp){
			if(algorithm.isCancelled()){
				return null;
			}else{
				throw exp;
			}
		}
		
		Log.d(TAG, "Calculate time:" + Long.toString(System.currentTimeMillis() - beginTime));
		
		Log.d(TAG, "Solution moves("+ Integer.toString(result.size())+"):" + getMovesString(result));
		
		return result;		
				
	}

	private String getStateString(byte[] state) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0;i<state.length; i++){
			sb.append(Byte.toString(state[i]));
			if(i!=state.length-1){
				sb.append(",");
			}
		}
		return sb.toString();
	}

	private String getMovesString(List<DIRECTION> moves) {
		StringBuffer sb = new StringBuffer();
		for(int i = 0;i<moves.size(); i++){
			switch(moves.get(i)){
			case UP:
				sb.append("Up");
				break;
			case DOWN:
				sb.append("Down");
				break;
			case LEFT:
				sb.append("Left");
				break;
			case RIGHT:
				sb.append("Right");
				break;
			}
			if(i!=moves.size()-1){
				sb.append(",");
			}
		}
		return sb.toString();
	}
	
	private byte[] getStartState(PuzzlePiece[] pieces){
		byte[] start = new byte[pieces.length];
		
		for(int i=0; i<pieces.length; i++){
			if(pieces[i] instanceof BlankPuzzlePiece){
				start[i] = PuzzleConstant.BLANK_STATE;
			}else{
				start[i] = (byte)(pieces[i].getImageIndex() + 1);
			}
		}
		
		return start;
	}
	
	private byte[] getTargetState(int dimension) {
		byte[] target = new byte [dimension * dimension];
		
		for(int i = 0; i < dimension * dimension - 1; i++){
			target[i] = (byte)(i +1);
		}
		
		target[dimension * dimension -1] = PuzzleConstant.BLANK_STATE;
		
		return target;
	}


	public void setCancelled(boolean cancelled) {
		algorithm.cancel();
	}
}
