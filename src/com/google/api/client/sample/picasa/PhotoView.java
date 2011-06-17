package com.google.api.client.sample.picasa;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.os.Handler;
import android.util.FloatMath;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;

import com.google.api.client.sample.picasa.model.PhotoEntry;

public class PhotoView extends Activity {
	private static final String TAG = "PhotoView";

	private ImageView imageView;
	private Integer position = 0;

	private GestureDetector gestureDetector;
	View.OnTouchListener gestureListener;

	final Handler getPhoto = new Handler();

	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	Toast msg = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "on create called " + this);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.photo);
		imageView = (ImageView) findViewById(R.id.photo);
		Intent intent = getIntent();
		position = intent.getIntExtra("position", 0);

		msg = Toast.makeText(this, "Loading...", Toast.LENGTH_LONG);
		msg.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
		msg.show();
		asyncLoad(position);

		gestureDetector = new GestureDetector(new MyGestureDetector());
		gestureListener = new View.OnTouchListener() {
			public boolean onTouch(View paramView, MotionEvent event) {
				if (imageView.getScaleType() != ScaleType.MATRIX) {
					int width = imageView.getWidth();
					int height = imageView.getHeight();
					matrix = imageView.getImageMatrix();
					imageView.setScaleType(ScaleType.MATRIX);
					Log.d(TAG, "image width: " + width + " height: " + height);
				}
				dumpEvent(event);
				boolean gestureDetectorHandled = gestureDetector
						.onTouchEvent(event);
				// Log.d(TAG, "gestureDetectorHandled=" +
				// gestureDetectorHandled);
				if (gestureDetectorHandled) {
					return true;
				} else {
					// Handle touch events here...
					switch (event.getAction() & MotionEvent.ACTION_MASK) {
					case MotionEvent.ACTION_DOWN:
						savedMatrix.set(matrix);
						start.set(event.getX(), event.getY());
						Log.d(TAG, "mode=DRAG");
						mode = DRAG;
						break;
					case MotionEvent.ACTION_POINTER_DOWN:
						oldDist = spacing(event);
						Log.d(TAG, "oldDist=" + oldDist);
						if (oldDist > 10f) {
							savedMatrix.set(matrix);
							midPoint(mid, event);
							mode = ZOOM;
							Log.d(TAG, "mode=ZOOM");
						}
						break;
					case MotionEvent.ACTION_UP:
					case MotionEvent.ACTION_POINTER_UP:
						mode = NONE;
						Log.d(TAG, "mode=NONE");
						break;
					case MotionEvent.ACTION_MOVE:
						if (mode == DRAG) {
							matrix.set(savedMatrix);
							matrix.postTranslate(event.getX() - start.x,
									event.getY() - start.y);
						} else if (mode == ZOOM) {
							float newDist = spacing(event);
							Log.d(TAG, "newDist=" + newDist);
							if (newDist > 10f) {
								matrix.set(savedMatrix);
								float scale = newDist / oldDist;
								matrix.postScale(scale, scale, mid.x, mid.y);
							}
						}
						break;
					}

					// Perform the transformation
					imageView.setImageMatrix(matrix);
				}
				return true;
			}
		};
		imageView.setOnTouchListener(gestureListener);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
		// ...
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event) {
		// ...
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	private void asyncLoad(final int position) {
		new Thread() {
			@Override
			public void run() {
				try {
					final Bitmap bitmap = loadImage();
					hideToast();
					imageView.post(new Runnable() {
						public void run() {
							setImage(position, bitmap);
						}
					});
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	private Bitmap loadImage() {
		PhotoEntry photoEntry = getPhotoEntry(position);
		// TODO
		// can we cache the image for faster performance?
		// Can we get a bitmap that is sized to the current device?
		Log.d(TAG, photoEntry.mediaGroup.mediaContent.url);
		// String imageUrl=photoEntry.mediaGroup.getLargeThumbNail().url;
		String imageUrl = photoEntry.mediaGroup.mediaContent.url;
		return getImageBitmap(imageUrl);
	}

	public void setImage(int position, Bitmap image) {
		this.position = position;
		imageView.setImageBitmap(image);
	}

	public void hideToast() {
		if (msg != null) {
			Log.d(TAG, "Canceling toast message");
			msg.cancel();
		} else {
			Log.d(TAG, "Toast message not found");
		}
	}

	private PhotoEntry getPhotoEntry(int position) {
		return getPhotos().get(position);
	}

	private List<PhotoEntry> getPhotos() {
		return ((PicasaApplication) getApplication()).getPhotos();
	}

	// TODO
	// need to implement gestures
	// support back and forth swipe
	// support animation during swipe

	// TODO
	// support image rotation

	// TODO create a util function for this
	private Bitmap getImageBitmap(String url) {
		Bitmap bm = null;
		InputStream is = null;
		FlushedInputStream bis = null;
		try {
			URL aURL = new URL(url);
			URLConnection conn = aURL.openConnection();
			conn.setConnectTimeout(1000);
			conn.connect();
			is = conn.getInputStream();
			bis = new FlushedInputStream(is);
			bm = BitmapFactory.decodeStream(bis);
			// if (bm == null)
			// {
			// Log.d(TAG, "unable to get bitmap for url: " + url);
			// }
			// else
			// {
			// Log.d(TAG, Calendar.getInstance().getTimeInMillis() + " url: " +
			// url + " bitmap: "
			// + bm.toString());
			// }
		} catch (IOException e) {
			// TODO
			// handle this
			e.printStackTrace();
		} finally {
			try {
				if (bis != null) {
					bis.close();
				}

				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				// TODO
				// handle this
				e.printStackTrace();
			}
		}

		return bm;
	}

	public void onStart() {
		super.onStart();
		Log.d(TAG, "on start called");
	}

	public void onRestart() {
		super.onRestart();
		Log.d(TAG, "on restart called");
	}

	public void onResume() {
		super.onResume();
		Log.d(TAG, "on resumes called");
	}

	public void onPause() {
		super.onResume();
		Log.d(TAG, "on pause called");
	}

	public void onDestroy() {
		super.onDestroy();
		Log.d(TAG, "on destroy called");
		imageView.setImageBitmap(null);
	}

	class MyGestureDetector extends SimpleOnGestureListener {
		private static final int SWIPE_MIN_DISTANCE = 30;
		private static final int SWIPE_MAX_OFF_PATH = 250;
		private static final int SWIPE_THRESHOLD_VELOCITY = 500;

		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			try {
				if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
					return false;

				Log.d(TAG, "e1.Y: " + e1.getY() + " e2.Y: " + e2.getY()
						+ " velocityY: " + velocityY);
				Log.d(TAG, "e1.X: " + e1.getX() + " e2.X: " + e2.getX()
						+ " velocityX: " + velocityX);
				if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					Log.d(TAG, "right to left swipe");
					if (position < getPhotos().size() - 1) {
						msg = Toast.makeText(PhotoView.this, "Loading...",
								Toast.LENGTH_LONG);
						msg.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
						msg.show();
						Log.d(TAG, "toast message shown");
						asyncLoad(position + 1);
					}
					// // Logic
				} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
						&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
					Log.d(TAG, "left to right swipe");
					if (position > 0) {
						msg = Toast.makeText(PhotoView.this, "Loading...",
								Toast.LENGTH_LONG);
						msg.setGravity(Gravity.BOTTOM | Gravity.CENTER, 0, 0);
						msg.show();
						Log.d(TAG, "toast message shown");
						asyncLoad(position - 1);

					}
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			return true;
		}

		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return super.onScroll(e1, e2, distanceX, distanceY);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
	}

	/** Show an event in the LogCat view, for debugging */
	private void dumpEvent(MotionEvent event) {
		String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
				"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_").append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			sb.append("(pid ").append(
					action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")");
		}
		sb.append("[");
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#").append(i);
			sb.append("(pid ").append(event.getPointerId(i));
			sb.append(")=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount())
				sb.append(";");
		}
		sb.append("]");
		Log.d(TAG, sb.toString());
	}
}
