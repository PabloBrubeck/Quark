package gui;

import java.io.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.sun.java.swing.plaf.windows.WindowsLookAndFeel;

public class MainApplication extends JFrame implements ActionListener, WindowListener{
    private class MyAction extends AbstractAction{
        private final String name;
        private MyAction(String s){
            super(s);
            name=s;
            //putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_A, KeyEvent.CTRL_MASK));
        }
        @Override
        public void actionPerformed(ActionEvent ae){
            switch(name){
                default:
                    System.out.println(name);
                    break;
                case "Open File":
                    openFile();
                    break;
            }
        }
    }
    
    private final static String[] tab={
        "Informacion", "Clientes", "Pedidos", "Ventas", "Recursos Humanos", 
        "Gastos", "Balance", "Catalogo", "Prestamos", "Balance Total", "Wizard"
    };
    private final String[] tag = {"File", "Edit", "View", "Tools", "Help"};
    private final String[][] sub={
            {"Open File", "Save", "Exit"},
            {"Cut", "Copy", "Paste"},
            {"Appearance"},
            {"Options"},
            {"Help Contents", "About"}
        };
    
    public static File file;
    private JFileChooser fc;
    private JTabbedPane tp;
    private JComponent[] cont;

    public MainApplication(){
        initcomp();
        setVisible(true);
    }
    private void initcomp(){
        //Initialize frame
        addWindowListener(this);
        setTitle("Quark Industries");
        setMinimumSize(new Dimension(800,600));
        Container contentPane=getContentPane();
        contentPane.setLayout(new BorderLayout());
        
        //Initialize fileChooser
        String dir=getClass().getProtectionDomain().getCodeSource().getLocation().toString();
        dir=dir.substring(6, dir.indexOf("/build/classes/"));
        fc=new JFileChooser(dir);
        file=new File("new.mdb");
        
        //Initialize tabbedPane
        tp=new JTabbedPane();
        cont=new JPanel[tab.length];
        for(int i=0; i<tab.length; i++){
            cont[i]=samplePanel(tab[i]);
            tp.addTab(tab[i], cont[i]);
        }
        
        //Initialize statusPanel
        JPanel bot=new JPanel(new GridLayout(1,3));
        bot.add(new Label("Contacto"));
        bot.add(new Label("Ayuda"));
        bot.add(new Label("Cerrar sesion"));
        
        //Add components to frame
        contentPane.add(tp, "Center");
        contentPane.add(bot, "South");
        setJMenuBar(new MyMenu(tag, setActions()));
    }
    private AbstractAction[][] setActions(){
        AbstractAction[][] a=new AbstractAction[sub.length][];
        for(int i=0; i<a.length; i++){
            a[i]= new AbstractAction[sub[i].length];
            for(int j=0; j<a[i].length; j++){
                a[i][j]=new MyAction(sub[i][j]);
            }
        }
        return a;
    }
    
    private void openFile(){
        fc.showOpenDialog(this);
        file=fc.getSelectedFile();
    }
    
    private static JPanel samplePanel(String s){
        try{
            switch(s){
                case "Clientes":
                case "Ventas":
                case "Catalogo":
                    return new DataTable(s);
                case "Informacion":
                    return new DataTable("Empleados");  
            }
        }catch(IOException e){  }
        JPanel p=new JPanel();
        p.setLayout(new FlowLayout());
        JLabel label=new JLabel(s);
        p.add(label);
        return p;
    }
    
    @Override
    public void actionPerformed(ActionEvent ae){
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