package main;

import AlcatrazRemote.Implementation.GameServiceImpl;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class main {
    public static void main(String [ ] args) throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(5099);
        GameServiceImpl game = new GameServiceImpl();

        System.out.println(game.createGame("My Game", 2).toString());
        System.out.println(game.createGame("Some other Game", 4).toString());
        System.out.println(game.createGame("Whatever Game", 3).toString());


        registry.rebind("gamelist", game);

    }
}
