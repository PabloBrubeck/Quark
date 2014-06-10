package sql;

import java.sql.*;
import java.util.*;

public class Connector {
    private Connection connection;
    public Connector(){
        
    }
    public boolean connectToAccess(String accessFilePath) {
        try {
            Class.forName("sun.jdbc.odbc.JdbcOdbcDriver");
            String url="jdbc:odbc:Driver={Microsoft Access Driver (*.mdb, *.accdb)};DBQ="+accessFilePath;  
            connection = DriverManager.getConnection(url,"",""); 
        } catch (ClassNotFoundException | SQLException ex) {
            ex.printStackTrace();
            return false;
        }
        return true;
    }
    public ArrayList executeQuery(String sql) {
        ArrayList rows = new ArrayList();
        try {
            Statement stmt = connection.createStatement();
            stmt.executeQuery(sql);//muestra resultados equivalentes en SQL  a utilizar SELECT
            ResultSet rs = stmt.getResultSet(); //obtiene el resultado de la consulta y lo guarda en rs
            if(rs!=null){
                while(rs.next()){
                    ArrayList ctemp = new ArrayList();
                    for (int i=1; i<=rs.getMetaData().getColumnCount(); i++){
                        ctemp.add(rs.getString(i));
                        System.out.println(ctemp.get(i - 1));
                    }
                    rows.add(ctemp);
                }
                rs.close();
                stmt.close();
                return rows;
            }else{
                System.out.println("No hay datos");
            }
        } catch (SQLException e) {
            System.out.println("Hubo un error");
        }
        return null;
    }
    public void closeConnection(){
        try {
            connection.close();
        } catch (SQLException e) {
        }
    }
    
    public static void main(String[] args){
        Connector c=new Connector();
        c.connectToAccess("new.mdb");
        c.executeQuery("SELECT * FROM new");
        c.closeConnection();
    }

}
