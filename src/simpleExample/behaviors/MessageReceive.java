package simpleExample.behaviors;

import java.awt.Point;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import simpleExample.BombSweeperAgent;

/**
 * Message receive
 */
public class MessageReceive extends CyclicBehaviour {
    private BombSweeperAgent agent = null;

    /* Messages types */
    public static final String C_NEW_BOMB = "new-bomb";
    public static final String C_TARGET_BOMB = "target-bomb";

    /**
     * Ctr
     */
    public MessageReceive(BombSweeperAgent agent) {
        this.agent = agent;
    }

    /**
     * Action
     */
    @Override
    public void action() {
        ACLMessage message = agent.receive();

        if (null == message) {
            block();
            return;
        }

        String content = message.getContent().toString().toLowerCase();
        System.out.println(this.agent.getLocalName() + " " + content);

        /* Add bomb to agents list */
        String[] messageData = content.split("\t");

        /* New Bomb */
        if (messageData[0].equals(C_NEW_BOMB)) {
            newBombEvent(messageData);
        }
        /* Target bomb */
        else if (messageData[0].equals(C_TARGET_BOMB)) {
            targetBombEvent(messageData);
        }
    }

    /**
     * Target Bomb Event
     * 
     * @param messageData
     */
    private void targetBombEvent(String[] messageData) {
        String sender = messageData[1];
        String[] pointData = messageData[2].split(",");

        /* Mark as targeted */
        Point bombPoint = new Point(Integer.valueOf(pointData[0]), Integer.valueOf(pointData[1]));
        agent.markBombAsTargeted(sender, bombPoint);

        /* Log */
        System.out.println("TARGET-BOM-MESSAGE\t" + sender + " " + messageData[2]);
    }

    /**
     * New Bomb Event
     * 
     * @param messageData
     */
    private void newBombEvent(String[] messageData) {
        String sender = messageData[1];
        String[] pointData = messageData[2].split(",");

        Point bombPoint = new Point(Integer.valueOf(pointData[0]), Integer.valueOf(pointData[1]));
        agent.addBombToList(sender, bombPoint);
    }
}
