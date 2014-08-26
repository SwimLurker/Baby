package org.slstudio.baby.game.lianliankan.ui;

import java.util.List;
import java.util.Random;

import org.slstudio.baby.R;
import org.slstudio.baby.data.PhotoManager;
import org.slstudio.baby.game.lianliankan.Block;
import org.slstudio.baby.game.lianliankan.LianLianKan;
import org.slstudio.baby.game.lianliankan.LianLianKanMap;
import org.slstudio.baby.game.lianliankan.Path;
import org.slstudio.baby.util.BitmapUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ImageView;

public class LianLianKanMapView extends ImageView {
	
	public static final String NAMESPACE_ANDROID = "http://schemas.android.com/apk/res/android";
	public static final String NAMESPACE_SWIMLURKER = "http://schemas.slstudio.org/apk/res/android";
	
	private int mapViewWidth, mapViewHeight;
	
	private Paint backgroundPaint, gamePaint,blockPaint, selectionPaint, pathPaint, darkBKPaint, infoBoxPaint, textPaint, hintPathPaint, hintBlockPaint;
	
	private int leftMargin, rightMargin, topMargin, bottomMargin;
	
	private int blockWidth, blockHeight;
	
	private LianLianKan game = null;
	
	private Context context = null;
	
	private Bitmap background = null;
	
	private Random random = new Random();
	
	public LianLianKanMapView(Context context) {
		super(context);
		this.context = context;
		init(null);
	}
	
