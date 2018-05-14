package main;

import AlcatrazRemote.Implementation.GameServiceImpl;
import spread.SpreadException;
import spread.SpreadWrapper;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Registrierungsserver {
    private ArrayList<String> rmiRegistryAddresses;
    private Registry registry;
    private GameServiceImpl game;
    private SpreadWrapper spread;

    public Registrierungsserver()
            throws RemoteException, NotBoundException {
        try {
            this.game = new GameServiceImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        this.rmiRegistryAddresses = new ArrayList<String>();
        this.rmiRegistryAddresses.add("192.168.176.1");
        //this.rmiRegistryAddresses.add("192.168.21.110");

        bindToRmiRegistry();

        this.game.createGame("My Game", 2);
        this.game.createGame("Some other Game", 4);
        this.game.createGame("Whatever Game", 3);

        try {
            joinSpread("privateName", "localhost");
        } catch (SpreadException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     * Constructor
     * @param rmiRegistryAddresses the addresses known to connect to
     */
    public Registrierungsserver(ArrayList<String> rmiRegistryAddresses)
            throws RemoteException, NotBoundException {
        try {
            this.game = new GameServiceImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        if (rmiRegistryAddresses.isEmpty())
            this.rmiRegistryAddresses.add("192.168.176.1");
            //rmiRegistryAddresses.add("192.168.21.110");

        this.rmiRegistryAddresses = rmiRegistryAddresses;

        bindToRmiRegistry();

        this.game.createGame("My Game", 2);
        this.game.createGame("Some other Game", 4);
        this.game.createGame("Whatever Game", 3);

        try {
            joinSpread("privateName", "localhost");
        } catch (SpreadException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param rmiRegistryAddresses
     */
    public void setRmiRegistryAddresses(ArrayList<String> rmiRegistryAddresses) {
        this.rmiRegistryAddresses = rmiRegistryAddresses;
    }

    /**
     *
     * @throws RemoteException
     * @throws NotBoundException
     */
    public void bindToRmiRegistry() throws RemoteException, NotBoundException {
        Registry registry;

        // TODO: Registry sollte aus RegistryServer kommen?!

        for (String address : rmiRegistryAddresses) {
            try {
                registry = LocateRegistry.getRegistry(address, 1099);
            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("Couldn't create reference to " + address + ". Trying next address...");
                continue;
            }

            this.registry = (Registry) registry.lookup("reg");
        }

        this.registry.rebind("gamelist", this.game);
    }

    /**
     * Join in the Spread Communication
     * @param privateName   private Name
     * @param hostName      hostname that is hosting daemon (localhost)
     * @throws SpreadException
     * @throws UnknownHostException
     */
    private void joinSpread(String privateName, String hostName) throws SpreadException, UnknownHostException {
        this.spread = new SpreadWrapper(privateName, hostName);
        this.spread.addReplicateGameMessageListener(this.game);
        this.spread.joinGroup(SpreadWrapper.GroupEnum.SERVER_GROUP);
        this.spread.joinGroup(SpreadWrapper.GroupEnum.FAULTTOLERANCE_GROUP);
    }
}
