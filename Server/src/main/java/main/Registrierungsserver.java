package main;

import AlcatrazRemote.Implementation.GameServiceImpl;
import spread.SpreadException;
import spread.SpreadWrapper;

import java.net.UnknownHostException;
import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;

public class Registrierungsserver {
    /**
     * RMI Registry Addresses - known addresses
     */
    private ArrayList<String> rmiRegistryAddresses;
    /**
     * Remote Registry for RMI
     */
    private Registry registry;

    /**
     * GameService
     */
    private GameServiceImpl game;

    /**
     *
     */
    private SpreadWrapper spread;


    /**
     *
     */
    public Registrierungsserver()
            throws RemoteException, NotBoundException {
        try {
            this.game = new GameServiceImpl();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        this.rmiRegistryAddresses.add("192.168.21.110");

        bindToRmiRegistry();
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
            rmiRegistryAddresses.add("192.168.21.110");

        this.rmiRegistryAddresses = rmiRegistryAddresses;

        bindToRmiRegistry();
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

        for (String address : rmiRegistryAddresses) {
            try {
                registry = LocateRegistry.getRegistry(address, 1099);
            } catch (RemoteException e) {
                e.printStackTrace();
                System.out.println("Couldn't create reference to " + address + ". Trying next address...");
                break;
            }

            this.registry = (Registry) registry.lookup("reg");
        }

        try {
            this.registry.bind("gamelist", this.game);
        } catch (AlreadyBoundException e) {
            e.printStackTrace();
            System.out.println("Already bound - someone was faster...");
        }
    }

    /**
     * Join in the Spread Communication
     * @param privateName   private Name
     * @param hostName      hostname that is hosting daemon (localhost)
     * @throws SpreadException
     * @throws UnknownHostException
     */
    private void joinSpread(String privateName, String hostName) throws SpreadException, UnknownHostException {
        spread = new SpreadWrapper(privateName, hostName, this.game);
        spread.joinGroup(SpreadWrapper.GroupEnum.SERVER_GROUP);
        spread.joinGroup(SpreadWrapper.GroupEnum.FAULTTOLERANCE_GROUP);
    }
}
