package java.hello;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Created by Oli on 14.04.2018.
 */
public interface HelloService extends Remote {
    String echo() throws RemoteException;
    void setString(String input) throws RemoteException;
}
