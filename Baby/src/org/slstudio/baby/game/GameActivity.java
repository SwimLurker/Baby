package org.slstudio.baby.game;

import java.lang.reflect.Field;
import java.util.HashMap;

import org.slstudio.baby.R;
import org.slstudio.baby.config.ConfigManager;
import org.slstudio.baby.game.service.BGMusicService;
import org.slstudio.baby.game.util.SoundPlayer;

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
import android.widget.RadioButton;
import android.widget.ToggleButton;

public abstract class GameActivity<T extends AbstractGame<?>, P extends IGameProfile>  extends Activity implements IGameListener<T>{
	
	public static final String CONFIGITEM_LEVEL = "CONFIGITEM_LEVEL";
	public static final String CONFIGITEM_MUSIC = "CONFIGITEM_MUSIC";
	public static final String CONFIGITEM_SFX = "CONFIGITEM_SFX";
	
	protected int level = IGameProfile.LEVEL_EASY;
	
	protected P profile = null;
	
	protected SoundPlayer soundPlayer = null;
	
	protected BGMusicService musicService = null;
	
	private boolean isSFXMute = true;
	private boolean isBGMusicMute = true;
	
	protected T game = null;
	
	protected HashMap<String, String> configItems = new HashMap<String, String>();
	
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

	public GameActivity() {
		super();
		initConfigItems();	
	}


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


	public T getGame() {
		return game;
	}


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		loadConfiguration();
		
