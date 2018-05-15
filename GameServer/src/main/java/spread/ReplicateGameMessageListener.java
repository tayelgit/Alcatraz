package spread;


import Service.Alcatraz.serviceData.GameLocal;
import Service.Alcatraz.AlcatrazRemote.Implementation.GameServiceImpl;

import java.util.*;

public class ReplicateGameMessageListener implements AdvancedMessageListener {
    /**
     * GameSerivceImpl object to be called for replication of objects
     */
    private GameServiceImpl gameService;

    /**
     * Ctor
     * @param gameService   The GameServiceImpl object on which replication is done
     */
    public ReplicateGameMessageListener(GameServiceImpl gameService) {
        this.gameService = gameService;
    }

    /**
     * Reacts to regular messages
     * Regular messages are all messages that aren't membership messages
     * => 'real' application messages/traffic
     * @param spreadMessage Message received
     */
    @Override
    public void regularMessageReceived(SpreadMessage spreadMessage) {
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

        String context = messageDigestVector.get(0).toString();

        replicateObject(context, messageDigestVector);
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
    boolean replicateObject(String context, Vector messageDigest) {
        boolean retValue = false;

        System.out.println("in replicateObject");

        switch (context) {
            case "UPDATE_GAMELOCALLIST": // expected digest is HashMap<String, GameLocal>
                System.out.println("in replicateObject -> UPDATE_GAMELOCALLIST");

                Map<UUID,GameLocal> gameLocalList = (Map<UUID,GameLocal>) messageDigest.get(1);
                gameService.setGameLocalList(gameLocalList);

                System.out.println("in replicateObject -> AFTER replication");
                retValue = true;
                break;
            default:
                System.out.println("Unknown Context: \"" + context + "\"");
                break;

            //<editor-fold desc="Obsolete cases ...">
            /*    // OBSOLETE .. design decision: only full state (GameLocalList will be replicated)
            case "CREATE_GAME":     // expected digest is HashMap<String,GameLocal>
                HashMap<String,GameLocal> gameLocalList = (HashMap<String,GameLocal>) messageDigest.get(1);
                gameService.setGameLocalList(gameLocalList);

                retValue = true;
                break;
            case "UPDATE_GAME":     // expected digest is GameLocal-Object
                GameLocal game = (GameLocal) messageDigest.get(1);
                gameService.updateGame(game);

                retValue = true;
                break;
            case "DESTROY_GAME":    // expected digest is UID-Object
                UUID uuid = (UUID) messageDigest.get(1);
                gameService.removeGame(uuid);

                retValue = true;
                break;
            */
            //</editor-fold>
        }

        return retValue;
    }
}
