package communication.Spread.TestSpread;

import communication.Spread.ReplicateObjectMessageFactory;
import spread.SpreadException;
import spread.SpreadMessage;
import communication.Spread.SpreadWrapper;

import java.net.UnknownHostException;
import java.util.ArrayList;

public class TestSpread {
    public static void main(String[] args) throws SpreadException, UnknownHostException {
        SpreadWrapper wrapper = new SpreadWrapper("TestSpread0", "localhost");

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
        wrapper.sendMessage(m1); System.out.println("##### Sent Message 1 (String)");

        SpreadMessage m2 = factory.createMessage("TEST_ARRAY", array);
        wrapper.sendMessage(m2); System.out.println("##### Sent Message 2 (String-Array)");

        SpreadMessage m3 = factory.createMessage("TEST_OBJECT", array);
        wrapper.sendMessage(m3); System.out.println("##### Sent Message 3 (String-Array)");

    }
}
