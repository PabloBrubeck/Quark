package gui;

import gui.MyMenuBar.*;
import com.healthmarketscience.jackcess.*;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;
import javax.swing.border.*;
import javax.swing.filechooser.FileFilter;

public class MainApplication extends JFrame{
    private final String[] menus={"File", "Edit", "View", "Tools", "Help"};
    private final String[] tabs={
        "Informacion", "Clientes", "Catalogo", "Pedidos", "Ventas", 
        "Gastos", "Recursos Humanos", "Prestamos", "Balance", "Wizard"
    };
    
    private File dbFile;
    private Database db;
    private JFileChooser fc;
    private JTabbedPane tp;
    private JComponent[] cont;
    
    private boolean isFullScreen=false;
    
    public MainApplication(){
        initcomp();
        setVisible(true);
    }
    private void initcomp(){
        
        //Initialize frame
        setTitle("Quark Industries");
        setMinimumSize(new Dimension(800,600));
        addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent we){
                exit();
            }
        });
        
        //Initialize fileChooser
        fc=new JFileChooser();
        fc.setFileFilter(new FileFilter(){
            @Override
            public boolean accept(File file) {
                return file.isDirectory() || file.getName().endsWith(".mdb");
            }
            @Override
            public String getDescription() {
                return "Microsoft Access Database";
            }
        });
        
        //Initialize database
        String path="DBQUARK.mdb";
        try{
            dbFile=new File(path);
            db=DatabaseBuilder.open(dbFile);
            fc.setCurrentDirectory(dbFile);
        }catch(IOException e){
            e.printStackTrace();
            fc.showOpenDialog(tp);
            dbFile=fc.getSelectedFile();
        }
        
        //Initialize tabbedPane
        tp=new JTabbedPane();
        try{
            for(String s: db.getTableNames()){
                tp.addTab(s, new DataTable(db, s));
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        
        //Initialize statusPanel
        JPanel status=new JPanel(new GridLayout(1,3));
        status.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        status.add(new Label("Contacto"));
        status.add(new Label("Ayuda"));
        status.add(new Label("Cerrar sesion"));
        
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
            {new MyMenuItem("Full Screen", "alt shift ENTER", "fullScreen", this)},
            {new MyMenuItem("Options", null, "options", this)},
            {new MyMenuItem("Help Contents", "F1", "helpContents", this), 
                new MyMenuItem("About", null, "about", this)}
        };
        setJMenuBar(new MyMenuBar(menus, items));
        
        //Add components to frame
        Container contentPane=getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tp, "Center");
        contentPane.add(status, "South");
        
    }
    private JPanel initPanel(String s){
        try{
            switch(s){
                case "Clientes":
                case "Ventas":
                case "Catalogo":
                    return new DataTable(db, s);
                case "Informacion":
                    return new DataTable(db, "Empleados");
                case "Recursos Humanos":
                    return new DataTable(db, "Test");  
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        JPanel p=new JPanel();
        p.setLayout(new FlowLayout());
        JLabel label=new JLabel(s);
        p.add(label);
        return p;
    }
    
    //File menu
    public void openFile(){
        fc.showOpenDialog(this);
        dbFile=fc.getSelectedFile();
    }
    public void saveFile(){
        fc.showSaveDialog(this);
        String dir=fc.getSelectedFile().getAbsolutePath();
    }
    public void exit(){
        try{
            db.close();
        }catch(IOException e){
            e.printStackTrace();
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
    public void fullScreen(){
        GraphicsDevice gd=GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if(gd.isFullScreenSupported()){
            gd.setFullScreenWindow(isFullScreen? null: this);
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
            e.printStackTrace();
        }
    }
    public void about(){
        JOptionPane.showMessageDialog(this,
                "2014 Quark Industries",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }
    
    public static void main(String[] args){
        try{
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
        }catch(UnsupportedLookAndFeelException e){
            e.printStackTrace();
        }
        MainApplication m=new MainApplication();
    }
}