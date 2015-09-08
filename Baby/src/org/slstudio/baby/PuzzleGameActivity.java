package org.slstudio.baby;

import java.util.List;
import java.util.Random;
import java.util.Stack;

import org.slstudio.baby.config.ConfigManager;
import org.slstudio.baby.data.PhotoManager;
import org.slstudio.baby.game.GameActivity;
import org.slstudio.baby.game.GameException;
import org.slstudio.baby.game.IGameProfileFactory;
import org.slstudio.baby.game.IGameTimerListener;
import org.slstudio.baby.game.puzzle.AlgorithmNotReadyException;
import org.slstudio.baby.game.puzzle.IDAStarWithWDAlgorithm;
import org.slstudio.baby.game.puzzle.IProgressListener;
import org.slstudio.baby.game.puzzle.IPuzzleGameListener;
import org.slstudio.baby.game.puzzle.Puzzle;
import org.slstudio.baby.game.puzzle.PuzzleProfile;
import org.slstudio.baby.game.puzzle.PuzzleProfileFactory;
import org.slstudio.baby.game.puzzle.PuzzleResolver;
import org.slstudio.baby.game.puzzle.DIRECTION;
import org.slstudio.baby.game.puzzle.ui.PuzzleView;
import org.slstudio.baby.util.BitmapUtil;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class PuzzleGameActivity extends GameActivity<Puzzle, PuzzleProfile> implements IGameTimerListener<Puzzle>, IPuzzleGameListener{
	
	public static final int MSG_RESOLVER_UPDATE = 10;
	
	public static final int SFX_PRESSKEY = 1;
	public static final int SFX_GAMEOVER = 2;
	public static final int SFX_GAMESTART = 3;
	public static final int SFX_GAMEFINISH = 4;
	public static final int SFX_PIECEMOVE = 5;
	public static final int SFX_TIMEUP = 6;
	
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
	
	
	private boolean timeUPPlayed = false;
	
	private Bitmap puzzlePicture = null;
	
	private Random random = new Random();
	
	private Stack<DIRECTION> resolveMoves = null;
	
	private int resolveSteps = 0;
	
	private Handler resolverHandler = new Handler(){
		
		@Override
		public void handleMessage(Message msg){
			
			if(game.isStarted()){
				switch (msg.what){
				case MSG_RESOLVER_UPDATE:
					
					if(resolveMoves!=null && resolveMoves.size()!=0){
						DIRECTION direction  = resolveMoves.pop();
						int from  = game.getBlankPieceIndex();
						int to  = -1;
						switch(direction){
						case UP:
							to = from - game.getDimension();
							break;
						case DOWN:
							to = from + game.getDimension();
							break;
						case LEFT:
							to = from -1;
							break;
						case RIGHT:
							to = from + 1;
							break;
						}
						game.movePiece(from, to);
						resolveSteps ++;
						this.sendEmptyMessageDelayed(MSG_RESOLVER_UPDATE, 500);
					}else{
						new AlertDialog.Builder(PuzzleGameActivity.this)
						.setIcon(R.drawable.ic_launcher)
						.setTitle(resources.getString(R.string.game_puzzle_info_resolvestep) + Integer.toString(resolveSteps))
						.setPositiveButton(resources.getString(R.string.game_lable_closebtn),
								new DialogInterface.OnClickListener() {
									@Override
									public void onClick(DialogInterface dialog,	int which) {
										dialog.dismiss();
										game.gameWin();
									}
								}).show();
						
						enableInput();
						
					}
					break;
				}
			}
			super.handleMessage(msg);
		}
	};
	
	
	private PuzzleResolverAsyncTask task = null;
	
	private PuzzleResolver resolver = null;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
        
		setContentView(R.layout.activity_puzzle);
		
		resources = getResources();
		
		
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
				if(game!=null){
					showPuzzlePicture();
				}
			}

			private void showPuzzlePicture() {
				puzzleView.setVisibility(View.GONE);
				
				fullPicIV.setImageBitmap(game.getOriginalBitmap());
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
				if(game!=null && game.isStarted()){
					if(game.isPaused()){
						game.resume();
						
					}else{
						game.pause();
					}
				}
			}
			
		});
		AsyncTask ta = new AsyncTask(){

			@Override
			protected Object doInBackground(Object... params) {
				IDAStarWithWDAlgorithm.initialize();
				return null;
			}
			
		};
		ta.execute(new Object[0]);
		
	}
	

	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		switch (item.getItemId()) {
		case R.id.game_puzzle_menuitem_resolve:
			if(game!=null && game.isStarted()){
				new AlertDialog.Builder(this)
					.setIcon(R.drawable.ic_launcher)
					.setTitle(resources.getString(R.string.game_puzzle_info_resolveconfirm))
					.setPositiveButton(resources.getString(R.string.game_lable_okbtn),
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,	int which) {
									resolvePuzzle();
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
			}else{
				Toast.makeText(this, "Game is not running!", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}
	

	@Override
	protected int getBGMResourceID() {
		return R.raw.music_bg_puzzle;
	}
	
	@Override
	protected void loadSFX() {
		soundPlayer.load(R.raw.key, SFX_PRESSKEY);
		soundPlayer.load(R.raw.gameover, SFX_GAMEOVER);
		soundPlayer.load(R.raw.gamestart, SFX_GAMESTART);
		soundPlayer.load(R.raw.gamefinish, SFX_GAMEFINISH);
		soundPlayer.load(R.raw.blockremove, SFX_PIECEMOVE);
		soundPlayer.load(R.raw.timeup, SFX_TIMEUP);
		
	}
	
	@Override
	protected Puzzle createGameInstance() {
		puzzlePicture = getPuzzlePicuture();		
		return new Puzzle(puzzlePicture, profile.getDimension(), profile.getMaxTime());
	}
	
	@Override
	protected boolean initGame(){
		game.addGameTimerListener(this);
		game.addCustomizedListener(this);
		
		puzzleView.setPuzzle(game);
		
		timeUPPlayed = false;
		
		enableInput();
		
		return true;
	}
	
	@Override
	protected IGameProfileFactory<PuzzleProfile> getProfileFactory() {
		return new PuzzleProfileFactory();
	}


	@Override
	protected int getMenuId() {
		return R.menu.puzzle;
	}

	@Override
	public void onFinished(Puzzle game) {
		super.onFinished(game);
		controlBtn.setEnabled(false);
		fullPicBtn.setEnabled(false);
		puzzleView.removeAllViews();
		puzzleView.invalidate();
		playSFX(SFX_GAMEFINISH);
	}


	@Override
	public void onPaused(Puzzle game) {
		super.onPaused(game);
		fullPicBtn.setEnabled(false);
		controlBtn.setBackgroundResource(R.layout.selector_btn_resume);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_resume));
		puzzleView.invalidate();
	}


	@Override
	public void onResumed(Puzzle game) {
		super.onResumed(game);
		fullPicBtn.setEnabled(true);
		controlBtn.setBackgroundResource(R.layout.selector_btn_pause);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_pause));
		puzzleView.invalidate();
	}


	@Override
	public void onStarted(Puzzle game) {
		playSFX(SFX_GAMESTART);
		
		counterProgress.setMax(profile.getMaxTime());
		timeValueTV.setText(resources.getString(R.string.game_puzzle_lable_counter) +":(" + profile.getMaxTime() +"s)");
		controlBtn.setEnabled(true);
		controlBtn.setBackgroundResource(R.layout.selector_btn_pause);
		controlTV.setText(resources.getString(R.string.game_puzzle_lable_pause));
		fullPicBtn.setEnabled(true);
		moveCountTV.setText(resources.getString(R.string.game_puzzle_lable_movecount) +":" + game.getMoveCount() );
		puzzleView.invalidate();
		
		super.onStarted(game);
	}


	@Override
	public void onStopped(Puzzle game) {
		super.onStopped(game);
		
		controlBtn.setEnabled(false);
		fullPicBtn.setEnabled(false);
		puzzleView.removeAllViews();
		puzzleView.invalidate();
	}


	@Override
	public void onGameOver(Puzzle game) {
		super.onGameOver(game);
		
		controlBtn.setEnabled(false);
		fullPicBtn.setEnabled(false);
		puzzleView.removeAllViews();
		puzzleView.invalidate();
		playSFX(SFX_GAMEOVER);
		
	}

	@Override
	public void onTimeLeftChanged(Puzzle game, int timeLeft) {
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
	
	private Bitmap getPuzzlePicuture() {
		List<String> photoList = PhotoManager.getInstance().getAllPhotos();
		
		String filename = photoList.get(random.nextInt(photoList.size()));
		if(filename != null){
			return BitmapUtil.getBitmapFromFile(filename);
		}
		return null;
		
	}
	
	private void resolvePuzzle() {

		
		
		final View dialogView = getLayoutInflater().inflate(R.layout.dialog_puzzle_resolving, null);
		
    	Dialog dialog = new AlertDialog.Builder(this)
        	.setIcon(R.drawable.ic_launcher)
        	.setTitle(resources.getString(R.string.game_puzzle_info_resolvingprogress))
        	.setView(dialogView)
        	.setCancelable(false)
        	.setPositiveButton(resources.getString(R.string.game_lable_cancelbtn), new DialogInterface.OnClickListener(){

				@Override
				public void onClick(DialogInterface dialog, int which) {
					if(task!=null){
						task.cancel(true);
					}
				}
        		
        	})
        	.show();
    	
    	
    	task = new PuzzleResolverAsyncTask(dialog);
    	
		task.execute(new Puzzle[]{game});
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
	
	class PuzzleResolverAsyncTask extends AsyncTask<Puzzle, Long, List<DIRECTION> > implements IProgressListener{
		private Dialog progressDialog = null;
		
		public PuzzleResolverAsyncTask(Dialog dialog){
			this.progressDialog = dialog;
		}
		
		@Override
		protected List<DIRECTION> doInBackground(Puzzle... params) {
			resolver = new PuzzleResolver(params[0]);
			List<DIRECTION> result = null;
			while(game.isStarted()){
				try{
					result = resolver.resolvePuzzle(this);
					break;
				}catch(AlgorithmNotReadyException anre){
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}catch(GameException exp){
					exp.printStackTrace();
					break;
				}
			}
			return result;
		}
		
		@Override
		protected void onPostExecute(List<DIRECTION> result){
			
			progressDialog.dismiss();
			
			if(game!=null && game.isStarted() && game.isPaused()){
				game.resume();
			}
			if(result!=null && !task.isCancelled()){
				resolveMoves = new Stack<DIRECTION>();
				resolveSteps = 0;
				for(int i = result.size() -1; i>=0; i--){
					resolveMoves.push(result.get(i));
				}
				bolckInput();
				resolverHandler.sendEmptyMessage(MSG_RESOLVER_UPDATE);
			}
		}
		
		@Override  
	    protected void onProgressUpdate(Long... values) {  
	        long ans = values[0];  
	        long iterationCount = values[1];
	        long consumeTime = values[2];
	        
	        TextView resolvingProgressTV = (TextView) progressDialog.findViewById(R.id.game_puzzle_textview_resolvingprogress);
			
	        resolvingProgressTV.setText("Target Moves("+ Long.toString(ans) + "), Iteration Count(" + Long.toString(iterationCount) +"), Time Consumed(" + Long.toString(consumeTime) + ")");  
	    }

		@Override
		public void onProgress(long[] progressData) {
			this.publishProgress(progressData[0], progressData[1], progressData[2]);
		}
		
		@Override
		protected void onCancelled() {
			resolver.setCancelled(true);
		}
	}

	@Override
	protected void initConfigItems() {
		configItems.put(CONFIGITEM_LEVEL, ConfigManager.CONFIG_PUZZLE_GAME_LEVEL);
		configItems.put(CONFIGITEM_MUSIC, ConfigManager.CONFIG_PUZZLE_GAME_MUSIC_ON);
		configItems.put(CONFIGITEM_SFX, ConfigManager.CONFIG_PUZZLE_GAME_SFX_ON);
	}

	

	

}
