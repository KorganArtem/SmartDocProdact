/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package su.smartdoc.imgProc;

import java.io.File;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import su.smartdoc.objectFinder.FileLister;
import su.smartdoc.objectFinder.TableFieldFinderIter;
import su.smartdoc.sql.WorkSQL;

/**
 *
 * @author korgan
 */
public class ImgCompres {
    public void allFileResize() throws SQLException{
        WorkSQL wsql = new WorkSQL();
        wsql.conInit();
        Map list = wsql.getStartPageList();
        System.out.println(list.size());
        Set keys = list.keySet();
        FileLister fl = new FileLister();
        for(Object key : keys){
            String folderName = list.get(key.toString()).toString();
            folderName = "C:/smartdoc/SORTED/"+folderName.split("SORTED")[1].split("/")[1];
            //"C:/smartdoc/SORTED/"+
            System.out.println(folderName);
            File oneFolder = new File(folderName);
            for(File entry : fl.getListInFolder(oneFolder)){
                resizeImg(entry.getPath());
            }
        }
            //wsql.updateState(key.toString(), 2);
        
    }
    public void resizeImg(String fileName){
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );
        fileName=fileName.replaceAll("\\\\","/");
        System.out.println(fileName);
        Mat imageOrig = Imgcodecs.imread(fileName);
        if(imageOrig.empty()){
            System.err.println("Файл не загружен");
            return;
        }
        Size sz = new Size();
        sz.height = imageOrig.rows()/2;
        sz.width = imageOrig.cols()/2;
        Imgproc.resize(imageOrig, imageOrig, sz);
        Imgcodecs.imwrite(fileName+"__res.jpg", imageOrig);
    }
}
