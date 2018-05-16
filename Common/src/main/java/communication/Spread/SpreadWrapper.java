/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package communication.Spread;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import Service.Alcatraz.AlcatrazRemote.Implementation.GameServiceImpl;
import communication.Spread.TestSpread.TestReplicateObjectMessageListener;
import spread.*;


/**
 * 
 * @author cdeCa
 */
public class SpreadWrapper {
    /**
     * connection to daemon
     */
    private final SpreadConnection connection;
    
    /**
     * contains all Groups that were joined (as Enum GroupEnum)
     */
    private ArrayList<GroupEnum> groupList = new ArrayList<GroupEnum>();
    
    /**
     * contains all Groups that were joined (as SpreadGroup)
     */
    private ArrayList<SpreadGroup> spreadGroupList = new ArrayList<SpreadGroup>();

    /**
     * GameService Object used in Listeners to replicate objects
     */
    private GameServiceImpl gameService;


    private String privateName;

    /**
     * Enum GroupEnum for our dedicated Spreadgroups (saves us the hassle to check for validity)
     * (valid chars: ASCII >= 36 && ASCII <= 126)
     */
    public enum GroupEnum {
        SERVER_GROUP("Registrierungsserver"),
        REGISTRY_GROUP("RMI_Registry"),
        FAULTTOLERANCE_GROUP("Fault_Tolerance");
        
        private final String name;
        
        GroupEnum(final String name) {
            this.name = name;
        }
        
        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Constructor - spread daemon MUST run before calling this
     * Creates the SpreadConnection on standard port 4803
     * @param privateName   private name of communicating user
     * @param hostName      name of host to connect to (usually localhost)
     * @throws UnknownHostException when host can't be found or Service isn't running
     * @throws SpreadException      when SpreadConnection can't be established
     */
    public SpreadWrapper(String privateName, String hostName)
            throws UnknownHostException, SpreadException {
        this.connection = new SpreadConnection();
        this.privateName = privateName;

        //<editor-fold desc="Starting Spread Daemon">
        /*
        Process spreadDaemon = null;
        try {
            //TODO: Change paths
            //TODO: Doesn't seem to work .. need to start spread.exe from outside first ...
            spreadDaemon = new ProcessBuilder(
                    "F:\\BIC\\BIC4\\SAM\\SpreadSources\\spread-bin-4.4.0-Windows8_x64\\bin\\win32\\spread.exe",
                    "-l y",
                    "-n localhost",
                    "-c F:\\BIC\\BIC4\\SAM\\SpreadSources\\used_spread\\alcatraz_spread.conf").start();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        */
        //</editor-fold>

        this.connection.connect(InetAddress.getByName(hostName), 4803, this.privateName, false, true);
    }
    
    /**
     * @return a list of joined groups
     */
    public ArrayList<String> getGroups() {
        ArrayList<String> groups = null;
        
        groupList.forEach((GroupEnum g) -> {
            groups.add(g.toString());
        });
        
        return groups;    
    }
    
    /**
     * Joins specified SpreadGroup
     * @param groupName Name of group to join
     * @throws SpreadException  when group can't be joined
     */
    public void joinGroup(GroupEnum groupName) throws SpreadException {
        // check if already in group
        if(groupList.contains(groupName)) {
            System.out.println("Already in Group " + groupName.toString());
            return;
        }
        
        SpreadGroup group = new SpreadGroup();
        
        group.join(connection, groupName.toString());
        
        spreadGroupList.add(group);
        groupList.add(groupName);
    }
    
    /**
     * Leave the group specified by groupName
     * @param groupName the group to leave
     * @throws SpreadException  when group can't be leaved
     */
    public void leaveGroup(GroupEnum groupName) throws SpreadException {
        // check if even in group
        if(!groupList.contains(groupName)) {
            System.out.println("Not in Group " + groupName.toString());
            return;
        }
        
        // leave Spreadgroup
        for(SpreadGroup g : spreadGroupList) {
            if(g.toString().equals(groupName.toString())) {
                g.leave();
                spreadGroupList.remove(g);
                break;
            }
        }
        
        // remove from list
        for(GroupEnum g : groupList) {
            if(g.toString().equals(groupName.toString())) {
                groupList.remove(g);
                break;
            }
        }
    }
    
