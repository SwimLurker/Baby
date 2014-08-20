package org.slstudio.baby;

import org.slstudio.baby.config.ConfigManager;
import org.slstudio.baby.game.AbstractGame;
import org.slstudio.baby.game.GameActivity;
import org.slstudio.baby.game.tetris.ITetrisListener;
import org.slstudio.baby.game.tetris.TetrisGame;
import org.slstudio.baby.game.tetris.TetrisProfile;
import org.slstudio.baby.game.tetris.ui.TetrisMapView;
import org.slstudio.baby.game.tetris.ui.TetrominoView;

import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.Window;
import android.widget.Toast;

public class TetrisGameActivity extends GameActivity implements ITetrisListener{

	public static final int SFX_PRESSKEY = 1;
	public static final int SFX_GAMEOVER = 2;
	public static final int SFX_GAMESTART = 3;
	public static final int SFX_GAMEFINISH = 4;
	public static final int SFX_LINECLEAR = 5;
	public static final int SFX_TETROMINODOWN = 6;
	

	public static final int LEVEL_EASY = 1;
	public static final int LEVEL_NORMAL = 2;
	public static final int LEVEL_HARD = 3;
	
	private Resources resources = null;
	
	private TetrisMapView tetrisMapView = null;
	
	private TetrominoView nextTetrominoView = null;
	
	private TetrisProfile profile = null;

	private int level = LEVEL_NORMAL;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.activity_tetris);
		
		resources = getResources();
		
		tetrisMapView = (TetrisMapView)findViewById(R.id.game_tetris_layout);
		nextTetrominoView = (TetrominoView)findViewById(R.id.game_next_tetromino);
		
		startGame();

	}
	@Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
	}
	
	
	@Override
	public void onNewTetromino() {
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
		playSFX(SFX_LINECLEAR);
		tetrisMapView.invalidate();
	}
	
	@Override
	public void onFinished(AbstractGame game) {
		super.onFinished(game);
		Toast.makeText(this, "You win", Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public void onPaused(AbstractGame game) {
		super.onPaused(game);
	}
	@Override
	public void onResumed(AbstractGame game) {
		super.onResumed(game);
	}
	
	@Override
	public void onStarted(AbstractGame game) {
		
		super.onStarted(game);
	}
	
	@Override
	public void onStopped(AbstractGame game) {
		super.onStopped(game);
	}
	
	@Override
	public void onGameOver(AbstractGame game) {
		Toast.makeText(this, "Game Over", Toast.LENGTH_SHORT).show();
		super.onGameOver(game);
	}
	
	@Override
	protected void loadConfiguration() {
		String levelStr = ConfigManager.getInstance().getConfigure(ConfigManager.CONFIG_TETRIS_GAME_LEVEL);
		if(levelStr == null || levelStr.equals("")){
			level = LEVEL_NORMAL;
			profile = TetrisProfile.NORMAL;
		}else if(levelStr.equalsIgnoreCase("easy")){
			level = LEVEL_EASY;
			profile = TetrisProfile.EASY;
		}else if(levelStr.equalsIgnoreCase("normal")){
			level = LEVEL_NORMAL;
			profile = TetrisProfile.NORMAL;
		}else if(levelStr.equalsIgnoreCase("hard")){
			level = LEVEL_HARD;
			profile = TetrisProfile.HARD;
		}else{
			level = LEVEL_NORMAL;
			profile = TetrisProfile.NORMAL;
		}
		
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
		
		setBGMusicMute(false);
		setSFXMute(false);
	}
	@Override
	protected void saveConfiguration() {
		switch(level){
		case LEVEL_EASY:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_TETRIS_GAME_LEVEL, "easy");
			break;
		case LEVEL_NORMAL:
			ConfigManager.getInstance().saveConfigure(ConfigManager.CONFIG_TETRIS_GAME_LEVEL, "normal");
			break;
		case LEVEL_HARD:
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
		return new TetrisGame();
	}
	
	@Override
	protected boolean initGame(){
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
		
	}
	
	@Override
	protected int getBGMResourceID() {
		return R.raw.music_bg;
	}
}
