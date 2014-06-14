package gui;

import com.healthmarketscience.jackcess.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class AdaptedDBMS extends SimpleDBMS{
    
    public class AdaptedDataTable extends DataTable{
        public AdaptedDataTable(Database db, String t, int... ints) {
            super(db, t, ints);
        }
        @Override
        public void goTo(DataTable dt){
            getTabbedPane().setSelectedComponent(dt);
        }
    }
    
    @Override
    public void openDataTables(Database dataBase){
        int[][] masks={{2,-3},{4,-3},{1},{1,3,-2},{1},{1,3,-2},{2,-3},{2,-3},{2,-4},{1},{2,-1}};
        try{
            int k=0;
            for(final String s: dataBase.getTableNames()){
                AdaptedDataTable dt;
                switch(s){
                    default:
                        dt=new AdaptedDataTable(dataBase, s, masks[k]);
                        break;
                    case "Asistencia":
                        dt=new AdaptedDataTable(dataBase, s, masks[k]){
                            @Override
                            public void calculate(Cursor cursor){
                                setCurrentRowValue(cursor, "Total", getCurrentRowTotal(cursor, "Lunes", "Martes", "Miercoles", "Jueves", "Viernes", "Sabado"));
                            }
                        };
                        break;
                    case "Balance":
                        dt=new AdaptedDataTable(dataBase, s, masks[k]){
                            @Override
                            public void calculate(Cursor cursor){
                                setCurrentRowValue(cursor, "Utilidades", subtract(getCurrentRowValue(cursor, "Ingreso"), getCurrentRowValue(cursor, "Egreso")));
                            }
                        };
                        break;
                    case "Pedidos":
                        dt=new AdaptedDataTable(dataBase, s, masks[k]){
                            @Override
                            public void calculate(Cursor cursor){
                                setCurrentRowValue(cursor, "Total", getToTableTotal(cursor, "Ordenes", "Precio"));
                            }
                        };
                        break;
                    case "Ordenes":
                        dt=new AdaptedDataTable(dataBase, s, masks[k]){
                            @Override
                            public void calculate(Cursor cursor){
                                setCurrentRowValue(cursor, "Precio", multiply(getFromTable(cursor, "Id Producto", "Precio de Venta"), getCurrentRowValue(cursor, "Cantidad")));
                            }
                        };
                        break;                        
                }
                addDataTable(s, dt);
                k++;
            }
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public static void main(String[] args){
        try{
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e){
            Logger.getLogger(SimpleDBMS.class.getName()).log(Level.SEVERE, null, e);
        }
        AdaptedDBMS m=new AdaptedDBMS();
    }
}