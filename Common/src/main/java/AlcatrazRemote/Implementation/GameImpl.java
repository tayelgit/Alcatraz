package AlcatrazRemote.Implementation;

import AlcatrazRemote.Interface.GameRemote;
import at.falb.games.alcatraz.api.Alcatraz;
import communctation.Interface.Observable;
import communctation.Interface.Observer;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;


public class GameImpl extends UnicastRemoteObject implements GameRemote , Observable {
    private UUID gameID;
    private ArrayList<String> gamerList ;
    private Alcatraz alcatraz;

    private Observer observer;
    public GameImpl(UUID gameID) throws RemoteException {
        super();
        this.gameID = gameID;
        this. gamerList = new ArrayList<>();
    }

    public UUID getGameID(){
        return this.gameID;
    }

    @Override
    public void notifyStateChanged() throws RemoteException {
        this.observer.update();
    }

    @Override
    public void startGame(ArrayList gamer) throws RemoteException {
        this.gamerList = new ArrayList<String>(gamer);
        System.out.print("New Game has started");
    }

    public void setObserver(Observer observer) {
        this.observer = observer;
    }

}
