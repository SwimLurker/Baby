package org.slstudio.baby.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;

public class PictureThumbnailDecorator implements IImageDecorator{
	
	public static final int FRAME_MARGIN = 5;
	public static final int FRAMEBORDER_WIDTH = 1;
	
	private static Paint picPaint = null;
	private static Paint framePaint = null;
	private static Paint frameBorderPaint = null;
	
	static{
		picPaint = new Paint();
		picPaint.setShadowLayer(FRAME_MARGIN, 1, 1, Color.argb(100, 255, 255, 255));
		
		framePaint = new Paint();
		framePaint.setStyle(Paint.Style.FILL);
		framePaint.setColor(Color.WHITE);
		framePaint.setShadowLayer(2, 3, 3, Color.argb(100, 89, 89, 89));
		
		
		frameBorderPaint = new Paint();
		frameBorderPaint.setStyle(Paint.Style.STROKE);
		frameBorderPaint.setStrokeWidth(FRAMEBORDER_WIDTH);
		frameBorderPaint.setColor(Color.GRAY);
		
	}
	
	public Bitmap decorateImage(Bitmap originalImage){
		Bitmap result = Bitmap.createBitmap(originalImage.getWidth() + FRAME_MARGIN * 2 + 3, originalImage.getHeight() + FRAME_MARGIN * 2 + 3, Bitmap.Config.RGB_565);
		Canvas canvas = new Canvas(result);
		canvas.drawColor(Color.rgb(255,232, 144));
		
		Rect frameRect = new Rect(0, 0, originalImage.getWidth() + FRAME_MARGIN * 2 - FRAMEBORDER_WIDTH, originalImage.getHeight() + FRAME_MARGIN * 2 - FRAMEBORDER_WIDTH);
		canvas.drawRect(frameRect, framePaint);
		
		canvas.drawBitmap(originalImage, FRAME_MARGIN, FRAME_MARGIN, picPaint);
		//canvas.drawBitmap(originalImage, 2, 2, picPaint);
		Rect frameBorderRect = new Rect(0, 0, originalImage.getWidth() + FRAME_MARGIN * 2 - FRAMEBORDER_WIDTH, originalImage.getHeight() + FRAME_MARGIN * 2 - FRAMEBORDER_WIDTH);
		canvas.drawRect(frameBorderRect, frameBorderPaint);
		
		return result;
	}
}
