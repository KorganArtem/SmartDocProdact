/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package su.smartdoc.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

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
    public void writeImageData(String path, String barCode, String directoryPath){
        try{
            path=path.replaceAll("\\\\","/");
            directoryPath=directoryPath.replaceAll("\\\\","/");
            String insertQuery = "INSERT INTO `markedImage` (`barCode`, `path`, `directoryPath`) VALUES ('"+barCode+"', '"+path+"', '"+directoryPath+"')";
            PreparedStatement  st = con.prepareStatement(insertQuery);
            st.execute();
        }
        catch(SQLException ex){
            System.out.println(ex.getMessage());
        }
    }
    public Map getStartPageList() throws SQLException{
        Map<String, String> list = new HashMap<String, String>();
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM `markedImage`");
        while(rs.next()){
            //System.out.println(rs.getString("ImageId") + "  " +rs.getString("path"));
            list.put(rs.getString("ImageId"), rs.getString("path"));
        }
        return list;
    }
    public Map getAllPageList() throws SQLException{
        Map<String, String> list = new HashMap<String, String>();
        try{
            Statement st = con.createStatement();
            ResultSet rs = st.executeQuery("SELECT * FROM `imgList` "
                    + "INNER JOIN `markedImage` "
                    + "ON `markedImage`.`barCode`=`imgList`.`causeNum`");
            while(rs.next()){
                list.put(rs.getString("idimgList"), rs.getString("directoryPath")+rs.getString("imgName"));
            }
        }
        catch(SQLException ex){
            System.out.println(ex.getMessage());
        }
        return list;
    }
    public void writeDogNum(String dogNum, String id) throws SQLException{
        Statement st = con.createStatement();
        st.execute("UPDATE `markedImage` SET `contractNumber`='"+dogNum+"' WHERE ImageId='"+ id +"'");
    }
    public void writeAcountNum(String dogNum, String id) throws SQLException{
        Statement st = con.createStatement();
        st.execute("UPDATE `markedImage` SET `accountNumber`='"+dogNum+"' WHERE ImageId='"+ id +"'");
    }
    public void writeFIO(String FIO, String id) throws SQLException{
        Statement st = con.createStatement();
        st.execute("UPDATE `markedImage` SET `LastName`='"+FIO+"' WHERE ImageId='"+ id +"'");
    }

    public void updateState(String id, int state) throws SQLException {
        Statement st = con.createStatement();
        st.execute("UPDATE `markedImage` SET `proccesedImafe`='"+state+"' WHERE ImageId='"+ id +"'");
    }
    public void writeImg(String imgName, String causeNum) throws SQLException{
        Statement st = con.createStatement();
        st.execute("INSERT `imgList` SET `imgName`='"+imgName+"',  causeNum='"+ causeNum +"'");
    }
}
