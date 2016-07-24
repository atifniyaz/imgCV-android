package com.techhounds.houndvision.utils;

import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import com.techhounds.houndvision.filters.MatFilter;
import com.techhounds.houndvision.PolygonCv;

public class PolygonRender implements MatFilter {
    
	Scalar    colors;
	int       thickness;
	PolygonCv polygon;
	
	public PolygonRender(Scalar colors, int thickness) {
		this.colors = colors;
		this.thickness = thickness;
	}
	
	public void setPolygon(PolygonCv polygon) {
		this.polygon = polygon;
	}
	
	public Mat process(Mat inputImage) {
    	
    	List<MatOfPoint> bestContours = new ArrayList<>();
    	
    	bestContours.add(polygon.toContour());
    	Imgproc.drawContours(inputImage, bestContours, -1, colors, thickness);

    	return inputImage;
    }
}
