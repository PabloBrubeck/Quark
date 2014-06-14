package gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultMutableTreeNode;

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
        public Object getTarget(){
            return target;
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
    public static class Hyperlink extends JLabel{
        public Hyperlink(String s, final String uri){
            super("<HTML><FONT color=\"#0000FF\"><U>"+s+"</U></FONT></HTML>");
            setToolTipText(uri);
            addMouseListener(new MouseAdapter(){
                @Override
                public void mouseClicked(MouseEvent me){
                    if(Desktop.isDesktopSupported()){
                        try{
                            Desktop.getDesktop().browse(new URI(uri));
                        }catch(IOException | URISyntaxException e){
                            Logger.getLogger(MyComponent.class.getName()).log(Level.SEVERE, null, e);
                        }
                    }
                }
                @Override
                public void mouseEntered(MouseEvent me) {
                    setCursor(new Cursor(Cursor.HAND_CURSOR));
                }
                @Override
                public void mouseExited(MouseEvent me) {
                    setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
                }
            });
        }      
    }
    public static class IconNode extends DefaultMutableTreeNode {
        private Icon icon;
        private String iconName;
        public IconNode(){
            this(null);
        }
        public IconNode(Object userObject){
            this(userObject, true, null);
        }
        public IconNode(Object userObject, boolean allowsChildren, Icon icon){
            super(userObject, allowsChildren);
            this.icon = icon;
        }
        public void setIcon(Icon icon){
            this.icon = icon;
        }
        public Icon getIcon() {
            return icon;
        }
        public String getIconName(){
            if (iconName != null){
                return iconName;
            } else {
                String str = userObject.toString();
                int index = str.lastIndexOf(".");
                if (index != -1) {
                    return str.substring(++index);
                } else {
                    return null;
                }
            }
        }
        public void setIconName(String name) {
            iconName=name;
        }
    }
    public static class IconNodeRenderer extends DefaultTreeCellRenderer {
        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value,
                boolean sel, boolean expanded, boolean leaf, int row,
                boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
            Icon icon=((IconNode)value).getIcon();
            if(icon==null){
                HashMap icons=(HashMap)tree.getClientProperty("JTree.icons");
                String name = ((IconNode)value).getIconName();
                if ((icons != null) && (name != null)) {
                    icon=(Icon)icons.get(name);
                    if(icon != null){
                        setIcon(icon);
                    }
                }
            }else{
                setIcon(icon);
            }
            return this;
        }
    }
}