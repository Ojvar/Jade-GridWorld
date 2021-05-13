package simpleExample.behaviors;

import java.awt.Point;

import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import simpleExample.MineSweeperAgent;

/**
 * Message receive
 */
public class MessageReceive extends CyclicBehaviour {
    private MineSweeperAgent agent = null;

    /**
     * Ctr
     */
    public MessageReceive(MineSweeperAgent agent) {
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

        /* Add mine to agents list */
        String[] messageData = content.split("\t");

        /* New Mine */
        if (messageData[0].equals("new-mine")) {
            String[] pointData = messageData[1].split(",");

            Point minePoint = new Point(Integer.valueOf(pointData[0]), Integer.valueOf(pointData[1]));
            agent.addMineToList(minePoint);
        }
    }
}
