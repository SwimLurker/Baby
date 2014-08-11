package org.slstudio.baby;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.slstudio.baby.config.ConfigManager;
import org.slstudio.baby.data.PhotoManager;
import org.slstudio.baby.game.AbstractGame;
import org.slstudio.baby.game.GameException;
import org.slstudio.baby.game.IGameListener;
import org.slstudio.baby.game.IGameTimerListener;
import org.slstudio.baby.game.TimeableGame;
import org.slstudio.baby.game.puzzle.IPuzzleGameListener;
import org.slstudio.baby.game.puzzle.Puzzle;
import org.slstudio.baby.game.puzzle.PuzzleProfile;
import org.slstudio.baby.game.puzzle.PuzzleResolver;
import org.slstudio.baby.game.puzzle.ui.PuzzleView;
import org.slstudio.baby.game.service.BGMusicService;
import org.slstudio.baby.game.util.SoundPlayer;
import org.slstudio.baby.util.BitmapUtil;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class PuzzleGameActivity extends Activity implements IGameListener, IGameTimerListener, IPuzzleGameListener{
	
	public static final int MSG_RESOLVER_UPDATE = 10;
	
	
	public static final int SFX_PRESSKEY = 1;
	public static final int SFX_GAMEOVER = 2;
	public static final int SFX_GAMESTART = 3;
	public static final int SFX_GAMEFINISH = 4;
	public static final int SFX_PIECEMOVE = 5;
	public static final int SFX_TIMEUP = 6;
	
	public static final int LEVEL_EASY = 1;
	public static final int LEVEL_NORMAL = 2;
	public static final int LEVEL_HARD = 3;
	
	private int level = LEVEL_EASY;
	private PuzzleProfile profile = null;
	
	private Resources resources = null;
	
	private PuzzleView puzzleView = null;
	private ProgressBar counterProgress = null;
	private TextView timeValueTV = null;
	private ImageButton controlBtn = null;
	private TextView controlTV = null;
	private ImageButton fullPicBtn = null;
	private TextView moveCountTV = null;
	private RelativeLayout fullPicLayout = null;
	private ImageView fullPicIV = null;
	private ImageButton closeFullPicIV = null;
	
	
	private Puzzle puzzle = null;
	
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
	
	
	private Bitmap puzzlePicture = null;
	
	private Random random = new Random();
	
	private Stack<PuzzleResolver.DIRECTION> resolveMoves = null;
	
	private int resolveSteps = 0;
	
	private Handler resolverHandler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch (msg.what){
			case MSG_RESOLVER_UPDATE:
				if(resolveMoves!=null && resolveMoves.size()!=0){
					PuzzleResolver.DIRECTION direction  = resolveMoves.pop();
					int from  = puzzle.getBlankPieceIndex();
					int to  = -1;
					switch(direction){
					case UP:
						to = from - puzzle.getDimension();
						break;
					case DOWN:
						to = from + puzzle.getDimension();
						break;
					case LEFT:
						to = from -1;
						break;
					case RIGHT:
						to = from + 1;
						break;
					}
					puzzle.movePiece(from, to);
					resolveSteps ++;
					this.sendEmptyMessageDelayed(MSG_RESOLVER_UPDATE, 500);
				}else{
					new AlertDialog.Builder(PuzzleGameActivity.this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle(resources.getString(R.string.game_puzzle_info_resolvestep) + Integer.toString(resolveSteps))
					.setPositiveButton(resources.getString(R.string.game_puzzle_lable_closebtn),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which) {
									dialog.dismiss();
									puzzle.gameWin();
								}
							}).show();
					
					enableInput();
					
				}
				break;
			}
			super.handleMessage(msg);
		}
	};
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.activity_puzzle);
		
		resources = getResources();
		
		loadConfiguration();
		
		initSFXandMusic();
		
		puzzleView = (PuzzleView)findViewById(R.id.game_puzzle_layout);
		controlBtn = (ImageButton)findViewById(R.id.game_puzzle_imagebtn_control);
		controlTV = (TextView)findViewById(R.id.game_puzzle_textview_control);
		counterProgress =(ProgressBar)findViewById(R.id.game_puzzle_processbar_counter);
		timeValueTV=(TextView)findViewById(R.id.game_puzzle_textview_counter);
		moveCountTV = (TextView)findViewById(R.id.game_puzzle_textview_movecount);
		fullPicLayout = (RelativeLayout)findViewById(R.id.game_puzzle_fullpic_layout);
		fullPicIV = (ImageView)findViewById(R.id.game_puzzle_fullpic);
		closeFullPicIV = (ImageButton)findViewById(R.id.game_puzzle_fullpic_closebtn);
		fullPicBtn = (ImageButton)findViewById(R.id.game_puzzle_imagebtn_fullpic);
		
		
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_pause));
		
		fullPicBtn.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				if(puzzle!=null){
					showPuzzlePicture();
				}
			}

			private void showPuzzlePicture() {
				puzzleView.setVisibility(View.GONE);
				
				fullPicIV.setImageBitmap(puzzle.getOriginalBitmap());
				fullPicLayout.setVisibility(View.VISIBLE);
				
				final AnimationSet as = new AnimationSet(false);
				
				final Animation ani1 = new ScaleAnimation(0f,1f, 0f, 1f, ScaleAnimation.RELATIVE_TO_SELF, 0f, ScaleAnimation.RELATIVE_TO_SELF, 0f);
				final Animation ani2 = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 1.0f, Animation.RELATIVE_TO_SELF, 0.0f,
		                Animation.RELATIVE_TO_SELF, -1.0f, Animation.RELATIVE_TO_SELF, 0.0f);
				
				as.addAnimation(ani1);
				as.addAnimation(ani2);
				
				ani1.setDuration(500);
				ani1.setFillAfter(true);
				
				ani2.setDuration(500);
				ani2.setFillAfter(true);
				fullPicLayout.setAnimation(as);
				as.start();
			}
			
		});
		
		
		closeFullPicIV.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View arg0) {
				fullPicLayout.setVisibility(View.GONE);
				puzzleView.setVisibility(View.VISIBLE);
			}
			
		});
		
		
		controlBtn.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				playSFX(SFX_PRESSKEY);
				if(puzzle!=null && puzzle.isStarted()){
					if(puzzle.isPaused()){
						puzzle.resume();
						
					}else{
						puzzle.pause();
					}
				}
			}
			
		});
		

		startGame();
		
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu){
		if(!puzzle.isPaused()){
			puzzle.pause();
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
		getMenuInflater().inflate(R.menu.puzzle, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.game_puzzle_menuitem_restart:
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle(resources.getString(R.string.game_puzzle_info_restartconfirm))
			.setPositiveButton(resources.getString(R.string.game_puzzle_lable_okbtn),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which) {
							restartGame();
						}
					})
			.setNegativeButton(resources.getString(R.string.game_puzzle_lable_cancelbtn),
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();
						}
					}).show();
			break;
		case R.id.game_puzzle_menuitem_settings:
			showSettingsDialog();
			break;
		case R.id.game_puzzle_menuitem_resolve:
			if(puzzle!=null && puzzle.isStarted()){
				new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle(resources.getString(R.string.game_puzzle_info_resolveconfirm))
					.setPositiveButton(resources.getString(R.string.game_puzzle_lable_okbtn),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which) {
									if(puzzle!=null &&puzzle.isStarted() && puzzle.isPaused()){
										puzzle.resume();
									}
									resolvePuzzle();
								}
							})
					.setNegativeButton(resources.getString(R.string.game_puzzle_lable_cancelbtn),
							new DialogInterface.OnClickListener() {
		
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									dialog.dismiss();
								}
							}).show();
			}else{
				Toast.makeText(this, "Game is not running!", Toast.LENGTH_SHORT).show();
			}
			break;
		case R.id.game_puzzle_menuitem_quit:
			new AlertDialog.Builder(this)
			.setIcon(R.drawable.ic_launcher)
			.setTitle(resources.getString(R.string.game_puzzle_info_quitconfirm))
			.setPositiveButton(resources.getString(R.string.game_puzzle_lable_okbtn),
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog,	int which) {
							exitGame();
						}
					})
			.setNegativeButton(resources.getString(R.string.game_puzzle_lable_cancelbtn),
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
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_puzzle_gamesettings, null);
		
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
        	.setTitle(resources.getString(R.string.game_puzzle_title_settings))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_puzzle_lable_okbtn),new DialogInterface.OnClickListener() {
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
					PuzzleProfile newProfile = PuzzleProfile.NORMAL;
					if(easyRB.isChecked()){
						newLevel = LEVEL_EASY;
						newProfile = PuzzleProfile.EASY;
					}else if(normalRB.isChecked()){
						newLevel = LEVEL_NORMAL;
						newProfile = PuzzleProfile.NORMAL;
					}else if(hardRB.isChecked()){
						newLevel = LEVEL_HARD;
						newProfile = PuzzleProfile.HARD;
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
					
						new AlertDialog.Builder(PuzzleGameActivity.this)
							.setIcon(R.drawable.ic_launcher)
							.setTitle(resources.getString(R.string.game_puzzle_info_settingschangeconfirm))
							.setPositiveButton(resources.getString(R.string.game_puzzle_lable_yesbtn), new DialogInterface.OnClickListener(){
								
								@Override
								public void onClick(DialogInterface dialog, int which) {
									restartGame();
									
								}
							})
							.setNegativeButton(resources.getString(R.string.game_puzzle_lable_nobtn), new DialogInterface.OnClickListener() {
								
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
        	.setNegativeButton(resources.getString(R.string.game_puzzle_lable_exitbtn), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
        	.show();
	}

	protected void showSucceedDialog() {
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_puzzle_gamesucceed, null);
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_puzzle_title_dialog))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_puzzle_lable_restartbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					restartGame();
				}
			})
        	.setNegativeButton(resources.getString(R.string.game_puzzle_lable_closebtn), new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
				}
			})
        	.show();
	}
	
	protected void showGameOverDialog() {
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_puzzle_gameover, null);
    	
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_puzzle_title_dialog))
        	.setView(dialogView)
        	.setPositiveButton(resources.getString(R.string.game_puzzle_lable_restartbtn),new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					restartGame();
				}
			})
        	.setNegativeButton(resources.getString(R.string.game_puzzle_lable_exitbtn), new DialogInterface.OnClickListener() {
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
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_PUZZLE_GAME_LEVEL, "easy");
			break;
		case LEVEL_NORMAL:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_PUZZLE_GAME_LEVEL, "normal");
			break;
		case LEVEL_HARD:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_PUZZLE_GAME_LEVEL, "hard");
			break;
		default:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_PUZZLE_GAME_LEVEL, "normal");
			break;
		}
		
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_PUZZLE_GAME_MUSIC_ON, isBGMusicMute?"0":"1");
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_PUZZLE_GAME_SFX_ON, isSFXMute?"0":"1");
	}
	

	private void loadConfiguration() {
		String levelStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_PUZZLE_GAME_LEVEL);
		if(levelStr == null || levelStr.equals("")){
			level = LEVEL_NORMAL;
			profile = PuzzleProfile.NORMAL;
		}else if(levelStr.equalsIgnoreCase("easy")){
			level = LEVEL_EASY;
			profile = PuzzleProfile.EASY;
		}else if(levelStr.equalsIgnoreCase("normal")){
			level = LEVEL_NORMAL;
			profile = PuzzleProfile.NORMAL;
		}else if(levelStr.equalsIgnoreCase("hard")){
			level = LEVEL_HARD;
			profile = PuzzleProfile.HARD;
		}else{
			level = LEVEL_NORMAL;
			profile = PuzzleProfile.NORMAL;
		}
		
		String musicOnStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_PUZZLE_GAME_MUSIC_ON);
		if(musicOnStr != null && (musicOnStr.equals("1")||musicOnStr.equalsIgnoreCase("true"))){
			isBGMusicMute = false;
		}else{
			isBGMusicMute = true;
		}
		
		String sfxOnStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_PUZZLE_GAME_SFX_ON);
		if(sfxOnStr != null && (sfxOnStr.equals("1")||sfxOnStr.equalsIgnoreCase("true"))){
			isSFXMute = false;
		}else{
			isSFXMute = true;
		}
	}
	
	
	private boolean startGame(){
		puzzlePicture = getPuzzlePicuture();
		
		puzzle = new Puzzle(puzzlePicture, profile.getDimension(), profile.getMaxTime());
		
		try {
			puzzle.initGame();
		} catch (GameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Toast.makeText(this, "Init game failed", Toast.LENGTH_SHORT).show();
		}
		
		puzzle.addListener(this);
		puzzle.addGameTimerListener(this);
		puzzle.addCustomizedListener(this);
		
		puzzleView.setPuzzle(puzzle);
		
		timeUPPlayed = false;
		
		enableInput();
		
		puzzle.start();
		
		return true;
	}

	protected void restartGame() {
		if(puzzle.isStarted()){
			puzzle.stop();
		}
		startGame();
	}
	
	protected void exitGame(){
		if(puzzle.isStarted()){
			puzzle.stop();
		}
		this.finish();
	}
	
	private void initSFXandMusic() {
		soundPlayer = new SoundPlayer(this);
		soundPlayer.load(R.raw.key, SFX_PRESSKEY);
		soundPlayer.load(R.raw.gameover, SFX_GAMEOVER);
		soundPlayer.load(R.raw.gamestart, SFX_GAMESTART);
		soundPlayer.load(R.raw.gamefinish, SFX_GAMEFINISH);
		soundPlayer.load(R.raw.blockremove, SFX_PIECEMOVE);
		soundPlayer.load(R.raw.timeup, SFX_TIMEUP);
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

	private Bitmap getPuzzlePicuture() {
		List<String> photoList = PhotoManager.getInstance().getAllPhotos();
		
		String filename = photoList.get(random.nextInt(photoList.size()));
		if(filename != null){
			return BitmapUtil.getBitmapFromFile(filename);
		}
		return null;
		
	}


	@Override
	public void onFinished(AbstractGame game) {
		stopPlayBGMusic();
		controlBtn.setEnabled(false);
		fullPicBtn.setEnabled(false);
		puzzleView.removeAllViews();
		puzzleView.invalidate();
		showSucceedDialog();
		playSFX(SFX_GAMEFINISH);
	}


	@Override
	public void onPaused(AbstractGame game) {
		pauseBGMusic();
		fullPicBtn.setEnabled(false);
		controlBtn.setBackgroundResource(R.layout.selector_btn_resume);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_resume));
		puzzleView.invalidate();
	}


	@Override
	public void onResumed(AbstractGame game) {
		resumeBGMusic();
		fullPicBtn.setEnabled(true);
		controlBtn.setBackgroundResource(R.layout.selector_btn_pause);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_pause));
		puzzleView.invalidate();
	}


	@Override
	public void onStarted(AbstractGame game) {
		playSFX(SFX_GAMESTART);
		
		counterProgress.setMax(profile.getMaxTime());
		timeValueTV.setText(resources.getString(R.string.game_puzzle_lable_counter) +":(" + profile.getMaxTime() +"s)");
		controlBtn.setEnabled(true);
		controlBtn.setBackgroundResource(R.layout.selector_btn_pause);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_pause));
		fullPicBtn.setEnabled(true);
		moveCountTV.setText(resources.getString(R.string.game_puzzle_lable_movecount) +":" + puzzle.getMoveCount() );
		puzzleView.invalidate();
		
		startPlayBGMusic();
	}


	@Override
	public void onStopped(AbstractGame game) {
		stopPlayBGMusic();
		controlBtn.setEnabled(false);
		fullPicBtn.setEnabled(false);
		puzzleView.removeAllViews();
		puzzleView.invalidate();
	}


	@Override
	public void onGameOver(AbstractGame game) {
		stopPlayBGMusic();
		controlBtn.setEnabled(false);
		fullPicBtn.setEnabled(false);
		puzzleView.removeAllViews();
		puzzleView.invalidate();
		playSFX(SFX_GAMEOVER);
		showGameOverDialog();
	}

	@Override
	public void onTimeLeftChanged(TimeableGame game, int timeLeft) {
		timeValueTV.setText(resources.getString(R.string.game_puzzle_lable_counter) +":(" + timeLeft +"s)");
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
	public void onPieceMoved(Puzzle puzzle, int from, int to) {
		puzzleView.movePieces(from, to);
		puzzleView.invalidate();
		moveCountTV.setText(resources.getString(R.string.game_puzzle_lable_movecount) +":" + puzzle.getMoveCount());
	}
	
	private void resolvePuzzle() {
		List<PuzzleResolver.DIRECTION> moves = PuzzleResolver.resolvePuzzle(puzzle);
		resolveMoves = new Stack<PuzzleResolver.DIRECTION>();
		resolveSteps = 0;
		for(int i = moves.size() -1; i>=0; i--){
			resolveMoves.push(moves.get(i));
		}
		bolckInput();
		
		resolverHandler.sendEmptyMessage(MSG_RESOLVER_UPDATE);
	}
	
	private void enableInput() {
		puzzleView.setEnabled(true);
		controlBtn.setEnabled(true);
		fullPicBtn.setEnabled(true);
	}
	
	private void bolckInput() {
		puzzleView.setEnabled(false);
		controlBtn.setEnabled(false);
		fullPicBtn.setEnabled(false);
	}
}
