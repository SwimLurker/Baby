package org.slstudio.baby.game.rsp;

import java.util.ArrayList;
import java.util.List;

import org.slstudio.baby.game.AbstractGame;

public class RSPGame extends AbstractGame<IRSPListener>{

	private int maxRound = 1;
	private Round currentRound = null;
	
	private List<Round> rounds = new ArrayList<Round>();
	
	
	public RSPGame(int maxRound) {
		super();
		this.maxRound = maxRound;
	}

	public Round getCurrentRound(){
		return currentRound;
	}

	@Override
	public void start(){
		super.start();
		rounds.clear();
	}
	
	
	@Override
	public void gameChecking() {
		// TODO Auto-generated method stub
		
	}
	
	public void startNewRound(){
		
		if(isRunning()){
			int roundNumber = 1;
			if(currentRound != null){
				roundNumber = currentRound.getRoundNumber() + 1;
				rounds.add(currentRound);
			}
			currentRound = new Round(roundNumber);
			currentRound.start();
			for(IRSPListener listener: customizedListeners){
				listener.onRoundStarted(roundNumber);
			}
		}
	}


	public void setP1Choice(RSPType choice) {
		currentRound.setP1Choice(choice);
		for(IRSPListener listener: customizedListeners){
			listener.onP1Ready();
		}
	}
	
	public void setP2Choice(RSPType choice) {
		currentRound.setP2Choice(choice);
		for(IRSPListener listener: customizedListeners){
			listener.onP2Ready();
		}
	}


	public boolean isReadyForFight() {
		return currentRound.getP1Choice() != null && currentRound.getP2Choice() != null;
	}


	public void fight() {
		for(IRSPListener listener: customizedListeners){
			listener.onFight();
		}
	}

	public void finishRound(){
		if(currentRound.checking()){
			for(IRSPListener listener: customizedListeners){
				listener.onRoundFinished(currentRound.getRoundNumber(), currentRound.getResult());
			}
		}
	}
	
}
