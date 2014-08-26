package org.slstudio.baby;


import org.slstudio.baby.config.ConfigManager;
import org.slstudio.baby.game.lianliankan.Block;
import org.slstudio.baby.game.AbstractGame;
import org.slstudio.baby.game.GameActivity;
import org.slstudio.baby.game.IGameProfile;
import org.slstudio.baby.game.IGameProfileFactory;
import org.slstudio.baby.game.IGameTimerListener;
import org.slstudio.baby.game.TimeableGame;
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

public class LianLianKanGameActivity extends GameActivity implements IGameTimerListener, ILianLianKanListener{
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
		hintNumberText.setText(resources.getString(R.string.game_lianliankan_lable_hint_number) + "(" + (((LianLianKanProfile)profile).getMaxHintNumber() == -1?"-":Integer.toString(((LianLianKanProfile)profile).getMaxHintNumber())) +")");
		
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
					
					int hintNumber = ((LianLianKan)game).getHintNumber();
					if(hintNumber<=0 && hintNumber!=-1){
						Toast.makeText(LianLianKanGameActivity.this, "No available hint", Toast.LENGTH_SHORT).show();
						return;
					}
					((LianLianKan)game).getHint();
				}
				
			}
			
		});
		startGame();
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
	protected void saveConfiguration() {
		switch(level){
		case IGameProfile.LEVEL_EASY:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_LEVEL, "easy");
			break;
		case IGameProfile.LEVEL_NORMAL:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_LEVEL, "normal");
			break;
		case IGameProfile.LEVEL_HARD:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_LEVEL, "hard");
			break;
		default:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_LEVEL, "normal");
			break;
		}
		
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_MUSIC_ON, isBGMusicMute()?"0":"1");
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_SFX_ON, isSFXMute()?"0":"1");
	}
	

	@Override
	protected void loadConfiguration() {
		String levelStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_LEVEL);
		if(levelStr == null || levelStr.equals("")){
			level = IGameProfile.LEVEL_NORMAL;
		}else if(levelStr.equalsIgnoreCase("easy")){
			level = IGameProfile.LEVEL_EASY;
		}else if(levelStr.equalsIgnoreCase("normal")){
			level = IGameProfile.LEVEL_NORMAL;
		}else if(levelStr.equalsIgnoreCase("hard")){
			level = IGameProfile.LEVEL_HARD;
		}else{
			level = IGameProfile.LEVEL_NORMAL;
		}
		profile = getProfileFactory().getProfile(level);
		
		String musicOnStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_MUSIC_ON);
		if(musicOnStr != null && (musicOnStr.equals("1")||musicOnStr.equalsIgnoreCase("true"))){
			setBGMusicMute(false);
		}else{
			setBGMusicMute(true);
		}
		
		String sfxOnStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_SFX_ON);
		if(sfxOnStr != null && (sfxOnStr.equals("1")||sfxOnStr.equalsIgnoreCase("true"))){
			setSFXMute(false);
		}else{
			setSFXMute(true);
		}
	}
	
	@Override
	protected AbstractGame createGameInstance() {
		LianLianKanProfile p = (LianLianKanProfile)profile;
		return new LianLianKan(p.getRowNumber(), p.getColumnNumber(), p.getSameImageCount(), p.getMaxHintNumber(), p.getMaxTime(), p.getBonusTime());
	}

	@Override
	protected boolean initGame() {
		LianLianKan lianliankan = (LianLianKan) game;
		
		lianliankan.addGameTimerListener(this);
		lianliankan.addCustomizedListener(this);
		
		gameMapView.setGame(lianliankan);
		
		timeUPPlayed = false;
		
		return true;
	}
	
	@Override
	protected IGameProfileFactory getProfileFactory() {
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
	public void onTimeLeftChanged(TimeableGame game, int timeLeft) {
		timeValueText.setText(resources.getString(R.string.game_lianliankan_lable_counter) +":(" + timeLeft +"s)");
		counterProgress.setProgress(timeLeft);
		
		if(!timeUPPlayed && (timeLeft <= ((LianLianKanProfile)profile).getMaxTime() * 0.25)){
			playSFX(SFX_TIMEUP);
			timeUPPlayed = true;
		}
		if(timeUPPlayed && (timeLeft > ((LianLianKanProfile)profile).getMaxTime() * 0.25)){
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
	public void onFinished(AbstractGame game) {
		super.onFinished(game);
		
		controlButton.setEnabled(false);
		hintButton.setEnabled(false);
		gameMapView.invalidate();

		playSFX(SFX_GAMEFINISH);
	}

	@Override
	public void onPaused(AbstractGame game) {
		super.onPaused(game);
		
		controlButton.setBackgroundResource(R.layout.selector_btn_resume);
		controlText.setText(resources.getString(R.string.game_lianliankan_lable_resume));
		hintButton.setEnabled(false);
		gameMapView.invalidate();
	}

	@Override
	public void onResumed(AbstractGame game) {
		super.onResumed(game);
		
		if(((LianLianKan)game).getHintNumber() == -1 ||((LianLianKan)game).getHintNumber() > 0){
			hintButton.setEnabled(true);
		}
		
		controlButton.setBackgroundResource(R.layout.selector_btn_pause);
		controlText.setText(resources.getString(R.string.game_lianliankan_lable_pause));
		gameMapView.invalidate();
	}

	@Override
	public void onStarted(AbstractGame game) {
		playSFX(SFX_GAMESTART);
		
		counterProgress.setMax(((LianLianKanProfile)profile).getMaxTime());
		timeValueText.setText(resources.getString(R.string.game_lianliankan_lable_counter) +":(" + ((LianLianKanProfile)profile).getMaxTime() +"s)");
		controlButton.setEnabled(true);
		controlButton.setBackgroundResource(R.layout.selector_btn_pause);
		controlText.setText(resources.getString(R.string.game_lianliankan_lable_pause));
		hintButton.setEnabled(true);
		hintNumberText.setText(resources.getString(R.string.game_lianliankan_lable_hint_number) + "(" + (((LianLianKanProfile)profile).getMaxHintNumber() == -1?"-":Integer.toString(((LianLianKanProfile)profile).getMaxHintNumber())) +")");
		gameMapView.invalidate();
		
		super.onStarted(game);
	}
	
	@Override
	public void onStopped(AbstractGame game) {
		super.onStopped(game);
		
		controlButton.setEnabled(false);
		hintButton.setEnabled(false);
		gameMapView.invalidate();
		
	}
	
	@Override
	public void onGameOver(AbstractGame game) {
		super.onGameOver(game);
		
		controlButton.setEnabled(false);
		hintButton.setEnabled(false);
		gameMapView.invalidate();
		playSFX(SFX_GAMEOVER);
	}
	
}
