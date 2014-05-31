package gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.sql.Date;
import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.Cursor;
import com.toedter.calendar.*;
import static database.DatabaseUtil.*;
import javax.swing.border.Border;

public class TableViewer extends JPanel{
    
    private class InputPanel extends JPanel implements ActionListener, FocusListener, KeyListener{
        private abstract class InputField extends JPanel{
            public InputField(String s){
                super(new BorderLayout());
                Border b=BorderFactory.createEmptyBorder(24, 8, 8, 8);
                b=BorderFactory.createTitledBorder(b, s, 1, 0, font);
                setBorder(b);
            }
            protected JTextField tf;
            void initcomp(){
                tf=new JTextField();
                tf.setFont(font);
                tf.setPreferredSize(new Dimension(200,30));
                add(tf,"North");
            }
            public abstract Object getData();
        }
        private class TextInputField extends InputField{
            public TextInputField(String s){
                super(s);
                initcomp();
            }
            @Override
            public String getData(){
                return tf.getText();
            }
        }
        private class DateInputField extends InputField{
            private final JDateChooser dc;
            public DateInputField(String s){
                super(s);
                dc=new JDateChooser();
                dc.setLocale(Locale.forLanguageTag("es_MX"));
                add(dc, "North");
            }
            @Override
            public Date getData(){
                return new Date(dc.getDate().getTime());
            }
        }
        private class DecimalInputField extends InputField{
            public DecimalInputField(String s){
                super(s);
                initcomp();
            }
            @Override
            public Float getData(){
                return Float.parseFloat(tf.getText());
            }
        }
        private class IntegerInputField extends InputField{
            public IntegerInputField(String s){
                super(s);
                initcomp();
            }
            @Override
            public Integer getData(){
                return Integer.parseInt(tf.getText());
            }
        }
        
        InputField[] fields;
        JButton button;
        int focus=0;
        
        public InputPanel(){
            super(new GridLayout(0,1));
            initcomp();
        }
        private void initcomp(){
            fields=new InputField[dbTable.getColumnCount()];
            int k=0;
            for(Column col: dbTable.getColumns()){
                switch(col.getType()){
                    default:
                    case TEXT:
                        fields[k]=new TextInputField(col.getName());
                        break;
                    case SHORT_DATE_TIME:
                        fields[k]=new DateInputField(col.getName());
                        break;
                    case FLOAT:
                    case DOUBLE:
                        fields[k]=new DecimalInputField(col.getName());
                        break;
                    case INT:
                    case LONG:
                        fields[k]=new IntegerInputField(col.getName());
                        break;
                }
                fields[k].setFocusable(true);
                fields[k].addKeyListener(this);
                fields[k].addFocusListener(this);
                add(fields[k]);
                k++;
            }
            button=new JButton("Capturar");
            button.setFont(new Font("arial.ttf",0,12));
            button.setPreferredSize(new Dimension(100, 40));
            button.addKeyListener(this);
            button.addFocusListener(this);
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
                }catch(IOException e){
                    System.err.println(e);
                }
            }
        }
        @Override
        public void focusGained(FocusEvent fe) {
            int k=0;
            while(k<fields.length){
                if(fe.getSource()==fields[k]){
                    focus=k; 
                }else{
                    k++;
                }
            }
        }
        @Override
        public void focusLost(FocusEvent fe) {
        }
        @Override
        public void keyTyped(KeyEvent ke) {
        }
        @Override
        public void keyPressed(KeyEvent ke) {
            switch(ke.getKeyCode()){
                case KeyEvent.VK_UP:
                    break;
                case KeyEvent.VK_DOWN:
                    break;
            }
        }
        @Override
        public void keyReleased(KeyEvent ke) {
        }
    }
    
    private Font font;
    private JTable table;
    private final Table dbTable;
    private TableCellListener tcl; 
            
    public TableViewer(String t) throws IOException{
        super();
        font=new Font("arial.ttf",0,12);
        Database db=DatabaseBuilder.open(new File("new.mdb"));
        dbTable=db.getTable(t);
        initcomp();
    }
    private void initcomp() throws IOException{
        table=new JTable(getRowData(dbTable), getColumnNames(dbTable));
        table.setFillsViewportHeight(true);
        add(new JScrollPane(table), "West");
        add(new InputPanel(), "East");
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