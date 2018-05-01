package AlcatrazRemote.Implementation;

import AlcatrazLocal.Gamer;
import AlcatrazRemote.Interface.GameRemote;
import at.falb.games.alcatraz.api.Prisoner;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;


public class GameImpl extends UnicastRemoteObject implements GameRemote {
    private UUID gameID;
    private ArrayList<String> gamerList ;

    public GameImpl(UUID gameID) throws RemoteException {
        super();
        this.gameID = gameID;
        this. gamerList = new ArrayList<>();
    }

    public UUID getGameID(){
        return this.gameID;
    }

    @Override
    public void startGame(ArrayList gamer) throws RemoteException {
        this.gamerList = new ArrayList<String>(gamer);
        System.out.print("New Game has started");
    }


}
