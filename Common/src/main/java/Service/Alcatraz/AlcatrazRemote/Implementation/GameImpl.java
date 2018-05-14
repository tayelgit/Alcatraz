package Service.Alcatraz.AlcatrazRemote.Implementation;

import Service.Alcatraz.AlcatrazRemote.Interface.GameRemote;
import Service.Alcatraz.serviceData.Gamer;
import at.falb.games.alcatraz.api.Alcatraz;
import at.falb.games.alcatraz.api.MoveListener;
import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;
import communctation.Interface.Observable;
import communctation.Interface.Observer;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;


public class GameImpl extends UnicastRemoteObject implements GameRemote , Observable, MoveListener {
    private UUID gameID;
    private Map<String, Gamer> gamerList ;
    private Alcatraz alcatraz;
    private String playerName;
    private Observer observer;

    private int myPosition;
    private int currentPosition = 0;
    private int maxPosition;

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

        this.myPosition = getmyNumber(new ArrayList<Gamer>(gamer.values()), gamer.get(myPlayerName));
        this.maxPosition = gamer.values().size();
        this.playerName = myPlayerName;

        gamer.remove(myPlayerName);
        this.gamerList = gamer;
        this.alcatraz = new Alcatraz();
        this.alcatraz.addMoveListener(this);

    }
    private int getmyNumber(ArrayList<Gamer> gamers , Gamer myGamerObject) {
        gamers.sort(Comparator.comparing(Gamer::getName));
        return  gamers.indexOf(myGamerObject);
    }
    public void startNewRound(){

    }
    public void prepareNewRound(){

        this.gamerList.values().forEach((gamer)->{
            gamer.setConfirmed(false);
        });
        if(currentPosition == myPosition){
            //passtoken(currentPosition = currentPosition++ % numberofPlayers)
            startNewRound();
        }
    }


    @Override
    public void confirmMove(String playerName) throws RemoteException{
        this.gamerList.get(playerName).setConfirmed(true);
    }
    @Override
    public void doOthersMove(String playerName,Player player, Prisoner prisoner, int rowOrCol, int row, int col){
        alcatraz.doMove(alcatraz.getPlayer(player.getId()), alcatraz.getPrisoner(prisoner.getId()), rowOrCol, row, col);
        Gamer gamer = this.gamerList.get(playerName);
        try {
            GameRemote game = (GameRemote) Naming.lookup(gamer.getEndpoint()+"/gameClient");
            game.confirmMove(playerName);
        }
        catch (NotBoundException | RemoteException | MalformedURLException e) {
            e.printStackTrace();
        }
    }


    public void doMove(Player player, Prisoner prisoner, int rowOrCol, int row, int col){
        alcatraz.doMove(alcatraz.getPlayer(player.getId()), alcatraz.getPrisoner(prisoner.getId()), rowOrCol, row, col);
    }

    //Listens on Ui for valid move.
    @Override
    public void moveDone(Player player, Prisoner prisoner, int rowOrCol, int row, int col) {
        doMove(player,prisoner,rowOrCol,row,col);
        this.gamerList.values().forEach((gamer)->{
            try {
                GameRemote game = (GameRemote) Naming.lookup(gamer.getEndpoint()+"/gameClient");
                game.doOthersMove(this.playerName, player, prisoner,  rowOrCol, row,  col);
            }
            catch (NotBoundException | RemoteException | MalformedURLException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void gameWon(Player player) {

    }
}
