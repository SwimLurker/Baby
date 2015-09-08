package org.slstudio.baby;


import org.slstudio.baby.config.ConfigManager;
import org.slstudio.baby.game.lianliankan.Block;
import org.slstudio.baby.game.GameActivity;
import org.slstudio.baby.game.IGameProfileFactory;
import org.slstudio.baby.game.IGameTimerListener;
import org.slstudio.baby.game.lianliankan.ILianLianKanListener;
import org.slstudio.baby.game.lianliankan.LianLianKan;
import org.slstudio.baby.game.lianliankan.LianLianKanProfile;
import org.slstudio.baby.game.lianliankan.LianLianKanProfileFactory;
import org.slstudio.baby.game.lianliankan.Path;
import org.slstudio.baby.game.lianliankan.ui.LianLianKanMapView;

import android.content.res.Resources;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class LianLianKanGameActivity extends GameActivity<LianLianKan, LianLianKanProfile> implements IGameTimerListener<LianLianKan>, ILianLianKanListener{
	public static final int SFX_PRESSKEY = 1;
	public static final int SFX_GAMEOVER = 2;
	public static final int SFX_GAMESTART = 3;
	public static final int SFX_GAMEFINISH = 4;
	public static final int SFX_HINT = 5;
	public static final int SFX_BLOCKREMOVE = 6;
	public static final int SFX_BLOCKSELECTED = 7;
	public static final int SFX_TIMEUP = 8;
	
	
	private Resources resources = null;
	
	private LianLianKanMapView gameMapView = null;
	private ImageButton hintButton = null;
	private TextView hintNumberText = null;
	private ProgressBar counterProgress = null;
	private TextView timeValueText = null;
	private ImageButton controlButton = null;
	private TextView controlText = null;
	
	private boolean timeUPPlayed = false;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.activity_lianliankan);
		
		resources = getResources();
		
		
		gameMapView = (LianLianKanMapView)findViewById(R.id.game_lianliankan_map);
		
		controlButton = (ImageButton)findViewById(R.id.game_lianliankan_imagebtn_control);
		controlText = (TextView)findViewById(R.id.game_lianliankan_textview_control);
		
		hintButton = (ImageButton)findViewById(R.id.game_lianliankan_imagebtn_hint);
		hintNumberText = (TextView)findViewById(R.id.game_lianliankan_textview_hintnumber);
		
		counterProgress =(ProgressBar)findViewById(R.id.game_lianliankan_processbar_counter);
		timeValueText=(TextView)findViewById(R.id.game_lianliankan_textview_counter);
		
		controlText.setText(resources.getString(R.string.game_lianliankan_lable_pause));
		hintNumberText.setText(resources.getString(R.string.game_lianliankan_lable_hint_number) + "(" + (profile.getMaxHintNumber() == -1?"-":Integer.toString(profile.getMaxHintNumber())) +")");
		
		controlButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				playSFX(SFX_PRESSKEY);
				
				if(game!=null && game.isStarted()){
					if(game.isPaused()){
						game.resume();
						
					}else{
						game.pause();
					}
				}
			}
			
		});
		
		hintButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(game!=null && game.isRunning()){
					playSFX(SFX_PRESSKEY);
					
					int hintNumber = game.getHintNumber();
					if(hintNumber<=0 && hintNumber!=-1){
						Toast.makeText(LianLianKanGameActivity.this, "No available hint", Toast.LENGTH_SHORT).show();
						return;
					}
					game.getHint();
				}
				
			}
			
		});
	}
	
	@Override
	protected void loadSFX() {
		soundPlayer.load(R.raw.key, SFX_PRESSKEY);
		soundPlayer.load(R.raw.gameover, SFX_GAMEOVER);
		soundPlayer.load(R.raw.gamestart, SFX_GAMESTART);
		soundPlayer.load(R.raw.gamefinish, SFX_GAMEFINISH);
		soundPlayer.load(R.raw.hint, SFX_HINT);
		soundPlayer.load(R.raw.blockremove, SFX_BLOCKREMOVE);
		soundPlayer.load(R.raw.blockselected, SFX_BLOCKSELECTED);
		soundPlayer.load(R.raw.timeup, SFX_TIMEUP);
	}
	
	@Override
	protected int getBGMResourceID() {
		return R.raw.music_bg;
	}
	
	
	@Override
	protected LianLianKan createGameInstance() {
		return new LianLianKan(profile.getRowNumber(), profile.getColumnNumber(), profile.getSameImageCount(), profile.getMaxHintNumber(), profile.getMaxTime(), profile.getBonusTime());
	}

	@Override
	protected boolean initGame() {
		game.addGameTimerListener(this);
		game.addCustomizedListener(this);
		
		gameMapView.setGame(game);
		
		timeUPPlayed = false;
		
		return true;
	}
	
	@Override
	protected IGameProfileFactory<LianLianKanProfile> getProfileFactory() {
		return new LianLianKanProfileFactory();
	}

	@Override
	protected int getMenuId() {
		return R.menu.lianliankan;
	}
	
	@Override
	public void onDeadLock(LianLianKan game) {
		game.rerange();
	}

	@Override
	public void onTimeLeftChanged(LianLianKan game, int timeLeft) {
		timeValueText.setText(resources.getString(R.string.game_lianliankan_lable_counter) +":(" + timeLeft +"s)");
		counterProgress.setProgress(timeLeft);
		
		if(!timeUPPlayed && (timeLeft <= profile.getMaxTime() * 0.25)){
			playSFX(SFX_TIMEUP);
			timeUPPlayed = true;
		}
		if(timeUPPlayed && (timeLeft > profile.getMaxTime() * 0.25)){
			timeUPPlayed = false;
		}
		
	}

	
	@Override
	public void onBlockStateChanged(LianLianKan game, int event, Block block) {
		if(event == ILianLianKanListener.BLOCK_REMOVED){
			playSFX(SFX_BLOCKREMOVE);
		}else if(event == ILianLianKanListener.BLOCK_SELECTED){
			playSFX(SFX_BLOCKSELECTED);
		}
		gameMapView.invalidate();
	}

	@Override
	public void onNewHintPathFound(LianLianKan game, Path hintPath) {
		playSFX(SFX_HINT);
		int hintNumber = game.getHintNumber();
		if(hintNumber == 0){
			hintButton.setEnabled(false);
		}
		hintNumberText.setText(resources.getString(R.string.game_lianliankan_lable_hint_number) + "(" + (hintNumber == -1?"-":Integer.toString(hintNumber)) +")");
		gameMapView.invalidate();
	}

	@Override
	public void onGetHintPath(LianLianKan game, Path hintPath) {
		gameMapView.invalidate();
	}
	
	@Override
	public void onFinished(LianLianKan game) {
		super.onFinished(game);
		
		controlButton.setEnabled(false);
		hintButton.setEnabled(false);
		gameMapView.invalidate();

		playSFX(SFX_GAMEFINISH);
	}

	@Override
	public void onPaused(LianLianKan game) {
		super.onPaused(game);
		
		controlButton.setBackgroundResource(R.layout.selector_btn_resume);
		controlText.setText(resources.getString(R.string.game_lianliankan_lable_resume));
		hintButton.setEnabled(false);
		gameMapView.invalidate();
	}

	@Override
	public void onResumed(LianLianKan game) {
		super.onResumed(game);
		
		if(game.getHintNumber() == -1 ||game.getHintNumber() > 0){
			hintButton.setEnabled(true);
		}
		
		controlButton.setBackgroundResource(R.layout.selector_btn_pause);
		controlText.setText(resources.getString(R.string.game_lianliankan_lable_pause));
		gameMapView.invalidate();
	}

	@Override
	public void onStarted(LianLianKan game) {
		playSFX(SFX_GAMESTART);
		
		counterProgress.setMax(profile.getMaxTime());
		timeValueText.setText(resources.getString(R.string.game_lianliankan_lable_counter) +":(" + profile.getMaxTime() +"s)");
		controlButton.setEnabled(true);
		controlButton.setBackgroundResource(R.layout.selector_btn_pause);
		controlText.setText(resources.getString(R.string.game_lianliankan_lable_pause));
		hintButton.setEnabled(true);
		hintNumberText.setText(resources.getString(R.string.game_lianliankan_lable_hint_number) + "(" + (profile.getMaxHintNumber() == -1?"-":Integer.toString(profile.getMaxHintNumber())) +")");
		gameMapView.invalidate();
		
		super.onStarted(game);
	}
	
	@Override
	public void onStopped(LianLianKan game) {
		super.onStopped(game);
		
		controlButton.setEnabled(false);
		hintButton.setEnabled(false);
		gameMapView.invalidate();
		
	}
	
	@Override
	public void onGameOver(LianLianKan game) {
		super.onGameOver(game);
		
		controlButton.setEnabled(false);
		hintButton.setEnabled(false);
		gameMapView.invalidate();
		playSFX(SFX_GAMEOVER);
	}

	@Override
	protected void initConfigItems() {
		configItems.put(CONFIGITEM_LEVEL, ConfigManager.CONFIG_LIANLIANKAN_GAME_LEVEL);
		configItems.put(CONFIGITEM_MUSIC, ConfigManager.CONFIG_LIANLIANKAN_GAME_MUSIC_ON);
		configItems.put(CONFIGITEM_SFX, ConfigManager.CONFIG_LIANLIANKAN_GAME_SFX_ON);
	}
	
}
