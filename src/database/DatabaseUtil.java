package database;

import java.io.*;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.Cursor.Savepoint;
import com.healthmarketscience.jackcess.Cursor;
import java.sql.*;
import java.sql.Date;
import java.util.*;

public class DatabaseUtil{
    
    public static Object[][] getRowData(Table t) throws IOException{
        int r=t.getRowCount();
        int c=t.getColumnCount();
        String[] field=getColumnNames(t);
        Object[][] data=new Object[r][];
        for(int i=0; i<r; i++){
            data[i]=new Object[c];
            Row row=t.getNextRow();
            for(int j=0; j<c; j++){
                data[i][j]=row.get(field[j]);
            }
        }
        return data;
    }
    public static String[] getColumnNames(Table t){
        String[] field=new String[t.getColumnCount()];
        int k=0;
        for(Column column : t.getColumns()){
           field[k++]=column.getName();
        }
        return field;
    }
    public static String getColumnName(Table t, int i){
        int k=0;
        for(Column column : t.getColumns()){
            if(k==i){
                return column.getName();
            }else{
                k++;
            }
        }
        return null;
    }
    
    public static void main(String[] args)throws IOException, SQLException{
        Database db = DatabaseBuilder.create(Database.FileFormat.V2000, new File("new.mdb"));
        
        Table empleados = new TableBuilder("Empleados").
                addColumn(new ColumnBuilder("Nombre").setSQLType(Types.VARCHAR)).
                addColumn(new ColumnBuilder("Telefono").setSQLType(Types.VARCHAR)).
                addColumn(new ColumnBuilder("Cumpleaños").setSQLType(Types.DATE)).
                toTable(db);
        
        Table clientes = new TableBuilder("Clientes").
                addColumn(new ColumnBuilder("Nombre").setSQLType(Types.VARCHAR)).
                addColumn(new ColumnBuilder("Empresa").setSQLType(Types.VARCHAR)).
                addColumn(new ColumnBuilder("Telefono").setSQLType(Types.VARCHAR)).
                addColumn(new ColumnBuilder("Direccion").setSQLType(Types.VARCHAR)).
                toTable(db);
        
        Table productos = new TableBuilder("Catalogo").
                addColumn(new ColumnBuilder("Nombre").setSQLType(Types.VARCHAR)).
                addColumn(new ColumnBuilder("Existencia").setSQLType(Types.INTEGER)).
                addColumn(new ColumnBuilder("Descripcion").setSQLType(Types.VARCHAR)).
                addColumn(new ColumnBuilder("Costo").setSQLType(Types.FLOAT)).
                addColumn(new ColumnBuilder("Precio").setSQLType(Types.FLOAT)).
                toTable(db);
        
        Table ingresos = new TableBuilder("Ventas").
                addColumn(new ColumnBuilder("Cantidad").setSQLType(Types.FLOAT)).
                addColumn(new ColumnBuilder("Descripcion").setSQLType(Types.VARCHAR)).
                addColumn(new ColumnBuilder("Fecha").setSQLType(Types.DATE)).
                toTable(db);
         
         clientes.addRow("Pablo Brubeck", "Quark", "8113118969", "Cda. Vicenzo 3708, Lomas del Paseo");
         clientes.addRow("Sebastian Rivera", "Quark", "8115855672", "La Fortaleza 105 Col. Fortin del Huajuco");
         clientes.addRow("Paco Treviño", "Quark", "8117093422", "Valle del Moscatel 208 Col. Valle del Contry");
        
         productos.addRow("Manzana", 10, "Roja", 10f, 12f);
         productos.addRow("Peras", 10, "Verdes", 5f, 6f);
         productos.addRow("Platanos", 10, "Amarillos", 20f, 30f);
        
         ingresos.addRow(100.50f,"Compra de Software 1", new Date(114, 0, 31));
         ingresos.addRow(230f,"Compra de Software 2", new Date(8099, 11, 29));
         ingresos.addRow(232f,"Compra de Software 3", new Date(System.currentTimeMillis()));
         
         empleados.addRow("Pablo Brubeck", "8113118969", new Date(95, 8, 3));
         empleados.addRow("Sebastian Rivera", "8115855672", new Date(96, 5, 26));
         empleados.addRow("Paco Treviño", "8117093422", new Date(95, 9, 6));
         
    }
}