    /**
     * Send a custom Message containing only specified text
     * OBSOLETE - only for testcases .. should use ReplicateObjectMessageFactory for prod
     * @param message Message to send
     * @param groups  Groups to send this message to
     * @throws SpreadException  when sending has failed
     */
    public void sendCustomMessage(String message, GroupEnum[] groups) throws SpreadException {
        SpreadMessage spreadMessage = new SpreadMessage();
        spreadMessage.digest(message);
        
        for(GroupEnum g : groups) {
            
            // check if in group
            boolean add = false;
            
            for(GroupEnum gg : groupList) {
                add |= gg.toString().equals(g.toString());
            }
            
            if(add) spreadMessage.addGroup(g.toString());
        }

        connection.multicast(spreadMessage);
    }
    
    /**
     * Send a custom Message containing only specified text
     * OBSOLETE
     * @param message Message to send
     * @param groups  Groups to send this message to
     * @throws SpreadException when sending has failed
     */
    public void sendCustomMessage(String message, String[] groups) throws SpreadException {
        SpreadMessage spreadMessage = new SpreadMessage();
        spreadMessage.digest(message);
        
        for(String s : groups) {
            // check if in group
            boolean add = false;
            
            for(GroupEnum gg : groupList) {
                add |= gg.toString().equals(s);
            }
            
            if(add) spreadMessage.addGroup(s);
        }

        connection.multicast(spreadMessage);
    }

    /**
     * Used by TestSpread
     * @param message
     * @param group
     * @throws SpreadException
     */
    public void sendCustomMessage(String message, GroupEnum group) throws SpreadException {
        SpreadMessage spreadMessage = new SpreadMessage();
        spreadMessage.digest(message);

        spreadMessage.addGroup(group.toString());
        connection.multicast(spreadMessage);
    }

    public void addReplicateGameMessageListener(GameServiceImpl game) {
        this.gameService = game;
        this.connection.add(new ReplicateGameMessageListener(this.gameService, this.privateName));
    }

    /**
     * Listener must be AdvancedMessageListener b/c moved to RegistryServer
     * @param listener
     */
    public void addReplicateRMIMessageListener(AdvancedMessageListener listener) {
        this.connection.add(listener);
    }

    public void addTestReplicateObjectMessageListener() { this.connection.add(new TestReplicateObjectMessageListener()); }

    /**
     * Should only be used by GameServer
     * @param localIP
     */
    public void sendHello(String localIP, String privateName) throws SpreadException {
        // to ensure that only GameServer uses this
        if(privateName.startsWith("rmi")) return;

        SpreadMessage message = new SpreadMessage();

        message.setReliable();

        message.digest("HELLO_INIT");
        message.digest(localIP);
        message.digest(privateName);

        message.addGroup(GroupEnum.FAULTTOLERANCE_GROUP.toString());

        connection.multicast(message);
    }

    /**
     *
     * @param recipient
     * @throws SpreadException
     */
    public void sendHelloResponse(String recipient) throws SpreadException {
        SpreadMessage message = new SpreadMessage();

        message.setReliable();

        message.digest("HELLO_RESPONSE");
        message.digest(recipient);
        message.digest(this.gameService.getGameLocalList());

        message.addGroup(GroupEnum.FAULTTOLERANCE_GROUP.toString());

        connection.multicast(message);
    }

    /**
     * Used in TestSpread
     * @param message
     * @throws SpreadException
     */
    public void sendMessage(SpreadMessage message) throws SpreadException {
        message.setReliable();
        message.addGroup(GroupEnum.FAULTTOLERANCE_GROUP.toString());
        connection.multicast(message);
    }

    public String getPrivateName() {
        return this.privateName;
    }
}
