package AlcatrazRemote.Implementation;

import AlcatrazLocal.GameLocal;
import AlcatrazRemote.Interface.GameRemote;
import AlcatrazRemote.Interface.GameServiceRemote;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;

public class GameServiceImpl extends UnicastRemoteObject implements GameServiceRemote{

    private ArrayList<GameRemote> gameList;
    private ArrayList<GameLocal> gameLocalList;

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
        this.gameLocalList.forEach( s -> System.out.println(s.toString()));

        return this.gameLocalList;
    }

    @Override
    public UUID createGame() throws RemoteException {
        UUID uuid = UUID.randomUUID();
        GameLocal game = new GameLocal(uuid);
        gameLocalList.add(game);
        return uuid;
    }

    @Override
    public void joindGame(String gamer, UUID gameId) throws RemoteException {
        this.gameLocalList.stream().filter((g ->  g.getGameID() == gameId)).findFirst().get().addGamer(gamer);
    }

    @Override
    public void leaveGame(String gamer, UUID gameId) throws RemoteException {
        this.gameLocalList.stream().filter((g ->  g.getGameID() == gameId)).findFirst().get().addGamer(gamer);
    }
}
