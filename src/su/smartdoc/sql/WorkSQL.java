/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package su.smartdoc.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;

/**
 *
 * @author korgan
 */
public class WorkSQL {
    public Connection con = null;
    public String url = ""; 
    public String login = "";
    public String pass = "";
    public void conInit(){
        url="jdbc:mysql://10.0.0.16:3306/IWTR?useUnicode=true&characterEncoding=UTF-8";
        try
        {
            login="root";
            pass="newpass";
            con = DriverManager.getConnection(url, login, pass);
            System.out.println("Connection is ready! ");
        }
        catch(SQLException ex)
        {
            System.out.println("Mysql ERROR: "+ex.getMessage());
        }
    }
    public void writeImageData(String path, String barCode){
        try{
            path=path.replaceAll("\\\\","/");
            String insertQuery = "INSERT INTO `markedImage` (`barCode`, `path`) VALUES ('"+barCode+"', '"+path+"')";
            PreparedStatement  st = con.prepareStatement(insertQuery);
            st.execute();
        }
        catch(SQLException ex){
            System.out.println(ex.getMessage());
        }
    }
}
