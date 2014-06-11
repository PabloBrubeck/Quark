package gui;

import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.Cursor;
import com.toedter.calendar.JDateChooser;
import gui.MyComponent.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.logging.*;
import java.util.regex.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.*;
import javax.swing.tree.*;

public class DataTable extends JPanel{
    private class InputPanel extends JPanel{
        private class InputField extends JPanel{
            private JTextField tf;
            public InputField(String s){
                super(new FlowLayout());
                Border b=BorderFactory.createEmptyBorder();
                b=BorderFactory.createTitledBorder(b, s);
                setBorder(b);
                initcomp();
            }
            public void initcomp(){
                tf=new JTextField();
                tf.setPreferredSize(new Dimension(200, 26));
                add(tf);
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
            }
            @Override
            public void initcomp(){
                setDefaultLocale(Locale.getDefault());
                dc=new JDateChooser(new Date(System.currentTimeMillis()));
                dc.setDateFormatString(dateFormat);
                dc.setPreferredSize(new Dimension(200,28));
                add(dc);
            }
            @Override
            public String getText(){
                return dc.getDate().toString();
            }
            @Override
            public Date getData(){
                return dc.getDate();
            }
        }
        private class LinkInputField extends InputField{
            private JComboBox temp;
            public LinkInputField(String s, final Table t) {
                super(s);
                temp.addItem("Haga click para activar las opciones");
                temp.setPreferredSize(new Dimension(200,28));
                temp.addMouseListener(new MouseAdapter(){
                    @Override
                    public void mousePressed(MouseEvent me) {
                        remove(temp);
                        add(temp=globalMap.get(t).combo);
                        revalidate();
                        repaint();
                    }
                });
                add(temp);
            }
            @Override
            public void initcomp(){
                temp=new JComboBox();
            }
            @Override
            public String getText(){
                return temp.getSelectedItem().toString();
            }
            @Override
            public Object getData(){
                return temp.getSelectedIndex()+1;
            }

        }
        
        private InputField[] fields;
        private JButton recordBtn;
        private JButton importBtn;

        public InputPanel(){
            super(new BorderLayout(10, 10));
            initcomp();
        }
        private void initcomp(){
            fields=new InputField[names.length];
            JPanel grid=new JPanel(new GridLayout(fields.length,1,20,20));
            grid.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 20));
            for(int i=0; i<fields.length; i++){
                String columnName=names[i];
                switch(model.getColumnClass(i).getSimpleName()){
                    case "Integer":
                        fields[i]=new InputField(columnName){
                            @Override
                            public Integer getData(){
                                String s=getText();
                                return s.isEmpty()? 0: Integer.parseInt(s);
                            }
                        };
                        break;
                    case "Link":
                    case "Long":
                        fields[i]=new InputField(columnName){
                            @Override
                            public Long getData(){
                                String s=getText();
                                return s.isEmpty()? 0: Long.parseLong(s);
                            }
                        };
                        break;
                    case "Float":
                        fields[i]=new InputField(columnName){
                            @Override
                            public Float getData(){
                                String s=getText();
                                return s.isEmpty()? 0: Float.parseFloat(getText());
                            }
                        };
                        break;
                    case "Double":
                        fields[i]=new InputField(columnName){
                            @Override
                            public Double getData(){
                                String s=getText();
                                return s.isEmpty()? 0: Double.parseDouble(s);
                            }
                        };
                        break;
                    case "Date":
                        fields[i]=new DateInputField(columnName);
                        break;
                    default:
                        fields[i]=new InputField(columnName);
                        break;
                    }
                    grid.add(fields[i]);
                }
            recordBtn=new JButton(new MyAction("Capturar", null, new Caller("record", this)));
            importBtn=new JButton(new MyAction("Importar", null, new Caller("importFromCsv", this)));

