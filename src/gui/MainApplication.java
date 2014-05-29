package gui;

import enterprise.*;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class MainApplication extends JFrame{
    private JPanel pnl;
    private Client[] clients;
    private ListPanel[] list;
    private static Client[] p={
            new Client("Pablo Brubeck", "Quark", "8113118969", "Cda. Vicenzo 3708 Col. Lomas del Paseo"),
            new Client("Sebastian Rivera", "Quark", "8115855672", "La Fortaleza 105 Col. Fortin del Huajuco"),
            new Client("Paco Treviño", "Quark", "8117093422", "Valle del Moscatel 208 Col. Valle del Contry"),
        };
    public MainApplication(){
    }
    public static void main(String[] args){
        MainApplication m=new MainApplication();
    }
}
