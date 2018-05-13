package AlcatrazRemote.Interface;




import AlcatrazLocal.GameLocal;
import AlcatrazLocal.Gamer;
import communctation.Interface.Observer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.UUID;

public interface GameServiceRemote extends Remote {


    ArrayList listGames()throws RemoteException;
    ArrayList getGamers(UUID gameId) throws RemoteException;
    GameLocal createGame(String gameName, int playerCount) throws RemoteException;

    boolean areAllReady(UUID gameId) throws  RemoteException;
    void notifyAll(UUID gameId) throws RemoteException;
    void toggleReady(String gamer, UUID gameId) throws RemoteException;
    void joinGame(String gamer , UUID gameId) throws RemoteException, ServerNotActiveException;
    void leaveGame(String gamer , UUID gameId) throws RemoteException;
    void initGameStart(UUID gameId) throws RemoteException;


}
