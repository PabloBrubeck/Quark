package gui;

import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.Cursor;
import com.toedter.calendar.JDateChooser;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.sql.Date;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
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
        
        private InputField[] fields;
        private JButton recordBtn;
        private JButton importBtn;
        
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
            recordBtn=new JButton("Capturar");
            recordBtn.addActionListener(this);
            add(recordBtn);
            
            importBtn=new JButton("Importar");
            importBtn.addActionListener(this);
            add(importBtn);
        }
        
        @Override
        public void actionPerformed(ActionEvent ae){
            if(ae.getSource()==recordBtn){
                Object[] rowData=new Object[fields.length];
                for(int i=0; i<fields.length; i++){
                    rowData[i]=fields[i].getData();
                }
                try{
                    addRow(rowData);
                }catch(IOException e){
                    System.err.println(e);
                }
            }if(ae.getSource()==importBtn){
                JFileChooser fc=new JFileChooser();
                fc.showOpenDialog(this);
                try{
                    addFromCsv(fc.getSelectedFile());
                }catch(SQLException e){
                    System.err.println(e);
                }
            }
        }
    }
    
    private final Table dbTable;
    private final String[] names;
    private final DataType[] types;
    
    private JTable table;
    private DefaultTableModel dtm;
    private TableCellListener tcl; 
     
    public DataTable(Database db, String t) throws IOException{
        super();
        dbTable=db.getTable(t);
        int k=0, n=dbTable.getColumnCount();
        names=new String[n];
        types=new DataType[n];
        for(Column column : dbTable.getColumns()){
           names[k]=column.getName();
           types[k]=column.getType();
           k++;
        }
        initcomp();
    }
    private void initcomp() throws IOException{
        dtm=new DefaultTableModel(getRowData(), names);
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
    public void addRow(Object... rowData)throws IOException{
        dbTable.addRow(rowData);
        dtm.addRow(rowData);
    }
    public void addFromCsv(File csvFileToRead)throws SQLException{
        BufferedReader br=null;
        String line;
        try{
            br=new BufferedReader(new FileReader(csvFileToRead));
            Object[] rowData=new Object[types.length];
            while((line=br.readLine())!=null){
                String[] cell=line.split(",");
                for(int i=0; i<types.length; i++){
                    rowData[i]=parseAs(cell[i], types[i]);
                }
                addRow(rowData);
            }
        }catch(FileNotFoundException e){
            System.err.println(e);
        }catch(IOException e){
            System.err.println(e);
        }finally{
            if(br!=null){
                try{
                    br.close();
                }catch(IOException e){
                    System.err.println(e);
                }
            }
        }
    }
    
    public Object parseAs(String s, DataType type){
        switch(type){
            case INT:
                return Integer.parseInt(s);
            case FLOAT:
                return Float.parseFloat(s);
            case DOUBLE:
                return Double.parseDouble(s);
            case SHORT_DATE_TIME:
                return Date.valueOf(s);
            default:
                return s;
        }
    }
    public Object[][] getRowData()throws IOException{
        int r=dbTable.getRowCount();
        int c=dbTable.getColumnCount();
        Object[][] rowData=new Object[r][];
        int i=0;
        for(Row row: dbTable){
            rowData[i]=new Object[c];
            for(int j=0; j<c; j++){
                rowData[i][j]=row.get(names[j]);
            }
            i++;
        }
        return rowData;
    }
}