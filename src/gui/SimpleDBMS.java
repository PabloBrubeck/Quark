package gui;

import gui.MyComponent.*;
import com.healthmarketscience.jackcess.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.file.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.*;
import javax.imageio.*;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;

public class SimpleDBMS extends JFrame{
    
    private final String[] menus={"File", "Edit", "View", "Tools", "Help"};
    private final SimpleDateFormat sdf=new SimpleDateFormat("hh:mm:ss EEE, dd MMM yyyy");
    private final String url="https://github.com/PabloBrubeck/Quark";
    private final File temp=new File("C:\\Temp\\DataBasePath.txt");
    
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
            {new MyMenuItem("Open", "control O", () -> openFile()), 
                new MyMenuItem("Save", "control S", () -> saveFile()), 
                new MyMenuItem("Exit", "alt F4", () -> exit())},
            {new MyMenuItem("Undo", "control Z", () -> undo()), 
                new MyMenuItem("Redo", "control Y", () -> redo()), 
                new MyMenuItem("Cut", "control X", () -> cut()), 
                new MyMenuItem("Copy", "control C", () -> copy()), 
                new MyMenuItem("Paste", "control V", () -> paste()),
                new MyMenuItem("Delete", "DELETE", () -> delete())},
            {new MyMenuItem("Search", "control F", () -> displaySearch()),
                new MyMenuItem("Full Screen", "F11", () -> fullScreen())},
            {new MyMenuItem("Options", null, () -> options())},
            {new MyMenuItem("Help Contents", "F1", () -> helpContents()), 
                new MyMenuItem("About", null, () -> about())}
        };
        
        searchBox=new JTextField(25){
            private boolean empty=false;
            {
                setVisible(false);
                setMaximumSize(getPreferredSize());
                addKeyListener(new KeyAdapter(){
                    @Override
                    public void keyReleased(KeyEvent ke){
                        if(ke.getKeyCode()==KeyEvent.VK_ESCAPE){
                            search("");
                            setVisible(false);
                            return;
                        }
                        search(getText());
                    }
                });
                addFocusListener(new FocusAdapter(){
                    @Override
                    public void focusGained(FocusEvent fe){
                        if(empty){
                            empty=false;
                            setText("");
                            setForeground(Color.BLACK);
                        }
                    }
                    @Override
                    public void focusLost(FocusEvent fe){
                        if(searchBox.getText().isEmpty()){
                            empty=true;
                            setText("Search (Ctrl+F)");
                            setForeground(Color.DARK_GRAY);
                        }
                    }
                });
            }
        };
        
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
        
        //Initialize tabbedPane
        tabbedPane=new JTabbedPane();
        cl=new ChangeListener(){
            @Override
            public void stateChanged(ChangeEvent ce){
                updateInfo();
            }
        };
        
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
        
        //Open DataBase
        setDatabase(getDefaultFile());
        
        //Add components to frame
        Container contentPane=getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add("North", new BackgroundPanel("resources/SQL Banner.png"));
        contentPane.add(tabbedPane, "Center");
        contentPane.add(status, "South");
    }
    
    public File getDefaultFile(){
        InputStream in;
        try{
            in=new FileInputStream(temp);
        }catch(FileNotFoundException e){
            in=getClass().getClassLoader().getResourceAsStream("resources/DataBasePath.txt");
            try{
                Files.copy(in, temp.toPath());
            }catch(IOException ex){
                Logger.getLogger(SimpleDBMS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        try(BufferedReader br=new BufferedReader(new InputStreamReader(in))){
            return new File(br.readLine());
        }catch(IOException e){
            fileChooser.showOpenDialog(this);
            return fileChooser.getSelectedFile();
        }
    }
    public void setDefaultFile(File file){
        try(FileWriter fw=new FileWriter(temp, false)){
            fw.write(file.getAbsolutePath());
        }catch(IOException e){
            Logger.getLogger(SimpleDBMS.class.getName()).log(Level.SEVERE, null, e);
        }
    }
    public void setDatabase(File file){
        try{
            db=DatabaseBuilder.open(file);
            tabbedPane.removeChangeListener(cl);
            tabbedPane.removeAll();
            addDataTables(db);
            tabbedPane.addChangeListener(cl);
            updatePath();
            updateInfo();
            fileChooser.setCurrentDirectory(file);
        }catch(IOException e){
            Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
        }
    }
    public void addDataTables(Database dataBase){
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
        pathLabel.setText(db.getFile().getAbsolutePath());
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
        int i=fileChooser.showOpenDialog(this);
        if(i==JFileChooser.APPROVE_OPTION){
            try {
                File openFile=fileChooser.getSelectedFile();
                db.close();
                setDatabase(openFile);
                setDefaultFile(openFile);
            } catch (IOException ex) {
                Logger.getLogger(SimpleDBMS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    public void saveFile(){
        int i=fileChooser.showSaveDialog(this);
        if(i==JFileChooser.APPROVE_OPTION){
            try{
                File saveFile=fileChooser.getSelectedFile();
                Files.copy(new FileInputStream(db.getFile()), saveFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
                db.close();
                setDatabase(saveFile);
                setDefaultFile(saveFile);
            }catch(IOException ex){
                Logger.getLogger(SimpleDBMS.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
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
        JOptionPane.showMessageDialog(this, about, "About", JOptionPane.INFORMATION_MESSAGE);
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