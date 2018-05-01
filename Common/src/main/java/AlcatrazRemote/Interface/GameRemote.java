package AlcatrazRemote.Interface;

import AlcatrazLocal.Gamer;
import at.falb.games.alcatraz.api.Prisoner;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.UUID;

public interface GameRemote extends Remote {

//get Gamer method?

    UUID getGameID() ;
  //  void ping();
    //void addGamer(String gamer)throws RemoteException;
    //void removeGamer(String gamer)throws RemoteException;
    void startGame(ArrayList gamer) throws RemoteException;
  //  void passMove(String gamer, Prisoner prisoner);
   // void confirmMove();
   // void passToken(String gamer);
 //   void confirmTokenPass();
}
