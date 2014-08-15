package org.slstudio.baby;


import java.lang.reflect.Field;

import org.slstudio.baby.config.ConfigManager;
import org.slstudio.baby.game.lianliankan.Block;
import org.slstudio.baby.game.AbstractGame;
import org.slstudio.baby.game.GameException;
import org.slstudio.baby.game.IGameListener;
import org.slstudio.baby.game.IGameTimerListener;
import org.slstudio.baby.game.TimeableGame;
import org.slstudio.baby.game.lianliankan.ILianLianKanListener;
import org.slstudio.baby.game.lianliankan.LianLianKan;
import org.slstudio.baby.game.lianliankan.LianLianKanProfile;
import org.slstudio.baby.game.lianliankan.Path;
import org.slstudio.baby.game.service.BGMusicService;
import org.slstudio.baby.game.util.SoundPlayer;
import org.slstudio.baby.game.lianliankan.ui.LianLianKanMapView;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class LianLianKanGameActivity extends Activity implements IGameListener, IGameTimerListener, ILianLianKanListener{
	public static final int SFX_PRESSKEY = 1;
	public static final int SFX_GAMEOVER = 2;
	public static final int SFX_GAMESTART = 3;
	public static final int SFX_GAMEFINISH = 4;
	public static final int SFX_HINT = 5;
	public static final int SFX_BLOCKREMOVE = 6;
	public static final int SFX_BLOCKSELECTED = 7;
	public static final int SFX_TIMEUP = 8;
	
	public static final int LEVEL_EASY = 1;
	public static final int LEVEL_NORMAL = 2;
	public static final int LEVEL_HARD = 3;
	
	
	private LianLianKan lianliankan = null;
	private int level = LEVEL_EASY;
	private LianLianKanProfile profile = null;
	
	
	private Resources resources = null;
	
	private LianLianKanMapView gameMapView = null;
	private ImageButton hintButton = null;
	private TextView hintNumberText = null;
	private ProgressBar counterProgress = null;
	private TextView timeValueText = null;
	private ImageButton controlButton = null;
	private TextView controlText = null;
	
	private SoundPlayer soundPlayer = null;
	
	private BGMusicService musicService = null;
	
	private boolean isSFXMute = true;
	private boolean isBGMusicMute = true;
	
	private boolean timeUPPlayed = false;
	
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
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.activity_lianliankan);
		
		resources = getResources();
		
		loadConfiguration();
		
		initSFXandMusic();
		
		
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
				
				if(lianliankan!=null && lianliankan.isStarted()){
					if(lianliankan.isPaused()){
						lianliankan.resume();
						
					}else{
						lianliankan.pause();
					}
				}
			}
			
		});
		
		hintButton.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(lianliankan!=null && lianliankan.isRunning()){
					playSFX(SFX_PRESSKEY);
					
					int hintNumber = lianliankan.getHintNumber();
					if(hintNumber<=0 && hintNumber!=-1){
						Toast.makeText(LianLianKanGameActivity.this, "No available hint", Toast.LENGTH_SHORT).show();
						return;
					}
					lianliankan.getHint();
				}
				
			}
			
		});
		startGame();
	}
	
	private void initSFXandMusic() {
		soundPlayer = new SoundPlayer(this);
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
	public boolean onPrepareOptionsMenu(Menu menu){
		if(!lianliankan.isPaused()){
			lianliankan.pause();
		}
		return true;
	}
	
	@Override
	public void onOptionsMenuClosed(Menu menu){
		super.onOptionsMenuClosed(menu);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.lianliankan, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.game_lianliankan_menuitem_restart:
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle(resources.getString(R.string.game_lianliankan_info_restartconfirm))
			.setPositiveButton(resources.getString(R.string.game_lianliankan_lable_okbtn),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which) {
							restartGame();
						}
					})
			.setNegativeButton(resources.getString(R.string.game_lianliankan_lable_cancelbtn),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
						}
					}).show();
			break;
		case R.id.game_lianliankan_menuitem_settings:
			showSettingsDialog();
			break;
		case R.id.game_lianliankan_menuitem_quit:
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle(resources.getString(R.string.game_lianliankan_info_quitconfirm))
			.setPositiveButton(resources.getString(R.string.game_lianliankan_lable_okbtn),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which) {
							exitGame();
						}
					})
			.setNegativeButton(resources.getString(R.string.game_lianliankan_lable_cancelbtn),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
						}
					}).show();
			break;
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

	private void showSettingsDialog() {
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_lianliankan_gamesettings, null);
		
		RadioButton easyRB = (RadioButton)dialogView.findViewById(R.id.game_settings_level_easy); 
		RadioButton normalRB = (RadioButton)dialogView.findViewById(R.id.game_settings_level_normal); 
		RadioButton hardRB = (RadioButton)dialogView.findViewById(R.id.game_settings_level_hard);
		
		
		switch(level){
		case LEVEL_EASY:
			easyRB.setChecked(true);
			normalRB.setChecked(false);
			hardRB.setChecked(false);
			break;
		case LEVEL_NORMAL:
			easyRB.setChecked(false);
			normalRB.setChecked(true);
			hardRB.setChecked(false);
			break;
		case LEVEL_HARD:
			easyRB.setChecked(false);
			normalRB.setChecked(false);
			hardRB.setChecked(true);
			break;
		default:
			easyRB.setChecked(false);
			normalRB.setChecked(true);
			hardRB.setChecked(false);
			break;
		}
    	
		
		ToggleButton musicTB = (ToggleButton) dialogView.findViewById(R.id.game_settings_music);
		if(isBGMusicMute){
			musicTB.setChecked(false);
		}else{
			musicTB.setChecked(true);
		}
		
		ToggleButton sfxTB = (ToggleButton) dialogView.findViewById(R.id.game_settings_sfx);
		if(isSFXMute){
			sfxTB.setChecked(false);
		}else{
			sfxTB.setChecked(true);
		}
		
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_lianliankan_title_settings))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_lianliankan_lable_okbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					try{
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						field.setAccessible(true);  
			            field.set(dialog, false);
			        }catch(Exception e) {
			        	e.printStackTrace();  
			        }
					
					boolean needRestartEffect = false;
					
					RadioButton easyRB = (RadioButton)dialogView.findViewById(R.id.game_settings_level_easy); 
					RadioButton normalRB = (RadioButton)dialogView.findViewById(R.id.game_settings_level_normal); 
					RadioButton hardRB = (RadioButton)dialogView.findViewById(R.id.game_settings_level_hard);
					
					int newLevel = LEVEL_NORMAL;
					LianLianKanProfile newProfile = LianLianKanProfile.NORMAL;
					if(easyRB.isChecked()){
						newLevel = LEVEL_EASY;
						newProfile = LianLianKanProfile.EASY;
					}else if(normalRB.isChecked()){
						newLevel = LEVEL_NORMAL;
						newProfile = LianLianKanProfile.NORMAL;
					}else if(hardRB.isChecked()){
						newLevel = LEVEL_HARD;
						newProfile = LianLianKanProfile.HARD;
					}
					
					if(newLevel != level){
						level = newLevel;
						profile = newProfile;
						needRestartEffect = true;
					}
					
					ToggleButton musicTB = (ToggleButton) dialogView.findViewById(R.id.game_settings_music);
					isBGMusicMute = !musicTB.isChecked();
					
					ToggleButton sfxTB = (ToggleButton) dialogView.findViewById(R.id.game_settings_sfx);
					isSFXMute = !sfxTB.isChecked();
					
					saveConfiguration();
					
					if(needRestartEffect){
					
						new AlertDialog.Builder(LianLianKanGameActivity.this)
							.setIcon(R.drawable.ic_launcher)
							.setTitle(resources.getString(R.string.game_lianliankan_info_settingschangeconfirm))
							.setPositiveButton(resources.getString(R.string.game_lianliankan_lable_yesbtn), new DialogInterface.OnClickListener(){
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									restartGame();
									
								}
							})
							.setNegativeButton(resources.getString(R.string.game_lianliankan_lable_nobtn), new DialogInterface.OnClickListener() {
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									
								}
							})
							.show();
					}
					
					try{
						Field field = dialog.getClass().getSuperclass().getDeclaredField("mShowing");
						field.setAccessible(true);  
			            field.set(dialog, true);
			        }catch(Exception e) {
			        	e.printStackTrace();  
			        }
				}

			})
        	.setNegativeButton(resources.getString(R.string.game_lianliankan_lable_exitbtn), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
        	.show();
	}

	protected void showSucceedDialog() {
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_lianliankan_gamesucceed, null);
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_lianliankan_title_dialog))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_lianliankan_lable_restartbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					restartGame();
				}
			})
        	.setNegativeButton(resources.getString(R.string.game_lianliankan_lable_exitbtn), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					exitGame();
				}
			})
        	.show();
	}
	
	protected void showGameOverDialog() {
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_lianliankan_gameover, null);
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_lianliankan_title_dialog))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_lianliankan_lable_restartbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					restartGame();
				}
			})
        	.setNegativeButton(resources.getString(R.string.game_lianliankan_lable_exitbtn), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					exitGame();
				}
			})
        	.show();
	}
	
	private void saveConfiguration() {
		switch(level){
		case LEVEL_EASY:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_LEVEL, "easy");
			break;
		case LEVEL_NORMAL:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_LEVEL, "normal");
			break;
		case LEVEL_HARD:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_LEVEL, "hard");
			break;
		default:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_LEVEL, "normal");
			break;
		}
		
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_MUSIC_ON, isBGMusicMute?"0":"1");
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_SFX_ON, isSFXMute?"0":"1");
	}
	

	private void loadConfiguration() {
		String levelStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_LEVEL);
		if(levelStr == null || levelStr.equals("")){
			level = LEVEL_NORMAL;
			profile = LianLianKanProfile.NORMAL;
		}else if(levelStr.equalsIgnoreCase("easy")){
			level = LEVEL_EASY;
			profile = LianLianKanProfile.EASY;
		}else if(levelStr.equalsIgnoreCase("normal")){
			level = LEVEL_NORMAL;
			profile = LianLianKanProfile.NORMAL;
		}else if(levelStr.equalsIgnoreCase("hard")){
			level = LEVEL_HARD;
			profile = LianLianKanProfile.HARD;
		}else{
			level = LEVEL_NORMAL;
			profile = LianLianKanProfile.NORMAL;
		}
		
		String musicOnStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_MUSIC_ON);
		if(musicOnStr != null && (musicOnStr.equals("1")||musicOnStr.equalsIgnoreCase("true"))){
			isBGMusicMute = false;
		}else{
			isBGMusicMute = true;
		}
		
		String sfxOnStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_LIANLIANKAN_GAME_SFX_ON);
		if(sfxOnStr != null && (sfxOnStr.equals("1")||sfxOnStr.equalsIgnoreCase("true"))){
			isSFXMute = false;
		}else{
			isSFXMute = true;
		}
	}
	
	private boolean startGame(){
		lianliankan = new LianLianKan(profile.getRowNumber(), profile.getColumnNumber(), profile.getSameImageCount(), profile.getMaxHintNumber(), profile.getMaxTime(), profile.getBonusTime());
		
		try {
			lianliankan.initGame();
		} catch (GameException e) {
			e.printStackTrace();
			Toast.makeText(this, "Init Game Failed:" + e.getMessage(), Toast.LENGTH_SHORT).show();
			return false;
		}
		
		lianliankan.addListener(this);
		lianliankan.addGameTimerListener(this);
		lianliankan.addCustomizedListener(this);
		
		gameMapView.setGame(lianliankan);
		
		timeUPPlayed = false;
		
		lianliankan.start();
		
		return true;
	}

	protected void restartGame() {
		if(lianliankan.isStarted()){
			lianliankan.stop();
		}
		startGame();
	}
	
	protected void exitGame(){
		if(lianliankan.isStarted()){
			lianliankan.stop();
		}
		this.finish();
	}
	
	private void playSFX(int id){
		if(!isSFXMute){
			soundPlayer.play(id, 0);
		}
	}
	
	private void startPlayBGMusic(){
		if(!isBGMusicMute){
			Intent intent = new Intent();
			intent.setClass(this, BGMusicService.class);
			intent.putExtra(BGMusicService.BGM_ID, R.raw.music_bg_lianliankan);
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

	@Override
	public void onDeadLock(LianLianKan game) {
		game.rerange();
	}

	@Override
	public void onTimeLeftChanged(TimeableGame game, int timeLeft) {
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
	public void onFinished(AbstractGame game) {
		stopPlayBGMusic();
		
		controlButton.setEnabled(false);
		hintButton.setEnabled(false);
		gameMapView.invalidate();
		showSucceedDialog();
		playSFX(SFX_GAMEFINISH);
	}

	@Override
	public void onPaused(AbstractGame game) {
		pauseBGMusic();
		controlButton.setBackgroundResource(R.layout.selector_btn_resume);
		controlText.setText(resources.getString(R.string.game_lianliankan_lable_resume));
		hintButton.setEnabled(false);
		gameMapView.invalidate();
	}

	@Override
	public void onResumed(AbstractGame game) {
		
		resumeBGMusic();
		if(lianliankan.getHintNumber() == -1 ||lianliankan.getHintNumber()>0){
			hintButton.setEnabled(true);
		}
		
		controlButton.setBackgroundResource(R.layout.selector_btn_pause);
		controlText.setText(resources.getString(R.string.game_lianliankan_lable_pause));
		gameMapView.invalidate();
	}

	@Override
	public void onStarted(AbstractGame game) {
		playSFX(SFX_GAMESTART);
		
		counterProgress.setMax(profile.getMaxTime());
		timeValueText.setText(resources.getString(R.string.game_lianliankan_lable_counter) +":(" + profile.getMaxTime() +"s)");
		controlButton.setEnabled(true);
		controlButton.setBackgroundResource(R.layout.selector_btn_pause);
		controlText.setText(resources.getString(R.string.game_lianliankan_lable_pause));
		hintButton.setEnabled(true);
		hintNumberText.setText(resources.getString(R.string.game_lianliankan_lable_hint_number) + "(" + (profile.getMaxHintNumber() == -1?"-":Integer.toString(profile.getMaxHintNumber())) +")");
		gameMapView.invalidate();
		
		startPlayBGMusic();
	}
	
	@Override
	public void onStopped(AbstractGame game) {
		stopPlayBGMusic();
		controlButton.setEnabled(false);
		hintButton.setEnabled(false);
		gameMapView.invalidate();
		
	}
	
	@Override
	public void onGameOver(AbstractGame game) {
		stopPlayBGMusic();
		controlButton.setEnabled(false);
		hintButton.setEnabled(false);
		gameMapView.invalidate();
		playSFX(SFX_GAMEOVER);
		showGameOverDialog();
	}
	
}
