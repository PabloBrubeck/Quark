package gui;

import com.healthmarketscience.jackcess.*;
import com.healthmarketscience.jackcess.impl.*;
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
            public boolean locked=false;
            public InputField(String s){
                super(new FlowLayout());
                Border b=BorderFactory.createEmptyBorder();
                b=BorderFactory.createTitledBorder(b, s);
                setBorder(b);
                setName(s);
                initcomp();
            }
            public void initcomp(){
                tf=new JTextField();
                tf.setPreferredSize(new Dimension(200, 26));
                add(tf);
            }
            public void clear(){
                tf.setText("");
            }
            public void lock(Object value){
                locked=true;
                tf.setText(value.toString());
                tf.setEnabled(false);
            }
            public void unlock(){
                locked=false;
                tf.setText("");
                tf.setEnabled(true);
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
            public void clear(){
                dc.setDate(new Date());
            }
            @Override
            public void lock(Object value){
                locked=true;
                dc.setDate((Date)value);
                dc.setEnabled(false);
            }
            @Override
            public void unlock(){
                locked=false;
                dc.setDate(new Date());
                dc.setEnabled(true);
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
            private JComboBox cb;
            private final Table from;
            public LinkInputField(String s) {
                super(s);
                from=fromTables.get(s);
                cb.addItem(s);
                cb.addMouseListener(new MouseAdapter(){
                    @Override
                    public void mouseEntered(MouseEvent me) {
                        update();
                    }
                });
            }
            private void update(){
                int index=cb.getSelectedIndex();
                cb.removeAllItems();
                ComboBoxModel m=globalMap.get(from).getCombo().getModel();
                for(int i=0; i<m.getSize(); i++){
                    cb.addItem(m.getElementAt(i));
                }
                cb.setSelectedIndex(index);
            }
            @Override
            public void initcomp(){
                cb=new JComboBox();
                cb.setPreferredSize(new Dimension(200, 26));
                add(cb);
            }
            @Override
            public void clear(){
                cb.setSelectedIndex(0);
            }
            @Override
            public void lock(Object value){
                locked=true;
                update();
                cb.setSelectedIndex((Integer)value);
                cb.setEnabled(false);
            }
            @Override
            public void unlock(){
                locked=false;
                cb.setSelectedIndex(0);
                cb.setEnabled(true);
            }
            @Override
            public String getText(){
                return cb.getSelectedItem().toString();
            }
            @Override
            public Object getData(){
                return cb.getSelectedIndex()+1;
            }
        }
        
        private InputField[] fields;
        private JButton recordBtn;
        private JButton importBtn;
        private JButton unlockBtn;
        
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
                        fields[i]=new LinkInputField(columnName);
                        break;
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
            unlockBtn=new JButton(new MyAction("Listo", null, new Caller("unlockFields", this)));
            unlockBtn.setVisible(false);
            
            JPanel bot=new JPanel(new FlowLayout(FlowLayout.CENTER));
            bot.add(recordBtn);
            bot.add(importBtn);
            bot.add(unlockBtn);
            add(bot, "South");
            add(new JScrollPane(grid, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "Center");
        }
        public void clearFields(){
            for(InputField f: fields){
                if(!f.locked){
                    f.clear();
                }
            }
        }
        public void lockField(String name, Object value){
            int k=0;
            while(k<fields.length){
                if(name.equals(fields[k].getName())){
                    fields[k].lock(value);
                    unlockBtn.setVisible(true);
                    importBtn.setVisible(false);
                    k+=fields.length;
                }
                k++;
            }
        }
        public void unlockFields(){
            for(InputField f: fields){
                f.unlock();
            }
            unlockBtn.setVisible(false);
            importBtn.setVisible(true);
        }
        public void record(){
            Object[] rowData=new Object[fields.length];
            for(int i=0; i<fields.length; i++){
                rowData[i]=fields[i].getData();
            }
            addRow(rowData);
            Object key=getLastRow().get(names[0]);
            for(Table t: toTables){
                if(JOptionPane.showConfirmDialog(null, "¿Desea agregar "+t.getName()+"?")==JOptionPane.YES_OPTION){
                    sendRequest(t, (Integer)key-1);
                }
            }
            clearFields();
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
        private final JTree tree;
        public TreePanel(){
            super(new BorderLayout());
            tree=new JTree(new DefaultTreeModel(null));
            tree.setPreferredSize(new Dimension(200,300));
            tree.setCellRenderer(new IconNodeRenderer());
            JButton expand=new JButton(new AbstractAction("Expandir"){
                @Override
                public void actionPerformed(ActionEvent ae){
                    for (int i=0; i<tree.getRowCount(); i++) {
                        tree.expandRow(i);
                    }
                }
            });
            add(new JScrollPane(tree),"Center");
            add(expand,"South");
        }
        public void display(TreeNode node){
            tree.setModel(new DefaultTreeModel(node));
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
            cb=globalMap.get(from).getCombo();
            cb.setSelectedIndex((Integer)value-1);
            return cb;
        }     
    }
    private class Link{}
    
    private final int[] mask;
    private final String[] names;
    private final DataType[] types;
    private final static String dateFormat="dd MMM yyyy";
    private final static SimpleDateFormat sdf=new SimpleDateFormat(dateFormat);
    private final static HashMap<Table, DataTable> globalMap=new HashMap();
    
    private Table dbTable;
    private HashMap<String, Table> fromTables;
    private ArrayList<Table> toTables;
    private JTable table;
    private DefaultTableModel model;
    private TableRowSorter sorter;
    private JComboBox<String> combo;
    private JTabbedPane tabbedPane;
    private TreePanel treePanel;
    private InputPanel inputPanel;
    private JPopupMenu cellPopup, headerPopup;
    private ArrayList<TableColumn> hiddenColumns;
    private int indexHeader;
    private boolean autoUpdate=false;
    
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
        
        hiddenColumns=new ArrayList();
        headerPopup=new JPopupMenu();
        headerPopup.add(new MyMenuItem("Ocultar columna", null, "hideColumn", this));
        headerPopup.add(new MyMenuItem("Mostrar columnas ocultas", null, "unhideColumns", this));
        cellPopup=new JPopupMenu();
        cellPopup.add(new MyMenuItem("Recalcular", null, "calculate", this));
        for(Table t: toTables){
            cellPopup.add(new MyMenuItem("Agregar "+t.getName(), null, "sendRequest", this, t));
        }
        cellPopup.add(new MyMenuItem("Eliminar registro", null, "deleteRow", this));
        
        model=new DefaultTableModel(getRowData(), names){
            {
                addTableModelListener(new TableModelListener(){
                    @Override
                    public void tableChanged(TableModelEvent tme){
                        if(!autoUpdate){
                            int row=tme.getFirstRow();
                            int col=tme.getColumn();
                            try{
                                IndexCursor cursor=CursorBuilder.createPrimaryKeyCursor(dbTable);
                                switch(tme.getType()){
                                    case TableModelEvent.UPDATE:
                                        //Assumes that the table has a primary key on the first column
                                        if(cursor.findFirstRow(Collections.singletonMap(names[0], getValueAt(row, 0)))){
                                            cursor.setCurrentRowValue(dbTable.getColumn(names[col]), getValueAt(row, col));
                                            calculate(cursor);
                                            updateRow(cursor, row);
                                        }
                                        break;
                                    case TableModelEvent.INSERT:
                                        cursor.afterLast();
                                        cursor.getPreviousRow();
                                        calculate(cursor);
                                        updateRow(cursor, row);
                                        break;
                                }
                                updateCombo();
                            }catch(IOException | IllegalArgumentException | ArrayIndexOutOfBoundsException e){
                                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                            }
                        }
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
                        JComboBox temp=globalMap.get(from).getCombo();
                        if(temp!=null){
                            value=temp.getItemAt((Integer)value-1);
                        }
                        return super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                    }
                });
                
                addMouseListener(new MouseAdapter(){
                    @Override
                    public void mouseReleased(MouseEvent me){
                        int row=rowAtPoint(me.getPoint());
                        if(me.isPopupTrigger() && row>=0){
                            clearSelection();
                            addRowSelectionInterval(row, row);
                            cellPopup.show(me.getComponent(), me.getX(), me.getY());
                        }
                    }
                });
                getTableHeader().addMouseListener(new MouseAdapter(){
                    @Override
                    public void mouseReleased(MouseEvent me) {
                        indexHeader=columnAtPoint(me.getPoint());
                        if(me.isPopupTrigger() && indexHeader>=0){
                            headerPopup.show(me.getComponent(), me.getX(), me.getY());
                        }
                    };
                });
                getSelectionModel().addListSelectionListener(new ListSelectionListener(){
                    @Override
                    public void valueChanged(ListSelectionEvent lse){
                        int row=getSelectedRow();
                        if(row>=0){
                            treePanel.display(getDescendants(model.getValueAt(convertRowIndexToModel(row), 0)));
                        }
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
        inputPanel=new InputPanel();
        tabbedPane=new JTabbedPane();
        tabbedPane.add("Capturar", inputPanel);
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
    private void updateRow(Cursor cursor, int rowIndex){
        autoUpdate=true;
        for(int i=0; i<model.getColumnCount(); i++){
            model.setValueAt(getCurrentRowValue(cursor, names[i]), rowIndex, i);
        }
        autoUpdate=false;
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
        if(combo.getItemCount()==0){
            updateCombo();
        }
        return combo;
    }
    
    public int getRowCount(){
        return dbTable.getRowCount();
    }
    public Integer getSelectedPrimaryKey(){
        return (Integer)model.getValueAt(table.convertRowIndexToModel(table.getSelectedRow()), 0);
    }
    public String getColumnName(int i){
        return names[i];
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
    public Row getLastRow(){
        Row row=null;
        try {
            IndexCursor cursor=CursorBuilder.createPrimaryKeyCursor(dbTable);
            cursor.afterLast();
            row=cursor.getPreviousRow();
        } catch (IOException e){
            Logger.getLogger(DataTable.class.getName()).log(Level.SEVERE, null, e);
        }
        return row;
    }
    public DefaultMutableTreeNode getDescendants(Object primaryKey){
        IconNode node=null;
        try{
            node=new IconNode(rowToString(CursorBuilder.findRowByPrimaryKey(dbTable, primaryKey)));
            node.setIconName(dbTable.getName());
            for(Table t: toTables){
                node.add(globalMap.get(t).getDescendants(primaryKey, dbTable));
            }
        }catch(IOException e){
            Logger.getLogger(DataTable.class.getName()).log(Level.SEVERE, null, e);
        }
        return node;
    }
    public DefaultMutableTreeNode getDescendants(Object foreignKey, Table fromTable) throws IOException{
        IconNode folder=new IconNode(dbTable.getName());
        IndexCursor cursor=CursorBuilder.createCursor(dbTable.getForeignKeyIndex(fromTable));
        for(Row row : cursor.newEntryIterable(foreignKey)){
            IconNode node=new IconNode(rowToString(row));
            node.setIconName(dbTable.getName());
            for(Table t: toTables){
                node.add(globalMap.get(t).getDescendants(row.get(names[0]), dbTable));
            }
            folder.add(node);
        }
        return folder;
    }
    
    public void calculate(){               
        try{
            IndexCursor cursor = CursorBuilder.createPrimaryKeyCursor(dbTable);
            if(cursor.findFirstRow(Collections.singletonMap(names[0], getSelectedPrimaryKey()))){
                calculate(cursor);
                updateRow(cursor, table.convertRowIndexToModel(table.getSelectedRow()));
            }
        }catch(IOException e){
            Logger.getLogger(DataTable.class.getName()).log(Level.SEVERE, null, e);
        }
           
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
    
    //Dependency adquisition
    public void requestFrom(Table from, Integer key){
        goTo(this);
        for(Index.Column col: dbTable.getForeignKeyIndex(from).getColumns()){
            inputPanel.lockField(col.getColumn().getName(), key-1);
        }
    }
    public void sendRequest(Table to, Integer key){
        DataTable dt=globalMap.get(to);
        dt.requestFrom(dbTable, key);
    }
    public void sendRequest(TableImpl to){
        DataTable dt=globalMap.get(to);
        dt.requestFrom(dbTable, getSelectedPrimaryKey());
    }
    
    //Empty methods, should override
    public void calculate(Cursor cursor) throws IOException{
        
    }
    public void goTo(DataTable dt){
        
    }
   
    //Formula operators
    public Object getCurrentRowValue(Cursor cursor, String columnName){
        try{
             return cursor.getCurrentRowValue(dbTable.getColumn(columnName));
        }catch(IOException ex){
            Logger.getLogger(DataTable.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    public Object getCurrentRowTotal(Cursor cursor, String... columnNames){
        Object[] array=new Object[columnNames.length];
        for(int i=0; i<columnNames.length; i++){
            array[i]=getCurrentRowValue(cursor, columnNames[i]);
        }
       return add(array);
    }
    public Object getFromTable(Cursor cursor, String fkColumn, String getColumn){
         try {
            Row r=CursorBuilder.findRowByPrimaryKey(fromTables.get(fkColumn), getCurrentRowValue(cursor, fkColumn));
            return r.get(getColumn);
         } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
         }
         return null;
    }
    public Object getToTableTotal(Cursor cursor, String toTableName, String getColumn){
        Object value=null;
        try{  
            Table toTable=dbTable.getDatabase().getTable(toTableName);
            IndexCursor foreign=CursorBuilder.createCursor(toTable.getForeignKeyIndex(dbTable));
            for(Row row: foreign.newEntryIterable(getCurrentRowValue(cursor, names[0]))){
                value=add(value, row.get(getColumn));
            }
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
        return value;
    }
    public void setCurrentRowValue(Cursor cursor, String columnName, Object value){
        try{
            cursor.setCurrentRowValue(dbTable.getColumn(columnName), value);
        }catch(IOException ex){
            Logger.getLogger(DataTable.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    //Object arithmetic operators
    public Object add(Object op1, Object op2){
        if(op1==null){
            return add(0, op2);
        }if(op2==null){
            return op1;
        }if(op1 instanceof Boolean){
            op1=(Boolean)op1? 1 : 0;
        }if(op2 instanceof Boolean){
            op2=(Boolean)op2? 1 : 0;
        }if(op1 instanceof String || op2 instanceof String) {
            return String.valueOf(op1) + String.valueOf(op2);
        }if(!(op1 instanceof Number) || !(op2 instanceof Number)) {
            return null;
        }if(op1 instanceof Double || op2 instanceof Double) {
            return ((Number)op1).doubleValue() + ((Number)op2).doubleValue();
        }if(op1 instanceof Float || op2 instanceof Float) {
            return ((Number)op1).floatValue() + ((Number)op2).floatValue();
        }if(op1 instanceof Long || op2 instanceof Long) {
            return ((Number)op1).longValue() + ((Number)op2).longValue();
        }
        return ((Number)op1).intValue() + ((Number)op2).intValue();
    }
    public Object add(Object... ops){
        Object sum=null;
        for(Object t: ops){
            sum=add(sum, t);
        }
        return sum;
    }
    public Object subtract(Object op1, Object op2){
        if(op1==null){
            return subtract(0, op2);
        }if(op2==null){
            return op1;
        }if(op1 instanceof Boolean){
            op1=(Boolean)op1? 1 : 0;
        }if(op2 instanceof Boolean){
            op2=(Boolean)op2? 1 : 0;
        }if(!(op1 instanceof Number) || !(op2 instanceof Number)) {
            return null;
        }if(op1 instanceof Double || op2 instanceof Double) {
            return ((Number)op1).doubleValue() - ((Number)op2).doubleValue();
        }if(op1 instanceof Float || op2 instanceof Float) {
            return ((Number)op1).floatValue() - ((Number)op2).floatValue();
        }if(op1 instanceof Long || op2 instanceof Long) {
            return ((Number)op1).longValue() - ((Number)op2).longValue();
        }
        return ((Number)op1).intValue() - ((Number)op2).intValue();
    }
    public Object multiply(Object op1, Object op2){
        if(op1==null){
            return multiply(1, op2);
        }if(op2==null){
            return op1;
        }if(op1 instanceof Boolean){
            op1=(Boolean)op1? 1 : 0;
        }if(op2 instanceof Boolean){
            op2=(Boolean)op2? 1 : 0;
        }if(!(op1 instanceof Number) || !(op2 instanceof Number)) {
            return null;
        }if(op1 instanceof Double || op2 instanceof Double) {
            return ((Number)op1).doubleValue() * ((Number)op2).doubleValue();
        }if(op1 instanceof Float || op2 instanceof Float) {
            return ((Number)op1).floatValue() * ((Number)op2).floatValue();
        }if(op1 instanceof Long || op2 instanceof Long) {
            return ((Number)op1).longValue() * ((Number)op2).longValue();
        }
        return ((Number)op1).intValue() * ((Number)op2).intValue();
    }
    public Object multiply(Object... ops){
        Object product=null;
        for(Object t: ops){
            product=multiply(product, t);
        }
        return product;
    }
    public Object divide(Object op1, Object op2){
        if(op1==null){
            return divide(1, op2);
        }if(op2==null){
            return op1;
        }if(op1 instanceof Boolean){
            op1=(Boolean)op1? 1 : 0;
        }if(op2 instanceof Boolean){
            op2=(Boolean)op2? 1 : 0;
        }if(!(op1 instanceof Number) || !(op2 instanceof Number)) {
            return null;
        }if(op1 instanceof Double || op2 instanceof Double) {
            return ((Number)op1).doubleValue() / ((Number)op2).doubleValue();
        }if(op1 instanceof Float || op2 instanceof Float) {
            return ((Number)op1).floatValue() / ((Number)op2).floatValue();
        }if(op1 instanceof Long || op2 instanceof Long) {
            return ((Number)op1).longValue() / ((Number)op2).longValue();
        }
        return ((Number)op1).intValue() / ((Number)op2).intValue();
    }

    public String rowToString(Row row){
        if(mask==null? true: mask.length==0){
            return row.toString();
        }else{
            String s="";
            for(int i: mask){
                String m=names[Math.abs(i)];
                Object value=row.get(m);
                if(fromTables.containsKey(m)){
                    try {
                        Table from=fromTables.get(m);
                        DataTable dt=globalMap.get(from);
                        value=dt.rowToString(CursorBuilder.findRowByPrimaryKey(from, value));
                    }catch(IOException ex){
                        Logger.getLogger(DataTable.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
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
    
    public static ArrayList<Row> filter(Table t, String columnName, Comparable value, int comp){
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