package com.atifniyaz.opencv;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    static {
        System.loadLibrary("opencv_java");
    }

    private CameraBridgeViewBase mOpenCvCameraView;

    private Timer processor;

    private Mat currentFrame;
    private Mat processedFrame;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i("OPENCV", "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

        if(processor == null) {
            processor = new Timer();
            processor.scheduleAtFixedRate(new ProcessorTask(), 0, (long) (1 / 30.0 * 1000.0));
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if(processor != null) {
            processor.cancel();
            processor = null;
        }

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();

        if(processor != null) {
            processor.cancel();
            processor = null;
        }

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        currentFrame = inputFrame.rgba();
        return processedFrame == null ? currentFrame : processedFrame;
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i("OPENCV", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

        if(processor == null) {
            processor = new Timer();
            processor.scheduleAtFixedRate(new ProcessorTask(), 0, (long) (1 / 30.0 * 1000.0));
        }

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
    }

    public class ProcessorTask extends TimerTask {

        public void run() {
            if(currentFrame != null) {
                processedFrame = currentFrame;
            }
        }
    }
}
