package org.slstudio.baby.game.rsp;

import android.util.Log;

public class Round {
	public static final String TAG = "Round";
	
	public static final int STATUS_NOTSTART = 1;
	public static final int STATUS_CHOOSE = 2;
	public static final int STATUS_CHECKING = 3;
	public static final int STATUS_FINISH = 4;
	
	private int roundNumber;
	private RSPType p1Choice = null;
	private RSPType p2Choice = null;
	private RSPResult result;
	private int status;
	
	public Round(int roundNumber) {
		super();
		this.roundNumber = roundNumber;
		status = STATUS_NOTSTART;
	}

	public RSPType getP1Choice() {
		return p1Choice;
	}

	public void setP1Choice(RSPType p1Choice) {
		this.p1Choice = p1Choice;
	}

	public RSPType getP2Choice() {
		return p2Choice;
	}

	public void setP2Choice(RSPType p2Choice) {
		this.p2Choice = p2Choice;
	}

	public int getRoundNumber() {
		return roundNumber;
	}

	public RSPResult getResult() {
		return result;
	}
	
	public void start(){
		Log.d(TAG, "Round " + roundNumber + " start...");
		status = STATUS_CHOOSE;
	}
	
	public boolean checking(){
		status = STATUS_CHECKING;
		
		if((p1Choice == null)||(p2Choice == null)){
			return false;
		}
		if(p1Choice == p2Choice){
			result = RSPResult.DRAW;
		}else if((p1Choice == RSPType.PAPER && p2Choice == RSPType.ROCK)||
				(p1Choice == RSPType.ROCK && p2Choice == RSPType.SCISSORS)||
				(p1Choice == RSPType.SCISSORS && p2Choice == RSPType.PAPER)){
			result = RSPResult.WIN;
		}else {
			result = RSPResult.LOSE;
		}
		return true;
	}
	
	public void finish(){
		Log.d(TAG, "Round " + roundNumber + " finish...");
		status = STATUS_FINISH;
	}
	
}
