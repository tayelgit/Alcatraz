package AlcatrazRemote.Interface;




import AlcatrazLocal.GameLocal;
import AlcatrazLocal.Gamer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

public interface GameServiceRemote extends Remote {

    //void register(Gamer game) ;
    //void unregister(Gamer game);

    ArrayList listGames()throws RemoteException;
    UUID createGame() throws RemoteException;

    void joindGame(String gamer , UUID gameId) throws RemoteException;
    void leaveGame(String gamer , UUID gameId) throws RemoteException;


}
