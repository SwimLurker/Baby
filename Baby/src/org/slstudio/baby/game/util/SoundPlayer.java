package org.slstudio.baby.game.util;

import java.util.HashMap;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

public class SoundPlayer {
	private Context context = null;
	private SoundPool soundPool = null;
	private HashMap<Integer, Integer> soundPoolMap;
	int streamVolume;
	
	public SoundPlayer(Context context){
		this.context = context;
		initSounds();
	}
	
	public void initSounds(){
		soundPool = new SoundPool(100, AudioManager.STREAM_MUSIC, 100);
		soundPoolMap = new HashMap<Integer, Integer>();
		
		AudioManager manager = (AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
		streamVolume = manager.getStreamVolume(AudioManager.STREAM_MUSIC);		
	}
	
	public void load(int raw, int ID){
		soundPoolMap.put(ID, soundPool.load(context, raw, ID));
	}
	
	public void play(int sound, int uLoop){
		soundPool.play(soundPoolMap.get(sound), streamVolume, streamVolume, 1, uLoop, 1f);
	}
	
}
