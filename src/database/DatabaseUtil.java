package database;

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.util.*;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.util.*;

public class DatabaseUtil{
    public static ArrayList<Row> sortBy(Table dbTable, String col) throws IOException{
        ArrayList<Row> sorted=new ArrayList();
        for(Row row : CursorBuilder.createCursor(dbTable.getIndex(col))) {
            sorted.add(row);
        }
        return sorted;
    }
    
    public static void main(String[] args)throws IOException, SQLException{
        Database db = DatabaseBuilder.open(new File("new.mdb"));
        Table table=db.getTable("Empleados");
        new ImportUtil.Builder(db, "Imported2").setDelimiter(",").importFile(new File("my.csv"));
        db.close();
        
        /*
        Table clientes = new TableBuilder("Clientes").
                addColumn(new ColumnBuilder("Nombre").setSQLType(Types.VARCHAR)).
                addColumn(new ColumnBuilder("Empresa").setSQLType(Types.VARCHAR)).
                addColumn(new ColumnBuilder("Telefono").setSQLType(Types.VARCHAR)).
                addColumn(new ColumnBuilder("Direccion").setSQLType(Types.VARCHAR)).
                toTable(db);;
         
         clientes.addRow("Pablo Brubeck", "Quark", "8113118969", "Cda. Vicenzo 3708, Lomas del Paseo");
         clientes.addRow("Sebastian Rivera", "Quark", "8115855672", "La Fortaleza 105 Col. Fortin del Huajuco");
         clientes.addRow("Paco Treviño", "Quark", "8117093422", "Valle del Moscatel 208 Col. Valle del Contry");
        */
         
         
    }
}