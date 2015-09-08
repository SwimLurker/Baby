package org.slstudio.baby;

import java.util.List;
import java.util.Random;

import org.slstudio.baby.config.ConfigManager;
import org.slstudio.baby.data.PhotoManager;
import org.slstudio.baby.game.GameActivity;
import org.slstudio.baby.game.IGameProfileFactory;
import org.slstudio.baby.game.tetris.ITetrisListener;
import org.slstudio.baby.game.tetris.TetrisGame;
import org.slstudio.baby.game.tetris.TetrisProfile;
import org.slstudio.baby.game.tetris.TetrisProfileFactory;
import org.slstudio.baby.game.tetris.ui.TetrisMapView;
import org.slstudio.baby.game.tetris.ui.TetrominoView;
import org.slstudio.baby.util.BitmapUtil;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TetrisGameActivity extends GameActivity<TetrisGame, TetrisProfile> implements ITetrisListener{

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
		
	}
	
	
	@Override
	public void onNewTetromino() {
		playSFX(SFX_TETROMINODOWN);
		leftBlockCountTV.setText(resources.getString(R.string.game_tetris_lable_blocknumber) + Long.toString(game.getTetrominoCount()));
		nextTetrominoView.setTetromino(game.getNextTetromino());
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
		scoreTV.setText(resources.getString(R.string.game_tetris_lable_score) + Long.toString(game.getScore()));
		tetrisMapView.invalidate();
	}
	
	@Override
	public void onFinished(TetrisGame game) {
		super.onFinished(game);
		controlBtn.setEnabled(false);
		tetrisMapView.invalidate();
		playSFX(SFX_GAMEFINISH);
	}
	
	@Override
	public void onPaused(TetrisGame game) {
		super.onPaused(game);
		controlBtn.setBackgroundResource(R.layout.selector_btn_resume);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_resume));
		tetrisMapView.invalidate();
	}
	@Override
	public void onResumed(TetrisGame game) {
		super.onResumed(game);
		controlBtn.setBackgroundResource(R.layout.selector_btn_pause);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_pause));
		tetrisMapView.invalidate();
	}
	
	@Override
	public void onStarted(TetrisGame game) {
		Log.d(TAG, "play start sfx");
		playSFX(SFX_GAMESTART);
		
		scoreTV.setText(resources.getString(R.string.game_tetris_lable_score) + Long.toString(game.getScore()));
		leftBlockCountTV.setText(resources.getString(R.string.game_tetris_lable_blocknumber) + Long.toString(game.getTetrominoCount()));
		
		controlBtn.setEnabled(true);
		controlBtn.setBackgroundResource(R.layout.selector_btn_pause);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_pause));
		tetrisMapView.invalidate();
		super.onStarted(game);
	}
	
	@Override
	public void onStopped(TetrisGame game) {
		super.onStopped(game);
		controlBtn.setEnabled(false);
		tetrisMapView.invalidate();
	}
	
	@Override
	public void onGameOver(TetrisGame game) {
		super.onGameOver(game);
		controlBtn.setEnabled(false);
		playSFX(SFX_GAMEOVER);
		tetrisMapView.invalidate();
	}
	
	@Override
	protected TetrisGame createGameInstance() {
		return new TetrisGame(profile.getTetrominoNumber(), profile.getMoveSpeed());
	}
	
	@Override
	protected boolean initGame(){
		LinearLayout layout = (LinearLayout)findViewById(R.id.tetris_bk);
		Drawable bk = getBackgroundPicuture();
		if(bk != null){
			layout.setBackgroundDrawable(bk);
		}
		
		game.addCustomizedListener(this);
		tetrisMapView.setGame(game);
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
	protected IGameProfileFactory<TetrisProfile> getProfileFactory() {
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


	@Override
	protected void initConfigItems() {
		configItems.put(CONFIGITEM_LEVEL, ConfigManager.CONFIG_TETRIS_GAME_LEVEL);
		configItems.put(CONFIGITEM_MUSIC, ConfigManager.CONFIG_TETRIS_GAME_MUSIC_ON);
		configItems.put(CONFIGITEM_SFX, ConfigManager.CONFIG_TETRIS_GAME_SFX_ON);
	}
}
