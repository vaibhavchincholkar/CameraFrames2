package cse.buffalo.edu;

import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;

import java.io.Serializable;

public class ImageFeatures implements Serializable {
    private static final long serialVersionUID= 1L;
    /*transient MatOfKeyPoint keypoints;
   transient Mat descriptors;
   public ImageFeatures(MatOfKeyPoint keypoints, Mat descriptors){
       this.keypoints=keypoints;
       this.descriptors=descriptors;
   }*/
    int val;
    public ImageFeatures(int val){
        this.val=val;
    }
}
