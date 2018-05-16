package rmiUtil;

import Service.Alcatraz.AlcatrazRemote.Interface.GameServiceRemote;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class RegistryService {

    private static  ArrayList<String> rmiRegistryAddresses = new ArrayList<String>() {{
        add("localhost");
       // add("localhost");
    }};
    public static Registry getRegistry(){
        Registry reg = null;
        try {


            for (String address : rmiRegistryAddresses) {
                try {
                    reg = LocateRegistry.getRegistry(address, 1099);
                } catch (RemoteException e) {
                    e.printStackTrace();
                    System.out.println("Couldn't create reference to " + address + ". Trying next address...");
                    continue;
                }

                reg = (Registry) reg.lookup("reg");
                break;
            }

        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
        return reg;
    }


    public static GameServiceRemote getGameService (){
        Registry reg = getRegistry();
        GameServiceRemote gameServiceRemote = null ;
        try {
            gameServiceRemote= (GameServiceRemote) reg.lookup("gamelist");
        } catch (RemoteException | NotBoundException e) {
            e.printStackTrace();
        }
        return gameServiceRemote;
    }
}
