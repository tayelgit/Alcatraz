package AlcatrazRemote.Interface;




import AlcatrazLocal.GameLocal;
import AlcatrazLocal.Gamer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.UUID;

public interface GameServiceRemote extends Remote {

    //void register(Gamer game) ;
    //void unregister(Gamer game);

    ArrayList listGames()throws RemoteException;
    UUID createGame(String gameName, int playerCount) throws RemoteException;

    void joindGame(String gamer , UUID gameId) throws RemoteException, ServerNotActiveException;
    void leaveGame(String gamer , UUID gameId) throws RemoteException;


}
