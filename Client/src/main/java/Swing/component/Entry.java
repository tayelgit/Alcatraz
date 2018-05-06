package Swing.component;

import AlcatrazLocal.GameLocal;
import AlcatrazRemote.Interface.GameRemote;
import AlcatrazRemote.Interface.GameServiceRemote;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

public class Entry extends JFrame {
    private GameServiceRemote games;
    private GameList gamelist;
    public Entry(){
        setTitle("List of games");
        setSize(500,400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //just swing things
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        JButton createGame = new JButton("Create Game");
        JButton joinGame = new JButton("Join Game");

        joinGame.addActionListener( action -> {

            this.refreshGamelist();
        });

        JPanel jpanel = new JPanel();
        jpanel.add(createGame);
        jpanel.add(joinGame);

        contentPane.add(jpanel,BorderLayout.CENTER);
    }

    public void refreshGamelist(){

        try {

        this.games   = (GameServiceRemote) Naming.lookup("rmi://localhost:5099/gamelist") ;
        this.gamelist =  new GameList();
        this.gamelist.setVisible(true);
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }

    }
}

