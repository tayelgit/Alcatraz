package Service.Alcatraz.AlcatrazRemote.Interface;

import Service.Alcatraz.serviceData.Gamer;
import at.falb.games.alcatraz.api.Player;
import at.falb.games.alcatraz.api.Prisoner;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public interface GameRemote extends Remote{


    UUID getGameID() throws RemoteException ;

    void notifyStateChanged() throws RemoteException;
    //void ping();
    void startGame(Map<String, Gamer> gamer , String myPlayerName) throws RemoteException;
    void doOthersMove(String playerName, Player player, Prisoner prisoner, int rowOrCol, int row, int col)throws  RemoteException;

    void ping() throws RemoteException;
   // void passToken(String playerName)throws  RemoteException;
    void abortGame()throws  RemoteException;
    //void confirmTokenPass();
}