		initSFX();
	}
	
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		startGame();
		super.onPostCreate(savedInstanceState);
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
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(getMenuId(), menu);
		return true;
	}

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Resources resources = getResources();
		
		switch (item.getItemId()) {
		case R.id.game_menuitem_restart:
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle(resources.getString(R.string.game_info_restartconfirm))
			.setPositiveButton(resources.getString(R.string.game_lable_okbtn),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which) {
							restartGame();
						}
					})
			.setNegativeButton(resources.getString(R.string.game_lable_cancelbtn),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
						}
					}).show();
			break;
		case R.id.game_menuitem_settings:
			showSettingsDialog();
			break;
		case R.id.game_menuitem_quit:
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle(resources.getString(R.string.game_info_quitconfirm))
			.setPositiveButton(resources.getString(R.string.game_lable_okbtn),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which) {
							exitGame();
						}
					})
			.setNegativeButton(resources.getString(R.string.game_lable_cancelbtn),
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
	
	
	protected void showSettingsDialog() {
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_gamesettings, null);
		
		RadioButton easyRB = (RadioButton)dialogView.findViewById(R.id.game_settings_level_easy); 
		RadioButton normalRB = (RadioButton)dialogView.findViewById(R.id.game_settings_level_normal); 
		RadioButton hardRB = (RadioButton)dialogView.findViewById(R.id.game_settings_level_hard);
		
		
		switch(level){
		case IGameProfile.LEVEL_EASY:
			easyRB.setChecked(true);
			normalRB.setChecked(false);
			hardRB.setChecked(false);
			break;
		case IGameProfile.LEVEL_NORMAL:
			easyRB.setChecked(false);
			normalRB.setChecked(true);
			hardRB.setChecked(false);
			break;
		case IGameProfile.LEVEL_HARD:
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
		if(isBGMusicMute()){
			musicTB.setChecked(false);
		}else{
			musicTB.setChecked(true);
		}
		
		ToggleButton sfxTB = (ToggleButton) dialogView.findViewById(R.id.game_settings_sfx);
		if(isSFXMute()){
			sfxTB.setChecked(false);
		}else{
			sfxTB.setChecked(true);
		}
		final Resources resources = getResources();
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_title_settings))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_lable_okbtn),new DialogInterface.OnClickListener() {
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
					
					int newLevel = IGameProfile.LEVEL_NORMAL;
					P newProfile = getProfileFactory().getProfile(IGameProfile.LEVEL_NORMAL);
					if(easyRB.isChecked()){
						newLevel = IGameProfile.LEVEL_EASY;
						newProfile = getProfileFactory().getProfile(IGameProfile.LEVEL_EASY) ;
					}else if(normalRB.isChecked()){
						newLevel = IGameProfile.LEVEL_NORMAL;
						newProfile = getProfileFactory().getProfile(IGameProfile.LEVEL_NORMAL);
					}else if(hardRB.isChecked()){
						newLevel = IGameProfile.LEVEL_HARD;
						newProfile = getProfileFactory().getProfile(IGameProfile.LEVEL_HARD);
					}
					
					if(newLevel != level){
						level = newLevel;
						profile = newProfile;
						needRestartEffect = true;
					}
					
					ToggleButton musicTB = (ToggleButton) dialogView.findViewById(R.id.game_settings_music);
					setBGMusicMute(!musicTB.isChecked());
					
					ToggleButton sfxTB = (ToggleButton) dialogView.findViewById(R.id.game_settings_sfx);
					setSFXMute(!sfxTB.isChecked());
					
					saveConfiguration();
					
					if(needRestartEffect){
					
						new AlertDialog.Builder(GameActivity.this)
							.setIcon(R.drawable.ic_launcher)
							.setTitle(resources.getString(R.string.game_info_settingschangeconfirm))
							.setPositiveButton(resources.getString(R.string.game_lable_yesbtn), new DialogInterface.OnClickListener(){
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									restartGame();
									
								}
							})
							.setNegativeButton(resources.getString(R.string.game_lable_nobtn), new DialogInterface.OnClickListener() {
								
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
        	.setNegativeButton(resources.getString(R.string.game_lable_exitbtn), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
        	.show();
	}

	protected void showSucceedDialog() {
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_gamesucceed, null);
		
		Resources resources = getResources();
		
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_title_dialog))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_lable_restartbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					restartGame();
				}
			})
        	.setNegativeButton(resources.getString(R.string.game_lable_closebtn), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
        	.show();
	}
	
	protected void showGameOverDialog() {
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_gameover, null);
    	
		Resources resources = getResources();
		
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_title_dialog))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_lable_restartbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					restartGame();
				}
			})
        	.setNegativeButton(resources.getString(R.string.game_lable_exitbtn), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					exitGame();
				}
			})
        	.show();
	}

	protected void saveConfiguration() {
		if(getConfigItemName(CONFIGITEM_LEVEL) != null){
			switch(level){
			case IGameProfile.LEVEL_EASY:
				ConfigManager.getInstance().saveConfigure(getConfigItemName(CONFIGITEM_LEVEL), "easy");
				break;
			case IGameProfile.LEVEL_NORMAL:
				ConfigManager.getInstance().saveConfigure(getConfigItemName(CONFIGITEM_LEVEL), "normal");
				break;
			case IGameProfile.LEVEL_HARD:
				ConfigManager.getInstance().saveConfigure(getConfigItemName(CONFIGITEM_LEVEL), "hard");
				break;
			default:
				ConfigManager.getInstance().saveConfigure(getConfigItemName(CONFIGITEM_LEVEL), "normal");
				break;
			}
		}
		
		if(getConfigItemName(CONFIGITEM_MUSIC) != null){
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_PUZZLE_GAME_MUSIC_ON, isBGMusicMute()?"0":"1");
		}
		if(getConfigItemName(CONFIGITEM_SFX) != null){
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_PUZZLE_GAME_SFX_ON, isSFXMute()?"0":"1");
		}
		
	}
	

	protected void loadConfiguration() {
		if(getConfigItemName(CONFIGITEM_LEVEL) != null){
			String levelStr = ConfigManager.getInstance().getConfigure(getConfigItemName(CONFIGITEM_LEVEL));
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
		}
		
		if(getConfigItemName(CONFIGITEM_MUSIC) != null){
			String musicOnStr = ConfigManager.getInstance().getConfigure(getConfigItemName(CONFIGITEM_MUSIC));
			if(musicOnStr != null && (musicOnStr.equals("1")||musicOnStr.equalsIgnoreCase("true"))){
				setBGMusicMute(false);
			}else{
				setBGMusicMute(true);
			}
		}
		if(getConfigItemName(CONFIGITEM_SFX) != null){
			String sfxOnStr = ConfigManager.getInstance().getConfigure(getConfigItemName(CONFIGITEM_SFX));
			if(sfxOnStr != null && (sfxOnStr.equals("1")||sfxOnStr.equalsIgnoreCase("true"))){
				setSFXMute(false);
			}else{
				setSFXMute(true);
			}
		}
	}
	
	protected String getConfigItemName(String type){
		if(configItems.containsKey(type)){
			return configItems.get(type);
		}else{
			return null;
		}
	}
	
	protected abstract T createGameInstance();
	
	protected abstract boolean initGame();
	
	protected abstract void loadSFX();
	
	protected abstract int getBGMResourceID();
	
	protected abstract void initConfigItems();
	
	protected abstract IGameProfileFactory<P> getProfileFactory();
	
	protected abstract int getMenuId();
	
	@Override
	public void onFinished(T game) {
		stopPlayBGMusic();
		showSucceedDialog();
	}


	@Override
	public void onPaused(T game) {
		pauseBGMusic();
	}


	@Override
	public void onResumed(T game) {
		resumeBGMusic();
	}


	@Override
	public void onStarted(T game) {
		startPlayBGMusic();
	}


	@Override
	public void onStopped(T game) {
		stopPlayBGMusic();
	}


	@Override
	public void onGameOver(T game) {
		stopPlayBGMusic();
		showGameOverDialog();
	}


	

	
}
