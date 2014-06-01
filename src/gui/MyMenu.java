package gui;

import java.awt.Font;
import javax.swing.AbstractAction;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

public class MyMenu extends JMenuBar {
    private class SubMenu extends JMenu {
        private final JMenuItem[] item;
        private SubMenu(String tag, AbstractAction[] op) {
            super(tag);
            item=new JMenuItem[op.length];
            for (int i = 0; i < op.length; i++) {
                item[i]=new JMenuItem(op[i]);
                item[i].setFont(font);
                add(item[i]);
            }
        }
    }
    private final Font font = new Font("arial.ttf", 0, 12);
    private final SubMenu[] menu;
    public MyMenu(String[] tags, AbstractAction[][] a) {
        menu=new SubMenu[tags.length];
        for (int i=0; i<tags.length; i++) {
            menu[i]=new SubMenu(tags[i], a[i]);
            menu[i].setFont(font);
            add(menu[i]);
        }
    }
}
