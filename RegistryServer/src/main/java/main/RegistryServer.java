package main;


import RemoteRMIRegistry.RemoteRMIRegistry;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;


public class RegistryServer {

    private static final int PORT_MIN = 1;
    private static final int PORT_MAX = 65535;

    public static void main(String [ ] args) throws IOException, ClassNotFoundException {

        int port;
        String bindingFile = ".bindings";
        Registry registry;

        //System.setProperty("java.security.policy", RegistryServer.class
         //       .getClassLoader().getResource("bindings.policy").toExternalForm());

        //System.setSecurityManager(new SecurityManager());

        // Parse given port number and check for common Exceptions
        try {
            port = Integer.valueOf(args[0]);
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException e) {
            // Do not exit, use default port
            System.out.println("No or invalid port number given. Using the default port (1099)");
            port = Registry.REGISTRY_PORT;
        }

        // Check if a valid port number was given
        if (port < PORT_MIN || port > PORT_MAX) {
            System.out.println("Given port number is out of allowed range (" + PORT_MIN + "-" + PORT_MAX + ")");
            System.exit(1);
        }

        registry = LocateRegistry.createRegistry(port);
        RemoteRMIRegistry reg = new RemoteRMIRegistry(bindingFile);
        registry.rebind("reg", reg);
        System.out.println("remote registry up and listening on port " + port);
        }
}
