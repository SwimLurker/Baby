package org.slstudio.baby.game.service;

import org.slstudio.baby.R;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.IBinder;

public class BGMusicService extends Service implements MediaPlayer.OnCompletionListener{

	private MediaPlayer player;
	
	private final IBinder binder = new BGMusicBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		return binder;
	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		//player.start();
	}
	
	@Override
	public void onCreate(){
		super.onCreate();
		player = MediaPlayer.create(this, R.raw.music_bg);
		player.setLooping(true);
		player.setOnCompletionListener(this);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		if(!player.isPlaying()){
			player.start();
		}
		return START_STICKY;
	}
	
	@Override
	public void onDestroy(){
		if(player.isPlaying()){
			player.stop();
		}
		player.release();
	}

	public class BGMusicBinder extends Binder{
		public BGMusicService getService(){
			return BGMusicService.this;
		}
	}
	
	public void pauseMusic(){
		if(player.isPlaying()){
			player.pause();
		}
	}
	
	public void resumeMusic(){
		if(!player.isPlaying()){
			player.start();
		}
	}
	
}