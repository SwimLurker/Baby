package org.slstudio.baby.game;

import java.util.ArrayList;
import java.util.List;

import android.os.Handler;
import android.os.Message;

public abstract class TimeableGame extends AbstractGame {
	
	public static final int MSG_COUNTER = 1;
	
	private Handler handler = null;
	
	private Runnable counterRunnable = null;
	
	private int maxTime;
	
	private int timeLeft;
	
	private int bonusTime = 10;
	
	private List<IGameTimerListener> timerListeners = new ArrayList<IGameTimerListener>();
	
	
	public TimeableGame(int maxTime, int bonusTime){
		this.maxTime = maxTime;
		this.bonusTime = bonusTime;
		
		this.timeLeft = maxTime;
		
		handler = new Handler(){
			@Override
			public void handleMessage(Message msg){
				switch (msg.what){
				case MSG_COUNTER:
					for(IGameTimerListener listener: timerListeners){
						listener.onTimeLeftChanged(TimeableGame.this, timeLeft);
					}
					break;
				}
				super.handleMessage(msg);
			}
		};
		
		counterRunnable =new Runnable(){
			@Override
			public void run() {
				if(isStarted()){
				
					if(!isPaused()){
						handler.sendEmptyMessage(MSG_COUNTER);
						timeLeft --;
					}
					if(timeLeft == 0){
						gameOver();
					}else if(timeLeft >0){
						handler.postDelayed(this, 1000);
					}
				}
			}
		};
		

	}
	
	public int getMaxTime() {
		return maxTime;
	}
	
	public int getBonusTime() {
		return bonusTime;
	}

	public int getTimeLeft() {
		return timeLeft;
	}

	public void setTimeLeft(int timeLeft) {
		this.timeLeft = timeLeft;
	}

	public void addGameTimerListener(IGameTimerListener listener){
		timerListeners.add(listener);
	}
	
	public void removeGameTimerListener(IGameTimerListener listener){
		timerListeners.remove(listener);
	}
	
	@Override
	public void start(){
		super.start();
		handler.post(counterRunnable);
	}
	
	@Override
	public void stop() {
		handler.removeCallbacks(counterRunnable);
		counterRunnable = null;
		super.stop();
	}
	
	public void timeBonus() {
		timeLeft+= bonusTime;
		
		if(timeLeft > maxTime){
			timeLeft = maxTime;
		}
		
		for(IGameTimerListener listener: timerListeners){
			listener.onTimeLeftChanged(this, timeLeft);
		}
		
	}
	
	
	
}
