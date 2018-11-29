/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package su.smartdoc.firstpoint;

import java.io.IOException;
import java.sql.SQLException;
import net.sourceforge.tess4j.TesseractException;
import su.smartdoc.imgProc.ImgCompres;
import su.smartdoc.objectFinder.*;

/**
 *
 * @author korgan
 */
public class SmartDocProdact {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException, TesseractException, SQLException {
        ObjectFounder md = new ObjectFounder();
        md.work();
        GetRequisites gr = new GetRequisites();
        //gr.getReq();
        PassportFinder pf = new PassportFinder();
        pf.starter();
        //ImgCompres ip = new ImgCompres();
        //ip.allFileResize();
        // TODO code application logic here
        
         
           
    }
    
}
