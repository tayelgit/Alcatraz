/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package RemoteRMIRegistry;

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
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author gk
 */
public class RemoteRMIRegistry extends UnicastRemoteObject implements Registry {

    // Variables
    private String bindingFile;
    private Map<String, BoundHosts> objectServers;

    // Private class which saves hostnames and stubs from bound hosts
    // Used for synchronization between Registry Servers and for persistence in a local hash map
    private static class BoundHosts implements Serializable {
        private String hostname;
        private Remote stub;

        private BoundHosts(String host, Remote stub) {
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
            MarshalledObject<HashMap<String, BoundHosts>> inputObject = (MarshalledObject) input.readObject();
            this.objectServers = (HashMap) inputObject.get();
            input.close();
        } catch (FileNotFoundException e) {
            // No other registry, no persistent file -> new run of Registry server(s)
            this.objectServers = new HashMap<>();
        }
    }


    // Methods to implement from the Registry interface

    public Remote lookup(String name)
            throws RemoteException, NotBoundException, AccessException {
        checkArguments(name);
        checkRemoteObjectExists(name);
        System.out.println("Return stub for remote object " + name);
        return objectServers.get(name).stub;
    }

    public void bind(String name, Remote obj)
            throws RemoteException, AlreadyBoundException, AccessException {
        String hostname = getHostname();

        checkArguments(name, obj);
        checkRemoteObjectBound(name, hostname);

        // todo: spread
        addObjectServer(name, new BoundHosts(hostname, obj));
        System.out.println("Remote object " + name + " now bound from host " + hostname);
    }

    public void unbind(String name)
            throws RemoteException, NotBoundException, AccessException {
        String hostname = getHostname();
        checkArguments(name);
        checkRemoteObjectExists(name);
        checkObjectServerAuthorized(name, hostname, "unbind");

        // todo: spread
        removeObjectServer(name);
    }

    public void rebind(String name, Remote obj)
            throws RemoteException, AccessException {
        String hostname = getHostname();
        checkArguments(name, obj);
        checkObjectServerAuthorized(name, hostname, "rebind");

        // todo: spread
        addObjectServer(name, new BoundHosts(hostname, obj));
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

    private synchronized void addObjectServer(String name, BoundHosts host) {
        objectServers.put(name, host);
        persistBoundHosts();
    }

    private synchronized void removeObjectServer(String name) {
        objectServers.remove(name);
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

    private void checkRemoteObjectBound(String name, String hostname) throws AlreadyBoundException {
        if (objectServers.containsKey(name)) {
            System.out.println("Remote object " + name + " already bound!");
            throw new AlreadyBoundException(name);
        }
    }

    private void checkObjectServerAuthorized(String name, String hostname, String method) throws AccessException {
        if (objectServers.containsKey(name) && !hostname.equals(objectServers.get(name).hostname)) {
            throw new AccessException("Only bound host is authorized to " + method + "remote object " + name + "!");
        }
    }
}
