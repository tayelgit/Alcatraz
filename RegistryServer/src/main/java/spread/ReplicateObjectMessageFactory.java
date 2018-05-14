package spread;

import java.io.Serializable;

public class ReplicateObjectMessageFactory extends MessageFactory {
    /**
     * ctor of MessageFactory
     * @param spreadMessage the message to be set as defaultmessage
     */
    public ReplicateObjectMessageFactory(SpreadMessage spreadMessage) {
        super(spreadMessage);
    }

    /**
     * Creates a message with context and a serializable object
     * Uses msg.digest() ==> message must be read with msg.getDigest()
     * @param context       the context of this message
     * @param serializable  the object to be sent
     * @return              the message to be sent
     * @throws SpreadException when digest failes
     */
    public SpreadMessage createMessage(String context, Serializable serializable) throws SpreadException {
        SpreadMessage message = super.createMessage();
        message.digest(context);
        message.digest(serializable);
        return message;
    }
}
