package org.slstudio.baby.ui;



import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.MotionEvent;
import android.widget.ImageView;

public class PictureView extends ImageView{

	public static final float MIN_SCALE = 0.2f;
	public static final float MAX_SCALE = 2f;
	
	private static Paint picturePaint = null;
	
	private Matrix matrix = new Matrix();
	private Matrix savedMatrix = new Matrix();

	private Bitmap picture = null;
	
	private enum MODE {
		NONE, DRAG, ZOOM
	}
	
	private PointF start = new PointF();
	private PointF mid = new PointF();
	private MODE mode = MODE.NONE;
	
	private float beforeLength;
	private int bitmapWidth, bitmapHeight;
	private int pictureViewWidth, pictureViewHeight;
	
	
	static {
		picturePaint = new Paint();
	}

	public PictureView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}
	
	public PictureView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}
	
	public void setCurrentMatrix(Matrix matrix){
		this.matrix = matrix;
	}
	
	public void setPicture(Bitmap bitmap){
		this.picture = bitmap;
		if(bitmap != null){
			this.bitmapWidth = bitmap.getWidth();
			this.bitmapHeight = bitmap.getHeight();
		}
		invalidate();
	}
	

	@Override
	public void onLayout(boolean changed ,int left, int top, int right, int bottom){
		super.onLayout(changed, left, top, right, bottom);
		pictureViewWidth = right - left;
		pictureViewHeight = bottom - top;
		center();
	}

	@Override
	protected void onDraw(Canvas canvas){
		super.onDraw(canvas);
		
		//draw picture
		if(picture !=null){
			canvas.drawBitmap(picture, matrix, picturePaint);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event){
		switch(event.getAction() & MotionEvent.ACTION_MASK){
			case MotionEvent.ACTION_DOWN:
				onTouchDown(event);
				break;
			case MotionEvent.ACTION_POINTER_DOWN:
				onPointerDown(event);
				break;
			case MotionEvent.ACTION_MOVE:
				onTouchMove(event);
				break;
			case MotionEvent.ACTION_POINTER_UP:
				onPointerUp(event);
				break;
			case MotionEvent.ACTION_UP:
				onTouchUp(event);
				break;
		}
		checkScale();
		//center();
		invalidate();
		return true;
	}
	
	public void zoomIn(){
		float p[] = new float[9];
		matrix.getValues(p);
		if ((p[0] * 1.25f) <= MAX_SCALE) {
			matrix.postScale(1.25f, 1.25f);
		}
		center();
		setCurrentMatrix(matrix);
		invalidate();
	}
	
	public void zoomOut(){
		float p[] = new float[9];
		matrix.getValues(p);
		if ((p[0] * 0.8f) >= MIN_SCALE) {
			matrix.postScale(0.8f, 0.8f);
		}
		center();
		setCurrentMatrix(matrix);
		invalidate();
	}
	
	
	private void onTouchDown(MotionEvent event){	
		savedMatrix.set(matrix);
		start.set(event.getX(), event.getY());
		mode = MODE.DRAG;
	}

	private void onTouchUp(MotionEvent event) {
		mode = MODE.NONE;
		center();
	}
	
	private void onTouchMove(MotionEvent event) {
		if (mode == MODE.DRAG) {
			matrix.set(savedMatrix);
			matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
		} else if (mode == MODE.ZOOM) {
			
			float afterLength = getDistance(event);
			if (afterLength > 10f) {
				midPoint(mid, event);
				matrix.set(savedMatrix);
				float scale = afterLength / beforeLength;
				matrix.postScale(scale, scale, mid.x, mid.y);
			}
		}
	}
	
	private void onPointerUp(MotionEvent event) {
		mode = MODE.NONE;
	}

	private void onPointerDown(MotionEvent event) {
		if (event.getPointerCount() == 2) {
			beforeLength = getDistance(event);
			if (beforeLength > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = MODE.ZOOM;
			}
		}
	}
	
	private float getDistance(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);

		return FloatMath.sqrt(x * x + y * y);
	}
	
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);

		point.set(x / 2, y / 2);
	}
	
	protected void checkScale() {
		float p[] = new float[9];
		matrix.getValues(p);
		if (mode == MODE.ZOOM) {
			if (p[0] < MIN_SCALE) {
				matrix.setScale(MIN_SCALE, MIN_SCALE);
			}
			if (p[0] > MAX_SCALE) {
				matrix.set(savedMatrix);
			}
		}
	}

	protected void center() {
		center(true, true);
	}

	private void center(boolean horizontal, boolean vertical) {
		Matrix m = new Matrix();
		m.set(matrix);
		RectF rect = new RectF(0, 0, bitmapWidth, bitmapHeight);
		m.mapRect(rect);
		float height = rect.height();
		float width = rect.width();

		float deltaX = 0, deltaY = 0;

		if (vertical) {
			if (height < pictureViewHeight) {
				deltaY = (pictureViewHeight - height) / 2 - rect.top;
			} else if (rect.top > 0) {
				deltaY = -rect.top;
			} else if (rect.bottom < pictureViewHeight) {
				deltaY = pictureViewHeight - rect.bottom;
			}
		}

		if (horizontal) {
			if (width < pictureViewWidth) {
				deltaX = (pictureViewWidth - width) / 2 - rect.left;
			} else if (rect.left > 0) {
				deltaX = -rect.left;
			} else if (rect.right < pictureViewWidth) {
				deltaX = pictureViewWidth - rect.right;
			}
		}
		matrix.postTranslate(deltaX, deltaY);

	}
}
