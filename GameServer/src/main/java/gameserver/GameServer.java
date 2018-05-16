package gameserver;

import spread.SpreadException;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Enumeration;

public class GameServer {
    public static void main(String [ ] args) throws RemoteException, NotBoundException, InterruptedException, SpreadException {
        Registrierungsserver regServer = new Registrierungsserver();
    }
}
