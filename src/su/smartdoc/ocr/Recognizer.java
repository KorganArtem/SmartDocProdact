/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package su.smartdoc.ocr;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.imageio.ImageIO;
import net.sourceforge.tess4j.ITesseract.RenderedFormat;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

/**
 *
 * @author korgan
 */
public class Recognizer {
    List<RenderedFormat> list = new ArrayList<RenderedFormat>();
    Tesseract instance = new Tesseract();
    public Recognizer(){
        list.add(RenderedFormat.PDF);
        instance.setDatapath("C:\\tessdata");
        instance.setLanguage("rus");
    }
    public String recBloc(BufferedImage imageBloc) throws TesseractException{
       String result = instance.doOCR(imageBloc);
        //System.out.println(result +"\n\n");
        return result;
    }
    public void doPdfFromJpg(String filePath, String fileId, String baseDir){
        System.out.println(filePath+" --> "+baseDir+"/pdf/"+fileId);
        File imgFl = new File(filePath);
        long start = new Date().getTime();
        try{
            //String result = instance.doOCR(imgFl);
            instance.createDocuments(imgFl.getAbsolutePath(), baseDir+"/pdf/"+fileId, list);
            //System.out.println(result);
        }
        catch(Exception ex){
            System.out.println(ex.getMessage());
        }
        System.out.println((new Date().getTime()-start)/1000+"");
    }

    public void checkDoc(String path) throws IOException, TesseractException {
        System.out.println(path);
        long start = new Date().getTime();
        File fl = new File(path);
        BufferedImage bi = ImageIO.read(fl);
        BufferedImage docParam = bi.getSubimage(bi.getWidth()*2/3, 0, (bi.getWidth()-bi.getWidth()*2/3), bi.getHeight()/8);
        String result = "";//instance.doOCR(docParam);
        System.out.println(result+" \n");
        BufferedImage name = bi.getSubimage(0, bi.getHeight()/8, (bi.getWidth()), 60);
        result = instance.doOCR(name);
        System.out.println(result+" \n\n\n");
        System.out.println((new Date().getTime()-start)/1000+"");
    }
}
