package Swing.component;

import AlcatrazLocal.GameLocal;
import AlcatrazRemote.Implementation.GameImpl;
import AlcatrazRemote.Implementation.GameServiceImpl;
import AlcatrazRemote.Interface.GameRemote;
import AlcatrazRemote.Interface.GameServiceRemote;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.UUID;

public class Entry extends JFrame {
    private GameServiceRemote games;
    private UUID gameId;
    public Entry(GameServiceRemote games, UUID gameId) throws RemoteException {
        this.gameId = gameId;
        this.games = games;
        setTitle("List of games");
        setSize(500,400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);
        this.createGameObject();

        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        JPanel gameinfo = new JPanel();
        JPanel buttons= new JPanel();
        buttons.setLayout(new FlowLayout());
        JButton button =new JButton("Start Game");
         buttons.add(button);
         button.addActionListener((action)->{
             try {
                 this.games.initGameStart(this.gameId);
             } catch (RemoteException e) {
                 e.printStackTrace();
             }
         });

        contentPane.add(gameinfo, BorderLayout.CENTER);
        contentPane.add(buttons, BorderLayout.SOUTH);
    }

     void createGameObject() throws RemoteException {
         Registry registry = LocateRegistry.createRegistry(5090);
         GameImpl game = new GameImpl();
         registry.rebind("gameClient", game);

     }

}

