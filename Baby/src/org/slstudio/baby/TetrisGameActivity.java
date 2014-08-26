package org.slstudio.baby;

import java.util.List;
import java.util.Random;

import org.slstudio.baby.config.ConfigManager;
import org.slstudio.baby.data.PhotoManager;
import org.slstudio.baby.game.AbstractGame;
import org.slstudio.baby.game.GameActivity;
import org.slstudio.baby.game.IGameProfile;
import org.slstudio.baby.game.IGameProfileFactory;
import org.slstudio.baby.game.tetris.ITetrisListener;
import org.slstudio.baby.game.tetris.TetrisGame;
import org.slstudio.baby.game.tetris.TetrisProfile;
import org.slstudio.baby.game.tetris.TetrisProfileFactory;
import org.slstudio.baby.game.tetris.ui.TetrisMapView;
import org.slstudio.baby.game.tetris.ui.TetrominoView;
import org.slstudio.baby.util.BitmapUtil;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class TetrisGameActivity extends GameActivity implements ITetrisListener{

	public static final String TAG = "TetrisGameActivity";
	
	public static final int SFX_PRESSKEY = 1;
	public static final int SFX_GAMEOVER = 2;
	public static final int SFX_GAMESTART = 3;
	public static final int SFX_GAMEFINISH = 4;
	public static final int SFX_LINECLEAR = 5;
	public static final int SFX_TETROMINODOWN = 6;
	
	private Resources resources = null;
	
	private TetrisMapView tetrisMapView = null;
	private TetrominoView nextTetrominoView = null;
	private ImageButton controlBtn = null;
	private TextView controlTV = null;
	private TextView scoreTV = null;
	private TextView leftBlockCountTV = null;

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.activity_tetris);
		
		resources = getResources();
		
		tetrisMapView = (TetrisMapView)findViewById(R.id.game_tetris_layout);
		nextTetrominoView = (TetrominoView)findViewById(R.id.game_next_tetromino);
		controlBtn = (ImageButton)findViewById(R.id.game_tetris_imagebtn_control);
		controlTV = (TextView)findViewById(R.id.game_tetris_textview_control);
		scoreTV = (TextView)findViewById(R.id.game_tetris_textview_score);
		leftBlockCountTV = (TextView)findViewById(R.id.game_tetris_textview_blocknumber);
		
		controlTV.setText(resources.getString(R.string.game_tetris_lable_pause));
		controlBtn.setOnClickListener(new OnClickListener(){
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
		
		
		startGame();
		
		scoreTV.setText(resources.getString(R.string.game_tetris_lable_score) + Long.toString(((TetrisGame)game).getScore()));
		leftBlockCountTV.setText(resources.getString(R.string.game_tetris_lable_blocknumber) + Long.toString(((TetrisGame)game).getTetrominoCount()));
		
	}
	
	
	@Override
	public void onNewTetromino() {
		playSFX(SFX_TETROMINODOWN);
		leftBlockCountTV.setText(resources.getString(R.string.game_tetris_lable_blocknumber) + Long.toString(((TetrisGame)game).getTetrominoCount()));
		nextTetrominoView.setTetromino(((TetrisGame)game).getNextTetromino());
		tetrisMapView.invalidate();
		nextTetrominoView.invalidate();
	}
	
	@Override
	public void onTetrominoMove() {
		tetrisMapView.invalidate();
	}
	
	@Override
	public void onTetrominoRotated() {
		tetrisMapView.invalidate();
	}
	
	@Override
	public void onLineCleaned(int lineNumber) {
		tetrisMapView.invalidate();
	}
	
	@Override
	public void onLinesCleaned(int cleanedLineNumber) {
		playSFX(SFX_LINECLEAR);
		scoreTV.setText(resources.getString(R.string.game_tetris_lable_score) + Long.toString(((TetrisGame)game).getScore()));
		tetrisMapView.invalidate();
	}
	
	@Override
	public void onFinished(AbstractGame game) {
		super.onFinished(game);
		controlBtn.setEnabled(false);
		tetrisMapView.invalidate();
		playSFX(SFX_GAMEFINISH);
	}
	
	@Override
	public void onPaused(AbstractGame game) {
		super.onPaused(game);
		controlBtn.setBackgroundResource(R.layout.selector_btn_resume);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_resume));
		tetrisMapView.invalidate();
	}
	@Override
	public void onResumed(AbstractGame game) {
		super.onResumed(game);
		controlBtn.setBackgroundResource(R.layout.selector_btn_pause);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_pause));
		tetrisMapView.invalidate();
	}
	
	@Override
	public void onStarted(AbstractGame game) {
		Log.d(TAG, "play start sfx");
		playSFX(SFX_GAMESTART);
		controlBtn.setEnabled(true);
		controlBtn.setBackgroundResource(R.layout.selector_btn_pause);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_pause));
		tetrisMapView.invalidate();
		super.onStarted(game);
	}
	
	@Override
	public void onStopped(AbstractGame game) {
		super.onStopped(game);
		controlBtn.setEnabled(false);
		tetrisMapView.invalidate();
	}
	
	@Override
	public void onGameOver(AbstractGame game) {
		super.onGameOver(game);
		controlBtn.setEnabled(false);
		playSFX(SFX_GAMEOVER);
		tetrisMapView.invalidate();
	}
	
	@Override
	protected void loadConfiguration() {
		String levelStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_TETRIS_GAME_LEVEL);
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
		
		String musicOnStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_TETRIS_GAME_MUSIC_ON);
		if(musicOnStr != null && (musicOnStr.equals("1")||musicOnStr.equalsIgnoreCase("true"))){
			setBGMusicMute(false);
		}else{
			setBGMusicMute(true);
		}
		
		String sfxOnStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_TETRIS_GAME_SFX_ON);
		if(sfxOnStr != null && (sfxOnStr.equals("1")||sfxOnStr.equalsIgnoreCase("true"))){
			setSFXMute(false);
		}else{
			setSFXMute(true);
		}
	}
	@Override
	protected void saveConfiguration() {
		switch(level){
		case IGameProfile.LEVEL_EASY:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_TETRIS_GAME_LEVEL, "easy");
			break;
		case IGameProfile.LEVEL_NORMAL:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_TETRIS_GAME_LEVEL, "normal");
			break;
		case IGameProfile.LEVEL_HARD:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_TETRIS_GAME_LEVEL, "hard");
			break;
		default:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_TETRIS_GAME_LEVEL, "normal");
			break;
		}
		
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_TETRIS_GAME_MUSIC_ON, isBGMusicMute()?"0":"1");
		ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_TETRIS_GAME_SFX_ON, isSFXMute()?"0":"1");
	}
	
	@Override
	protected AbstractGame createGameInstance() {
		return new TetrisGame(((TetrisProfile)profile).getTetrominoNumber(), ((TetrisProfile)profile).getMoveSpeed());
	}
	
	@Override
	protected boolean initGame(){
		LinearLayout layout = (LinearLayout)findViewById(R.id.tetris_bk);
		Drawable bk = getBackgroundPicuture();
		if(bk != null){
			layout.setBackgroundDrawable(bk);
		}
		TetrisGame tetris = (TetrisGame)game;
		tetris.addCustomizedListener(this);
		tetrisMapView.setGame(tetris);
		return true;
	}
	
	@Override
	protected void loadSFX() {
		soundPlayer.load(R.raw.key, SFX_PRESSKEY);
		soundPlayer.load(R.raw.gameover, SFX_GAMEOVER);
		soundPlayer.load(R.raw.gamestart, SFX_GAMESTART);
		soundPlayer.load(R.raw.gamefinish, SFX_GAMEFINISH);
		soundPlayer.load(R.raw.hint, SFX_LINECLEAR);
		soundPlayer.load(R.raw.blockselected, SFX_TETROMINODOWN);
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
		}
	}
	
	@Override
	protected int getBGMResourceID() {
		return R.raw.music_bg;
	}
	
	@Override
	protected IGameProfileFactory getProfileFactory() {
		return new TetrisProfileFactory();
	}
	@Override
	protected int getMenuId() {
		return R.menu.tetris;
	}

	private Drawable getBackgroundPicuture() {
		List<String> photoList = PhotoManager.getInstance().getAllPhotos();
		
		String filename = photoList.get(new Random().nextInt(photoList.size()));
		if(filename != null){
			return BitmapUtil.getDrawableFromFile(filename);
		}
		return null;
		
	}
}