	public LianLianKanMapView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		init(attrs);
	}
	
	public void setGame(LianLianKan game){
		this.game = game;
		blockWidth = (mapViewWidth - leftMargin - rightMargin)/(game.getMapColumnNumber()-2);
		blockHeight = (mapViewHeight - topMargin - bottomMargin)/(game.getMapRowNumber()-2);
		background = getBackgroundImage();
		
	}
	
	private void init(AttributeSet attrs) {
		if(attrs != null){
			topMargin = attrs.getAttributeIntValue(NAMESPACE_SWIMLURKER, "marginTop", 50);
			leftMargin = attrs.getAttributeIntValue(NAMESPACE_SWIMLURKER, "marginLeft", 50);
			bottomMargin = attrs.getAttributeIntValue(NAMESPACE_SWIMLURKER, "marginBottom", 50);
			rightMargin = attrs.getAttributeIntValue(NAMESPACE_SWIMLURKER, "marginRight", 50);
			
			blockWidth = attrs.getAttributeIntValue(NAMESPACE_SWIMLURKER, "block_width", 80);
			blockHeight = attrs.getAttributeIntValue(NAMESPACE_SWIMLURKER, "block_height", 120);
			
		}else{
			topMargin = 10;
			leftMargin = 50;
			bottomMargin = 50;
			rightMargin = 50;
			
			blockWidth = 80;
			blockHeight = 120;
		}
		
		
		backgroundPaint = new Paint();
		backgroundPaint.setAlpha(100);
	
		gamePaint = new Paint();
		
		blockPaint = new Paint();
		blockPaint.setStyle(Paint.Style.STROKE);	
		blockPaint.setStrokeWidth(2);
		blockPaint.setColor(Color.BLACK);
		
		selectionPaint = new Paint();
		selectionPaint.setStyle(Paint.Style.STROKE);
		selectionPaint.setStrokeWidth(5);
		selectionPaint.setColor(Color.RED);
		
		pathPaint = new Paint();
		pathPaint.setStyle(Paint.Style.STROKE);
		pathPaint.setStrokeWidth(5);
		pathPaint.setColor(Color.RED);
		pathPaint.setAlpha(100);
		
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
		
		int hintColor = Color.parseColor("#fe01d7");
		hintPathPaint = new Paint();
		hintPathPaint.setStyle(Paint.Style.STROKE);
		hintPathPaint.setStrokeWidth(5);
		hintPathPaint.setColor(hintColor);
		hintPathPaint.setAlpha(100);
		
		
		hintBlockPaint = new Paint();
		hintBlockPaint.setStyle(Paint.Style.STROKE);
		hintBlockPaint.setStrokeWidth(5);
		hintBlockPaint.setColor(hintColor);
		
	}

	@Override
	public void onLayout(boolean changed ,int left, int top, int right, int bottom){
		super.onLayout(changed, left, top, right, bottom);
		mapViewWidth = right - left;
		mapViewHeight = bottom - top;
		
		if(game!=null){
			blockWidth = (mapViewWidth - leftMargin - rightMargin)/(game.getMapColumnNumber()-2);
			blockHeight = (mapViewHeight - topMargin - bottomMargin)/(game.getMapRowNumber()-2);
			background = getBackgroundImage();
		}
	}

	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		if(game != null){
			Bitmap bitmap = getGameMapBitmap();
			if(bitmap !=null){
				canvas.drawBitmap(bitmap, new Matrix(), gamePaint);
			} 
		}
		
	}
	@Override
	public boolean onTouchEvent(MotionEvent event){
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		if(game!=null){
			if(action == MotionEvent.ACTION_DOWN){
				if(game.isRunning()){
					Point pos = findPositionByXY(event.getX(), event.getY());
					if(pos!=null){
						game.selectBlock(pos.x, pos.y);
						
					}
				}
			}else if(action == MotionEvent.ACTION_UP){
				if(game.isRunning()){
					game.handleRemoveBlocks();
				}
			}
		}
		
		return true;
	}
	

	private Point findPositionByXY(float x, float y) {
		if(x < leftMargin || x > (mapViewWidth -rightMargin) || y<topMargin ||y > (mapViewHeight - bottomMargin)){
			return null;
		}
		int col = (((int)x - leftMargin)/blockWidth) +1;
		int row = (((int)y - topMargin)/blockHeight) +1;
		
		return new Point(row, col);
		
	}

	private Bitmap getGameMapBitmap() {
		Bitmap gameBmp = null;
		
		LianLianKanMap map = game.getLianLianKanMap();
		
		int rows = game.getMapRowNumber();
		int cols = game.getMapColumnNumber();
		
		gameBmp = Bitmap.createBitmap(mapViewWidth, mapViewHeight, Bitmap.Config.RGB_565);
		
		Canvas canvas = new Canvas(gameBmp);
		
		if(background!= null){
			if(game.isGameSucceed()){
				backgroundPaint.setAlpha(255);
			}else{
				backgroundPaint.setAlpha(100);
			}
			canvas.drawBitmap(background, 0, 0, backgroundPaint);
		}
		
		
		
		Block selectedBlock = game.getSelectedBlock();
		
		for(int i=1;i<rows-1; i++){
			for(int j=1;j<cols-1; j++){
				Block b = map.getBlock(i, j);
				Rect rect = new Rect(leftMargin + (j-1)*blockWidth, 
						topMargin + (i-1)*blockHeight, 
						leftMargin + j*blockWidth,
						topMargin + i*blockHeight);
				
				if(b!=null && !b.isEmpty()){
				
					Bitmap blockImg = b.getBitmap();
					
					if(blockImg!= null){
						
						float scalex = (float)blockWidth/(float)blockImg.getWidth();
						float scaley = (float)blockHeight/(float)blockImg.getHeight();
						float dx = leftMargin + (j-1)*blockWidth;
						float dy = topMargin + (i-1)*blockHeight;
						
						Matrix m = new Matrix();
						m.postScale(scalex, scaley);
						m.postTranslate(dx, dy);
						if(selectedBlock != null && selectedBlock.sameAs(b)){
							blockPaint.setStyle(Paint.Style.STROKE);
							blockPaint.setAlpha(255);
							canvas.drawBitmap(blockImg, m, blockPaint);
							blockPaint.setStyle(Paint.Style.FILL);	
							blockPaint.setColor(Color.WHITE);
							blockPaint.setAlpha(50);
							canvas.drawRect(rect, blockPaint);
							
						}else{
							blockPaint.setAlpha(255);
							blockPaint.setStyle(Paint.Style.STROKE);	
							blockPaint.setStrokeWidth(2);
							blockPaint.setColor(Color.BLACK);
							canvas.drawBitmap(blockImg, m, blockPaint);
						}
						
						canvas.drawRect(rect,blockPaint);
					}
				}
				
			}
		}
		
		if(selectedBlock!= null){
			drawBlockRect(selectedBlock, canvas, selectionPaint);
		}
		
		if(game.getCurrentPath()!=null){
			drawPath(game.getCurrentPath(), canvas, pathPaint);
		}
		
		Path hintPath = game.getHintPath();
		if(hintPath!=null && game.isShowHint()){
			drawBlockRect(hintPath.getStartBlock(), canvas, hintBlockPaint);
			drawBlockRect(hintPath.getEndBlock(), canvas, hintBlockPaint);
			drawPath(hintPath, canvas, hintPathPaint);
		}
		
		if(game.isPaused() || game.isGameOver()){
			Bitmap newGameBmp = Bitmap.createBitmap(mapViewWidth, mapViewHeight, Bitmap.Config.RGB_565);
			Canvas newCanvas = new Canvas(newGameBmp);
			newCanvas.drawBitmap(gameBmp, new Matrix(), darkBKPaint);
			
			String infoText = null;
			if(game.isPaused()){
				infoText = context.getResources().getString(R.string.game_info_gamepaused);
			}else if(game.isGameOver()){
				infoText = context.getResources().getString(R.string.game_info_gameover);
			}
			drawInfoBox(infoText, newCanvas, infoBoxPaint, textPaint);
			
			return newGameBmp;
		}
		return gameBmp;
	}

	private Bitmap getBackgroundImage() {
		List<String> photos = PhotoManager.getInstance().getAllPhotos();
		String bkFilename = photos.get(random.nextInt(photos.size()));
		return BitmapUtil.getImageThumbnail(bkFilename, mapViewWidth, mapViewHeight);		
	}

	private void drawBlockRect(Block block, Canvas canvas, Paint paint) {
		int row = block.getRow();
		int column = block.getColumn();
		
		Rect rect = new Rect(leftMargin + (column-1)*blockWidth - 1, 
				topMargin + (row-1)*blockHeight - 1, 
				leftMargin + column*blockWidth + 1,
				topMargin + row*blockHeight + 1);
		
		canvas.drawRect(rect, paint);
		
	}

	private void drawPath(Path path, Canvas canvas, Paint paint) {
		LianLianKanMap map = game.getLianLianKanMap();
		List<Block> pathBlocks = path.getBlockList();
		for(int i=0; i<pathBlocks.size()-1; i++){
			Block startBlock = pathBlocks.get(i);
			Block endBlock = pathBlocks.get(i+1);
			
			int startX = leftMargin + (startBlock.getColumn()-1) * blockWidth + blockWidth/2;
			int startY = topMargin + (startBlock.getRow()-1) * blockHeight + blockHeight/2;
			int endX = leftMargin + (endBlock.getColumn()-1) * blockWidth + blockWidth/2;
			int endY = topMargin + (endBlock.getRow()-1) * blockHeight + blockHeight/2;
			
			if(startBlock.getRow() == 0){
				startY = topMargin/2;
			}
			if(startBlock.getColumn() == 0){
				startX = leftMargin/2;
			}
			if(endBlock.getRow() == 0){
				endY = topMargin/2;
			}
			if(endBlock.getColumn() == 0){
				endX = leftMargin/2;
			}
			
			if(startBlock.getRow() == map.getRows()-1){
				startY = mapViewHeight - bottomMargin/2;
			}
			if(startBlock.getColumn() == map.getColumns()-1){
				startX = mapViewWidth - rightMargin/2;
			}
			if(endBlock.getRow() == map.getRows()-1){
				endY = mapViewHeight - bottomMargin/2;
			}
			if(endBlock.getColumn() == map.getColumns()-1){
				endX = mapViewWidth - rightMargin/2;
			}
			
			if(i == 0){
				if(endBlock.upSide(startBlock)){
					startY = topMargin + (startBlock.getRow()-1) * blockHeight;
				}else if(endBlock.downSide(startBlock)){
					startY = topMargin + startBlock.getRow() * blockHeight;
				}else if(endBlock.leftSide(startBlock)){
					startX = leftMargin + (startBlock.getColumn()-1) * blockWidth;
				}else if(endBlock.rightSide(startBlock)){
					startX = leftMargin + startBlock.getColumn() * blockWidth;
				}
				float oldWidth = paint.getStrokeWidth();
				paint.setStrokeWidth(oldWidth * 2);
				canvas.drawPoint(startX, startY, paint);
				paint.setStrokeWidth(oldWidth);
			}
			
			if(i == pathBlocks.size()-2){
				if(endBlock.upSide(startBlock)){
					endY = topMargin + endBlock.getRow() * blockHeight;
				}else if(endBlock.downSide(startBlock)){
					endY = topMargin + (endBlock.getRow() - 1) * blockHeight;
				}else if(endBlock.leftSide(startBlock)){
					endX = leftMargin + endBlock.getColumn() * blockWidth;
				}else if(endBlock.rightSide(startBlock)){
					endX = leftMargin + (endBlock.getColumn()-1) * blockWidth;
				}
				
				float oldWidth = paint.getStrokeWidth();
				paint.setStrokeWidth(oldWidth * 2);
				canvas.drawPoint(endX, endY, paint);
				paint.setStrokeWidth(oldWidth);
			}
			canvas.drawLine(startX, startY, endX, endY, paint);
			
			
		}
		
		
	}

	private void drawInfoBox(String infoText, Canvas canvas, Paint boxPaint, Paint textPaint){
		if(infoText!= null){
			FontMetrics fontMetrics = textPaint.getFontMetrics(); 
			float fontHeight = fontMetrics.ascent + fontMetrics.descent;
			float textWidth = textPaint.measureText(infoText);
			
			int infoBoxHeight = (int)fontHeight * 6;
			int infoBoxWidth = (int)(textWidth * 1.5f);
					
			Rect pauseRect = new Rect(mapViewWidth/2 - infoBoxWidth/2,
									mapViewHeight/2 - infoBoxHeight /2, 
									mapViewWidth/2 + infoBoxWidth/2,
									mapViewHeight/2 + infoBoxHeight /2);
			canvas.drawRect(pauseRect, boxPaint);
			
			canvas.drawText(infoText, mapViewWidth/2 - textWidth/2, mapViewHeight/2 - fontHeight/2, textPaint);
		}
	}
	

}
