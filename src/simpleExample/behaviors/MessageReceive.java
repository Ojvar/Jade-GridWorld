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
        if (messageData[0].equals("new-bomb")) {
            String[] pointData = messageData[1].split(",");

            Point bombPoint = new Point(Integer.valueOf(pointData[0]), Integer.valueOf(pointData[1]));
            agent.addBombToList(bombPoint);
        }
    }
}
