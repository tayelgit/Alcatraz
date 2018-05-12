package AlcatrazRemote.Implementation;

import AlcatrazLocal.GameLocal;
import AlcatrazLocal.Gamer;
import AlcatrazRemote.Interface.GameRemote;
import AlcatrazRemote.Interface.GameServiceRemote;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.UUID;

public class GameServiceImpl extends UnicastRemoteObject implements GameServiceRemote{

    //private ArrayList<GameRemote> gameList;
    private ArrayList<GameLocal> gameLocalList;
    private Registry registry;
    public GameServiceImpl() throws RemoteException {
        super();
      //  this.gameList = new ArrayList<>();
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
        return uuid;
    }

    @Override
    public void joinGame(String gamerName, UUID gameId) throws RemoteException, ServerNotActiveException {
        this.gameLocalList.stream().filter((g ->  g.getGameID().equals(gameId))).findFirst()
                .get().addGamer(new Gamer(gamerName,"rmi://"+getClientHost()+":5090"));
    }

    @Override
    public void leaveGame(String gamer, UUID gameId) throws RemoteException {
        //this.gameLocalList.stream().filter((g ->  g.getGameID() == gameId)).findFirst().get().addGamer(gamer);
    }

    @Override
    public void initGameStart(UUID gameId) throws RemoteException {
        ArrayList<Gamer> gamers = this.gameLocalList.stream().filter((g ->  g.getGameID().equals(gameId))).findFirst()
                .get().getGamers();
        gamers.forEach((gamer)->{
            try {
                GameRemote games   = (GameRemote) Naming.lookup(gamer.getEndpoint()+"/gameClient") ;
                games.getGameID();
            } catch (NotBoundException | RemoteException | MalformedURLException e) {
                e.printStackTrace();
            }

        });
    }

    /**
     * Removes specific game from local list
     * Used by ReplicateObjectMessageListener
     * @param gameID The UUID of the game to be removed
     */
    public void removeGame(UUID gameID){
        this.gameLocalList.remove(this.gameLocalList.stream().filter((g-> g.getGameID().equals(gameID))).findFirst().get());
    }

    /**
     * Sets the List of GameLocal-s to a specific list
     * Used by ReplicateObjectMessageListener
     * @param gameLocalList The new list to be used
     */
    public void setGameLocalList(ArrayList<GameLocal> gameLocalList) {
        this.gameLocalList = gameLocalList;
    }

    /**
     * Removes a specific game and adds the game with the same UUID again
     * @param game  to be updated
     */
    public void updateGame(GameLocal game) {
        removeGame(game.getGameID());
        gameLocalList.add(game);
    }
}
