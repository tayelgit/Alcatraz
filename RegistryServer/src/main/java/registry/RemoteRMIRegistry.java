/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package registry;

import java.io.*;
import java.rmi.*;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ObjID;
import java.rmi.server.ServerNotActiveException;
import java.util.HashMap;
import java.util.Map;

import sun.rmi.server.UnicastServerRef;
import sun.rmi.transport.LiveRef;

/**
 *
 * @author gk
 */
public class RemoteRMIRegistry extends RemoteServer implements Registry {

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
    public RemoteRMIRegistry(int port, String bindingFile) throws ClassNotFoundException, IOException {

        // Ablauf
        // todo: 1. Andere RMI Registry über Spread abfragen


        // 2. Lokale Datei einlesen bzw. neu anlegen
        try {
            this.bindingFile = bindingFile;
            ObjectInputStream input = new ObjectInputStream(new FileInputStream(bindingFile));
            MarshalledObject inputObject = (MarshalledObject) input.readObject();
            objectServers = (HashMap<String, BoundHosts>) inputObject.get();
            input.close();
        } catch (FileNotFoundException e) {
            // No other registry, no persistent file -> new run of Registry server(s)
            objectServers = new HashMap<String, BoundHosts>();
        }

        LiveRef lr = new LiveRef(new ObjID(ObjID.REGISTRY_ID), port);
        new UnicastServerRef(lr).exportObject(this, null);
    }


    // Methods to implement from the Registry interface

    public Remote lookup(String name)
            throws RemoteException, NotBoundException, AccessException {
        checkArguments(name);
        checkRemoteObjectExists(name);
        return objectServers.get(name).stub;
    }

    public void bind(String name, Remote obj)
            throws RemoteException, AlreadyBoundException, AccessException {
        String hostname = getHostname();

        checkArguments(name, obj);
        checkRemoteObjectBound(name, hostname);

        // todo: spread
        addObjectServer(name, new BoundHosts(hostname, obj));
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
    }

    public String[] list() throws RemoteException, AccessException {
        //return objectServers.keySet().toArray(new String[objectServers.size()]);
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

    private synchronized void addObjectServer(String name, BoundHosts hosts) {
        objectServers.put(name, hosts);
        persistBoundHosts();
    }

    private synchronized void removeObjectServer(String name) {
        objectServers.remove(name);
        persistBoundHosts();
    }

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
