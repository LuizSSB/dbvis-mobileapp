package br.unesp.amoraes.dbvis.components;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.ImageView;
import br.unesp.amoraes.dbvis.ExplorerActivity;
import br.unesp.amoraes.dbvis.R;
import br.unesp.amoraes.dbvis.exception.NotConnectedException;
import br.unesp.amoraes.dbvis.logic.ServerConnection;
import br.unesp.amoraes.dbvis.logic.ServerConnectionFactory;
import br.unesp.amoraes.dbvis.logic.Visualization;
import br.unesp.amoraes.dbvis.userdata.UserText;

public class ZoomImageView extends ImageView {
	
	private int userMode;
	
	private static final String TAG = "DBVis::ZoomImageView";
	Matrix matrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF last = new PointF();
	PointF start = new PointF();
	float minScale = 1f;
	float maxScale = 3f;
	float[] m;

	float redundantXSpace, redundantYSpace;

	float width, height;
	static final int CLICK = 5;
	float saveScale = 1f;
	float right, bottom, origWidth, origHeight, bmWidth, bmHeight;
	Float selRectX, selRectY, selRectWidth, selRectHeight;
	
	//saves the current global x,y,width and height
	

	ScaleGestureDetector mScaleDetector;

	Context context;
	
	boolean firstRun = true;

	private float globalX;

	private float globalY;

	private float globalWidth;

	private float globalHeight;
	
	public float getGlobalX() {
		return globalX;
	}
	public float getGlobalY() {
		return globalY;
	}
	public float getGlobalWidth() {
		return globalWidth;
	}
	public float getGlobalHeight() {
		return globalHeight;
	}

	public ZoomImageView(Context context) {
		super(context);
		sharedConstructing(context);
	}

