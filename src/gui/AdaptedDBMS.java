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

public class AdaptedDBMS extends SimpleDBMS{
    private class AdaptedDataTable extends DataTable{
        private Caller caller=null;
        public AdaptedDataTable(Database db, String t, int... ints) {
            super(db, t, ints);
        }
        public void setCaller(Caller c){
            caller=c;
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
        int[][] masks={{1,2,3},{1},{1},{1},{1},{1},{3,2},{1},{2},{1},{1}};
        try{
            int k=0;
            for(String s: dataBase.getTableNames()){
                AdaptedDataTable dt=new AdaptedDataTable(dataBase, s, masks[k]);
                addDataTable(s, dt);
                k++;
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