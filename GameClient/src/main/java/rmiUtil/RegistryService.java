package rmiUtil;

import Service.Alcatraz.AlcatrazRemote.Interface.GameServiceRemote;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class RegistryService {

    private static  ArrayList<String> rmiRegistryAddresses = new ArrayList<String>() {{
        add("192.168.21.107");
        add("192.168.21.110");
    }};

    public static Registry getRegistry() {
        Registry reg = null;
        for (String address : rmiRegistryAddresses) {
            try {
                reg = LocateRegistry.getRegistry(address, 1099);
            } catch (RemoteException e) {
                e.printStackTrace();
            } try {
                reg = (Registry) reg.lookup("reg");
            } catch (RemoteException | NotBoundException e) {
                e.printStackTrace();
                System.out.println("Couldn't make lookup at " + address + ". Trying next address...");
                continue;
                }
            break;
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
