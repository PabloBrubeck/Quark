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

public class MainApplication extends JFrame implements WindowListener{
    private final MyMenuBar.MyMenuItem[][] options;
    private final String[] menus={"File", "Edit", "View", "Tools", "Help"};
    private final String[] tabs={
        "Informacion", "Clientes", "Pedidos", "Ventas", "Recursos Humanos", 
        "Gastos", "Balance", "Catalogo", "Prestamos", "Balance Total", "Wizard"
    };
    
    public static File dbFile;
    private static BufferedReader fileIn;
    private static PrintWriter fileOut;
    private Database db;
    private JFileChooser fc;
    private JTabbedPane tp;
    private JComponent[] cont;

    public MainApplication(){
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
            {new MyMenuItem("Full Screen", "control shift ENTER", "fullScreen", this)},
            {new MyMenuItem("Options", null, "options", this)},
            {new MyMenuItem("Help Contents", "F1", "helpContents", this), 
                new MyMenuItem("About", null, "about", this)}
        };
        options=items;
        initcomp();
        setVisible(true);
    }
    private void initcomp(){
        
        //Initialize frame
        addWindowListener(this);
        setTitle("Quark Industries");
        setMinimumSize(new Dimension(800,600));
        
        //Get file path
        String path="new.mdb";
        try{
            fileIn=new BufferedReader(new FileReader("properties.txt"));
            path=fileIn.readLine();
        }catch(IOException e){
            System.err.println(e);
        }
        
        //Open database
        try{
            dbFile=new File(path);
            db=DatabaseBuilder.open(dbFile);
        }catch(IOException e){
            System.err.println(e);
        }
        
        //Initialize fileChooser
        fc=new JFileChooser(path);
        
        //Initialize tabbedPane
        tp=new JTabbedPane();
        cont=new JPanel[tabs.length];
        for(int i=0; i<tabs.length; i++){
            cont[i]=initPanel(tabs[i]);
            tp.addTab(tabs[i], cont[i]);
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
        try{
            fileOut=new PrintWriter(new FileWriter(new File("properties.txt"), false));
            fileOut.println(dbFile.getAbsolutePath());
            fileOut.close();
        }catch(IOException e){
            System.out.println(e);
        }
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
    public void delete(){
        
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
        try {
            Desktop.getDesktop().open(new File("Help Contents.pdf"));
        } catch (IOException e) {
            System.err.println(e);
        }
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