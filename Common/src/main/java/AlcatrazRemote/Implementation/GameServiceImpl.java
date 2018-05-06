package AlcatrazRemote.Implementation;

import AlcatrazLocal.GameLocal;
import AlcatrazLocal.Gamer;
import AlcatrazRemote.Interface.GameRemote;
import AlcatrazRemote.Interface.GameServiceRemote;

import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;

public class GameServiceImpl extends UnicastRemoteObject implements GameServiceRemote{

    private ArrayList<GameRemote> gameList;
    private ArrayList<GameLocal> gameLocalList;
    private Registry registry;
    public GameServiceImpl() throws RemoteException {
        super();
        this.gameList = new ArrayList<>();
        this.gameLocalList = new ArrayList<>();

    }
/*
    @Override
    public void register(Gamer game) { }
    @Override
   public void unregister(Gamer game) { }
*/
    @Override
    public ArrayList<GameLocal> listGames() {
        return this.gameLocalList;
    }

    @Override
    public UUID createGame(String gameName, int playerCount) throws RemoteException {
        UUID uuid = UUID.randomUUID();
        GameLocal game = new GameLocal(uuid, gameName,  playerCount);
        gameLocalList.add(game);

//        System.out.println( );

        return uuid;
    }

    @Override
    public void joindGame(String gamerName, UUID gameId) throws RemoteException, ServerNotActiveException {
        this.gameLocalList.stream().filter((g ->  g.getGameID().equals(gameId))).findFirst()
                .get().addGamer(new Gamer(gamerName,getClientHost()+"5099"));
    }

    @Override
    public void leaveGame(String gamer, UUID gameId) throws RemoteException {
        //this.gameLocalList.stream().filter((g ->  g.getGameID() == gameId)).findFirst().get().addGamer(gamer);
    }
}
