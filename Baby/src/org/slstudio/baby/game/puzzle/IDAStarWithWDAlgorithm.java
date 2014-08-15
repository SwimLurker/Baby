package org.slstudio.baby.game.puzzle;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.slstudio.baby.game.GameException;

import android.util.Log;


public class IDAStarWithWDAlgorithm implements IPuzzleAlgorithm{
	
	public static final String TAG = "IDAStarWithWDAlgorithm";
	
	private static final String WD_DB_FILENAME = "WD.db";
	
	private static final int MOVE_UP = -4;
	private static final int MOVE_LEFT = -1;
	private static final int MOVE_DOWN = 4;
	private static final int MOVE_RIGHT = 1;
	
	public static final int dimension = 4;
	public static final int BOARD_SIZE = 16;
	public static final int IDTBL_SIZE = 106;
	public static final int WDTBL_SIZE = 24964;
	
	
	private static long[] WDPTN = new long[WDTBL_SIZE];
	
	private static byte[] WDTBL = new byte[WDTBL_SIZE];
	
	private static short[][][] WDLINK = new short[WDTBL_SIZE][2][dimension];
	
	private static byte[] IDTBL = new byte[IDTBL_SIZE];
	
	private static boolean initialized = false;
	
	private static boolean isInitializing = false;
	
	private static final int[][] MOVAL = new int[][]{
			{1,  4, -1,  0,  0},
			{2,  5,  0, -1,  0},
			{3,  6,  1, -1,  0},
			{7,  2, -1,  0,  0},
			{0,  5,  8, -1,  0},
			{1,  6,  9,  4, -1},
			{2,  7, 10,  5, -1},
			{3, 11,  6, -1,  0},
			{4,  9, 12, -1,  0},
			{5, 10, 13,  8, -1},
			{6, 11, 14,  9, -1},
			{7, 15, 10, -1,  0},
			{8, 13, -1,  0,  0},
			{9, 14, 12, -1,  0},
			{10, 15, 13, -1,  0},
			{11, 14, -1,  0,  0}
	};
	
	private static final int[] CONV = new int[]{0, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15, 4, 8, 12};
	
	private byte[] startState  = null;
	
	private int[] moves = new int[PuzzleConstant.MAX_MOVES];
	
	private int targetDepth;	
	
	private int iterationCounter = 0;
	
	private boolean bCancelled = false;
	
	public IDAStarWithWDAlgorithm(byte[] startState){
		this.startState = startState;
	}
	
	public static void initialize() {
		if(isInitializing){
			return;
		}
		if(!initialized){
			isInitializing = true;
			generateIDTable();
			try {
				readWDTableFromStream(IDAStarWithWDAlgorithm.class.getResourceAsStream(WD_DB_FILENAME));
				initialized = true;
			} catch (IOException e) {
				e.printStackTrace();
				initialized = false;
			}
			isInitializing = false;
		}
	}
	
	private static void generateIDTable() {
		for(int i = 0; i < IDTBL_SIZE; i++){
			IDTBL[i] = (byte)((i/3) + (i % 3));
		}
	}

	private static void readWDTableFromStream(InputStream stream) throws IOException{
		
		DataInputStream dis = new DataInputStream(stream);
		
		for(int i = 0; i<WDTBL_SIZE; i++){
						
			WDPTN[i] = dis.readLong();
			WDTBL[i] = dis.readByte();
			
			for(int j = 0; j< 2; j++){
				for (int k= 0; k<4; k++){
					WDLINK[i][j][k] = dis.readShort();
				}
			}
			if(i%1000 == 0){
				Log.d(TAG, "read data:" + i);
			}
		}
		
		dis.close();	
		
	}

