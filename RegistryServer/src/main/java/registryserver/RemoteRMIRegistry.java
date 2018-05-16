/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package registryserver;

import com.google.common.collect.HashMultimap;
import communication.Spread.ReplicateObjectMessageFactory;
import communication.Spread.ReplicateRMIMessageListener;
import communication.Spread.SpreadWrapper;
import spread.SpreadException;
import spread.SpreadMessage;

import java.io.*;
import java.net.UnknownHostException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.MarshalledObject;
import java.rmi.NotBoundException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Set;


/**
 *
 * @author gk
 */
public class RemoteRMIRegistry extends UnicastRemoteObject implements Registry {

    // Variables
    private static final long serialVersionUID = 1L;
    private String bindingFile;
    private HashMultimap<String, BoundHost> objectServers;
    private enum operation {
        REBIND, UNBIND
    }
    private SpreadWrapper spread;
    static int spreadNameIncrementer = 0;
    private HashMap<String, String> spreadBoundHosts;


    // Public class which saves hostnames and stubs from bound hosts
    // Used for synchronization between Registry Servers and for persistence in a local hash map
    // edit Carlos: is now public so it is accessible by ReplicateRMIMessageListener
    public static class BoundHost implements Serializable {
        private String hostname;
        private Remote stub;

        private BoundHost(String host, Remote stub) {
            this.hostname = host;
            this.stub = stub;
        }
    }


    // Constructor
    @SuppressWarnings("unchecked")
    public RemoteRMIRegistry(String bindingFile) throws ClassNotFoundException,
            IOException, RemoteException {

        super();

        // Ablauf
        // 1. Lokale Datei einlesen bzw. neu anlegen
        try {
            this.bindingFile = bindingFile;
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(bindingFile));
            MarshalledObject<HashMultimap<String, BoundHost>> inputObject = (MarshalledObject) input.readObject();
            this.objectServers = (HashMultimap) inputObject.get();
            input.close();
        } catch (FileNotFoundException e) {
            // No other registry, no persistent file -> new run of Registry gameserver(s)
            this.objectServers = HashMultimap.create();
        }

