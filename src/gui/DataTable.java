package gui;

import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.Cursor;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.Date;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.table.*;

public class DataTable extends JPanel{
    private class InputPanel extends JPanel implements ActionListener{
        private class InputField extends JPanel{
            private JTextField tf;
            public InputField(String s){
                super(new BorderLayout());
                Border b=BorderFactory.createEmptyBorder(24, 8, 8, 8);
                b=BorderFactory.createTitledBorder(b, s, 1, 0);
                setBorder(b);
                initcomp();
            }
            private void initcomp(){
                tf=new JTextField();
                tf.setPreferredSize(new Dimension(200,30));
                this.add(tf, "North");
            }
            public String getText(){
                return tf.getText();
            }
            public Object getData(){
                return tf.getText();
            }
        }
        private class DateInputField extends InputField{
            private JDateChooser dc;
            public DateInputField(String s){
                super(s);
                initcomp();
            }
            private void initcomp(){
                dc=new JDateChooser(new Date(System.currentTimeMillis()));
                dc.setPreferredSize(new Dimension(200,30));
                dc.setLocale(Locale.forLanguageTag("es_MX"));
                this.add(dc, "North");
            }
            @Override
            public String getText(){
                return dc.getDate().toString();
            }
            @Override
            public Date getData(){
                return new Date(dc.getDate().getTime());
            }
        }
        
        InputField[] fields;
        JButton button;
        
        public InputPanel(){
            super(new GridLayout(0,1));
            initcomp();
        }
        private void initcomp(){
            fields=new InputField[dbTable.getColumnCount()];
            int k=0;
            for(Column col: dbTable.getColumns()){
                switch(col.getType()){                        
                    case INT:
                        fields[k]=new InputField(col.getName()){
                            @Override
                            public Integer getData(){
                                return Integer.parseInt(getText());
                            }
                        };
                        break;
                    case LONG:
                        fields[k]=new InputField(col.getName()){
                            @Override
                            public Long getData(){
                                return Long.parseLong(getText());
                            }
                        };
                        break;
                    case FLOAT:
                        fields[k]=new InputField(col.getName()){
                            @Override
                            public Float getData(){
                                return Float.parseFloat(getText());
                            }
                        };
                        break;
                    case DOUBLE:
                        fields[k]=new InputField(col.getName()){
                            @Override
                            public Double getData(){
                                return Double.parseDouble(getText());
                            }
                        };
                        break;
                    case SHORT_DATE_TIME:
                        fields[k]=new DateInputField(col.getName());
                        break;
                    default:
                        fields[k]=new InputField(col.getName());
                        break;
                }
                add(fields[k]);
                k++;
            }
            button=new JButton("Capturar");
            button.addActionListener(this);
            add(button);
        }
        
        @Override
        public void actionPerformed(ActionEvent ae){
            if(ae.getSource()==button){
                Object[] rowData=new Object[fields.length];
                for(int i=0; i<fields.length; i++){
                    rowData[i]=fields[i].getData();
                }
                try{
                    dbTable.addRow(rowData);
                    dtm.addRow(rowData);
                }catch(IOException e){
                    System.err.println(e);
                }
            }
        }
    }
    
    private JTable table;
    private final Table dbTable;
    private DefaultTableModel dtm;
    private TableCellListener tcl; 
            
    public DataTable(Database db, String t) throws IOException{
        super();
        dbTable=db.getTable(t);
        initcomp();
    }
    private void initcomp() throws IOException{
        dtm=new DefaultTableModel(getRowData(), getColumnNames());
        table=new JTable(dtm){{
            getTableHeader().addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent mouseEvent) {
                    int index=convertColumnIndexToModel(columnAtPoint(mouseEvent.getPoint()));
                    if(index>=0){
                        sortBy(index);
                    }
                };
            });
        }};
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), "Center");
        add(new InputPanel(), "East");
        
        tcl=new TableCellListener(table, new AbstractAction(){
            @Override
            public void actionPerformed(ActionEvent ae){
                TableCellListener tcl=(TableCellListener)ae.getSource();
                String columnName=tcl.getTable().getColumnName(tcl.getColumn());
                try{
                    Cursor cursor=CursorBuilder.createCursor(dbTable);
                    if(cursor.findFirstRow(Collections.singletonMap(columnName, tcl.getOldValue()))){
                        cursor.setCurrentRowValue(dbTable.getColumn(columnName), tcl.getNewValue());
                    }
                }catch(IOException e){
                    System.err.println(ae);
                }
            }
        });
    }
    
    public void sortBy(int col){
        
    }
    public Object[][] getRowData() throws IOException{
        int r=dbTable.getRowCount();
        int c=dbTable.getColumnCount();
        String[] field=getColumnNames();
        Object[][] rowData=new Object[r][];
        int i=0;
        for(Row row: dbTable){
            rowData[i]=new Object[c];
            for(int j=0; j<c; j++){
                rowData[i][j]=row.get(field[j]);
            }
            i++;
        }
        return rowData;
    }
    public String[] getColumnNames(){
        String[] field=new String[dbTable.getColumnCount()];
        int k=0;
        for(Column column : dbTable.getColumns()){
           field[k++]=column.getName();
        }
        return field;
    }
}