	private boolean IDA(int blankIndex, int prevBlankIndex, int idx1o, int idx2o, int inv1o, int inv2o, int depth) throws GameException{
		
		if(bCancelled){
			throw new GameException("Cancelled");
		}
		
		iterationCounter++;
		
		depth ++;
		
		for(int i = 0; ; i++){
			//get possible index which blank piece can move
			int targetPieceIndex = getAvailableTargetPieceIndex(blankIndex, i);
			if(targetPieceIndex == -1){
				break;
			}
			
			//do not need to move blank to previous position
			if(targetPieceIndex == prevBlankIndex){
				continue;
			}
			
			byte targetPieceValue = startState[targetPieceIndex];
			int idx1 = idx1o;
			int idx2 = idx2o;
			int inv1 = inv1o;
			int inv2 = inv2o;
			int direction = targetPieceIndex - blankIndex;
			switch(direction){
				case MOVE_DOWN:
					for(int j = blankIndex + 1; j<targetPieceIndex; j++){
						if(startState[j] > targetPieceValue){
							inv1--;
						}else{
							inv1++;
						}
					}
					idx1 = WDLINK[idx1o][0][row(targetPieceValue)];
					break;
				case MOVE_RIGHT:
					for(int j = blankIndex +dimension; j<16; j+=dimension){
						if(conv(startState[j]) > conv(targetPieceValue)){
							inv2--;
						}else{
							inv2++;
						}
					}
					for(int j = targetPieceIndex - dimension; j>=0; j-=dimension){
						if(conv(startState[j]) >conv(targetPieceValue)){
							inv2--;
						}else{
							inv2++;
						}
					}
					idx2 = WDLINK[idx2o][0][column(targetPieceValue)];
					break;
				case MOVE_UP:
					for(int j = targetPieceIndex +1; j<blankIndex; j++){
						if(startState[j]>targetPieceValue){
							inv1++;
						}else{
							inv1--;
						}
					}
					idx1 = WDLINK[idx1o][1][row(targetPieceValue)];
					break;
				case MOVE_LEFT:
					for(int j = targetPieceIndex + dimension; j<16; j+=dimension){
						if(conv(startState[j]) > conv(targetPieceValue)){
							inv2++;
						}else{
							inv2--;
						}
					}
					for(int j = blankIndex -dimension; j>=0; j-=dimension){
						if(conv(startState[j]) > conv(targetPieceValue)){
							inv2++;
						}else{
							inv2--;
						}
					}
					idx2 = WDLINK[idx2o][1][column(targetPieceValue)];
					break;
			}
			
			int wd1 = WDTBL[idx1];
			int wd2 = WDTBL[idx2];
			int id1 = IDTBL[inv1];
			int id2 = IDTBL[inv2];
			
			int lowb1 = Math.max(wd1 ,id1);
			int lowb2 = Math.max(wd2 ,id2);
			
			if(depth + lowb1 +lowb2 <= targetDepth){
				startState[targetPieceIndex] = PuzzleConstant.BLANK_STATE;
				startState[blankIndex] = targetPieceValue;
				if(depth == targetDepth || IDA(targetPieceIndex, blankIndex, idx1, idx2, inv1, inv2, depth)){
					moves[depth -1] = direction; 
					return true;
				}
				startState[blankIndex] = PuzzleConstant.BLANK_STATE;
				startState[targetPieceIndex] = targetPieceValue;
			}
			
		}
		return false;
	}
	
	private int getAvailableTargetPieceIndex(int sourcePieceIndex, int moveIndex) {
		return MOVAL[sourcePieceIndex][moveIndex];
	}

	private int conv(int value){
		return CONV[value];
	}
	
	private int row(int index){
		return (index-1) >>2;
	}
	
	private int column(int index){
		return (conv(index) -1)>>2;
		//return (index -1) % BOARD_WIDTH;
	}
	
	private String toTableString(long table) {
		long tvalue = table;
		int[][] t = new int[dimension][dimension];
		
		for(int i=3; i>=0; i--){
			for(int j = 3; j>=0; j--){
				t[i][j] = (int)(table & 7);
				table>>=3;
			}
		}
		StringBuffer sb = new StringBuffer();
		sb.append("{");
		for(int i=0; i<4; i++){
			sb.append("{");
			for(int j = 0; j<4; j++){
				sb.append(t[i][j]);
				if(j != 3){
					sb.append(" ");
				}
			}
			sb.append("}");
		}
		
		sb.append("}");
		sb.append("--");
		sb.append(Long.toString(tvalue));
		return sb.toString();
		
	}
	
