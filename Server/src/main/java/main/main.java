package main;

import AlcatrazRemote.Implementation.GameServiceImpl;
import java.net.UnknownHostException;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import spread.SpreadWrapper;
import spread.SpreadWrapper.GroupEnum;
import spread.SpreadException;

public class main {
    public static void main(String [ ] args) throws  RemoteException, UnknownHostException,
            SpreadException, NotBoundException, AlreadyBoundException{

        //SpreadException, NotBoundException, AlreadyBoundException {
            //Registry registry = LocateRegistry.getRegistry("192.168.21.110",1099);
            //Registry reg = (Registry) registry.lookup("reg");

            //GameServiceImpl game = new GameServiceImpl();
            //reg.bind("gamelist", game);


        Registry registry = LocateRegistry.createRegistry(5099);
        GameServiceImpl game = new GameServiceImpl();

        registry.rebind("gamelist", game);
        System.out.println("GameService is binded to port 5099");

        //GameServiceImpl game = new GameServiceImpl();
       // reg.bind("gamelist", game);


        game.createGame("My Game", 2);
        game.createGame("Some other Game", 4);
        game.createGame("Whatever Game", 3);

      /*  SpreadWrapper wrapper = new SpreadWrapper("Server1", "localhost");
        wrapper.joinGroup(GroupEnum.SERVER_GROUP);
        wrapper.joinGroup(GroupEnum.FAULTOLERANCE_GROUP);

        GroupEnum[] a = {
            GroupEnum.SERVER_GROUP,
            GroupEnum.FAULTOLERANCE_GROUP
        };

        wrapper.sendCustomMessage("test1", a);
        wrapper.sendCustomMessage("test2", new String[] {"Registrierungsserver", "Fault Tolerance"});
*/
    }
}
