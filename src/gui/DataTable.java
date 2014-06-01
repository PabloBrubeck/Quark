package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.Date;
import java.util.*;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.Border;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.Cursor;
import com.toedter.calendar.*;
import static database.DatabaseUtil.*;

public class DataTable extends JPanel{
    private class InputPanel extends JPanel implements ActionListener{
        private class InputField extends JPanel{
            private JTextField tf;
            public InputField(String s){
                super(new BorderLayout());
                Border b=BorderFactory.createEmptyBorder(24, 8, 8, 8);
                b=BorderFactory.createTitledBorder(b, s, 1, 0, font);
                setBorder(b);
                initcomp();
            }
            private void initcomp(){
                tf=new JTextField();
                tf.setFont(font);
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
                dc.setFont(font);
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
            button.setFont(new Font("arial.ttf",0,12));
            button.setPreferredSize(new Dimension(100, 40));
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
    private final Font font;
    private final Table dbTable;
    private DefaultTableModel dtm;
    private TableCellListener tcl; 
            
    public DataTable(String t) throws IOException{
        super();
        font=new Font("arial.ttf",0,12);
        Database db=DatabaseBuilder.open(MainApplication.file);
        dbTable=db.getTable(t);
        initcomp();
    }
    private void initcomp() throws IOException{
        dtm=new DefaultTableModel(getRowData(dbTable), getColumnNames(dbTable));
        table=new JTable(dtm);
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), "West");
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
    
}