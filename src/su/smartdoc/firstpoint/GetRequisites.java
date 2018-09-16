/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package su.smartdoc.firstpoint;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.sourceforge.tess4j.TesseractException;
import su.smartdoc.objectFinder.TableFieldFinderIter;
import su.smartdoc.sql.WorkSQL;

/**
 *
 * @author korgan
 */
public class GetRequisites {
    public String getReq() throws SQLException, IOException, TesseractException{
        WorkSQL wsql = new WorkSQL();
        wsql.conInit();
        Map list = wsql.getStartPageList();
        System.out.println(list.size());
        Set keys = list.keySet();
        for(Object key : keys){
            TableFieldFinderIter tffi = new TableFieldFinderIter();
            System.out.println(list.get(key.toString()).toString());
            String rightBlocText = tffi.blockSearcher(list.get(key.toString()).toString());
            rightBlocText = rightBlocText.replaceAll(" ", "");
            //rightBlocText = rightBlocText.replaceAll("\\W", "");
            //System.out.println(rightBlocText);
            wsql.writeDogNum(dogNum(rightBlocText), key.toString());
            wsql.writeAcountNum(acountNum(rightBlocText), key.toString());
            wsql.writeFIO(clearFIO(tffi.secondBlockSearcher(list.get(key.toString()).toString())), key.toString());
            wsql.updateState(key.toString(), 1);
        }
        return null;
    }
    private String dogNum(String rightBlocText){
        Pattern p = Pattern.compile("\\D\\d{10}\\D");
        Matcher m1 = p.matcher(rightBlocText);
        String result = "";
        if(m1.find()){
            result =   m1.group(0).trim();
            result = result.replaceAll("\\D", "");
            System.out.println("\n\n Finded " +result);
        }
        return result;
    }
    private String acountNum(String rightBlocText){
        Pattern p = Pattern.compile("[0-9]{20}");
        Matcher m1 = p.matcher(rightBlocText);
        String result = "";
        if(m1.find()){
            result =   m1.group(0).trim();
            System.out.println("Finded " +result + " \n\n");
        }
        return result;
    }
    private String clearFIO(String input){
        String forRet = "";
        input = input.replaceAll("\\s", " ");
        input = input.replaceAll("\\d", "");
        input = input.replaceAll("'", "");
        String[] splited = input.replaceAll("[^а-яА-Я ]", "").split("\\s+"); 
        System.out.println(input);
        for(int i=0; i<splited.length; i++){
            String word = splited[i].trim();
            word = word.replaceAll("\\w", "");
            if(word.length()<4){
                continue;
            }
            String suff = word.substring(word.length()-3);
            if(word.substring(word.length()-3).equals("вна")){
                System.out.println("\n"+splited[i-2]+" "+splited[i-1]+" "+word);
                if(splited[i-2].length()<4 || splited[i-1].length()<4 )
                    continue;
                forRet = splited[i-2]+" "+splited[i-1]+" "+word;
                return forRet;
            }
            if(word.substring(word.length()-3).equals("вич")){
                System.out.println("\n"+splited[i-2]+" "+splited[i-1]+" "+word);
                if(splited[i-2].length()<4 || splited[i-1].length()<4 )
                    continue;
                forRet = splited[i-2]+" "+splited[i-1]+" "+word;
                return forRet;
            }
            if(word.substring(word.length()-3).equals("оглы")){
                System.out.println("\n"+splited[i-2]+" "+splited[i-1]+" "+word);
                if(splited[i-2].length()<4 || splited[i-1].length()<4 )
                    continue;
                forRet = splited[i-2]+" "+splited[i-1]+" "+word;
                return forRet;
            }
        }
        return forRet;
    }
}
