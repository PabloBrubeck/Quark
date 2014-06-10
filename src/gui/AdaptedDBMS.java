package gui;

import com.healthmarketscience.jackcess.*;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import gui.MyComponent.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.*;
import java.awt.*;

public class AdaptedDBMS extends SimpleDBMS{
    private class AdaptedDataTable extends DataTable{
        private Caller caller=null; 
        private JPanel relationPnl;
        public AdaptedDataTable(Database db, String t) {
            super(db, t);
        }
        public void setCaller(Caller c){
            caller=c;
        }
        @Override
        public String rowToString(Row row){
            return row.get(getColumnName(1)).toString();
        }
        @Override
        public void calculate(int r, int c){
            if(caller!=null){
                caller.invoke();
            }
        }
    }
    @Override
    public void openDataTables(Database dataBase){
        try{
            AdaptedDataTable dt=null;
            for(String s: dataBase.getTableNames()){
                dt=new AdaptedDataTable(dataBase, s);
                addDataTable(s, dt);
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
