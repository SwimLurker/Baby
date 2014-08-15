package org.slstudio.baby.game.puzzle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slstudio.baby.game.GameException;

import android.util.Log;

public class IDAStarAlgorithm implements IPuzzleAlgorithm {
	
	public static final String TAG = "IDAAlgorithm";
	
	//below we will use ((dPre != dCurr)&&(dPre %2 == dCurr%2)) to check if the move is UP/DOWN and LEFT/RIGHT
	private final byte UP = 0;
	private final byte DOWN = 2;
	private final byte LEFT = 1;
	private final byte RIGHT = 3;
	
	private int dimension;
	
	private static long iterationCounter = 0;
	
	private Map<Byte, Byte> targetIndexs;
	
	private static byte[] moves = new byte[PuzzleConstant.MAX_MOVES];
	
	private static long ans = 0;
	
	private byte[] targetState = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, PuzzleConstant.BLANK_STATE};
	private byte[] startState;
	
	private int blankIndex;
	
	private boolean bCancelled = false;
	
	public IDAStarAlgorithm(byte[] startState, byte[] targetState, int dimension){
		
		this.dimension = dimension;
		this.startState = startState;
		this.targetState = targetState;
		
		targetIndexs = new HashMap<Byte, Byte>();
		
		//get blank piece position
		for (int i = 0 ;i < startState.length; i++){
			if(startState[i] == PuzzleConstant.BLANK_STATE){
				blankIndex = i;
				break;
			}
		}
		
		//get target point position array
		for(byte i = 0; i<startState.length; i++){
			byte index = targetState[i];
			targetIndexs.put(index, i);
		}
	}
	
	public long getANS(){
		return ans;
	}
	
	/*
	 * @param state current state
	 * @param blankRow row number of blank piece
	 * @param blankColumn column number of blank piece
	 * @param dep current depth
	 * @param d last move direction
	 * @param h current state valuation function
	 * @return
	 */
	private boolean solve(byte[] state, int blankIndex, int dep, long d, long h) throws GameException{
		iterationCounter++;
		
		if(bCancelled){
			throw new GameException("Cancelled");
		}
		
		long h1;
		
		
		if(Arrays.equals(state, targetState)){
			return true;
		}
		/*
		boolean isSolved = true;
		for (int i = 0; i < dimension * dimension; i++){
			if(state[i] != targetState[i]){
				isSolved = false;
				break;
			}
		}
		
		if(isSolved){
			return true;
		}
		*/
		if(dep == ans){
			return false;
		}
		
		//the position for blank piece after move
		int blankIndexAfterMove = blankIndex;
		
		byte[] state2 = new byte [dimension * dimension];
		
		//mem.put(counter, state2);
		
		for(byte direction = 0; direction < 4; direction ++){
			/*
			for(int i = 0; i<state.length; i++){
				state2[i] = state[i];
			}
			*/
			System.arraycopy(state, 0, state2, 0, dimension * dimension);
			
			if(direction != d && (d%2 == direction%2)){
				continue;
			}
			
			if(direction  ==  UP){
				if(row(blankIndex) == 0){
					continue;
				}else{
					blankIndexAfterMove = blankIndex - dimension;
				}
			}else if(direction  ==  DOWN){
				if(row(blankIndex) == dimension-1){
					continue;
				}else{
					blankIndexAfterMove = blankIndex + dimension;
				}
			}else if(direction  ==  LEFT){
				if(column(blankIndex) == 0){
					continue;
				}else{
					blankIndexAfterMove = blankIndex - 1;
				}
			}else if(direction  ==  RIGHT){
				if(column(blankIndex) == dimension-1){
					continue;
				}else{
					blankIndexAfterMove = blankIndex + 1;
				}
			}
			
			state2[blankIndex] = state2[blankIndexAfterMove];
			state2[blankIndexAfterMove] = PuzzleConstant.BLANK_STATE;
			
			if(direction == DOWN && row(blankIndexAfterMove) > row(targetIndexs.get(state[blankIndexAfterMove]))){
				h1 = h -1;
			}else if(direction == UP && row(blankIndexAfterMove) < row(targetIndexs.get(state[blankIndexAfterMove]))){
				h1 = h -1;
			}else if(direction == RIGHT && column(blankIndexAfterMove) > column(targetIndexs.get(state[blankIndexAfterMove]))){
				h1 = h -1;
			}else if(direction == LEFT && column (blankIndexAfterMove) < column(targetIndexs.get(state[blankIndexAfterMove]))){
				h1 = h -1;
			}else{
				h1 = h + 1;
			}
			
			if(h1 +dep + 1 > ans){
				continue;
			}
			
			moves[dep] = direction;
			if (solve(state2, blankIndexAfterMove, dep+1, direction , h1)){
				return true;
			}
			
		}
		
		
		return false;
		
	}
	
	private int getHeuristic(){
		int heuristic = 0;
		for(int i=0; i<startState.length; i++){
			if(startState[i] != PuzzleConstant.BLANK_STATE){
				heuristic = heuristic + Math.abs(row(targetIndexs.get(startState[i])) - row(i)) + Math.abs(column(targetIndexs.get(startState[i])) - column(i));
			}
		}
		return heuristic;
	}
	
	private int row(int index){
		return index / dimension;
	}
	
	private int column(int index){
		return index % dimension;
	}
	
	@Override
	public List<DIRECTION> calculate(IProgressListener listener) throws GameException{
		int j = getHeuristic();
		for(ans = j; ; ans ++){
			iterationCounter = 0;
			boolean resolved = false;
			long beginTime = System.currentTimeMillis();
			
			if(solve(startState, blankIndex, 0, -1, j)){
				resolved = true;
			}
			
			long consumeTime = System.currentTimeMillis() - beginTime;
			
			if(listener != null){
				listener.onProgress(new long[]{ans, iterationCounter, consumeTime});
			}
			
			Log.d(TAG, "running time for ans(" + ans +") is " + (consumeTime) + "ms");
			Log.d(TAG, "iteration count for ans(" + ans +") is " + iterationCounter);
			
			if(resolved){
				break;
			}
		}
	
		List<DIRECTION> solution  = new ArrayList<DIRECTION>();
		
		for (int i = 0;i < ans; i ++){
			switch(moves[i]){
			case UP:
				solution.add(DIRECTION.UP);
				break;
			case DOWN:
				solution.add(DIRECTION.DOWN);
				break;
			case LEFT:
				solution.add(DIRECTION.LEFT);
				break;
			case RIGHT:
				solution.add(DIRECTION.RIGHT);
				break;
			}
		}
		
		return solution;
		
	}

	@Override
	public void cancel() {
		bCancelled = true;
	}
	
	@Override
	public boolean isCancelled(){
		return bCancelled;
	}

	
}
