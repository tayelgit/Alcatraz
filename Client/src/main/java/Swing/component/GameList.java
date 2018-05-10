package Swing.component;

import AlcatrazLocal.GameLocal;
import AlcatrazRemote.Interface.GameServiceRemote;

import javax.swing.*;
import java.awt.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.UUID;
import java.util.Vector;


/**
 * Created by Oli on 26.03.2018.
 */

public class GameList extends JFrame{


    private JLabel jLabel1;
    private JLabel jLabel2;


    private JList jList;
    private JPanel buttonPanel;
    private JPanel gameInformationPanel;
    private JPanel playerlist;

    private GameServiceRemote games;
    private GameLocal selectedGame;
    public GameList(){
        setTitle("List of games");
        setSize(500,400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        //just swing things
        Container contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        this.jLabel1 = new JLabel("GameName");
        this.jLabel2 = new JLabel("Player Count");




        this.jList = new JList<>();
        this.buttonPanel = new JPanel();
        this.gameInformationPanel = new JPanel();




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

                if(selectedGame != null) {
                    // ((JLabel)((JPanel) getContentPane().getComponent(0)).getComponent(0)).setText(selectionValues.toString());
                    this.jLabel1.setText("Game name:\t"+selectedGame.getGameName());
                    this.jLabel2.setText("Players:\t"+selectedGame.getTakenPlaces()+ " of " +selectedGame.getPlayerCount());

                }
            }

        });




        JButton createGame = new JButton("Create Game");
        JButton joinGame = new JButton("Join Game");
        JButton refresh = new JButton("refresh");


        refresh.addActionListener( action -> {
            this.updateGamelist();
        });
        joinGame.addActionListener( action -> {

            if(this.selectedGame != null && this.selectedGame.getTakenPlaces()< this.selectedGame.getPlayerCount()) {
                //System.out.println("Joined Game"+this.selectedGame.getGameID());
                String playerName = JOptionPane.showInputDialog("Enter your username");
                while (!this.selectedGame.isGamerNameAvaliable(playerName)) {
                    playerName = JOptionPane.showInputDialog("Username is already taken for this game\n Enter other username", JOptionPane.ERROR_MESSAGE);
                }
                try {
                    this.games.joinGame(playerName, this.selectedGame.getGameID());
                    Entry entry = new Entry(this.games,this.selectedGame.getGameID());
                    this.updateGamelist();

                } catch (RemoteException | ServerNotActiveException e) {
                    e.printStackTrace();
                }
            }else{
                System.out.println("not joinable");
            }
        });

        createGame.addActionListener( action -> {
            try {

                String gameName = JOptionPane.showInputDialog("Enter  Game name");
                String playerName = JOptionPane.showInputDialog("Enter your username");
                int numberOfPlayers =  Integer.parseInt(JOptionPane.showInputDialog("Enter the number of player( 2- 4)"));

                UUID gameID = this.games.createGame(gameName, numberOfPlayers);
                this.games.joinGame(playerName, gameID);
                this.updateGamelist();

            } catch (RemoteException | ServerNotActiveException e) {
                e.printStackTrace();
            }
        });

        gameInformationPanel.setLayout(new BoxLayout(gameInformationPanel,BoxLayout.PAGE_AXIS));

        buttonPanel.setLayout(new FlowLayout());



        gameInformationPanel.add(jLabel1);
        gameInformationPanel.add(jLabel2);
        gameInformationPanel.add(jLabel2);


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
            this.games   = (GameServiceRemote) Naming.lookup("rmi://localhost:5099/gamelist") ;
            this.jList.setListData(new Vector<GameLocal>(this.games.listGames()));
            //this.jList.setSelectedIndex();
        } catch (NotBoundException | MalformedURLException | RemoteException e) {
            e.printStackTrace();
        }

    }
}
