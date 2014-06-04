package gui;

import java.awt.event.ActionEvent;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class MyMenuBar extends JMenuBar {
    public static class MyAction extends AbstractAction{
        private Method method;
        private final Object target;
        private final Object[] params;
        public MyAction(String label, String keyStroke, String methodName, Object obj, Object... args){
            super(label);
            target=obj;
            params=args;
            Class<?>[] types=new Class[args.length];
            for(int i=0; i<args.length; i++){
                types[i]=args[i].getClass();
            }
            try{
                method=obj.getClass().getMethod(methodName, types);
            }catch(NoSuchMethodException | SecurityException e){
                e.printStackTrace();
            }
            if(keyStroke!=null? !keyStroke.isEmpty(): false){
                putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getAWTKeyStroke(keyStroke));
            }
        }
        @Override
        public void actionPerformed(ActionEvent ae) {
            try{
                method.invoke(target, params);
            }catch(IllegalAccessException | IllegalArgumentException | InvocationTargetException e){
                e.printStackTrace();
            }
        }
    }
    public static class MyMenuItem extends JMenuItem{
        public MyMenuItem(String label, String keyStroke, String methodName, Object obj, Object... args){
            super(new MyAction(label, keyStroke, methodName, obj, args));            
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
    public MyMenuBar(String[] s, JMenuItem[][] options){
        for(int i=0; i<s.length; i++) {
            add(new MyMenu(s[i], options[i]));
        }
    }
}