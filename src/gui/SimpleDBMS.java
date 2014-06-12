package gui;

import gui.MyComponent.*;
import com.healthmarketscience.jackcess.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class SimpleDBMS extends JFrame{
    private final String[] menus={"File", "Edit", "View", "Tools", "Help"};
    private final SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss EEE, dd MMM yyyy");
    private final String url="https://github.com/PabloBrubeck/Quark";
    
    private File dbFile;
    private Database db;
    private JFileChooser fileChooser;
    private JTabbedPane tabbedPane;
    private ChangeListener cl;
    private JTextField searchBox;
    private JLabel infoLabel, pathLabel;
    
    private boolean isFullScreen=false;
    
    public SimpleDBMS(){
        initcomp();
        setVisible(true);
    }
    private void initcomp(){
        
        //Initialize frame
        setTitle("Quark Industries");
        setMinimumSize(new Dimension(800, 600));
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent we){
                exit();
            }
        });
        
        //Initialize menuBar
        MyMenuItem[][] items={
            {new MyMenuItem("Open", "control O", "openFile", this), 
                new MyMenuItem("Save", "control S", "saveFile", this), 
                new MyMenuItem("Exit", "alt F4", "exit", this)},
            {new MyMenuItem("Undo", "control Z", "undo", this), 
                new MyMenuItem("Redo", "control Y", "redo", this), 
                new MyMenuItem("Cut", "control X", "cut", this), 
                new MyMenuItem("Copy", "control C", "copy", this), 
                new MyMenuItem("Paste", "control V", "paste", this),
                new MyMenuItem("Delete", "DELETE", "delete", this)},
            {new MyMenuItem("Search", "control F", "displaySearch", this),
                new MyMenuItem("Full Screen", "F11", "fullScreen", this)},
            {new MyMenuItem("Options", null, "options", this)},
            {new MyMenuItem("Help Contents", "F1", "helpContents", this), 
                new MyMenuItem("About", null, "about", this)}
        };
        searchBox=new JTextField(25);
        searchBox.setVisible(false);
        searchBox.setMaximumSize(searchBox.getPreferredSize());
        searchBox.addKeyListener(new KeyAdapter(){
            @Override
            public void keyReleased(KeyEvent ke){
                if(ke.getKeyCode()==KeyEvent.VK_ESCAPE){
                    search("");
                    searchBox.setVisible(false);
                    return;
                }
                search(searchBox.getText());
            }
        });
        searchBox.addFocusListener(new FocusAdapter(){
            private boolean empty=false;
            @Override
            public void focusGained(FocusEvent fe){
                if(empty){
                    searchBox.setText("");
                    searchBox.setForeground(Color.BLACK);
                }
            }
            @Override
            public void focusLost(FocusEvent fe){
                if(searchBox.getText().isEmpty()){
                    empty=true;
                    searchBox.setText("Search (Ctrl+F)");
                    searchBox.setForeground(Color.DARK_GRAY);
                }
            }
        });
        JMenuBar mb=new MyMenuBar(menus, items);
        mb.add(Box.createHorizontalGlue());
        mb.add(searchBox);
        setJMenuBar(mb);
        
        //Initialize fileChooser
        fileChooser=new JFileChooser();
        fileChooser.setFileFilter(new FileFilter(){
            @Override
            public boolean accept(File file){
                return file.isDirectory() || file.getName().endsWith(".mdb");
            }
            @Override
            public String getDescription(){
                return "Microsoft Access Database";
            }
        });
        
        //Initialize database
        dbFile=new File("MyDataBase.mdb");
        if(!dbFile.exists() || dbFile.isDirectory()){
            fileChooser.showOpenDialog(this);
            dbFile=fileChooser.getSelectedFile();
        }
        try{
            db=DatabaseBuilder.open(dbFile);
            fileChooser.setCurrentDirectory(dbFile);
        }catch(IOException e1){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e1);
        }
        
        //Initialize statusPanel
        infoLabel=new JLabel();
        pathLabel=new JLabel();
        JPanel status=new JPanel(new BorderLayout(30,0)){
            {
                setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
                add(pathLabel, "West");
                JPanel grid=new JPanel(new GridLayout(0,2,30,0));
                grid.add(infoLabel);
                grid.add(new Hyperlink("Online support", url));
                add(grid, "Center");
                add(new RealTimeLabel(1000, new Caller("getTime", SimpleDBMS.this)), "East");
            }
        };
        updatePath();
        
        //Initialize tabbedPane
        tabbedPane=new JTabbedPane();
        openDataTables(db);
        tabbedPane.addChangeListener(cl=new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent ce){
                updateInfo();
            }
        });
        
        //Add components to frame
        Container contentPane=getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tabbedPane, "Center");
        contentPane.add(status, "South");
    }
    
    public void openDataTables(Database dataBase){
        try{
            for(String s: dataBase.getTableNames()){
                addDataTable(s, new DataTable(dataBase, s));
            }
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public void addDataTable(String s, DataTable dt){
        tabbedPane.addTab(s, dt);
    }
    
    public JTabbedPane getTabbedPane(){
        return tabbedPane;
    }
    
    //Status bar
    public String getTime(){
        return sdf.format(new Date());
    }
    public void updatePath(){
        pathLabel.setText(dbFile.getAbsolutePath());
    }
    public void updateInfo(){
        Component c=tabbedPane.getSelectedComponent();
        String title = tabbedPane.getTitleAt(tabbedPane.getSelectedIndex());
        if(c instanceof DataTable){
            int i=((DataTable)c).getRowCount();
            title+=": "+i+" registros";
        }
        infoLabel.setText(title);
    }
    
    //File menu
    public void openFile(){
        fileChooser.showOpenDialog(this);
        dbFile=fileChooser.getSelectedFile();
        try{
            db.close();
            db=DatabaseBuilder.open(dbFile);
            tabbedPane.removeChangeListener(cl);
            tabbedPane.removeAll();
            openDataTables(db);
            tabbedPane.addChangeListener(cl);
            updatePath();
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public void saveFile(){
        fileChooser.showSaveDialog(this);
        updatePath();
    }
    public void exit(){
        try{
            db.close();
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
        System.exit(0);
    }
    //Edit menu
    public void undo(){
        
    }
    public void redo(){
        
    }
    public void cut(){
        
    }
    public void copy(){
        
    }
    public void paste(){
        
    }
    public void delete(){
        
    }
    //View menu
    public void displaySearch(){
        if(!searchBox.isVisible()){
            searchBox.setVisible(true);
        }
        if(!searchBox.hasFocus()){
            searchBox.requestFocus();
        }
    }
    public void search(String s){
        Component c=tabbedPane.getSelectedComponent();
        if(c instanceof DataTable){
            ((DataTable)c).search(s);
        }
    }
    public void fullScreen(){
        GraphicsDevice gd=getGraphicsConfiguration().getDevice();
        if(gd.isFullScreenSupported()){
            removeNotify();
            setUndecorated(!isFullScreen);
            gd.setFullScreenWindow(isFullScreen? null: this);
            if(isFullScreen){
                setVisible(true);
            }
            requestFocus(true);
            isFullScreen=!isFullScreen;
        }
    }
    //Tools menu
    public void options(){
        
    }
    //Help menu
    public void helpContents(){
        try {
            Desktop.getDesktop().open(new File("Help Contents.pdf"));
        } catch (IOException e) {
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public void about(){
        JPanel about=new JPanel(new GridLayout(4,1));
        about.add(new JLabel("2014 Quark Industries"));
        about.add(new JLabel("Designed with Java TM"));
        about.add(new JLabel("License: XXXX-XXXX-XXXX-XXXX"));
        about.add(new Hyperlink("Online support", url));
        JOptionPane.showMessageDialog(null, about, "About", JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args){
        try{
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }catch(ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e){
            Logger.getLogger(SimpleDBMS.class.getName()).log(Level.SEVERE, null, e);
        }
        SimpleDBMS m=new SimpleDBMS();
    }
}