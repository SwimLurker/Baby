package org.slstudio.baby.game.puzzle;

import java.util.ArrayList;
import java.util.List;

public class PuzzleResolver {
	public enum DIRECTION{
		UP, DOWN, LEFT, RIGHT
	}
	
	public static List<DIRECTION> resolvePuzzle(Puzzle puzzle){
		
		int dimension = puzzle.getDimension();
		
		int[] startState = getStartState(puzzle.getPieceList());
		
		int[] targetState = getTargetState(dimension);
		
		IDAStarAlgorithm algorithm = new IDAStarAlgorithm(startState, targetState, dimension);
		
		int[] moves = algorithm.calculateSolutionMoves();
		
		List<DIRECTION> result = new ArrayList<DIRECTION>();
		
		for(int i = 0 ; i<moves.length; i++){
			if(moves[i] == 0){
				result.add(DIRECTION.UP);
			}else if(moves[i] == 1){
				result.add(DIRECTION.LEFT);
			}else if(moves[i] == 2){
				result.add(DIRECTION.DOWN);
			}else if(moves[i] == 3){
				result.add(DIRECTION.RIGHT);
			}
			
		}
		return result;		
				
	}

	private static int[] getStartState(PuzzlePiece[] pieces){
		int[] start = new int[pieces.length];
		
		for(int i=0; i<pieces.length; i++){
			if(pieces[i] instanceof BlankPuzzlePiece){
				start[i] = IDAStarAlgorithm.BLANK_STATE;
			}else{
				start[i] = pieces[i].getImageIndex();
			}
		}
		
		return start;
	}
	
	private static int[] getTargetState(int dimension) {
		int[] target = new int [dimension * dimension];
		
		for(int i = 0; i < dimension * dimension -1 ; i++){
			target[i] = i;
		}
		
		target[dimension * dimension -1] = IDAStarAlgorithm.BLANK_STATE;
		
		return target;
	}
}
