package com.techhounds.houndvision.frc2016;

import android.widget.TextView;

import com.techhounds.houndvision.LiveView;
import com.techhounds.houndvision.R;
import com.techhounds.houndvision.web.DataIO;

import org.opencv.core.Mat;

import java.util.TimerTask;

public class Processor2016 extends TimerTask {

    private int stage;
    public Filter filter = new TargetFilter(stage = 4);
    public Mat currProcessedFrame;

    public boolean running;

    private static Processor2016 instance;

    public static Processor2016 getInstance() {
        return instance == null ? instance = new Processor2016() : instance;
    }

    @Override
    public void run() {

        running = true;
        DataIO.getInstance();

        while(running) {
            if (currProcessedFrame != LiveView.currentFrame && LiveView.currentFrame != null) {
                    Mat frame = filter.process(currProcessedFrame = LiveView.currentFrame);
                    LiveView.processedFrame = frame;
            } else if(!LiveView.getLiveView().isCameraOn) {
                    Mat frame = filter.process(currProcessedFrame = LiveView.currentFrame);
                    LiveView.processedFrame = frame;

                try {
                    Thread.sleep((long) (1 / 30.0 * 1000));
                }catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void setStage(final int stage) {
        filter = new TargetFilter(stage > 4 ? 4 : (stage < 0 ? 0 : stage));

        LiveView.getLiveView().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) LiveView.getLiveView().findViewById(R.id.filterStage)).setText("Filter Stage: " + stage);
            }
        });
    }

    public void changeStage() {
        stage++;
        setStage(stage % 5);
    }
}
