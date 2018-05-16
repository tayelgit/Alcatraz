package Service.Alcatraz.AlcatrazRemote.Implementation;

import Service.Alcatraz.AlcatrazRemote.Interface.GameRemote;
import Service.Alcatraz.gameData.GameState;
import Service.Alcatraz.serviceData.Gamer;
import at.falb.games.alcatraz.api.Alcatraz;
import at.falb.games.alcatraz.api.MoveListener;
import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;
import communctation.Implementation.Ping;
import communctation.Interface.Observable;
import communctation.Interface.Observer;

import javax.swing.*;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.Timer;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class GameImpl extends UnicastRemoteObject implements GameRemote , Observable, MoveListener {
    private UUID gameID;
    private Map<String, Gamer> gamerList ;
    private Alcatraz alcatraz;
    private String playerName;
    private Observer observer;

    private int myPosition;

    private int maxPosition;
    private GameState gameState;
    private boolean error;
    private Timer timer;
    private Ping ping;

    public GameImpl(UUID gameID) throws RemoteException {
        super();
        this.gameID = gameID;

    }

    public UUID getGameID(){
        return this.gameID;
    }
    public void setObserver(Observer observer) {
        this.observer = observer;
    }

    @Override
    public void notifyStateChanged() throws RemoteException {
        this.observer.update();
    }

    @Override
    public void startGame(Map<String, Gamer> gamer , String myPlayerName) throws RemoteException, MalformedURLException, NotBoundException {
        ArrayList<Gamer> tempList =(ArrayList<Gamer>) gamer.values().stream().map(item -> new Gamer(item.getName(),item.getEndpoint())).collect(Collectors.toList());
        Gamer temp = tempList.stream().filter(g-> g.getName().equals(myPlayerName)).findFirst().get();
         timer = new Timer();
        this.maxPosition = gamer.values().size();
        this.playerName = myPlayerName;

        this.gameState = new GameState(tempList, maxPosition);
        this.myPosition = gameState.calculatePosition(temp);
        gamer.remove(myPlayerName);
        this.gamerList = gamer;


        this.alcatraz = new Alcatraz();
        this.alcatraz.addMoveListener(this);
        System.out.println(this.maxPosition);
        System.out.println(this.myPosition);
        this.alcatraz.init(this.maxPosition,this.myPosition);

        this.alcatraz.showWindow();
        this.alcatraz.start();
        //this.observer.close();
       if(myPosition != 0){
            GameRemote game = (GameRemote) Naming.lookup(this.gameState.getCurrentGamer().getEndpoint()+"/gameClient");
            timer.schedule(this.ping = new Ping(game,this), 0, 5000);
        }
    }


    public void initAbort(){
        gamerList.values().forEach((gamer) -> {
            try {
                System.out.println(gamer.getName());
                GameRemote game = (GameRemote) Naming.lookup(gamer.getEndpoint() + "/gameClient");
                game.abortGame();
            } catch (NotBoundException | MalformedURLException | RemoteException e) {

            }
        });
        try {
            this.abortGame();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void ping() throws RemoteException {
        try {
            System.out.println(getClientHost()+"ping");
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void abortGame() throws RemoteException {
        JOptionPane.showMessageDialog(this.alcatraz.getGameBoard(), "Error one Player left", "error",JOptionPane.ERROR_MESSAGE);
        this.alcatraz.closeWindow();
    }

    @Override
    public void doOthersMove(String playerName,Player player, Prisoner prisoner, int rowOrCol, int row, int col){
        if(this.ping != null && this.ping.isStarted())timer.cancel();
        alcatraz.doMove(alcatraz.getPlayer(player.getId()), alcatraz.getPrisoner(prisoner.getId()), rowOrCol, row, col);
        this.gameState.next();
    }


    @Override
    public void moveDone(Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
        this.gamerList.values().forEach((gamer)->{
            try {
                GameRemote game = (GameRemote) Naming.lookup(gamer.getEndpoint()+"/gameClient");
                game.doOthersMove(this.playerName, player, prisoner,  rowOrCol, row,  col);
            }
            catch (NotBoundException | RemoteException | MalformedURLException e) {

                this.error = true;
            }
        });
        if(this.error){ this.initAbort();}
        else{
            this.gameState.next();

            try {
                GameRemote game = (GameRemote) Naming.lookup(this.gameState.getCurrentGamer().getEndpoint()+"/gameClient");
                timer= new Timer();
                timer.schedule(this.ping = new Ping(game,this), 0, 5000);

                } catch (NotBoundException | MalformedURLException | RemoteException e) {
                    this.gamerList.remove(this.gameState.getCurrentGamer().getName());
                this.initAbort();
            }
        }

    }

    @Override
    public void gameWon(Player player)
    {
        JOptionPane.showMessageDialog(this.alcatraz.getGameBoard(), "Yeah you won!!!", "YOU WON!",JOptionPane.INFORMATION_MESSAGE);

    }
}
