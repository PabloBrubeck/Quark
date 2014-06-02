package gui;

import com.healthmarketscience.jackcess.*;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import javax.swing.*;

public class MainApplication extends JFrame implements WindowListener{
    private final MyMenuBar.MyMenuItem[][] options;
    private final String[] menus={"File", "Edit", "View", "Tools", "Help"};
    private final static String[] tab={
        "Informacion", "Clientes", "Pedidos", "Ventas", "Recursos Humanos", 
        "Gastos", "Balance", "Catalogo", "Prestamos", "Balance Total", "Wizard"
    };
    
    public static File dbFile;
    private Database db;
    private JFileChooser fc;
    private JTabbedPane tp;
    private JComponent[] cont;

    public MainApplication(){
        MyMenuBar.MyMenuItem[][] op={
            {new MyMenuBar.MyMenuItem("Open", 'o', "openFile", this), new MyMenuBar.MyMenuItem("Save", 's', "saveFile", this), new MyMenuBar.MyMenuItem("Exit", 'e', "exit", this)},
            {new MyMenuBar.MyMenuItem("Undo", 'z', "undo", this), new MyMenuBar.MyMenuItem("Redo", 'y', "redo", this), new MyMenuBar.MyMenuItem("Cut", 'x', "cut", this), new MyMenuBar.MyMenuItem("Copy", 'c', "copy", this), new MyMenuBar.MyMenuItem("Paste", 'v', "paste", this)},
            {new MyMenuBar.MyMenuItem("Full Screen", '+', "fullScreen", this)},
            {new MyMenuBar.MyMenuItem("Options", 0, "options", this)},
            {new MyMenuBar.MyMenuItem("Help Contents", 0, "helpContents", this), new MyMenuBar.MyMenuItem("About", 0, "about", this)}
        };
        options=op;
        initcomp();
        setVisible(true);
    }
    private void initcomp(){
        
        //Initialize frame
        addWindowListener(this);
        setTitle("Quark Industries");
        setMinimumSize(new Dimension(800,600));
        
        //Open file
        dbFile=new File("new.mdb");

        //Open database
        try{
            db=DatabaseBuilder.open(dbFile);
        }catch(IOException e){
            System.err.println(e);
        }
        
        //Initialize fileChooser
        String dir=getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        dir=dir.substring(6, dir.indexOf("/build/classes/"));
        fc=new JFileChooser(dbFile.getPath());
        
        //Initialize tabbedPane
        tp=new JTabbedPane();
        cont=new JPanel[tab.length];
        for(int i=0; i<tab.length; i++){
            cont[i]=initPanel(tab[i]);
            tp.addTab(tab[i], cont[i]);
        }
        
        //Initialize statusPanel
        JPanel bot=new JPanel(new GridLayout(1,3));
        bot.add(new Label("Contacto"));
        bot.add(new Label("Ayuda"));
        bot.add(new Label("Cerrar sesion"));
        
        //Add components to frame
        Container contentPane=getContentPane();
        contentPane.setLayout(new BorderLayout());
        contentPane.add(tp, "Center");
        contentPane.add(bot, "South");
        setJMenuBar(new MyMenuBar(menus, options));
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
            }
        }catch(IOException e){
            System.err.println(e);
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
        String dir=fc.getSelectedFile().getName();
    }
    public void exit(){
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
    //View menu
    public void fullScreen(){
        setExtendedState(JFrame.MAXIMIZED_BOTH);
    }
    //Tools menu
    public void options(){
        
    }
    //Help menu
    public void helpContents(){
        
    }
    public void about(){
        JOptionPane.showMessageDialog(this,
                "2014 Quark Industries",
                "About",
                JOptionPane.INFORMATION_MESSAGE);
    }

    @Override
    public void windowOpened(WindowEvent we){
    }
    @Override
    public void windowClosing(WindowEvent we){
        System.exit(0);
    }
    @Override
    public void windowClosed(WindowEvent we){
    }
    @Override
    public void windowIconified(WindowEvent we){
    }
    @Override
    public void windowDeiconified(WindowEvent we){
    }
    @Override
    public void windowActivated(WindowEvent we){
    }
    @Override
    public void windowDeactivated(WindowEvent we){
    }
    
    public static void main(String[] args){
        try{
            UIManager.setLookAndFeel(new WindowsLookAndFeel());
        }catch(UnsupportedLookAndFeelException e){
            System.out.println(e);
        }
        MainApplication m=new MainApplication();
    }
}