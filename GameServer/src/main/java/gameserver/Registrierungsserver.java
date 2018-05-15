package gameserver;

import Service.Alcatraz.AlcatrazRemote.Implementation.GameServiceImpl;
import Service.Alcatraz.serviceData.GameLocal;
import communctation.Interface.ServerReplication.GameStateObserver;
import communication.Spread.ReplicateObjectMessageFactory;
import communication.Spread.SpreadWrapper;
import spread.SpreadException;
import spread.SpreadMessage;

import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Registrierungsserver implements GameStateObserver {
    private ArrayList<String> rmiRegistryAddresses;
    private Registry registry;
    private GameServiceImpl game;
    private SpreadWrapper spread;

    public Registrierungsserver()
            throws RemoteException, NotBoundException, InterruptedException {
        try {
            this.game = new GameServiceImpl();
            this.game.setGameStateObserver(this);

            joinSpread("rs1", "localhost");
        } catch (RemoteException | SpreadException | UnknownHostException e) {
            e.printStackTrace();
        }

        this.rmiRegistryAddresses = new ArrayList<String>();
        //this.rmiRegistryAddresses.add("127.0.0.1");
        this.rmiRegistryAddresses.add("192.168.21.110");

        bindToRmiRegistry();

        //this.game.createGame("My Game", 2);
        //this.game.createGame("Some other Game", 4);
        //this.game.createGame("Whatever Game", 3);

        Thread.sleep(30000);
        this.registry.unbind("gamelist");
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
            break;
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

    /**
     *
     * @param gameLocalList
     */
    @Override
    public void replicateGameState(Map<UUID, GameLocal> gameLocalList) {
        ReplicateObjectMessageFactory factory = new ReplicateObjectMessageFactory();

        SpreadMessage message;
        try {
            message = factory.createMessage("UPDATE_GAMELOCALLIST", (HashMap<UUID, GameLocal>)gameLocalList);
            this.spread.sendMessage(message);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }
}