        // 2. Spread joinen - ggf wird objectServers ueberschrieben
        try {
            joinSpread("rmi" + spreadNameIncrementer++, "localhost");
        } catch (SpreadException e) {
            e.printStackTrace();
        }
        this.spreadBoundHosts = new HashMap<String, String>();
    }


    // Methods to implement from the Registry interface

    public Remote lookup(String name)
            throws RemoteException, NotBoundException, AccessException {
        checkArguments(name);
        checkRemoteObjectExists(name);
        System.out.println("Return stub for remote object " + name);
        Set<BoundHost> hosts = objectServers.get(name);
        BoundHost first = hosts.iterator().next();
        return first.stub;
    }

    public void bind(String name, Remote obj)
            throws RemoteException, AlreadyBoundException, AccessException {
        String hostname = getHostname();
        checkArguments(name, obj);
        checkObjectServerBound(name, hostname);
        try {
            addObjectServer(name, new BoundHost(hostname, obj));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Remote object " + name + " now bound from host " + hostname);
        Set<BoundHost> hosts = objectServers.get(name);
        for (BoundHost host : hosts) {
            System.out.println("Hostnames with binding to " + name + ": " + host.hostname);
        }
    }

    public void unbind(String name)
            throws RemoteException, NotBoundException, AccessException {
        String hostname = getHostname();
        checkArguments(name);
        checkRemoteObjectExists(name);
        try {
            removeObjectServer(name, hostname, operation.UNBIND.ordinal());
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(objectServers.toString());
    }

    public void rebind(String name, Remote obj)
            throws RemoteException, AccessException {
        String hostname = getHostname();
        checkArguments(name, obj);
        try {
            removeObjectServer(name, hostname, operation.REBIND.ordinal());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            addObjectServer(name, new BoundHost(hostname, obj));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Remote object " + name + " now bound from host " + hostname);
    }

    public String[] list() throws RemoteException, AccessException {
        return objectServers.keySet().toArray(new String[0]);
    }

    // Private helper methods

    private String getHostname() throws RemoteException {
        String hostname;

        try {
            hostname = getClientHost();
        } catch (ServerNotActiveException e) {
            e.printStackTrace();
            throw new RemoteException(e.getMessage());
        }

        return hostname;
    }

    private synchronized void addObjectServer(String name, BoundHost host) throws IOException {
        objectServers.put(name, host);
        persistBoundHosts();
        replicateRMIRegistryState();
    }

    private synchronized void removeObjectServer(String name, String hostname, int method) throws IOException {
        BoundHost deleteHost = null;
        boolean hostFound = false;
        System.out.println("before: " + objectServers.toString());

        Set<BoundHost> hosts = objectServers.get(name);
        for (BoundHost host : hosts) {
            System.out.println(host.toString());
            if (host.hostname.equals(hostname)) {
                System.out.println("Removing binding " + name + " from host " + hostname);
                deleteHost = host;
                hostFound = true;
            }
        }

        if (hostFound) {
            objectServers.remove(name, deleteHost);
        }
        System.out.println("after: " + objectServers.toString());

        if (!hostFound) {
            if (method == operation.REBIND.ordinal()) {
                System.out.println("Remote object " + name +
                        " was not bound from host " + hostname + ". Doing normal bind...");
            }
            else if (method == operation.UNBIND.ordinal()) {
                System.out.println("Remote object " + name +
                        " was not bound from host " + hostname + ". Nothing to do...");
            }
        }
        persistBoundHosts();
        replicateRMIRegistryState();
    }

    /**
     * Used by ReplicateRMIMessageListener to replicate state
     * @param objectServers
     */
    public synchronized void setObjectServers(HashMultimap<String, BoundHost> objectServers) {
        this.objectServers = objectServers;
    }

    @SuppressWarnings("unchecked")
    private void persistBoundHosts() {
        try {
            ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(bindingFile));
            output.writeObject(new MarshalledObject(objectServers));
            output.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void checkArguments(Object... args) {
        for (Object arg : args) {
            if (arg == null) {
                throw new NullPointerException();
            }
        }
    }

    private void checkRemoteObjectExists(String name) throws NotBoundException {
        if (!objectServers.containsKey(name)) {
            System.out.println("Remote object " + name + " does not exist!");
            throw new NotBoundException(name);
        }
    }

    private void checkObjectServerBound(String name, String hostname) throws AlreadyBoundException {
        if (objectServers.containsKey(name)) {
            Set<BoundHost> hosts = objectServers.get(name);
            for (BoundHost host : hosts) {
                if (host.hostname.equals(hostname)) {
                    System.out.println("Remote object " + name + " already bound from host " + hostname + "!");
                    throw new AlreadyBoundException(name);
                }
            }
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
        this.spread = new SpreadWrapper(privateName, hostName);
        this.spread.addReplicateRMIMessageListener(new ReplicateRMIMessageListener(this));
        this.spread.joinGroup(SpreadWrapper.GroupEnum.REGISTRY_GROUP);
        this.spread.joinGroup(SpreadWrapper.GroupEnum.FAULTTOLERANCE_GROUP);
    }

    /**
     * Sends the objectServers HashMultimap via Spread
     */
    public void replicateRMIRegistryState() throws IOException {
        ReplicateObjectMessageFactory factory = new ReplicateObjectMessageFactory();

        SpreadMessage message;
        try {
            message = factory.createMessage("UPDATE_RMIREGISTRY", new MarshalledObject<HashMultimap>(this.objectServers));
            message.addGroup(SpreadWrapper.GroupEnum.REGISTRY_GROUP.toString());
            this.spread.sendMessage(message);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }


    public void addSpreadBoundHost(String key_PrivateName, String value_IP) {
        this.spreadBoundHosts.put(key_PrivateName, value_IP);
    }

    public void setSpreadBoundHosts(HashMap<String, String> spreadBoundHosts) {
        this.spreadBoundHosts = spreadBoundHosts;
    }

    public HashMap<String, String> getSpreadBoundHosts() {
        return this.spreadBoundHosts;
    }

    public void replicateSpreadBoundHosts() {
        ReplicateObjectMessageFactory factory = new ReplicateObjectMessageFactory();

        SpreadMessage message;
        try {
            message = factory.createMessage("UPDATE_RMI_SPREADBOUND_HOSTS", this.spreadBoundHosts);
            message.addGroup(SpreadWrapper.GroupEnum.REGISTRY_GROUP.toString());
            this.spread.sendMessage(message);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

}
