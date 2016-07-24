package com.techhounds.houndvision;

import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.InputConfiguration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.techhounds.houndvision.R;
import com.techhounds.houndvision.frc2016.Processor2016;
import com.techhounds.houndvision.web.DataIO;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.util.List;

public class LiveView extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    static {
        System.loadLibrary("opencv_java");
    }

    private CameraBridgeViewBase mOpenCvCameraView;

    private CaptureRequest.Builder mPreviewRequestBuilder;
    private CaptureRequest mPreviewRequest;

    private Thread processor;

    private long exposureTime = 1000,frameDuration = 1000;
    private int sensitivity = 200;

    public static Mat currentFrame;
    public static Mat processedFrame;

    private static LiveView live;

    private int frameCount;
    public boolean isCameraOn = true;

    private final int MENU_FILTER = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        live = this;
        Log.i("OPENCV", "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setIcon(R.mipmap.ic_launcher);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.setMaxFrameSize(600, 400);
        mOpenCvCameraView.disableFpsMeter();
        mOpenCvCameraView.setCameraIndex(0);

        if(processor == null) {
            processor = new Thread(Processor2016.getInstance());
            processor.start();
            frameCount = 0;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_filter:
                Processor2016.getInstance().changeStage();
                return true;
            case R.id.menu_calibration:
                return true;
            case R.id.menu_toggle:

                isCameraOn = !isCameraOn;

                if(!isCameraOn) {
                    item.setIcon(R.drawable.ic_menu_start);
                } else {
                    item.setIcon(R.drawable.ic_menu_stop);
                }

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onPause()
    {
        super.onPause();

        if(processor != null) {
            Processor2016.getInstance().running = false;
            processor = null;
        }

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();

        if(processor != null) {
            Processor2016.getInstance().running = false;
            processor = null;
        }

        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public static LiveView getLiveView() {
        return live;
    }

    public void onCameraViewStarted(int width, int height) {
    }

    public void onCameraViewStopped() {
    }

    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        if(!isCameraOn)
            return processedFrame;

        Mat currentFrame = inputFrame.rgba();
        Core.flip(currentFrame, currentFrame, 0);
        this.currentFrame = currentFrame;


        frameCount++;
        DataIO.getInstance().send("frameCount::" + frameCount);

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView)  findViewById(R.id.frameCount)).setText("Frame Count: " + frameCount);
            }
        });

        return processedFrame;
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
            processor = new Thread(new Processor2016());
            processor.start();
        }

        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_11, this, mLoaderCallback);
    }
}
