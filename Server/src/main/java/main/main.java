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
    public static void main(String [ ] args) throws RemoteException, UnknownHostException, SpreadException {
        Registry registry = LocateRegistry.createRegistry(5099);
        GameServiceImpl game = new GameServiceImpl();

        registry.rebind("gamelist", game);

        System.out.println(game.createGame("My Game", 2).toString());
        System.out.println(game.createGame("Some other Game", 4).toString());
        System.out.println(game.createGame("Whatever Game", 3).toString());



        SpreadWrapper wrapper = new SpreadWrapper("Server1", "localhost", game);

        wrapper.joinGroup(GroupEnum.SERVER_GROUP);
        wrapper.joinGroup(GroupEnum.FAULTTOLERANCE_GROUP);

        GroupEnum[] groups = {
            GroupEnum.SERVER_GROUP,
            GroupEnum.FAULTTOLERANCE_GROUP
        };

        wrapper.sendCustomMessage("test1", groups);
        wrapper.sendCustomMessage("test2", new String[] {
            GroupEnum.FAULTTOLERANCE_GROUP.toString(),
            GroupEnum.REGISTRY_GROUP.toString()
        });

    }
}
