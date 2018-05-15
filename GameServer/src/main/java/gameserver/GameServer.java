package gameserver;

import java.net.UnknownHostException;

import java.rmi.AlreadyBoundException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import spread.SpreadException;

public class GameServer {
    public static void main(String [ ] args) throws RemoteException, NotBoundException, InterruptedException {
        Registrierungsserver regServer = new Registrierungsserver();
    }
}
