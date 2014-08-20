package org.slstudio.baby.game;

import org.slstudio.baby.game.service.BGMusicService;
import org.slstudio.baby.game.util.SoundPlayer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;

public abstract class GameActivity  extends Activity implements IGameListener{
	protected SoundPlayer soundPlayer = null;
	
	protected BGMusicService musicService = null;
	
	private boolean isSFXMute = true;
	private boolean isBGMusicMute = true;
	
	protected AbstractGame game = null;
	
	private ServiceConnection conn = new ServiceConnection(){
		@Override
		public void onServiceDisconnected(ComponentName name){
			musicService = null;
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder binder){
			musicService = ((BGMusicService.BGMusicBinder)binder).getService();
		}
	};
	

	public boolean isSFXMute() {
		return isSFXMute;
	}


	public void setSFXMute(boolean isSFXMute) {
		this.isSFXMute = isSFXMute;
	}


	public boolean isBGMusicMute() {
		return isBGMusicMute;
	}


	public void setBGMusicMute(boolean isBGMusicMute) {
		this.isBGMusicMute = isBGMusicMute;
	}


	public SoundPlayer getSoundPlayer() {
		return soundPlayer;
	}


	public BGMusicService getMusicService() {
		return musicService;
	}


	public AbstractGame getGame() {
		return game;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		loadConfiguration();
		
		initSFX();
	}
	

	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if(!game.isPaused()){
			game.pause();
		}
		return true;
	}
	
	@Override
	protected void onDestroy(){
		if(musicService != null){
			stopPlayBGMusic();
		}
		super.onDestroy();
	}
	
	@Override
	protected void onPause(){
		if(musicService != null){
			pauseBGMusic();
		}
		super.onPause();
	}
	
	@Override
	protected void onResume(){
		super.onResume();
		if(musicService != null){
			resumeBGMusic();
		}
	}
	
	@Override
	public void onBackPressed() {
		exitGame();
    }

	private void startPlayBGMusic(){
		if(!isBGMusicMute){
			Intent intent = new Intent();
			intent.setClass(this, BGMusicService.class);
			intent.putExtra(BGMusicService.BGM_ID, getBGMResourceID());
			ComponentName name = startService(intent);
			bindService(intent, conn, Context.BIND_AUTO_CREATE);
		}
	}
	
	private void stopPlayBGMusic(){
		if(musicService != null){
			Intent intent = new Intent();
			intent.setClass(this, BGMusicService.class);
			unbindService(conn);
			stopService(intent);
			musicService = null;
		}
	}
	
	private void pauseBGMusic(){
		if(musicService!=null){
			musicService.pauseMusic();
		}
	}
	
	private void resumeBGMusic(){
		if(!isBGMusicMute){
			if(musicService == null){
				startPlayBGMusic();
			}else{
				musicService.resumeMusic();
			}
		}else{
			if(musicService != null){
				stopPlayBGMusic();
			}
		}
	}

	protected void playSFX(int id){
		if(!isSFXMute){
			soundPlayer.play(id, 0);
		}
	}
	
	
	protected boolean startGame(){
		game = createGameInstance();
		if(!prepareGame()){
			return false;
		}
		game.start();
		return true;
	}
	
	protected boolean prepareGame() {
		try{
			game.initGame();
		}catch(Exception exp){
			exp.printStackTrace();
			return false;
		}
		game.addListener(this);
		return initGame();
	}
	
	protected void restartGame() {
		if(game.isStarted()){
			game.stop();
		}
		startGame();
	}
	
	protected void exitGame(){
		if(game.isStarted()){
			game.stop();
		}
		this.finish();
	}
	
	protected void initSFX(){
		soundPlayer = new SoundPlayer(this);
		loadSFX();
	}
	

	protected abstract AbstractGame createGameInstance();
	
	protected abstract boolean initGame();
	
	protected abstract void loadSFX();
	
	protected abstract int getBGMResourceID();
	
	protected abstract void loadConfiguration();
	
	protected abstract void saveConfiguration();
	
	@Override
	public void onFinished(AbstractGame game) {
		stopPlayBGMusic();
	}


	@Override
	public void onPaused(AbstractGame game) {
		pauseBGMusic();
	}


	@Override
	public void onResumed(AbstractGame game) {
		resumeBGMusic();
	}


	@Override
	public void onStarted(AbstractGame game) {
		startPlayBGMusic();
	}


	@Override
	public void onStopped(AbstractGame game) {
		stopPlayBGMusic();
	}


	@Override
	public void onGameOver(AbstractGame game) {
		stopPlayBGMusic();
	}


	

	
}
