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

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.function.Predicate;


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
    public void startGame(Map<String, Gamer> gamer , String myPlayerName) throws RemoteException {
        Gamer temp = gamer.get(myPlayerName);
         timer = new Timer();
        this.maxPosition = gamer.values().size();
        this.playerName = myPlayerName;

        gamer.remove(myPlayerName);
        this.gamerList = gamer;
        this.gameState = new GameState(this.gamerList.values(),maxPosition);
        this.myPosition = gameState.calculatePosition(temp);


        this.alcatraz = new Alcatraz();
        this.alcatraz.addMoveListener(this);
        this.alcatraz.init(this.maxPosition,this.myPosition);
        this.alcatraz.showWindow();
    }


    //@Override
   /* public void confirm(String playerName) throws RemoteException{
        this.gamerList.get(playerName).setConfirmed();
        if(this.gameState.next()){
            //if(this.gameState.getCurrentState()== GameState.State.TOKEN_PASSED) passToken("");
            //else if(this.gameState.getCurrentState()== GameState.State.TOKEN_PASSED)startNewRound();
        } else{
            gamerList.values().stream().filter(((Predicate<Gamer>) Gamer::isConfirmed).negate()).forEach((gamer) -> {
                try {
                    GameRemote game = (GameRemote) Naming.lookup(gamer.getEndpoint() + "/gameClient");
                } catch (NotBoundException | MalformedURLException | RemoteException e) {
                    this.gamerList.remove(gamer.getName());
                }
            });
            gamerList.values().forEach((gamer) -> {
                try {
                    GameRemote game = (GameRemote) Naming.lookup(gamer.getEndpoint() + "/gameClient");
                    game.abortGame();
                } catch (NotBoundException | MalformedURLException | RemoteException e) {
                    e.printStackTrace();
                }
            });
        }
    }*/
    public void initAbort(){
        gamerList.values().forEach((gamer) -> {
            try {
                GameRemote game = (GameRemote) Naming.lookup(gamer.getEndpoint() + "/gameClient");
                game.abortGame();
            } catch (NotBoundException | MalformedURLException | RemoteException e) {
                e.printStackTrace();
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
        System.out.println("ping");
    }

    @Override
    public void abortGame() throws RemoteException {
       // this.alcatraz.closeWindow();
        System.out.println("Game is over ");
    }

    @Override
    public void doOthersMove(String playerName,Player player, Prisoner prisoner, int rowOrCol, int row, int col){
        timer.cancel();
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
                GameRemote game = (GameRemote) Naming.lookup(this.gameState.getCurrentGamer()+"/gameClient");
                timer.schedule(new Ping(game,this), 0, 5000);

                } catch (NotBoundException | MalformedURLException | RemoteException e) {
                this.initAbort();
            }
        }

    }

    @Override
    public void gameWon(Player player) {
        System.out.println("Yeah you won .....");
    }
}
