package Swing.component;


import Service.Alcatraz.serviceData.Gamer;
import Service.Alcatraz.AlcatrazRemote.Implementation.GameImpl;
import Service.Alcatraz.AlcatrazRemote.Interface.GameServiceRemote;
import communctation.Interface.Observer;
import rmiUtil.RegistryService;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

public class GameLobby extends JFrame implements Observer {
    private GameServiceRemote games;
    private UUID gameId;
    private String playerName;
    private ArrayList<Gamer> playerList;
    private JPanel gameInformationPanel;
    private JPanel buttons;
    private Container contentPane;

    public GameLobby(GameServiceRemote games, GameImpl gameImpl, String playerName) throws RemoteException {
        this.gameId = gameImpl.getGameID();
        this.games = games;
        this.playerName = playerName;

        setTitle("Game Lobby");
        setSize(500,400);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setVisible(true);



        contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());
        gameInformationPanel = new JPanel();
        gameInformationPanel.setLayout(new BoxLayout(gameInformationPanel,BoxLayout.PAGE_AXIS));
        buttons = new JPanel();
        buttons.setLayout(new FlowLayout());
        buildInfoscreen();
        JButton startGame = new JButton("Start Game");
        JButton leaveGame = new JButton("Leave Game");
        JButton ready = new JButton("ready");
        buttons.add(leaveGame);
        buttons.add(startGame);
        buttons.add(ready);
        leaveGame.addActionListener((action)->{
            try {
                this.games.leaveGame(this.playerName, this.gameId);
                System.exit(0);
            } catch (RemoteException e) {
                handleServerError();
            }
        });
        ready.addActionListener((action)->{
            try {
                this.games.toggleReady(this.playerName, this.gameId);
            } catch (RemoteException e) {
                handleServerError();
            }
        });
        startGame.addActionListener((action)->{
             try {
                 if(this.games.areAllReady(this.gameId)){
                    this.games.initGameStart(this.gameId);
                 }else{
                     JOptionPane.showMessageDialog(this, "Start is only possible if all players are ready", "error",JOptionPane.ERROR_MESSAGE);
                 }
             } catch (RemoteException e) {
                 handleServerError();
             }
         });
        contentPane.add(gameInformationPanel, BorderLayout.CENTER);
        contentPane.add(buttons, BorderLayout.SOUTH);
    }

    public void buildInfoscreen() throws RemoteException {

        this.playerList = this.games.getGamers(gameId);
        this.gameInformationPanel.removeAll();


        this.playerList.forEach((player)->{
            String isReady = player.isReady()?"ready": "not ready";
            System.out.println(isReady);
            this.gameInformationPanel.add(new JLabel(player.getName()+" is "+isReady ));
            this.gameInformationPanel.revalidate();
        });


    }

    @Override
    public void update() {
        try {
            buildInfoscreen();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }


    private void handleServerError(){
        this.games = RegistryService.getGameService();
        JOptionPane.showMessageDialog(this, "Some Server Error happend, pls try again", "error",JOptionPane.ERROR_MESSAGE);
    }
}
