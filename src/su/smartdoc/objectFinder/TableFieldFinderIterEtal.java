/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package su.smartdoc.objectFinder;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.TesseractException;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import su.smartdoc.ocr.Recognizer;


/**
 *
 * @author korgan
 */
public class TableFieldFinderIterEtal {
    boolean moved = false;
    String dstPath = null;
    String dstRoot = "C:\\smartdoc\\SORTED\\";
    int rightBlockEnd = 0;
    public void work() throws IOException{
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        String rootDirectiryPath = "C:\\smartdoc\\TRASH";
        for(File lowEntry : getListInFolder(new File(rootDirectiryPath))){
            System.out.println(lowEntry.getAbsolutePath());
            barCodeSearcher(rootDirectiryPath, lowEntry.getName(), "C:\\smartdoc\\CROPED");
         }
    }
    private File[] getListInFolder(File folder){
        File[] fileList = folder.listFiles();
        return fileList;
    }
    public void barCodeSearcher(String srcPath, String fileName, String dstPath) throws IOException {
        Mat imageOrig;
        imageOrig = Imgcodecs.imread(srcPath+"\\"+fileName);
        if(imageOrig.empty()){
            System.err.println("Файл не загружен");
            return;
        }
        BufferedImage rightContour =  getRightContour(imageOrig, fileName);
        //Imgproc.drawContours(imageOrig, filteredContours, -1, new Scalar(255, 0 , 0), 3);
        
        
    }
    public String blockSearcher(String fileName) throws IOException, TesseractException {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        Mat imageOrig=null;
        try{
            imageOrig = Imgcodecs.imread(fileName);
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        if(imageOrig.empty()){
            System.err.println("Файл не загружен");
            return null;
        }
        BufferedImage rightContour =  getRightContour(imageOrig, fileName);
        //Imgproc.drawContours(imageOrig, filteredContours, -1, new Scalar(255, 0 , 0), 3);
        Recognizer rc = new Recognizer();
        String forRet = rc.recBloc(rightContour);
        return forRet;
        
    }
    public String secondBlockSearcher(String fileName) throws IOException, TesseractException {
        Mat imageOrig;
        imageOrig = Imgcodecs.imread(fileName);
        if(imageOrig.empty()){
            System.err.println("Файл не загружен");
            return null;
        }
        BufferedImage rightContour =  getSecondContour(imageOrig, fileName);
        getSecondContour(imageOrig, fileName);
        //Imgproc.drawContours(imageOrig, filteredContours, -1, new Scalar(255, 0 , 0), 3);
        Recognizer rc = new Recognizer();
        String forRet  = rc.recBloc(rightContour);
        //System.out.println(forRet);
        return forRet;
        
    }
    private BufferedImage getRightContour(Mat imageOrig, String fileName) throws IOException{
        Double heigh = imageOrig.rows()/5.5;
        Double xStart = imageOrig.cols()/2.0;
        System.out.println("ImgWidth  " + imageOrig.cols() + " ImgHeigh " + imageOrig.rows()
                + " RectStart " + xStart + " imgWidth " + (heigh.intValue()));
        Rect rectCrop = new Rect(xStart.intValue(), 0, imageOrig.cols()-xStart.intValue(), heigh.intValue());
        System.out.println(rectCrop);
        Mat image = new Mat(imageOrig, rectCrop);
        Imgproc.cvtColor(image, image, Imgproc.COLOR_RGB2GRAY);
        int ddepth  = CvType.CV_32F;
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Imgproc.Sobel(image, gradX, ddepth, 1, 0);
        Imgproc.Sobel(image, gradY, ddepth, 0, 1);
        Mat gradientOrig = new Mat();
        Core.subtract(gradX, gradY, gradientOrig);
        Core.convertScaleAbs(gradientOrig, gradientOrig);
        Imgproc.blur(gradientOrig, gradientOrig, new Size(1, 1));
        int idRightBox = -1;
        int unknowParametr = 250;
        List<MatOfPoint> filteredContours = new ArrayList<>();
        while(idRightBox == -1 && unknowParametr > 0){
            Mat gradient = new Mat();
            unknowParametr = unknowParametr-10;
            Imgproc.threshold(gradientOrig, gradient, unknowParametr, 250, Imgproc.THRESH_BINARY);
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
            Imgproc.morphologyEx(gradient, gradient, Imgproc.MORPH_CLOSE, kernel);
            Point anchor = new Point();
            Imgproc.erode(gradient, gradient, kernel, anchor, 300);
            Imgproc.dilate(gradient, gradient, kernel);
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(gradient, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            filteredContours = findCotour(contours);
            idRightBox = searchRightBox(filteredContours); 
        }
        //writeBox(imageOrig, filteredContours.get(idRightBox), fileName, xStart.intValue());
        Imgproc.drawContours(gradientOrig, filteredContours, idRightBox, new Scalar(255, 0 , 0), 30);
        //Imgcodecs.imwrite(dstRoot+"\\"+fileName, gradientOrig);
        MatOfPoint2f mop = new MatOfPoint2f(filteredContours.get(idRightBox).toArray());
        RotatedRect rc = Imgproc.minAreaRect(mop);
        Rect box = rc.boundingRect();
        rightBlockEnd = box.y+box.height;
        return matToBuff(imageOrig, filteredContours.get(idRightBox), fileName, xStart.intValue());
    }
    private BufferedImage getSecondContour(Mat imageOrig, String fileName) throws IOException{
        Double xStart = imageOrig.rows()/15.0;
        Double yStart = imageOrig.rows()/8.0;
        Double height = imageOrig.rows()/15.0;
        Double width = imageOrig.cols()/1.6;
        //System.out.println("ImgWidth  " + imageOrig.cols() + " ImgHeigh " + imageOrig.rows()
                //+ " RectStart " + 0 + " imgWidth " + 100);
        if(rightBlockEnd>20)
            rightBlockEnd = rightBlockEnd-20;
        Rect rectCrop = new Rect(xStart.intValue(), rightBlockEnd, width.intValue(), height.intValue());
        //System.out.println(rectCrop);
        Mat image = new Mat(imageOrig, rectCrop);
        Mat imageGray = new Mat();
        Imgproc.cvtColor(image, imageGray, Imgproc.COLOR_RGB2GRAY);
        int ddepth  = CvType.CV_32F;
        Mat gradX = new Mat();
        Mat gradY = new Mat();
        Imgproc.Sobel(imageGray, gradX, ddepth, 1, 0);
        Imgproc.Sobel(imageGray, gradY, ddepth, 0, 1);
        Mat gradientOrig = new Mat();
        Core.subtract(gradX, gradY, gradientOrig);
        Core.convertScaleAbs(gradientOrig, gradientOrig);
        Imgproc.blur(gradientOrig, gradientOrig, new Size(1, 1));
        int idRightBox = -1;
        int unknowParametr = 50;
        List<MatOfPoint> contours = new ArrayList<>();
        List<MatOfPoint> filteredContours = new ArrayList<>();
            Mat gradient = new Mat();
        //while(idRightBox == -1 && unknowParametr > 0){
            unknowParametr = unknowParametr-10;
            Imgproc.threshold(gradientOrig, gradient, unknowParametr, 250, Imgproc.THRESH_BINARY);
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(1, 1));
            Imgproc.morphologyEx(gradient, gradient, Imgproc.MORPH_CLOSE, kernel);
            Point anchor = new Point();
            Imgproc.erode(gradient, gradient, kernel, anchor, 300);
            Imgproc.dilate(gradient, gradient, kernel);
            Imgproc.findContours(gradient, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            //filteredContours = findSecondCotour(contours);
            //idRightBox = searchRightBox(filteredContours); 
        //}
        //writeBox(imageOrig, filteredContours.get(idRightBox), fileName, 0);
        //Imgproc.drawContours(image, filteredContours, -1, new Scalar(255, 0 , 0), 3);
        //Imgcodecs.imwrite(fileName+"__sec.jpg", image);
        return matToBuff(image);
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
        //System.out.println("Te bigest contour "+id);
        return id;
    }
    public List<MatOfPoint> findCotour(List<MatOfPoint> contours){
        List<MatOfPoint> forRet = new ArrayList<>();
        for(int i =0; i < contours.size(); i++){
            if(Imgproc.contourArea(contours.get(i)) > 90000.00){
                forRet.add(contours.get(i));
            }
        }
        return forRet;
    }
    public List<MatOfPoint> findSecondCotour(List<MatOfPoint> contours){
        List<MatOfPoint> forRet = new ArrayList<>();
        int id = -1;
        for(int contourId = 0; contourId<contours.size(); contourId++){
            if(Imgproc.contourArea(contours.get(contourId))<100)
                continue;
            MatOfPoint2f mop = new MatOfPoint2f(contours.get(contourId).toArray());
            RotatedRect rc = Imgproc.minAreaRect(mop);
            double soot = rc.size.width / rc.size.height;
            System.out.println(contourId+"  "+rc.size.width+"x"+rc.size.height);
            forRet.add(contours.get(contourId));
        }
        return forRet;
    }
    private int searchRightBox(List<MatOfPoint> contours){
        int id = -1;
        for(int contourId = 0; contourId<contours.size(); contourId++){
            MatOfPoint2f mop = new MatOfPoint2f(contours.get(contourId).toArray());
            RotatedRect rc = Imgproc.minAreaRect(mop);
            double soot = rc.size.width / rc.size.height;
            if(soot<3.2){
                id = contourId;
            } else {
            }
        }
        return id;
    }
    private int searchMainBox(List<MatOfPoint> contours){
        int id = -1;
        for(int contourId = 0; contourId<contours.size(); contourId++){
            MatOfPoint2f mop = new MatOfPoint2f(contours.get(contourId).toArray());
            RotatedRect rc = Imgproc.minAreaRect(mop);
            double soot = rc.size.width / rc.size.height;
            System.out.println(contourId + "  " + soot);
            if(soot<2.8){
                id = contourId;
            } else {
            }
        }
        System.out.println("Right  "+id);
        return id;
    }
    private void writeBox(Mat startImag, MatOfPoint contour, String fileName, int xStart){
        MatOfPoint2f mop = new MatOfPoint2f(contour.toArray());
        RotatedRect rc = Imgproc.minAreaRect(mop);
        Rect box = rc.boundingRect();
        box.x = box.x+xStart;
        if(box.x<0){
            box.x=0;
        }
        if(box.y<0){
            box.y=0;
        }
        try{
            Mat image = new Mat(startImag, box);
            Imgcodecs.imwrite(fileName+"__.jpg", image);
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
            Imgcodecs.imwrite(fileName+"__.jpg", startImag);
        }
    }
    private BufferedImage matToBuff(Mat startImag, MatOfPoint contour, String fileName, int xStart) throws IOException{
        MatOfPoint2f mop = new MatOfPoint2f(contour.toArray());
        RotatedRect rc = Imgproc.minAreaRect(mop);
        Rect box = rc.boundingRect();
        box.x = box.x+xStart;
        if(box.x<0){
            box.x=0;
        }
        if(box.y<0){
            box.y=0;
        }
        Mat image = new Mat();
        try{
            image = new Mat(startImag, box);
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        MatOfByte bytemat = new MatOfByte();

        Imgcodecs.imencode(".jpg", image, bytemat);

        byte[] bytes = bytemat.toArray();

        InputStream in = new ByteArrayInputStream(bytes);

        BufferedImage img = ImageIO.read(in);
        return img;
    }
    private BufferedImage matToBuff(Mat startImag) throws IOException{
        
        MatOfByte bytemat = new MatOfByte();

        Imgcodecs.imencode(".jpg", startImag, bytemat);

        byte[] bytes = bytemat.toArray();

        InputStream in = new ByteArrayInputStream(bytes);

        BufferedImage img = ImageIO.read(in);
        return img;
    }
}
