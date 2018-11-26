/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package su.smartdoc.objectFinder;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import su.smartdoc.barcodeDetecter.BarcodeDetector;
import su.smartdoc.ocr.Recognizer;
import su.smartdoc.sql.WorkSQL;
/**
 *
 * @author korgan
 */
public class PassportFinder {

    boolean moved = false;
    String dstPath = null;
    String dstRoot = "C:/smartdoc/SORTED/";
    WorkSQL rc = new WorkSQL();
    public void starter() throws SQLException, IOException, TesseractException{
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        rc.conInit();
        Map<String, String> list = rc.getAllPageList();
        Set keys = list.keySet();
        for(Object key : keys){
            
            System.out.println(key.toString()+"  -->  " +list.get(key.toString()).toString());
            //System.out.println(lowEntry.getAbsolutePath());
            barCodeSearcher(list.get(key.toString()).toString(), "C:\\smartdoc\\CROPED");
        }
    }
    public void barCodeSearcher(String fileName, String dstPath) throws IOException, TesseractException, SQLException {
        Mat image = Imgcodecs.imread(fileName);
        if(image.empty()){
            System.err.println("Файл не загружен");
            return;
        }
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        int ddepth  = CvType.CV_32F;
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Imgproc.Sobel(image, gradX, ddepth, 1, 0);
        Imgproc.Sobel(image, gradY, ddepth, 0, 1);
        Mat gradient = new Mat();
        Core.subtract(gradX, gradY, gradient);
        Core.convertScaleAbs(gradient, gradient);
        Imgproc.blur(gradient, gradient, new Size(2, 5));
        Imgproc.threshold(gradient, gradient, 225, 255, Imgproc.THRESH_BINARY);
        Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(21, 7));
        Imgproc.morphologyEx(gradient, gradient, Imgproc.MORPH_CLOSE, kernel);
        Point anchor = new Point();
        Imgproc.erode(gradient, gradient, kernel, anchor, 5);
        Imgproc.dilate(gradient, gradient, kernel);
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(gradient, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        int bgestContour = getIdBigestCountour(contours);
        Scalar color=new Scalar(255, 0, 0);
        Imgproc.drawContours(image, contours, -1, color, ddepth);
        
        
        System.out.println("C:\\smartdoc\\FIRST"+"\\"+fileName.split("/")[fileName.split("/").length-1]);
        
        
        Imgcodecs.imwrite("C:\\smartdoc\\FIRST"+"\\"+fileName.split("/")[fileName.split("/").length-1], gradient);
        Point center = new Point();
        float[] radius = new float[1];
        Imgproc.minEnclosingCircle(new MatOfPoint2f(contours.get(bgestContour).toArray()), center, radius);
       
        Imgcodecs.imwrite(dstPath+"/"+fileName.split("/")[fileName.split("/").length-1], image);
        
    }
    public int getIdBigestCountour(List<MatOfPoint> contours){
        double maxSize = 0;
        int id = 0;
        for(int contourId = 0; contourId<contours.size(); contourId++){
            if(maxSize<Imgproc.contourArea(contours.get(contourId))){
                maxSize=Imgproc.contourArea(contours.get(contourId));
                id = contourId;
            }
        }
        return id;
    }

}