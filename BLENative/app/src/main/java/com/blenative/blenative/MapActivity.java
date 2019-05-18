package com.blenative.blenative;

import android.app.ActionBar;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

public class MapActivity extends AppCompatActivity {

    private ScaleGestureDetector mScaleGestureDetector;
    private float mScaleFactor = 1.0f;
    private ImageView imageView;
    int windowwidth; // Actually the width of the RelativeLayout.
    int windowheight; // Actually the height of the RelativeLayout.
    private ViewGroup mRrootLayout;
    private int _xDelta;
    private int _yDelta;
    private boolean isOutReported = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.map_main);
//        ActionBar actionBar = getActionBar();
//        actionBar.setDisplayHomeAsUpEnabled(true);

//        BitmapFactory.Options myOptions = new BitmapFactory.Options();
//        myOptions.inDither = true;
//        myOptions.inScaled = false;
//        myOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;// important
//
//        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.floorplan, myOptions);
//        Paint paint = new Paint();
//        paint.setAntiAlias(true);
//        paint.setColor(Color.BLUE);
//
//        Bitmap workingBitmap = Bitmap.createBitmap(bitmap);
//        Bitmap mutableBitmap = workingBitmap.copy(Bitmap.Config.ARGB_8888, true);
//
//        Canvas canvas = new Canvas(mutableBitmap);
//        canvas.drawCircle(60, 50, 25, paint);
//
//        imageView = (ImageView)findViewById(R.id.imageView);
//        imageView.setAdjustViewBounds(true);
//        imageView.setImageBitmap(mutableBitmap);
//
//        imageView=(ImageView)findViewById(R.id.imageView);
//        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleListener());
//
//        mRrootLayout = (ViewGroup) findViewById(R.id.root);
//        imageView.setOnTouchListener(new View.OnTouchListener()
//        {
//            @Override
//            public boolean onTouch(View v, MotionEvent event){
//                final int X = (int) event.getRawX();
//                final int Y = (int) event.getRawY();
//
//                // Check if the image view is out of the parent view and report it if it is.
//                // Only report once the image goes out and don't stack toasts.
////                if (isOut(v)) {
////                    if (!isOutReported) {
////                        isOutReported = true;
////                        Toast.makeText(this, "OUT", Toast.LENGTH_SHORT).show();
////                    }
////                } else {
////                    isOutReported = false;
////                }
//                switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                    case MotionEvent.ACTION_DOWN:
//                        // _xDelta and _yDelta record how far inside the view we have touched. These
//                        // values are used to compute new margins when the view is moved.
//                        _xDelta = X - v.getLeft();
//                        _yDelta = Y - v.getTop();
//                        break;
//                    case MotionEvent.ACTION_UP:
//                    case MotionEvent.ACTION_POINTER_DOWN:
//                    case MotionEvent.ACTION_POINTER_UP:
//                        // Do nothing
//                        break;
//                    case MotionEvent.ACTION_MOVE:
//                        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) v
//                                .getLayoutParams();
//                        // Image is centered to start, but we need to unhitch it to move it around.
////                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//                        lp.removeRule(RelativeLayout.CENTER_HORIZONTAL);
//                        lp.removeRule(RelativeLayout.CENTER_VERTICAL);
////                        } else {
////                            lp.addRule(RelativeLayout.CENTER_HORIZONTAL, 0);
////                            lp.addRule(RelativeLayout.CENTER_VERTICAL, 0);
////                        }
//                        lp.leftMargin = X - _xDelta;
//                        lp.topMargin = Y - _yDelta;
//                        // Negative margins here ensure that we can move off the screen to the right
//                        // and on the bottom. Comment these lines out and you will see that
//                        // the image will be hemmed in on the right and bottom and will actually shrink.
//                        lp.rightMargin = v.getWidth() - lp.leftMargin - windowwidth;
//                        lp.bottomMargin = v.getHeight() - lp.topMargin - windowheight;
//                        v.setLayoutParams(lp);
//                        break;
//                }
//                // invalidate is redundant if layout params are set or not needed if they are not set.
////        mRrootLayout.invalidate();
//                return true;
//            }
//        });
    }

//    private boolean isOut(View view) {
//        // Check to see if the view is out of bounds by calculating how many pixels
//        // of the view must be out of bounds to and checking that at least that many
//        // pixels are out.
//        float percentageOut = 0.50f;
//        int viewPctWidth = (int) (view.getWidth() * percentageOut);
//        int viewPctHeight = (int) (view.getHeight() * percentageOut);
//
//        return ((-view.getLeft() >= viewPctWidth) ||
//                (view.getRight() - windowwidth) > viewPctWidth ||
//                (-view.getTop() >= viewPctHeight) ||
//                (view.getBottom() - windowheight) > viewPctHeight);
//    }
//
//    public boolean onTouchEvent(MotionEvent motionEvent) {
//        mScaleGestureDetector.onTouchEvent(motionEvent);
//        return true;
//    }
//
////    public boolean onOptionsItemSelected(MenuItem item){
////        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
////        startActivityForResult(myIntent, 0);
////        return true;
////    }
//
//    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
//        @Override
//        public boolean onScale(ScaleGestureDetector scaleGestureDetector){
//            mScaleFactor *= scaleGestureDetector.getScaleFactor();
//            mScaleFactor = Math.max(0.1f,
//                    Math.min(mScaleFactor, 10.0f));
//            imageView.setScaleX(mScaleFactor);
//            imageView.setScaleY(mScaleFactor);
//            return true;
//        }
//    }
}

