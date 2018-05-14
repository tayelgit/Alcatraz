package main;

import java.net.UnknownHostException;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import spread.SpreadException;

public class main {
    public static void main(String [ ] args) throws RemoteException, UnknownHostException,
            SpreadException, NotBoundException, AlreadyBoundException {

        // in default ctor is "192.168.21.110" already added
        Registrierungsserver regServer = new Registrierungsserver();

        /*
        Registry registry = LocateRegistry.getRegistry("192.168.21.110",1099);
        Registry reg = (Registry) registry.lookup("reg");

        GameServiceImpl game = new GameServiceImpl();
        reg.rebind("gamelist", game);

        game.createGame("My Game", 2);
        game.createGame("Some other Game", 4);
        game.createGame("Whatever Game", 3);
        */
    }
}
