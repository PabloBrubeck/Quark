package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.swing.AbstractAction;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import java.util.*;
import javax.swing.*;
import java.lang.*;
import java.net.*;
import java.awt.*;
import java.io.*;


public class MyComponent{
    public static class Caller{
        private Method method;
        private final Object target;
        private final Object[] args;
        public Caller(String methodName, Object obj, Object... params){
            target=obj;
            args=new Object[params.length];
            Class<?>[] types=new Class[params.length];
            for(int i=0; i<params.length; i++){
                args[i]=params[i];
                types[i]=params[i].getClass();
            }
            try{
                method=obj.getClass().getMethod(methodName, types);
            }catch(NoSuchMethodException | SecurityException e){
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
            }
        }
        public Object invoke(){
            return invoke(target, args);
        }
        public Object invoke(Object[] params){
            return invoke(target, params);
        }
        public Object invoke(Object obj, Object... params){
            try{
                return method.invoke(obj, params);
            }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
                Logger.getLogger(this.getClass().getName()).log(Level.SEVERE, null, e);
                return null;
            } 
        }
    }
    public static class RealTimeLabel extends JLabel{
        public RealTimeLabel(int ms, Caller caller){
            super(caller.invoke().toString());
            final Caller method=caller;
            new Timer(ms, new ActionListener(){
                @Override
                public void actionPerformed(ActionEvent event){
                    Object obj=method.invoke();
                    if(obj!=null){
                         setText(obj.toString());
                    }
                }
            }).start();
        }
    }
    public static class MyAction extends AbstractAction{
        private final Caller method;
        public MyAction(String label, String keyStroke, Caller caller){
            super(label);
            method=caller;
            if(keyStroke==null? false: !keyStroke.isEmpty()){
                putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getAWTKeyStroke(keyStroke));
            }
        }
        @Override
        public void actionPerformed(ActionEvent ae){
            method.invoke();
        }
    }
    public static class MyMenuItem extends JMenuItem{
        public MyMenuItem(String label, String keyStroke, String methodName, Object obj, Object... args){
            super(new MyAction(label, keyStroke, new Caller(methodName, obj, args)));            
        }
    }
    public static class MyMenu extends JMenu{
        public MyMenu(String s, JMenuItem... options) {
            super(s);
            for(JMenuItem option : options){
                add(option);
            }
        }
    }
    public static class MyMenuBar extends JMenuBar {
        public MyMenuBar(String[] s, JMenuItem[][] options){
            for(int i=0; i<s.length; i++) {
                add(new MyMenu(s[i], options[i]));
            }
        }
    } 
    public static void main(String[] args) throws URISyntaxException {
    final URI uri = new URI("http://java.sun.com");
    class OpenUrlAction implements ActionListener {
      @Override public void actionPerformed(ActionEvent e) {
        open(uri);
      }
    }
    JFrame frame = new JFrame("Links");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setSize(100, 400);
    Container container = frame.getContentPane();
    container.setLayout(new GridBagLayout());
    JButton button = new JButton();
    button.setText("<HTML>Click the <FONT color=\"#000099\"><U>link</U></FONT>"
        + " to go to the Java website.</HTML>");
    button.setHorizontalAlignment(SwingConstants.LEFT);
    button.setBorderPainted(false);
    button.setOpaque(false);
    button.setBackground(Color.WHITE);
    button.setToolTipText(uri.toString());
    button.addActionListener(new OpenUrlAction());
    container.add(button);
    frame.setVisible(true);
  }

    private static void open(URI uri) {
      if (Desktop.isDesktopSupported()) {
          try{
              Desktop.getDesktop().browse(uri);
          }catch(IOException ex){
              Logger.getLogger(MyComponent.class.getName()).log(Level.SEVERE, null, ex);
          }
      } else { /* TODO: error handling */ }
    }
}