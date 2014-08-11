package org.slstudio.baby.game.puzzle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IDAStarAlgorithm {
	
	public static final int MAX_MOVES = 100000;
	public static final int BLANK_STATE = -1;
	
	//below we will use ((dPre != dCurr)&&(dPre %2 == dCurr%2)) to check if the move is UP/DOWN and LEFT/RIGHT
	private final int UP = 0;
	private final int DOWN = 2;
	private final int LEFT = 1;
	private final int RIGHT = 3;
	
	private int dimension;
	
	private Map<Integer, Integer> targetIndexs;
	
	private int[] moves = new int[MAX_MOVES];
	
	private long ans = 0;
	
	private int[] targetState = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, BLANK_STATE};
	private int[] startState;

	private int blankIndex;
	
	public IDAStarAlgorithm(int[] startState, int[] targetState, int dimension){
		
		this.dimension = dimension;
		this.startState = startState;
		this.targetState = targetState;
		
		targetIndexs = new HashMap<Integer, Integer>();
		
		//get blank piece position
		for (int i = 0 ;i < startState.length; i++){
			if(startState[i] == BLANK_STATE){
				blankIndex = i;
				break;
			}
		}
		
		//get target point position array
		for(int i = 0; i<startState.length; i++){
			int index = targetState[i];
			targetIndexs.put(index, i);
		}
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
	private boolean solve(int[] state, int blankIndex, int dep, long d, long h){
		long h1;
		
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
		
		if(dep == ans){
			return false;
		}
		
		//the position for blank piece after move
		int blankIndexAfterMove = blankIndex;
		
		int[] state2 = new int [dimension * dimension];
		
		for(int direction = 0; direction < 4; direction ++){
			for(int i = 0; i<state.length; i++){
				state2[i] = state[i];
			}
			
			if(direction != d && (d%2 == direction%2)){
				continue;
			}
			boolean isValid = true;
			
			if(direction  ==  UP){
				if(blankIndex/dimension == 0){
					isValid = false;
				}else{
					blankIndexAfterMove = blankIndex - dimension;
				}
			}else if(direction  ==  DOWN){
				if(blankIndex/dimension == dimension-1){
					isValid = false;
				}
				blankIndexAfterMove = blankIndex + dimension;
			}else if(direction  ==  LEFT){
				if(blankIndex % dimension == 0){
					isValid = false;
				}
				blankIndexAfterMove = blankIndex - 1;
			}else if(direction  ==  RIGHT){
				if(blankIndex % dimension == dimension-1){
					isValid = false;
				}
				blankIndexAfterMove = blankIndex + 1;
			}
			
			if(!isValid){
				continue;
			}
			
			state2[blankIndex] = state2[blankIndexAfterMove];
			state2[blankIndexAfterMove] = BLANK_STATE;
			
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
			if(startState[i] != BLANK_STATE){
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
	
	public int[] calculateSolutionMoves(){
		int j = getHeuristic();
		for(ans = j; ; ans ++){
			if(solve(startState, blankIndex, 0, -1, j)){
				break;
			}
		}
		
		int[] solution  = new int[(int) ans];
		
		for (int i = 0;i < ans; i ++){
			solution[i] = moves[i];
		}
		
		return solution;
		
	}
	
}
