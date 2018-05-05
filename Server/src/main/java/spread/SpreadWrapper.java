/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package spread;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;

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
     * contains all Groups that was joined (as Enum GroupEnum)
     */
    private ArrayList<GroupEnum> groupList;
    
    /**
     * contains all Groups that was joined (as SpreadGroup)
     */
    private ArrayList<SpreadGroup> spreadGroupList;
    
    /**
     * Enum GroupEnum for our dedicated Spreadgroups (saves us the hassle to check for validity)
     */
    public static enum GroupEnum {
        SERVER_GROUP("Registrierungsserver"),
        REGISTRY_GROUP("RMI Registry"),
        FAULTOLERANCE_GROUP("Fault Tolerance");
        
        private final String name;
        
        private GroupEnum(final String name) {
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
     * @param privateName private name of communicating user
     * @param hostName name of host to connect to (usually localhost)
     * @throws UnknownHostException
     * @throws SpreadException 
     */
    public SpreadWrapper(String privateName, String hostName) throws UnknownHostException, SpreadException {
        connection = new SpreadConnection();
        connection.connect(InetAddress.getByName(hostName), 4803, privateName, false, false);    
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
     * @throws SpreadException 
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
     * @throws SpreadException 
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
     * @param message Message to send
     * @param groups  Groups to send this message to
     * @throws SpreadException 
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
        
    }
    
    /**
     * Send a custom Message containing only specified text
     * @param message Message to send
     * @param groups  Groups to send this message to
     * @throws SpreadException 
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
        
    }
    
}
