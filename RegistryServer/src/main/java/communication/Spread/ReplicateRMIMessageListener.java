package communication.Spread;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import registryserver.RemoteRMIRegistry;
import registryserver.RemoteRMIRegistry.BoundHost;
import spread.*;

import java.io.IOException;
import java.io.Serializable;
import java.rmi.MarshalledObject;
import java.sql.SQLOutput;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class ReplicateRMIMessageListener implements AdvancedMessageListener {
    private RemoteRMIRegistry remoteRMIRegistry;
    private String privateName;
    private boolean initDone = false;

    /**
     * Ctor
     */
    public ReplicateRMIMessageListener(RemoteRMIRegistry remoteRMIRegistry, String privateName) {
        this.remoteRMIRegistry = remoteRMIRegistry;
        this.privateName = privateName;
    }
    /**
     * Reacts to regular messages
     * Regular messages are all messages that aren't membership messages
     * => 'real' application messages/traffic
     * @param spreadMessage Message received
     */
    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
        /*
         * ##### Digest received message #####
         * Protocol ensures, that first digest is context
         * Context is a string that provides replication context used to replicate object
         * Digest after the first is either the object to be replicated
         * or an ArrayList of objects to be replicated
         */
        Vector messageDigestVector = null;
        try {
            messageDigestVector = spreadMessage.getDigest();
        } catch (SpreadException e) {
            e.printStackTrace();
        }

        if(messageDigestVector == null)
            return;

        // context - what to do with this message
        String context = messageDigestVector.get(0).toString();

        // check target group
        SpreadGroup groups[] = spreadMessage.getGroups();

        for (SpreadGroup g : groups) {
            if(g.toString().equals(SpreadWrapper.GroupEnum.FAULTTOLERANCE_GROUP.toString())) {
                switch (context) {
                    case "HELLO_INIT" :
                        // i - expected
                        // 0 - context (HELLO_INIT/HELLO_RESPONSE)
                        // 1 - senderType (GAME_SERVER/RMI_REGISTRY)
                        // 2 - IP
                        // 3 - privateName

                        // sender GameServer or RMI?
                        String senderType = (String) messageDigestVector.get(1);
                        //String sender = spreadMessage.getSender().toString();
                        String sender = (String) messageDigestVector.get(3);

                        // remember GameServer in RMI
                        if(senderType.equals("GAME_SERVER")) {
                            String ip = (String) messageDigestVector.get(2);
                            String privateName = (String) messageDigestVector.get(3);
                            this.remoteRMIRegistry.addSpreadBoundHost(privateName, ip);
                        }

                        // If Hello from RMI_REGISTRY
                        if(senderType.equals("RMI_REGISTRY")) {
                            this.remoteRMIRegistry.answerRMIHello(sender);
                        }

                        break;
                    case "HELLO_RESPONSE" :
                        // i - expected
                        // 0 - context (HELLO_INIT/HELLO_RESPONSE)
                        // 1 - recipient
                        // 2 - recipientType (GAME_SERVER/RMI_REGISTRY)
                        // 3 - HashMultimap objectServer

                        String recipient = (String) messageDigestVector.get(1);
                        String recipientType = (String) messageDigestVector.get(2);
                        System.out.println("HelloResponse for: \"" + recipient + "\"");
                        System.out.println("-> recipient: " + recipient + ", me: " + privateName);
                        // message for me?
                        if(recipient.equals(this.privateName)
                                && recipientType.equals("RMI_REGISTRY")
                                && !this.initDone) {

                            try {
                                MarshalledObject<HashMultimap<String, BoundHost>> inputObject = (MarshalledObject) messageDigestVector.get(3);
                                HashMultimap<String, BoundHost> objectServers = (HashMultimap) inputObject.get();
                                this.remoteRMIRegistry.setObjectServers(objectServers);
                                this.initDone = true;
                                System.out.println("Hello successfully done for " + recipient + "(" + recipientType + ")");
                            } catch (IOException | ClassNotFoundException e) {
                                e.printStackTrace();
                                System.out.println("Couldn't deserialize HashMultimap...");
                            }
                        } else {
                            System.out.println("Hello NOT successfully done for " + recipient + "(" + recipientType + ")");
                        }
                        break;
                    default:
                        System.out.println("Don't know what to do with message context: \"" + context + "\"");
                        break;
                }
            } else if (g.toString().equals(SpreadWrapper.GroupEnum.SERVER_GROUP.toString())) {
                System.out.println("Shouldn't be in here ...");
            } else if (g.toString().equals(SpreadWrapper.GroupEnum.REGISTRY_GROUP.toString())) {
                try {
                    replicateObject(context, messageDigestVector);
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void obsoleteRegularMessageReceived(SpreadMessage spreadMessage) {
        System.out.print("Received a ");
        if(spreadMessage.isUnreliable())
            System.out.print("UNRELIABLE");
        else if(spreadMessage.isReliable())
            System.out.print("RELIABLE");
        else if(spreadMessage.isFifo())
            System.out.print("FIFO");
        else if(spreadMessage.isCausal())
            System.out.print("CAUSAL");
        else if(spreadMessage.isAgreed())
            System.out.print("AGREED");
        else if(spreadMessage.isSafe())
            System.out.print("SAFE");

        System.out.println(" message.");
        System.out.println("Sent by  " + spreadMessage.getSender() + ".");
        System.out.println("Type is " + spreadMessage.getType() + ".");

        if(spreadMessage.getEndianMismatch() == true)
            System.out.println("There is an endian mismatch.");
        else
            System.out.println("There is no endian mismatch.");

        SpreadGroup groups[] = spreadMessage.getGroups();
        System.out.println("To " + groups.length + " groups.");

        byte data[] = spreadMessage.getData();
        System.out.println("The data is " + data.length + " bytes.");
        System.out.println("The message is: " + new String(data));
    }

    /**
     * Reacts to membership messages
     * Membership messages can be regular, transistion or self-leave.
     * Regular messages can be caused by
     * > JOIN       someone joined the group
     * > LEAVE      someone left the group
     * > DISCONNECT someone disconnected from SpreadConnection (w/o leaving groups)
     * > NETWORK    caused by network, not gameclient calling join/leave/disconnect/...
     * @param spreadMessage the message received
     */
    @Override
    public void membershipMessageReceived(SpreadMessage spreadMessage) {
        MembershipInfo membershipInfo = spreadMessage.getMembershipInfo();
        SpreadGroup group = membershipInfo.getGroup();

        if(!group.toString().equals(SpreadWrapper.GroupEnum.FAULTTOLERANCE_GROUP.toString())) {
            // only FT messages are interesting
            return;
        }

        String sender = null;
        if(membershipInfo.isCausedByDisconnect()) {
            sender = membershipInfo.getDisconnected().toString();
        } else if (membershipInfo.isCausedByLeave()) {
            sender = membershipInfo.getLeft().toString();
        } else if(membershipInfo.isCausedByNetwork()) {
            MembershipInfo.VirtualSynchronySet virtual_synchrony_sets[] = membershipInfo.getVirtualSynchronySets();

            MembershipInfo.VirtualSynchronySet set = virtual_synchrony_sets[0];
            SpreadGroup setMembers[] = set.getMembers();

            HashMap<String, String> temp = this.remoteRMIRegistry.getSpreadBoundHosts();


            for (String s : temp.keySet()) {
                if(!Arrays.asList(setMembers).contains(s)){
                    sender =  s;
                    break;
                }
            }
        }

        if(sender == null) {
            System.out.println("Couldn't verify sender");
            return;
        }

        HashMap<String, String> hosts = this.remoteRMIRegistry.getSpreadBoundHosts();
        String value_IP = hosts.get(sender);

        if (value_IP == null) {
            System.out.println("SpreadBoundHost " + sender + " already removed");
            return;
        }

        try {
            this.remoteRMIRegistry.removeObjectServer("gamelist", value_IP, 2);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Error removing " + sender + "(" + value_IP + ")");
        }

        hosts.remove(sender);
        this.remoteRMIRegistry.setSpreadBoundHosts(hosts);
        this.remoteRMIRegistry.replicateSpreadBoundHosts();

    }

    public void obsoleteMembershipMessageReceived(SpreadMessage spreadMessage) {
        MembershipInfo membershipInfo = spreadMessage.getMembershipInfo();
        SpreadGroup group = membershipInfo.getGroup();

        if(membershipInfo.isRegularMembership()) {
            SpreadGroup members[] = membershipInfo.getMembers();
            MembershipInfo.VirtualSynchronySet virtual_synchrony_sets[] = membershipInfo.getVirtualSynchronySets();
            MembershipInfo.VirtualSynchronySet my_virtual_synchrony_set = membershipInfo.getMyVirtualSynchronySet();

            System.out.println("REGULAR membership for group " + group +
                    " with " + members.length + " members:");
            for( int i = 0; i < members.length; ++i ) {
                System.out.println("\t\t" + members[i]);
            }
            System.out.println("Group ID is " + membershipInfo.getGroupID());

            System.out.print("\tDue to ");
            if(membershipInfo.isCausedByJoin()) {
                System.out.println("the JOIN of " + membershipInfo.getJoined());
            }	else if(membershipInfo.isCausedByLeave()) {
                System.out.println("the LEAVE of " + membershipInfo.getLeft());
            }	else if(membershipInfo.isCausedByDisconnect()) {
                System.out.println("the DISCONNECT of " + membershipInfo.getDisconnected());
            } else if(membershipInfo.isCausedByNetwork()) {
                System.out.println("NETWORK change");
                for( int i = 0 ; i < virtual_synchrony_sets.length ; ++i ) {
                    MembershipInfo.VirtualSynchronySet set = virtual_synchrony_sets[i];
                    SpreadGroup setMembers[] = set.getMembers();
                    System.out.print("\t\t");
                    if( set == my_virtual_synchrony_set ) {
                        System.out.print("(LOCAL) ");
                    } else {
                        System.out.print("(OTHER) ");
                    }
                    System.out.println( "Virtual Synchrony Set " + i + " has " +
                            set.getSize() + " members:");
                    for( int j = 0; j < set.getSize(); ++j ) {
                        System.out.println("\t\t\t" + setMembers[j]);
                    }
                }
            }
        } else if(membershipInfo.isTransition()) {
            System.out.println("TRANSITIONAL membership for group " + group);
        } else if(membershipInfo.isSelfLeave()) {
            System.out.println("SELF-LEAVE message for group " + group);
        }
    }

    /**
     * Replicate an object depending on context
     * @param context       the context which defines how to replicate
     * @param messageDigest the message digest that holds the data
     * @return  true if everything goes well
     */
    boolean replicateObject(String context, Vector messageDigest) throws IOException, ClassNotFoundException {
        boolean retValue = false;

        switch (context) {
            case "UPDATE_RMIREGISTRY":     // expected digest is Serializable (b/c no dep to RegistryServer!)
                System.out.println("in replicateObject -> UPDATE_RMIREGISTRY");

                //HashMultimap<String, BoundHost> objectServers =
                        //(HashMultimap<String, BoundHost>)messageDigest.get(1);

                MarshalledObject<HashMultimap<String, BoundHost>> inputObject = (MarshalledObject)messageDigest.get(1);
                HashMultimap<String, BoundHost> objectServers = (HashMultimap) inputObject.get();

                this.remoteRMIRegistry.setObjectServers(objectServers);

                System.out.println("in replicateObject -> AFTER replication");
                retValue = true;
                break;
            case "UPDATE_RMI_SPREADBOUND_HOSTS":
                System.out.println("in replicateObject -> UPDATE_RMI_SPREADBOUND_HOSTS");

                HashMap<String, String> spreadBoundHosts =
                        (HashMap<String, String>)messageDigest.get(1);

                this.remoteRMIRegistry.setSpreadBoundHosts(spreadBoundHosts);

                System.out.println("in replicateObject -> AFTER replication");
                retValue = true;
                break;
            default:
                System.out.println("Unknown Context: \"" + context + "\"");
                break;
        }

        return retValue;
    }
}
