package hello;

import hello.HelloService;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by Oli on 14.04.2018.
 */
public class HelloServer extends UnicastRemoteObject implements HelloService {


    private String killme;

    public HelloServer() throws RemoteException {
        super();
        this.killme="blaa";
    }

    @Override
    public String echo() throws RemoteException {
        return killme;
    }

    @Override
    public void setString(String input) {
        this.killme = input;
    }
}