	@Override
	public List<DIRECTION> calculate(IProgressListener listener) throws GameException{
		
		if(!initialized){
			throw new AlgorithmNotReadyException("Walking database is not initilized");
		}
		
		int blankIndex;
		
		//find blank index
		for( blankIndex = 0 ; startState[blankIndex] != PuzzleConstant.BLANK_STATE; blankIndex++);
		
		//calculate if has solution
		int tsum = (dimension -1) - blankIndex /dimension;
		
		for(int i = 0; i < BOARD_SIZE; i++){
			if(startState[i] == PuzzleConstant.BLANK_STATE){
				continue;
			}
			for(int j = i+1; j<BOARD_SIZE; j++){
				if(startState[j]!=PuzzleConstant.BLANK_STATE && startState[j] < startState[i]){
					tsum ++;
				}
			}
		}
		if((tsum & 1)!=0){
			throw new GameException("No solution");
		}
		
		//calculate up/down WD table
		int[] work = new int[dimension];
		
		long table  = 0;
		
		for (int i=0; i<dimension; i++){
			for(int j=0; j<dimension; j++){
				work[j] = 0;
			}
			for(int j = 0; j<dimension; j++){
				int peiceValue = startState[i * dimension + j];
				if(peiceValue == PuzzleConstant.BLANK_STATE){
					continue;
				}
				work[row(peiceValue)]++;
			}
			for(int j=0; j<dimension; j++){
				table = (table<<3)| work[j];
			}
		}
		
		//Log.d(TAG, "UP/DOWN table string:" + toTableString(table));
		
		//find UP/DOWN WD table index
		
		int idx1;
		
		for(idx1 = 0; WDPTN[idx1]!=table; idx1++);
		
		//Log.d(TAG, "UP/DOWN table index:" + idx1);
		
		//calculate left/right WD table
		
		table  = 0;
		
		for (int i=0; i<dimension; i++){
			for(int j=0; j<dimension; j++){
				work[j] = 0;
			}
			for(int j = 0; j<dimension; j++){
				int peiceValue = startState[j * dimension + i];
				if(peiceValue == PuzzleConstant.BLANK_STATE){
					continue;
				}
				work[column(peiceValue)]++;
			}
			for(int j=0; j<dimension; j++){
				table = (table<<3)| work[j];
			}
		}
		//Log.d(TAG, "LEFT/RIGHT table string:" + toTableString(table));
		
		//find LEFT/RIGHT WD table index
		int idx2;
		
		for(idx2 = 0; WDPTN[idx2]!=table; idx2++);
		
		//Log.d(TAG, "LEFT/RIGHT table index:" + idx2);
		
		
		//get id1 index
		int inv1 = 0;
		for(int i = 0; i<BOARD_SIZE; i++){
			int pieceValue = startState[i];
			if(pieceValue == PuzzleConstant.BLANK_STATE){
				continue;
			}
			for(int j = i+1; j<BOARD_SIZE; j++){
				int otherValue = startState[j];
				if(otherValue!=PuzzleConstant.BLANK_STATE && otherValue < pieceValue){
					inv1++;
				}
			}
		}
		
		//get id2 index
		int[] cnvp = new int[]{0, 4, 8, 12, 1, 5, 9, 13, 2, 6, 10, 14, 3, 7, 11, 15};
		
		int inv2 = 0;
		for(int i = 0; i<BOARD_SIZE; i++){
			int pieceValue = conv(startState[cnvp[i]]);
			if(pieceValue == PuzzleConstant.BLANK_STATE){
				continue;
			}
			for(int j = i+1; j<BOARD_SIZE; j++){
				int otherValue = conv(startState[cnvp[j]]);
				if(otherValue!=PuzzleConstant.BLANK_STATE && otherValue < pieceValue){
					inv2++;
				}
			}
		}
		
		//get WD and ID value
		int wd1 = WDTBL[idx1];
		int wd2 = WDTBL[idx2];
		int id1 = IDTBL[inv1];
		int id2 = IDTBL[inv2];
		
		//get lowerbound
		int lowb1 = Math.max(wd1, id1);
		int lowb2 = Math.max(wd2, id2);
		
		for(targetDepth = lowb1+lowb2; ; targetDepth+=2){
			
			iterationCounter = 0;
			
			long beginTime = System.currentTimeMillis();
			
			boolean bResolved = IDA(blankIndex, -1, idx1, idx2, inv1, inv2, 0);
			
			long consumeTime = System.currentTimeMillis() - beginTime;
			
			if(listener != null){
				listener.onProgress(new long[]{targetDepth, iterationCounter, consumeTime});
			}
			Log.d(TAG, "running time for targetDepth(" + targetDepth +") is " + (consumeTime) + "ms");
			Log.d(TAG, "iteration count for targetDepth(" + targetDepth +") is " + iterationCounter);
			
			if(bResolved){
				break;
			}
		}
		
		List<DIRECTION> solution  = new ArrayList<DIRECTION>();
		
		for (int i = 0;i < targetDepth; i ++){
			switch(moves[i]){
			case MOVE_UP:
				solution.add(DIRECTION.UP);
				break;
			case MOVE_DOWN:
				solution.add(DIRECTION.DOWN);
				break;
			case MOVE_LEFT:
				solution.add(DIRECTION.LEFT);
				break;
			case MOVE_RIGHT:
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


