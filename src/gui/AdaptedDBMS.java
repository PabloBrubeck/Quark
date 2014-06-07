package gui;

import com.healthmarketscience.jackcess.*;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class AdaptedDBMS extends SimpleDBMS{
    @Override
    public void openDataTables(Database dataBase){
        try{
            for(String s: dataBase.getTableNames()){
                addDataTable(s, new DataTable(dataBase, s){
                    @Override
                    public String rowToString(Row row){
                        return row.get(getColumnName(1)).toString();
                    }
                });
            }
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public static void main(String[] args){
        try{
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
        }catch(UnsupportedLookAndFeelException e){
            Logger.getLogger(SimpleDBMS.class.getName()).log(Level.SEVERE, null, e);
        }
        AdaptedDBMS m=new AdaptedDBMS();
    }
}
