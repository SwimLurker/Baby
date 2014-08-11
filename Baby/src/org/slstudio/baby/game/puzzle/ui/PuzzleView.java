package org.slstudio.baby.game.puzzle.ui;

import org.slstudio.baby.R;
import org.slstudio.baby.game.puzzle.Puzzle;
import org.slstudio.baby.game.puzzle.PuzzlePiece;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.FontMetrics;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.AbsoluteLayout;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

@SuppressWarnings("deprecation")
public class PuzzleView extends AbsoluteLayout{
	public static final int PIECEVIEW_ID_BASE = 1;
	public static final String NAMESPACE_SWIMLURKER = "http://schemas.slstudio.org/apk/res/android";
	
	private static Paint darkBKPaint, infoBoxPaint, textPaint;
	
	private Context context = null;
	//private ImageView[] piecesIV;
	
	
	private boolean onAnimation = false;
	
	private Puzzle puzzle = null;
	
	private int dimension = 0;
	private int puzzleViewWidth, puzzleViewHeight;
	private int pieceWidth, pieceHeight;
	
	private static TranslateAnimation upAni = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
	            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f);
	private static TranslateAnimation downAni = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f);
	private static TranslateAnimation leftAni = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, -1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
	private static TranslateAnimation rightAni = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.0f,
            Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f);
	
	static{
		darkBKPaint = new Paint();
		darkBKPaint.setAlpha(100);
		
		infoBoxPaint = new Paint();
		infoBoxPaint.setStyle(Paint.Style.FILL);
		infoBoxPaint.setColor(Color.WHITE);
		infoBoxPaint.setAlpha(100);
		
		textPaint = new Paint();
		textPaint.setTextSize(30);
		textPaint.setTextScaleX(1);
		textPaint.setTextSkewX(0);
		textPaint.setColor(Color.BLACK);
		
	}
	public PuzzleView(Context context) {
		super(context);
		this.context = context;
	}

	public PuzzleView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
	}

	public PuzzleView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
	}

	public void setPuzzle(Puzzle puzzle){
		this.puzzle = puzzle;
		this.dimension = puzzle.getDimension();
		this.onAnimation = false;
		createPieceViews();
		reLayoutPieceViews();
	}
	
	@Override
	public void onLayout(boolean changed ,int left, int top, int right, int bottom){
		super.onLayout(changed, left, top, right, bottom);
		puzzleViewWidth = right - left;
		puzzleViewHeight = bottom - top;		
		reLayoutPieceViews();
	}
	

	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
	}

	@Override  
	protected void dispatchDraw(Canvas canvas) { 
		super.dispatchDraw(canvas);
		if(puzzle != null){
			if(puzzle.isGameOver() ||puzzle.isGameSucceed()){
				Bitmap bk = puzzle.getOriginalBitmap();
				canvas.drawBitmap(bk, new Rect(0, 0, bk.getWidth(), bk.getHeight()), 
						new Rect(0, 0, puzzleViewWidth, puzzleViewHeight), null);
			}
			
			if(puzzle.isPaused() || puzzle.isGameOver()){
				String infoText = null;	
				if(puzzle.isPaused()){
					infoText = context.getResources().getString(R.string.game_puzzle_info_gamepaused);
				}else if(puzzle.isGameOver()){
					infoText = context.getResources().getString(R.string.game_puzzle_info_gameover);
				}
				drawInfoBox(canvas, infoText);
			}
		}
	}
	
	private void createPieceViews(){
		//piecesIV = new ImageView[dimension * dimension];
		for(int i = 0;i < dimension * dimension; i++){
			ImageView view = new ImageView(context);
			view.setId(PIECEVIEW_ID_BASE + i);
			view.setImageBitmap(puzzle.getPuzzlePiece(i).getPicture());
			view.setScaleType(ScaleType.FIT_XY);
			addView(view);
		}
	}
	private void reLayoutPieceViews(){
		if(puzzle == null) return;
		
		pieceWidth = puzzleViewWidth /dimension;
		pieceHeight = puzzleViewHeight /dimension;
		
		int index = 0;
		for(int i = 0;i < dimension; i++){
			for(int j=0; j<dimension; j++){
				View view = findViewById(PIECEVIEW_ID_BASE + index);
				if(view!= null){
					AbsoluteLayout.LayoutParams lp = new AbsoluteLayout.LayoutParams(pieceWidth, pieceHeight, j*pieceWidth, i* pieceHeight);
					view.setLayoutParams(lp);
				}
				index ++;
			}
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event){
		if(isEnabled() && !onAnimation){
			int action = event.getAction() & MotionEvent.ACTION_MASK;
			if(puzzle!=null){
				if(action == MotionEvent.ACTION_DOWN){
					if(puzzle.isRunning()){
						int index = findPositionByXY(event.getX(), event.getY());
						if(index!=-1){
							puzzle.selectPiece(index);
						}
					}
				}else if(action == MotionEvent.ACTION_UP){
					if(puzzle.isRunning()){
						//refreshPieceViews();
						puzzle.gameChecking();
						invalidate();
					}
				}
			}
		}
		return true;
	}

	private void refreshPieceViews(){
		if(puzzle == null) return;
		
		int index = 0;
		for(int i = 0;i < dimension; i++){
			for(int j=0; j<dimension; j++){
				ImageView view = (ImageView)findViewById(PIECEVIEW_ID_BASE + index);
				PuzzlePiece piece = puzzle.getPuzzlePiece(index);
				view.setImageBitmap(piece.getPicture());
				index ++;
			}
		}
	}
	
	private int findPositionByXY(float x, float y) {
		if(x < 0 || x > puzzleViewWidth || y < 0 || y > puzzleViewHeight){
			return -1;
		}
		int row = (int)y / pieceHeight;
		int col = (int)x / pieceWidth;
		
		if(row <0 || row >= dimension || col <0 || col >= dimension){
			return -1;
		}
		
		return row * dimension + col;
	}

	public void movePieces(int from, int to) {
		final int fromPieceIndex = from;
		final int toPieceIndex = to;
		
		Animation ani = null;
		
		if(moveToUpside(from, to)){
			ani = getMoveUpAnimation();
		}else if(moveToDownside(from, to)){
			ani = getMoveDownAnimation();
		}else if(moveToLeftside(from, to)){
			ani = getMoveLeftAnimation();
		}else if(moveToRightside(from, to)){
			ani = getMoveRightAnimation();
		}
		
		ani.setDuration(300);
		ani.setFillAfter(false);
		ani.setRepeatCount(0);
		
		ani.setAnimationListener(new AnimationListener(){

			@Override
			public void onAnimationEnd(Animation animation) {
				
				swapPieceImage(fromPieceIndex, toPieceIndex);
				invalidate();
				onAnimation = false;
				Log.d("Animation", "Animation End");
			}

			@Override
			public void onAnimationRepeat(Animation animation) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onAnimationStart(Animation animation) {
				// TODO Auto-generated method stub
				onAnimation = true;
				Log.d("Animation", "Animation Start");
			}
			
		});
		
		
		
		ImageView fromView = (ImageView)findViewById(PIECEVIEW_ID_BASE + from);
		fromView.setAnimation(ani);
		ani.start();
		
		
		
		
		
	}
	
	private Animation getMoveUpAnimation() {
		return upAni;
	}
	
	private Animation getMoveDownAnimation() {
		return downAni;
	}
	
	private Animation getMoveLeftAnimation() {
		return leftAni;
	}
	
	private Animation getMoveRightAnimation() {
		return rightAni;
	}

	private boolean moveToUpside(int from, int to){
		return from == to + dimension;
	}
	
	private boolean moveToDownside(int from, int to){
		return from == to - dimension;
	}
	
	private boolean moveToLeftside(int from, int to){
		return from == to + 1;
	}
	
	private boolean moveToRightside(int from, int to){
		return from == to -1;
	}
	private void swapPieceImage(int from, int to){
		ImageView fromView = (ImageView)findViewById(PIECEVIEW_ID_BASE + from);
		PuzzlePiece fromPiece = puzzle.getPuzzlePiece(from);
		if(fromPiece!=null && fromView != null){
			fromView.setImageBitmap(fromPiece.getPicture());
		}
		ImageView toView = (ImageView)findViewById(PIECEVIEW_ID_BASE + to);
		PuzzlePiece toPiece = puzzle.getPuzzlePiece(to);
		if(toPiece!=null && toView != null){
			toView.setImageBitmap(toPiece.getPicture());
		}
	}
	
	private void drawInfoBox(Canvas canvas, String infoText){
		if(infoText!= null){
			//first draw dark rect for bk
			canvas.drawRect(new Rect(0,0, puzzleViewWidth,puzzleViewHeight), darkBKPaint);
			
			
			//calculate box and draw box
			FontMetrics fontMetrics = textPaint.getFontMetrics(); 
			float fontHeight = fontMetrics.ascent + fontMetrics.descent;
			float textWidth = textPaint.measureText(infoText);
			
			int infoBoxHeight = (int)fontHeight * 6;
			int infoBoxWidth = (int)(textWidth * 1.5f);
					
			Rect pauseRect = new Rect(puzzleViewWidth/2 - infoBoxWidth/2,
									puzzleViewHeight/2 - infoBoxHeight /2, 
									puzzleViewWidth/2 + infoBoxWidth/2,
									puzzleViewHeight/2 + infoBoxHeight /2);
			canvas.drawRect(pauseRect, infoBoxPaint);
			
			canvas.drawText(infoText, puzzleViewWidth/2 - textWidth/2, puzzleViewHeight/2 - fontHeight/2, textPaint);
			
		}
	}
	
}
