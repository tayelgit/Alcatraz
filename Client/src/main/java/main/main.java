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

     /*  HelloService helloService = (HelloService) Naming.lookup("rmi://localhost:5099/hello");
        System.out.print(helloService.echo());
        helloService.setString("sinethboasdflgnj");
        System.out.print(helloService.echo());

       */

        EventQueue.invokeLater(()->{
           // GameList test = new GameList();
            Entry entry = new Entry();
            entry.setVisible(true);

        });

    }


}