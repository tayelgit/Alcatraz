/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RemoteRMIRegistry;

import com.google.common.collect.HashMultimap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.MarshalledObject;
import java.rmi.NotBoundException;
import java.rmi.AccessException;
import java.rmi.AlreadyBoundException;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
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

    // Private class which saves hostnames and stubs from bound hosts
    // Used for synchronization between Registry Servers and for persistence in a local hash map
    private static class BoundHost implements Serializable {
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
        // todo: 1. Andere RMI Registry über Spread abfragen


        // 2. Lokale Datei einlesen bzw. neu anlegen
        try {
            this.bindingFile = bindingFile;
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(bindingFile));
            MarshalledObject<HashMultimap<String, BoundHost>> inputObject = (MarshalledObject) input.readObject();
            this.objectServers = (HashMultimap) inputObject.get();
            input.close();
        } catch (FileNotFoundException e) {
            // No other registry, no persistent file -> new run of Registry server(s)
            this.objectServers = HashMultimap.create();
        }
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

        // todo: spread
        addObjectServer(name, new BoundHost(hostname, obj));
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

        // todo: spread
        removeObjectServer(name, hostname, operation.UNBIND.ordinal());
        System.out.println(objectServers.toString());
    }

    public void rebind(String name, Remote obj)
            throws RemoteException, AccessException {
        String hostname = getHostname();
        checkArguments(name, obj);
        removeObjectServer(name, hostname, operation.REBIND.ordinal());

        // todo: spread

        addObjectServer(name, new BoundHost(hostname, obj));
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

    private synchronized void addObjectServer(String name, BoundHost host) {
        objectServers.put(name, host);
        persistBoundHosts();
    }

    private synchronized void removeObjectServer(String name, String hostname, int method) {
        boolean hostFound = false;
        System.out.println("before: " + objectServers.toString());

        Set<BoundHost> hosts = objectServers.get(name);
        for (BoundHost host : hosts) {
            System.out.println(host.toString());
            if (host.hostname.equals(hostname)) {
                System.out.println("Removing binding " + name + " from host " + hostname);
                objectServers.remove(name, host);
                hostFound = true;
            }
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
}
