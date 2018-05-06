package main;

import Swing.component.Entry;
import Swing.component.GameList;

import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by Oli on 26.03.2018.
 */
public class main  {
    public static void main(String [ ] args) throws RemoteException, NotBoundException, MalformedURLException {


        EventQueue.invokeLater(()->{
           GameList entry=  new GameList();
           // Entry entry = new Entry();
            entry.setVisible(true);

        });

    }


}