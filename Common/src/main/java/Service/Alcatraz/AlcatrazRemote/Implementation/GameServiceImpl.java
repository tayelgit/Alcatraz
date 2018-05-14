package Service.Alcatraz.AlcatrazRemote.Implementation;

import Service.Alcatraz.serviceData.GameLocal;
import Service.Alcatraz.serviceData.Gamer;
import Service.Alcatraz.AlcatrazRemote.Interface.GameRemote;
import Service.Alcatraz.AlcatrazRemote.Interface.GameServiceRemote;

import communctation.Interface.Observable;
import communctation.Interface.Observer;
import communctation.Interface.ServerReplication.GameStateObservable;
import communctation.Interface.ServerReplication.GameStateObserver;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class GameServiceImpl extends UnicastRemoteObject implements GameServiceRemote, GameStateObservable {

    private GameStateObserver observer;
    private Map<UUID,GameLocal> gameLocalList;

    public GameServiceImpl() throws RemoteException {
        super();
        this.gameLocalList = new HashMap<>();
        System.out.println("Game service has started");
    }

    @Override
    public ArrayList<GameLocal> listGames() {
        return new ArrayList<>(this.gameLocalList.values());
    }

    @Override
    public ArrayList getGamers(UUID gameId) throws RemoteException {
        return new ArrayList(this.gameLocalList.get(gameId).getGamers().values());
    }

    @Override
    public GameLocal createGame(String gameName, int playerCount) throws RemoteException {
        UUID uuid = UUID.randomUUID();
        GameLocal game = new GameLocal(uuid, gameName,  playerCount);
        this.gameLocalList.put(uuid,game);
        System.out.printf("Game: \"%s\" was created \n",game.getGameName());
        observer.replicateGameState(this.gameLocalList);
        return game;
    }

    @Override
    public boolean areAllReady(UUID gameId) throws RemoteException {
        return this.gameLocalList.get(gameId).areAllReady();
    }

    @Override
    public void notifyAll(UUID gameId) throws RemoteException {
        Map<String,Gamer> gamers = this.gameLocalList.get(gameId).getGamers();
        gamers.values().forEach((gamer)->{
            try {

                GameRemote gameClient= (GameRemote) Naming.lookup(gamer.getEndpoint()+"/gameClient") ;
                gameClient.notifyStateChanged();
            } catch (NotBoundException | RemoteException | MalformedURLException e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    public void toggleReady(String gamer, UUID gameId) throws RemoteException {
        this.gameLocalList.get(gameId).toggleReady(gamer);
        notifyAll(gameId);
    }


    @Override
    public void joinGame(String gamerName, UUID gameId) throws RemoteException, ServerNotActiveException {
        GameLocal game = this.gameLocalList.get(gameId);
        game.addGamer(new Gamer(gamerName,"rmi://"+getClientHost()+":5092"));
        System.out.printf("Gamer: \"%s\"  joined Game: \"%s\" \n",gamerName,game.getGameName());
        observer.replicateGameState(this.gameLocalList);
        notifyAll(gameId);
    }

    @Override
    public void leaveGame(String gamerName, UUID gameId) throws RemoteException {
        GameLocal game =this.gameLocalList.get(gameId);
        game.removeGamer(gamerName);
        System.out.printf("Gamer: \"%s\"  left Game: \"%s\"  \n",gamerName,game.getGameName());
        if(game.getTakenPlaces() == 0) this.gameLocalList.remove(gameId);
        else notifyAll(gameId);
        System.out.printf("No Players left, Game: \"%s\" was closed \n",gamerName,game.getGameName());
        observer.replicateGameState(this.gameLocalList);
    }

    @Override
    public void initGameStart(UUID gameId) throws RemoteException {
        GameLocal game = this.gameLocalList.get(gameId);
        Map<String, Gamer> gamers = game.getGamers();
        gamers.values().forEach((gamer)->{
            try {
                GameRemote games = (GameRemote) Naming.lookup(gamer.getEndpoint()+"/gameClient") ;
                games.startGame(gamers, gamer.getName());
            } catch (NotBoundException | RemoteException | MalformedURLException e) {
                e.printStackTrace();
            }
        });
        this.gameLocalList.remove(gameId);
        System.out.printf(" Game: \"%s\" has started \n",game.getGameName());
        observer.replicateGameState(this.gameLocalList);
    }

    /**
     * Removes specific game from local list
     * Used by ReplicateObjectMessageListener
     * @param gameID The UUID of the game to be removed
     */
    public void removeGame(UUID gameID){
        this.gameLocalList.remove(gameID);
    }

    /**
     * Sets the List of GameLocal-s to a specific list
     * Used by ReplicateObjectMessageListener
     * @param gameLocalList The new list to be used
     */
    public void setGameLocalList(Map gameLocalList) {
        this.gameLocalList = gameLocalList;
    }

    /**
     * Removes a specific game and adds the game with the same UUID again
     * @param game  to be updated
     */
    public void updateGame(GameLocal game) {
        removeGame(game.getGameID());
        gameLocalList.put(game.getGameID(),game);
    }

    /**
     * Sets the Observer - GameStateObserver
     * @param observer the GameStateObserver
     */
    @Override
    public void setGameStateObserver(GameStateObserver observer) {
        this.observer = observer;
    }
}
