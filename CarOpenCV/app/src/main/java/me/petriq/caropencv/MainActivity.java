package me.petriq.caropencv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "ColorDetector4000";
    private CameraBridgeViewBase mOpenCvCameraView;
    private Mat hierarchy = new Mat();

    Mat mHsvMat = new Mat();
    Mat mMaskMat = new Mat();
    Mat mDilatedMat = new Mat();
    Mat mRgba = new Mat();

    static {
        if (!OpenCVLoader.initDebug()) {
            // Handle initialization error
        }
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            mOpenCvCameraView.enableView();
        }
    };

    public MainActivity() {
        Log.i(TAG, "Vytvorena instancia " + this.getClass());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.surface_view);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        JavaCameraView cameraView = (JavaCameraView) findViewById(R.id.tutorial1_activity_java_surface_view);
        int height = cameraView.getHeight();
        int width = cameraView.getWidth();
        mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        mRgba = new Mat(height, width, CvType.CV_8UC4);


        //Camera resolution set to 800x480 for better FPS performance
        cameraView.setMaxFrameSize(800, 480);
        cameraView.setCvCameraViewListener(this);

        if (!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback)) {
            Log.e("TEST", "Cannot connect to OpenCV Manager");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found.");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the bmenu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {
        hierarchy.release();
    }

    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();

        ArrayList<MatOfPoint> contours = new ArrayList<>();
        Scalar colorRed = new Scalar(220, 20, 60);
        int iLineThickness = 5;

        int channelCount = 8;
        Imgproc.cvtColor(mRgba, mHsvMat, Imgproc.COLOR_RGB2HSV, channelCount);
        Scalar lowerThreshold = new Scalar(120, 100, 100); // Blue color – lower hsv values
        Scalar upperThreshold = new Scalar(179, 255, 255); // Blue color – higher hsv values

        Core.inRange(mHsvMat, lowerThreshold, upperThreshold, mMaskMat);

        Imgproc.dilate(mMaskMat, mDilatedMat, new Mat());
        Imgproc.findContours(mDilatedMat, contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        for (int contourIdx = 0; contourIdx < contours.size(); contourIdx++) {
            if (contours.get(contourIdx).size().height > 100)  // Minimum size allowed for consideration
            {
                Imgproc.drawContours(mRgba, contours, contourIdx, colorRed, iLineThickness);
            }
        }
        return mRgba;
    }
}
