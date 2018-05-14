package TestSpread;

import spread.ReplicateObjectMessageFactory;
import spread.SpreadException;
import spread.SpreadMessage;
import spread.SpreadWrapper;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

public class TestSpread {
    public static void main(String[] args) throws SpreadException, UnknownHostException {
        SpreadWrapper wrapper = new SpreadWrapper("TestSpread", "localhost");

        System.out.println("##### Created Wrapper");

        wrapper.addTestReplicateObjectMessageListener();

        System.out.println("##### Added MessageListener");

        wrapper.joinGroup(SpreadWrapper.GroupEnum.FAULTTOLERANCE_GROUP);

        System.out.println("##### Joined Group FT");

        wrapper.sendCustomMessage("##### Testmessage", SpreadWrapper.GroupEnum.FAULTTOLERANCE_GROUP);

        System.out.println("##### Sent TestMessage");

        ReplicateObjectMessageFactory factory = new ReplicateObjectMessageFactory();
        ArrayList<String> array = new ArrayList<>();
        array.add("one");
        array.add("two");
        array.add("three");

        System.out.println("##### Created Factory");

        SpreadMessage m1 = factory.createMessage("TEST_STRING", "ASDF");
        SpreadMessage m2 = factory.createMessage("TEST_ARRAY", array);
        SpreadMessage m3 = factory.createMessage("TEST_OBJECT", array);

        System.out.println("##### Created Messages");

        wrapper.sendMessage(m1); System.out.println("##### Sent Message 1 (String)");
        wrapper.sendMessage(m2); System.out.println("##### Sent Message 2 (String-Array)");
        wrapper.sendMessage(m3); System.out.println("##### Sent Message 3 (String-Array)");
    }
}
