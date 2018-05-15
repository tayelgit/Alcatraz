package gameclient;

import Swing.component.GameList;

import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Created by Oli on 26.03.2018.
 */
public class GameClient {
    public static void main(String [ ] args) throws RemoteException, NotBoundException, MalformedURLException {


        EventQueue.invokeLater(()->{
            GameList gamelist;
            try {
                gamelist = new GameList();
                gamelist.setVisible(true);

            } catch (RemoteException | NotBoundException | MalformedURLException e) {
                e.printStackTrace();
            }

        });

    }


}