package main;

import AlcatrazRemote.Implementation.GameServiceImpl;
import java.net.UnknownHostException;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import spread.SpreadWrapper;
import spread.SpreadWrapper.GroupEnum;
import spread.SpreadException;

public class main {
    public static void main(String [ ] args) throws RemoteException {
        Registry registry = LocateRegistry.createRegistry(5099);
        GameServiceImpl game = new GameServiceImpl();

        System.out.println(game.createGame().toString());
        System.out.println(game.createGame().toString());
        System.out.println(game.createGame().toString());

        registry.rebind("gamelist", game);


        System.out.println(game.createGame("My Game", 2).toString());
        System.out.println(game.createGame("Some other Game", 4).toString());
        System.out.println(game.createGame("Whatever Game", 3).toString());




        SpreadWrapper wrapper = new SpreadWrapper("Server1", "localhost");
        wrapper.joinGroup(GroupEnum.SERVER_GROUP);
        wrapper.joinGroup(GroupEnum.FAULTOLERANCE_GROUP);

        GroupEnum[] a = {
            GroupEnum.SERVER_GROUP,
            GroupEnum.FAULTOLERANCE_GROUP
        };

        wrapper.sendCustomMessage("test1", a);
        wrapper.sendCustomMessage("test2", new String[] {"Registrierungsserver", "Fault Tolerance"});

    }
}