	public ZoomImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		sharedConstructing(context);
	}

	private void sharedConstructing(Context context) {
		super.setClickable(true);
		this.context = context;
		mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
		matrix.setTranslate(1f, 1f);
		m = new float[9];
		setImageMatrix(matrix);
		setScaleType(ScaleType.MATRIX);
		setCurrentUserMode(ExplorerActivity.MODE_NAVIGATION);
		
	}

	
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		float[] values = new float[9];
		matrix.getValues(values);
		globalX = Math.abs(values[Matrix.MTRANS_X]/values[Matrix.MSCALE_X]);
		globalY = Math.abs(values[Matrix.MTRANS_Y]/values[Matrix.MSCALE_Y]);
		globalWidth = ((width/values[Matrix.MSCALE_X])/ExplorerActivity.TOTAL_WIDTH)*ExplorerActivity.VIEW_WIDTH;
		globalHeight = height/values[Matrix.MSCALE_Y];		
		
		if(selRectX != null && selRectY != null){
			Paint paint = new Paint();
			paint.setColor(Color.CYAN);
			paint.setStyle(Paint.Style.STROKE);
			paint.setStrokeWidth(2.0f);
			if(selRectWidth > 10f && selRectHeight > 10f)
				canvas.drawRect(selRectX, selRectY, selRectX+selRectWidth, selRectY+selRectHeight, paint);
		}
		List<UserText> allTexts = new ArrayList<UserText>();
		if((Visualization.getInstance().getUserContents() != null
				&& Visualization.getInstance().getUserContents().getTexts() != null
				&& Visualization.getInstance().getUserContents().getTexts().size() > 0)){
			for(UserText ut : Visualization.getInstance().getUserContents().getTexts().values()){
				allTexts.add(ut);
			}
		}
		if((Visualization.getInstance().getPreferences().isShowOtherUsersTexts()
				&& Visualization.getInstance().getOtherUsersContents() != null
				&& Visualization.getInstance().getOtherUsersContents().getTexts() != null
				&& Visualization.getInstance().getOtherUsersContents().getTexts().size() > 0)){
			for(UserText ut : Visualization.getInstance().getOtherUsersContents().getTexts().values()){
				allTexts.add(ut);
			}
		}
		for(UserText ut : allTexts){
			
			
			Paint paintUser = new Paint();
			paintUser.setColor(Color.rgb(255, 255, 127));
			paintUser.setStyle(Paint.Style.FILL_AND_STROKE);
			
			Paint paintOther = new Paint();
			paintOther.setColor(Color.rgb(255, 212, 249));
			paintOther.setStyle(Paint.Style.FILL_AND_STROKE);
			
			
			Paint paint2 = new Paint();
			paint2.setColor(Color.BLACK);
			paint2.setStyle(Paint.Style.FILL_AND_STROKE);
			paint2.setTextSize(14*values[Matrix.MSCALE_X]);
							
			if(ut.getX() >= globalX && ut.getX() <= globalX+globalWidth
					&& ut.getY() >= globalY && ut.getY() <= globalY+globalHeight){
				Log.d(TAG, "Showing text "+ut.getText());
				int screenX = new Float((ut.getX()-globalX)*values[Matrix.MSCALE_X]).intValue();	
				int screenY = new Float((ut.getY()-globalY)*values[Matrix.MSCALE_Y]).intValue();
				FontMetrics fm = new FontMetrics();
				paint2.getFontMetrics(fm);
				paint2.setTextAlign(Paint.Align.LEFT);
				int margin = 5*new Float(values[Matrix.MSCALE_X]).intValue();
				Resources res = getResources();
				String timeFormat = res.getString(R.string.format_time);
				SimpleDateFormat timeFormatter = new SimpleDateFormat(timeFormat);
				String text = ut.getText();
				String text2 = "("+ut.getDeviceName()+", "+timeFormatter.format(ut.getDate())+")";
				RectF rect = new RectF(new Float(screenX-margin), 
						screenY-(paint2.getTextSize())-(margin/2), 
						screenX+paint2.measureText((text.length()>text2.length())?text:text2)+margin,
						screenY+paint2.getTextSize()+margin);
				canvas.drawRect(rect, (Visualization.getInstance().getServerConnection().getDeviceName().equals(ut.getDeviceName()))?paintUser:paintOther);
				ut.setLastRect(rect);
				canvas.drawText(text, screenX, screenY, paint2);
				canvas.drawText(text2, screenX, screenY+paint2.getTextSize(), paint2);
				
			}
		}
	}

	@Override
	public void setImageBitmap(Bitmap bm) {
		super.setImageBitmap(bm);
		if (bm != null) {
			bmWidth = bm.getWidth();
			bmHeight = bm.getHeight();
		}
	}

	public void setMaxZoom(float x) {
		maxScale = x;
	}

	private class ScaleListener extends
			ScaleGestureDetector.SimpleOnScaleGestureListener {
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			mode = ZOOM;
			return true;
		}

		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			float mScaleFactor = detector.getScaleFactor();
			float origScale = saveScale;
			saveScale *= mScaleFactor;
			if (saveScale > maxScale) {
				saveScale = maxScale;
				mScaleFactor = maxScale / origScale;
			} else if (saveScale < minScale) {
				saveScale = minScale;
				mScaleFactor = minScale / origScale;
			}
			right = width * saveScale - width
					- (2 * redundantXSpace * saveScale);
			bottom = height * saveScale - height
					- (2 * redundantYSpace * saveScale);
			if (origWidth * saveScale <= width
					|| origHeight * saveScale <= height) {
				matrix.postScale(mScaleFactor, mScaleFactor, width / 2,
						height / 2);
				if (mScaleFactor < 1) {
					matrix.getValues(m);
					float x = m[Matrix.MTRANS_X];
					float y = m[Matrix.MTRANS_Y];
					if (mScaleFactor < 1) {
						if (Math.round(origWidth * saveScale) < width) {
							if (y < -bottom)
								matrix.postTranslate(0, -(y + bottom));
							else if (y > 0)
								matrix.postTranslate(0, -y);
						} else {
							if (x < -right)
								matrix.postTranslate(-(x + right), 0);
							else if (x > 0)
								matrix.postTranslate(-x, 0);
						}
					}
				}
			} else {
				matrix.postScale(mScaleFactor, mScaleFactor,
						detector.getFocusX(), detector.getFocusY());
				matrix.getValues(m);
				float x = m[Matrix.MTRANS_X];
				float y = m[Matrix.MTRANS_Y];
				if (mScaleFactor < 1) {
					if (x < -right)
						matrix.postTranslate(-(x + right), 0);
					else if (x > 0)
						matrix.postTranslate(-x, 0);
					if (y < -bottom)
						matrix.postTranslate(0, -(y + bottom));
					else if (y > 0)
						matrix.postTranslate(0, -y);
				}
			}
			return true;

		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		if(userMode != ExplorerActivity.MODE_NAVIGATION){
			setImageMatrix(matrix);
			return;
		}
		if(firstRun == false) return;
		firstRun = false;
		width = (MeasureSpec.getSize(widthMeasureSpec)/ExplorerActivity.TOTAL_WIDTH)*ExplorerActivity.VIEW_WIDTH;
		height = MeasureSpec.getSize(heightMeasureSpec);
		// Fit to screen.
		float scale;
		float scaleX = (float) width / (float) bmWidth;
		float scaleY = (float) height / (float) bmHeight;
		scale = Math.max(scaleX, scaleY);
		matrix.setScale(scale, scale);
		setImageMatrix(matrix);
		saveScale = 1f;

		// Center the image
		redundantYSpace = (float) height - (scale * (float) bmHeight);
		redundantXSpace = (float) width - (scale * (float) bmWidth);
		redundantYSpace /= (float) 2;
		redundantXSpace /= (float) 2;
		matrix.postTranslate(redundantXSpace, redundantYSpace);

		origWidth = width - 2 * redundantXSpace;
		origHeight = height - 2 * redundantYSpace;
		right = width * saveScale - width - (2 * redundantXSpace * saveScale);
		bottom = height * saveScale - height
				- (2 * redundantYSpace * saveScale);
		setImageMatrix(matrix);
				
	}

	public void setCurrentUserMode(int userMode) {
		this.userMode = userMode;
		
		if(userMode == ExplorerActivity.MODE_NAVIGATION){
			Log.d(TAG,"Entering Navigation mode");
			selRectX = selRectY = selRectWidth = selRectHeight = null;
			invalidate();
			setOnTouchListener(navigationListener);
		}else if(userMode == ExplorerActivity.MODE_INFORMATION){
			Log.d(TAG,"Entering Information mode");
			selRectX = selRectY = selRectWidth = selRectHeight = null;
			invalidate();
			setOnTouchListener(informationListener);
		}else if(userMode == ExplorerActivity.MODE_SELECTION){
			Log.d(TAG,"Entering Selection mode");
			selRectX = selRectY = selRectWidth = selRectHeight = null;
			invalidate();
			setOnTouchListener(selectionListener);
		}else if(userMode == ExplorerActivity.MODE_TEXT){
			Log.d(TAG, "Entering Text mode");
			selRectX = selRectY = selRectWidth = selRectHeight = null;
			invalidate();
			setOnTouchListener(textListener);
		}
	}
	
	private OnTouchListener textListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				last.set(event.getX(), event.getY());
				start.set(last);
				mode = CLICK;
				break;

			case MotionEvent.ACTION_UP:
				mode = NONE;
				int xDiff = (int) Math.abs(event.getX() - start.x);
				int yDiff = (int) Math.abs(event.getY() - start.y);
				if (xDiff < CLICK && yDiff < CLICK){
					float[] values = new float[9];
					matrix.getValues(values);
					float visX = (Math.abs(values[Matrix.MTRANS_X])+event.getX())/values[Matrix.MSCALE_X];
					float visY = (Math.abs(values[Matrix.MTRANS_Y])+event.getY())/values[Matrix.MSCALE_Y];
					explorerActivity.showTextPopup(new Float(event.getX()).intValue(),new Float(event.getY()).intValue(),new Float(visX).intValue(),new Float(visY).intValue());
				}					
				break;
			}
			
			setImageMatrix(matrix);
			invalidate();
			return true;
		}
	};
	
	private OnTouchListener informationListener = new OnTouchListener() {
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				last.set(event.getX(), event.getY());
				start.set(last);
				mode = CLICK;
				break;

			case MotionEvent.ACTION_UP:
				mode = NONE;
				int xDiff = (int) Math.abs(event.getX() - start.x);
				int yDiff = (int) Math.abs(event.getY() - start.y);
				if (xDiff < CLICK && yDiff < CLICK){
					float[] values = new float[9];
					matrix.getValues(values);
					float nodeX = (Math.abs(values[Matrix.MTRANS_X])+event.getX())/values[Matrix.MSCALE_X];
					float nodeY = (Math.abs(values[Matrix.MTRANS_Y])+event.getY())/values[Matrix.MSCALE_Y];
					explorerActivity.showInfoPopup(new Float(event.getX()).intValue(),new Float(event.getY()).intValue(),new Float(nodeX).intValue(),new Float(nodeY).intValue());
				}					
				break;
			}
			
			setImageMatrix(matrix);
			invalidate();
			return true;
		}
	}; 
	private OnTouchListener navigationListener = new OnTouchListener() {

		public boolean onTouch(View v, MotionEvent event) {
			mScaleDetector.onTouchEvent(event);

			matrix.getValues(m);
			float x = m[Matrix.MTRANS_X];
			float y = m[Matrix.MTRANS_Y];
			PointF curr = new PointF(event.getX(), event.getY());

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				last.set(event.getX(), event.getY());
				start.set(last);
				mode = DRAG;
				break;
			case MotionEvent.ACTION_MOVE:
				if (mode == DRAG) {
					float deltaX = curr.x - last.x;
					float deltaY = curr.y - last.y;
					float scaleWidth = Math.round(origWidth * saveScale);
					float scaleHeight = Math.round(origHeight * saveScale);
					if (scaleWidth < width) {
						deltaX = 0;
						if (y + deltaY > 0)
							deltaY = -y;
						else if (y + deltaY < -bottom)
							deltaY = -(y + bottom);
					} else if (scaleHeight < height) {
						deltaY = 0;
						if (x + deltaX > 0)
							deltaX = -x;
						else if (x + deltaX < -right)
							deltaX = -(x + right);
					} else {
						if (x + deltaX > 0)
							deltaX = -x;
						else if (x + deltaX < -right)
							deltaX = -(x + right);

						if (y + deltaY > 0)
							deltaY = -y;
						else if (y + deltaY < -bottom)
							deltaY = -(y + bottom);
					}
					matrix.postTranslate(deltaX, deltaY);
					last.set(curr.x, curr.y);
				}
				break;

			case MotionEvent.ACTION_UP:
				mode = NONE;
				int xDiff = (int) Math.abs(curr.x - start.x);
				int yDiff = (int) Math.abs(curr.y - start.y);
				if (xDiff < CLICK && yDiff < CLICK)
					performClick();
				break;

			case MotionEvent.ACTION_POINTER_UP:
				mode = NONE;
				break;
			}
			setImageMatrix(matrix);
			
			float[] values = new float[9];
			matrix.getValues(values);
			float _globalX = values[Matrix.MTRANS_X]/values[Matrix.MSCALE_X];
			float _globalY = values[Matrix.MTRANS_Y]/values[Matrix.MSCALE_Y];
			float _width = ((width/values[Matrix.MSCALE_X])/ExplorerActivity.TOTAL_WIDTH)*ExplorerActivity.VIEW_WIDTH;
			float _height = height/values[Matrix.MSCALE_Y];
			
			Log.d(TAG, "x="+_globalX+",y="+_globalY+",scale="+saveScale+",w="+_width+",h="+_height);
			try {
				Visualization.getInstance().getServerConnection().updatePosition(
						new Float(Math.abs(_globalX)).intValue()
						,new Float(Math.abs(_globalY)).intValue()
						,new Float(_width).intValue()
						,new Float(_height).intValue());
			} catch (NotConnectedException e) {
				Log.e(TAG, "Error updating position", e);
			}
			return true; // indicate event was handled
		}

	};
	
	private OnTouchListener selectionListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {

			switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN:
				last.set(event.getX(), event.getY());
				start.set(last);
				mode = CLICK;
				selRectX = selRectY = selRectWidth = selRectHeight = null;
				break;
				
			case MotionEvent.ACTION_MOVE:
				ZoomImageView view = (ZoomImageView)v;
				selRectX = start.x;
				selRectY = start.y;
				selRectWidth = event.getX() - start.x;
				selRectHeight = event.getY() - start.y;
				view.invalidate();
				break;
				

			case MotionEvent.ACTION_UP:
				mode = NONE;
				int xDiff = (int) Math.abs(event.getX() - start.x);
				int yDiff = (int) Math.abs(event.getY() - start.y);
				if (xDiff < CLICK && yDiff < CLICK){
					float[] values = new float[9];
					matrix.getValues(values);
					float x = (Math.abs(values[Matrix.MTRANS_X])+event.getX())/values[Matrix.MSCALE_X];
					float y = (Math.abs(values[Matrix.MTRANS_Y])+event.getY())/values[Matrix.MSCALE_Y];
					try {
						Visualization.getInstance().getServerConnection().selectPoint(new Float(x).intValue(), new Float(y).intValue());
					} catch (NotConnectedException e) {
						Log.e(TAG, e.getMessage());
					}
				}else{
					float[] values = new float[9];
					matrix.getValues(values);
					float x = (Math.abs(values[Matrix.MTRANS_X])+event.getX())/values[Matrix.MSCALE_X];
					float y = (Math.abs(values[Matrix.MTRANS_Y])+event.getY())/values[Matrix.MSCALE_Y];
					float startX = (Math.abs(values[Matrix.MTRANS_X])+start.x)/values[Matrix.MSCALE_X];
					float startY = (Math.abs(values[Matrix.MTRANS_Y])+start.y)/values[Matrix.MSCALE_Y];
					try {
						Visualization.getInstance().getServerConnection().selectArea(new Float(startX).intValue(), new Float(startY).intValue(),new Float(x-startX).intValue(),new Float(y-startY).intValue());
					} catch (NotConnectedException e) {
						Log.e(TAG, e.getMessage());
					}
				}
				break;
			}
			
			setImageMatrix(matrix);
			invalidate();
			return true;
		}
		
	};

	private ExplorerActivity explorerActivity;

	public void setExplorerActivity(ExplorerActivity explorerActivity) {
		this.explorerActivity = explorerActivity;
		
	}

	public int getCurrentMode() {
		return userMode;
	}
}
