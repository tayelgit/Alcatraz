package TestSpread;

import spread.*;

import java.util.ArrayList;
import java.util.Vector;

public class TestReplicateObjectMessageListener implements AdvancedMessageListener {

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

        switch (context) {
            case "TEST_STRING":
                String data = (String)messageDigest.get(1);
                System.out.println("Data is \"" + data + "\"");
                retValue = true;
                break;
            case "TEST_ARRAY":
                ArrayList<String> array = (ArrayList<String>)messageDigest.get(1);

                for (int i = 0; i <= array.size(); i++)
                    System.out.println(array.get(i));

                retValue = true;
                break;
            case "TEST_OBJECT":
                Object obj = messageDigest.get(1);

                System.out.println(obj.getClass().toString());

                retValue = true;
                break;
            default:
                System.out.println("Unknown Context: " + messageDigest.get(0).toString());
                break;
        }

        return retValue;
    }
}
