package gui;

import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.Cursor;
import static database.DatabaseUtil.*;

public class TableViewer extends JPanel{
    private JTable table;
    private final Table dbTable;
    private TableCellListener tcl;
    private Object selectedCell;
            
    public TableViewer(String t) throws IOException{
        super();
        Database db=DatabaseBuilder.open(new File("new.mdb"));
        dbTable=db.getTable(t);
        initcomp();
    }
    private void initcomp() throws IOException{
        table=new JTable(getRowData(dbTable), getColumnNames(dbTable));
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table));
        tcl=new TableCellListener(table, new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent ae){
                TableCellListener tcl = (TableCellListener)ae.getSource();
                String columnName = tcl.getTable().getColumnName(tcl.getColumn());
                try{
                    Cursor cursor = CursorBuilder.createCursor(dbTable);
                    if(cursor.findFirstRow(Collections.singletonMap(columnName, tcl.getOldValue()))){
                        cursor.setCurrentRowValue(dbTable.getColumn(columnName), tcl.getNewValue());
                    }
                }catch(IOException e){
                    System.err.println(ae);
                }
            }
        });
    }
}