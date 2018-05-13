package Swing.component;

import AlcatrazLocal.GameLocal;
import AlcatrazRemote.Implementation.GameImpl;
import AlcatrazRemote.Interface.GameServiceRemote;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.util.Vector;


/**
 * Created by Oli on 26.03.2018.
 */

public class GameList extends JFrame{


    private JLabel gameName;
    private JLabel numberOfPlayers;


    private JList jList;
    private JPanel buttonPanel;
    private JPanel gameInformationPanel;
    private JPanel playerlist;

    private String playerName;
    private GameServiceRemote games;
    private GameLocal selectedGame;
    public GameList() throws RemoteException, NotBoundException, MalformedURLException {
        this.games   = (GameServiceRemote) Naming.lookup("rmi://localhost:5099/gamelist") ;

        setTitle("List of games");
        setSize(500,400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //just swing things
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        this.gameName = new JLabel("GameName");
        this.numberOfPlayers = new JLabel("Player Count");




        this.jList = new JList<>();
        this.buttonPanel = new JPanel();
        this.gameInformationPanel = new JPanel();



        JButton createGame = new JButton("Create Game");
        JButton joinGame = new JButton("Join Game");
        JButton refresh = new JButton("refresh");


        this.updateGamelist();
        this.jList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component renderer = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (renderer instanceof JLabel && value instanceof GameLocal) {
                    ((JLabel) renderer).setText(((GameLocal) value).getGameName());
                }
                return renderer;
            }
        });

        jList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList.setFixedCellWidth(150);
        jList.addListSelectionListener(( listSelectionEvent) ->{
            if (!listSelectionEvent.getValueIsAdjusting()) {
                JList list = (JList) listSelectionEvent.getSource();
                this.selectedGame = (GameLocal)list.getSelectedValue();
                updateInfoPanel();
            }

        });





        refresh.addActionListener( action -> {
            updateGamelist();
            updateInfoPanel();
        });
        joinGame.addActionListener( action -> {
            if(this.selectedGame != null && this.selectedGame.getTakenPlaces() < this.selectedGame.getPlayerCount()) {
                joinGame();
            }else{
                System.out.println("not joinable");
            }
        });

        createGame.addActionListener( action -> {
            try {
                int numberOfPlayers;
                String gameName = JOptionPane.showInputDialog("Enter  Game name");
                do{
                    numberOfPlayers =  Integer.parseInt(JOptionPane.showInputDialog("Enter the number of player( 2- 4)"));
                } while(numberOfPlayers < 2 || numberOfPlayers >4);

                this.selectedGame = this.games.createGame(gameName, numberOfPlayers);
                joinGame();
            } catch (RemoteException  e) {
                e.printStackTrace();
            }
        });

        gameInformationPanel.setLayout(new BoxLayout(gameInformationPanel,BoxLayout.PAGE_AXIS));

        buttonPanel.setLayout(new FlowLayout());



        gameInformationPanel.add(gameName);
        gameInformationPanel.add(numberOfPlayers);
        gameInformationPanel.add(numberOfPlayers);


        buttonPanel.add(createGame);
        buttonPanel.add(joinGame);
        buttonPanel.add(refresh);


        contentPane.add(gameInformationPanel);
        contentPane.add(jList, BorderLayout.WEST);
        contentPane.add(buttonPanel, BorderLayout.SOUTH);
    }



    private void updateGamelist(){

        try {

            //TODO: update labels after joining and game
            //Registry registry = LocateRegistry.getRegistry("192.168.21.110",1099);
            //Registry reg = (Registry) registry.lookup("reg");
           // this.games   = (GameServiceRemote) reg.lookup("gamelist");

            int index = this.jList.getSelectedIndex();
            this.jList.setListData(new Vector<GameLocal>(this.games.listGames()));
            this.jList.setSelectedIndex(index);
        } catch ( RemoteException e) {
            //this.jList.setSelectedIndex();
        }

    }

    private void joinGame(){

        this.playerName = JOptionPane.showInputDialog("Enter your username");
        while (!this.selectedGame.isGamerNameAvaliable(this.playerName)) {
            this.playerName = JOptionPane.showInputDialog("Username is already taken for this game\n Enter other username", JOptionPane.ERROR_MESSAGE);
        }
        try {
            GameImpl gameImpl = createGameObject();
            GameLobby gameLobby = new GameLobby(this.games, gameImpl, this.playerName);
            gameImpl.setObserver(gameLobby);
            this.games.joinGame(this.playerName, this.selectedGame.getGameID());

            gameLobby.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    onExit();
                }
            });

            this.dispose();

        } catch (RemoteException | ServerNotActiveException e) {
            e.printStackTrace();
        }
    }

    private GameImpl createGameObject() throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(5092);
        GameImpl game = new GameImpl(this.selectedGame.getGameID());
        registry.rebind("gameClient", game);
        return game;
    }
    private void onExit(){
        try {
            this.games.leaveGame(this.playerName,this.selectedGame.getGameID());
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void updateInfoPanel(){
        if(selectedGame != null) {
            this.gameName.setText("Game name:\t"+selectedGame.getGameName());
            this.numberOfPlayers.setText("Players:\t"+selectedGame.getTakenPlaces()+ " of " +selectedGame.getPlayerCount());
        }
    }
}