            JPanel bot=new JPanel(new FlowLayout(FlowLayout.CENTER));
            bot.add(recordBtn);
            bot.add(importBtn);
            add(bot, "South");
            add(new JScrollPane(grid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "Center");
        }
        public void record(){
            Object[] rowData=new Object[fields.length];
            for(int i=0; i<fields.length; i++){
                rowData[i]=fields[i].getData();
            }
            addRow(rowData);
        }
        public void importFromCsv(){
            JFileChooser fc=new JFileChooser();
            fc.setFileFilter(new FileFilter(){
                @Override
                public boolean accept(File file){
                    return file.isDirectory() || file.getName().endsWith(".csv");
                }
                @Override
                public String getDescription() {
                    return "CSV (Comma delimited)";
                }
            });
            fc.showOpenDialog(this);
            addFromCsv(fc.getSelectedFile());
        }
    }
    private class TreePanel extends JPanel{
        private JTree tree;
        public TreePanel(){
            super();
            tree=new JTree();
            add(tree);
        }
        public void display(TreeNode node){
            remove(tree);
            add(tree=new JTree(node));
            revalidate();
            repaint();
        }
    }
    private class DateEditor extends AbstractCellEditor implements TableCellEditor{
        JDateChooser dc;
        public DateEditor(){
            dc=new JDateChooser();
            dc.setDateFormatString(dateFormat);
        }
        @Override
        public Object getCellEditorValue(){
            return dc.getDate();
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean bln, int i, int i1){
            dc.setDate((Date)value);
            return dc;
        }     
    }
    private class LinkEditor extends AbstractCellEditor implements TableCellEditor{
        private JComboBox<String> cb;
        public LinkEditor(){
            cb=new JComboBox();
        }
        @Override
        public Object getCellEditorValue(){
            return cb.getSelectedIndex()+1;
        }
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean bln, int row, int column){
            Table from=fromTables.get(names[column]);
            cb=globalMap.get(from).combo;
            cb.setSelectedIndex((Integer)value-1);
            return cb;
        }     
    }
    private class Link implements Comparable{
        @Override
        public int compareTo(Object t){
            return 0;
        }
    }
    
    private final int[] mask;
    private final String[] names;
    private final DataType[] types;
    private final String dateFormat="dd MMM yyyy";
    private final SimpleDateFormat sdf=new SimpleDateFormat(dateFormat);
    private final static Map<Table, DataTable> globalMap=new HashMap();
    
    private Table dbTable;
    private Map<String, Table> fromTables;
    private ArrayList<Table> toTables;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter sorter;
    private JComboBox<String> combo;
    private JTabbedPane tabbedPane;
    private TreePanel treePanel;
    private JPopupMenu cellPopup, headerPopup;
    private ArrayList<TableColumn> hiddenColumns;
    private int indexHeader;
    
    public DataTable(Database db, String t, int... ints){
        super(new BorderLayout(20, 20));
        mask=ints;
        dbTable=null;
        try{
            dbTable=db.getTable(t);
            findRelationships(db);
        } catch (IOException ex) {
            Logger.getLogger(DataTable.class.getName()).log(Level.SEVERE, null, ex);
        }
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
    private void initcomp(){
        globalMap.put(dbTable, this);
        
        combo=new JComboBox();
        combo.setEditable(false);
        updateCombo();
        
        hiddenColumns=new ArrayList();
        cellPopup=new JPopupMenu();
        cellPopup.add(new MyMenuItem("Eliminar registro", null, "deleteRow", this));
        headerPopup=new JPopupMenu();
        headerPopup.add(new MyMenuItem("Ocultar columna", null, "hideColumn", this));
        headerPopup.add(new MyMenuItem("Mostrar columnas ocultas", null, "unhideColumns", this));
        
        model=new DefaultTableModel(getRowData(), names){
            {
                addTableModelListener(new TableModelListener(){
                    @Override
                    public void tableChanged(TableModelEvent tme){
                        int row=tme.getFirstRow();
                        int col=tme.getColumn();
                        if(tme.getType()==TableModelEvent.UPDATE){
                            try{
                                String columnName=getColumnName(col);
                                //Assumes that the table has a primary key on the first column
                                Cursor cursor=CursorBuilder.createPrimaryKeyCursor(dbTable);
                                if(cursor.findFirstRow(Collections.singletonMap(names[0], getValueAt(row, 0)))){
                                    cursor.setCurrentRowValue(dbTable.getColumn(columnName), getValueAt(row, col));
                                }
                            }catch(IOException | IllegalArgumentException | ArrayIndexOutOfBoundsException e){
                                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                            }
                        }
                        calculate(row, col);
                        updateCombo();
                    }
                });
            }
            @Override
            public Class getColumnClass(int col){
                String name=getColumnName(col);
                if(fromTables.containsKey(name)){
                    return Link.class;
                }
                switch(types[col]){
                    case BOOLEAN:
                        return Boolean.class;
                    case INT:
                        return Integer.class;
                    case LONG:
                        return Long.class;
                    case FLOAT:
                        return Float.class;
                    case DOUBLE:
                        return Double.class;
                    case SHORT_DATE_TIME:
                        return Date.class;
                    default:
                        return String.class;
                }
            }
        };
        sorter=new TableRowSorter(model);
        table=new JTable(model){
            {
                setRowSorter(sorter);
                setFillsViewportHeight(true);
                setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
                addMouseListener(new MouseAdapter(){
                    @Override
                    public void mouseReleased(MouseEvent me) {
                        int r=rowAtPoint(me.getPoint());
                        if(r>=0 && r<getRowCount()){
                            setRowSelectionInterval(r, r);
                        }else{
                            clearSelection();
                        }
                        int rowindex=getSelectedRow();
                        if(rowindex<0){
                            return;
                        }if(me.isPopupTrigger() && me.getComponent() instanceof JTable){
                            cellPopup.show(me.getComponent(), me.getX(), me.getY());
                        }
                        treePanel.display(getDescendants(convertRowIndexToModel(rowindex)+1));
                    }
                });
                getTableHeader().addMouseListener(new MouseAdapter(){
                    @Override
                    public void mouseReleased(MouseEvent me) {
                        indexHeader=columnAtPoint(me.getPoint());
                        if(indexHeader>=0 && me.getButton()==MouseEvent.BUTTON3){
                            headerPopup.show(me.getComponent(), me.getX(), me.getY());
                        }
                    };
                });
                setDefaultEditor(Date.class, new DateEditor());
                setDefaultEditor(Link.class, new LinkEditor());
                setDefaultRenderer(Date.class, new DefaultTableCellRenderer(){
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                        return super.getTableCellRendererComponent(table, sdf.format(value), isSelected, hasFocus, row, column);
                    }
                });
                setDefaultRenderer(Link.class, new DefaultTableCellRenderer(){
                    @Override
                    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column){
                        Table from=fromTables.get(getColumnName(column));
                        JComboBox temp=globalMap.get(from).combo;
                        if(temp!=null){
                            value=temp.getItemAt((Integer)value-1);
                        }
                        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    }
                });
            }
            @Override
            public String getToolTipText(MouseEvent me){
                Point p=me.getPoint();
                int r=rowAtPoint(p), c=columnAtPoint(p);
                if(r>=0 && r<getRowCount() && c>=0 && c<getColumnCount()){
                    return getValueAt(r, c).toString();
                }
                return super.getToolTipText(me);
            }
        };
        
        treePanel=new TreePanel();
        tabbedPane=new JTabbedPane();
        tabbedPane.add("Capturar", new InputPanel());
        tabbedPane.add("Ver", treePanel);
        
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                        JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED), "Center");
        add(tabbedPane, "East");
    }
    private void findRelationships(Database db) throws IOException{
        toTables=new ArrayList();
        fromTables=new HashMap();
        for(Relationship rel: db.getRelationships(dbTable)){
            Table from=rel.getFromTable();
            if(from==dbTable){
                toTables.add(rel.getToTable());
            }else{
                for(Column c: rel.getToColumns()){
                    fromTables.put(c.getName(), from);
                }
            }
        }
    }
    
    public Table getTable(){
        return dbTable;
    }
    public Map<String, Table> getFromTables(){
        return fromTables;
    }
    public ArrayList<Table> getToTables(){
        return toTables;
    }
    public JTable getJTable(){
        return table;
    }
    public DefaultTableModel getModel(){
        return model;
    }
    public TableRowSorter getSorter(){
        return sorter;
    }
    public JComboBox<String> getCombo(){
        return combo;
    }
    
    public void deleteRow(){
        int i=JOptionPane.showConfirmDialog(null, "¿Está seguro de que quiere borrar este registro?");
        if(i!=JOptionPane.YES_OPTION){
            return;
        }
        int row=table.convertRowIndexToModel(table.getSelectedRow());
        try{
            dbTable.deleteRow(CursorBuilder.findRowByPrimaryKey(dbTable, model.getValueAt(row, 0)));
            model.removeRow(row);
        }catch(IOException | IllegalArgumentException | ArrayIndexOutOfBoundsException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public void addRow(Object... rowData){
        try {
            dbTable.addRow(rowData);
            model.addRow(rowData);
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public void addFromCsv(File csvFileToRead){
        try{
            String line;
            try(BufferedReader br = new BufferedReader(new FileReader(csvFileToRead))){
                Object[] rowData=new Object[types.length];
                while((line=br.readLine())!=null){
                    String[] cell=line.split(",");
                    for(int i=0; i<types.length; i++){
                        rowData[i]=parseType(cell[i], types[i]);
                    }
                    addRow(rowData);
                }
            }
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public void hideColumn(){
        TableColumnModel tcm = table.getColumnModel();
        TableColumn column=tcm.getColumn(indexHeader);
        hiddenColumns.add(column);
        tcm.removeColumn(column);
    }
    public void unhideColumns(){
        TableColumnModel tcm = table.getColumnModel();
        for(TableColumn column: hiddenColumns){
            tcm.addColumn(column);
        }
        hiddenColumns.clear();
    }
    public void search(String s){
        javax.swing.RowFilter<DefaultTableModel, Object> rf;
        try{
            rf=javax.swing.RowFilter.regexFilter(s);
        }catch(PatternSyntaxException e){
            return;
        }
        sorter.setRowFilter(rf);
    }
    public void updateCombo(){
        combo.removeAllItems();
        for(Row row: dbTable){
            combo.addItem(rowToString(row));
        }
    }
    public void refresh(){
        model.setDataVector(getRowData(), names);
    }
    
    public int getRowCount(){
        return dbTable.getRowCount();
    }
    public Object parseType(String s, DataType type){
        switch(type){
            case BOOLEAN:
                return Boolean.parseBoolean(s);
            case INT:
                return Integer.parseInt(s);
            case FLOAT:
                return Float.parseFloat(s);
            case DOUBLE:
                return Double.parseDouble(s);
            case SHORT_DATE_TIME:
                return Date.parse(s);
            default:
                return s;
        }
    }
    public Object[][] getRowData(){
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
    public String getColumnName(int i){
        return names[i];
    }
    public String rowToString(Row row){
        if(mask==null? true: mask.length==0){
            return row.toString();
        }else{
            String s="";
            for(int i: mask){
                String m=names[Math.abs(i)];
                Object value=row.get(m);
                if(value instanceof Date){
                    value=sdf.format(value);
                }
                if(i>=0){
                    s+=" "+value;
                }else{
                    s+=" "+m+"="+value;
                }
            }
            return s.substring(1);
        }
    }
    
    public DefaultMutableTreeNode getDescendants(Object primaryKey){
        DefaultMutableTreeNode node=null;
        try{
            node=new DefaultMutableTreeNode(rowToString(CursorBuilder.findRowByPrimaryKey(dbTable, primaryKey)));
            for(Table t: toTables){
                node.add(globalMap.get(t).getDescendants(primaryKey, dbTable));
            }
        }catch(IOException e){
            Logger.getLogger(DataTable.class.getName()).log(Level.SEVERE, null, e);
        }
        return node;
    }
    public DefaultMutableTreeNode getDescendants(Object foreignKey, Table fromTable) throws IOException{
        DefaultMutableTreeNode folder=new DefaultMutableTreeNode(dbTable.getName());
        IndexCursor cursor=CursorBuilder.createCursor(dbTable.getForeignKeyIndex(fromTable));
        for(Row row : cursor.newEntryIterable(foreignKey)){
            DefaultMutableTreeNode node=new DefaultMutableTreeNode(rowToString(row));
            for(Table t: toTables){
                node.add(globalMap.get(t).getDescendants(row.get(names[0]), dbTable));
            }
            folder.add(node);
        }
        return folder;
    }

    public void calculate(int row, int col){
        
    }
    
    public static ArrayList<Row> retrieve(Table t, String columnName, Comparable value, int comp){
        ArrayList<Row> list=new ArrayList();
        for(Row row: t){
            Object temp=row.get(columnName);
            int m=value.compareTo(temp);
            boolean b=false;
            switch(m){
                case -2:
                    b=(m<=0);
                    break;
                case -1:
                    b=(m<0);
                    break;
                case 0:
                    b=(m==0);
                    break;
                case 1:
                    b=(m>0);
                    break;
                case 2:
                    b=(m>=0);
                    break;
            }if(b){
                list.add(row);
            }
        }
        return list;
    }
}