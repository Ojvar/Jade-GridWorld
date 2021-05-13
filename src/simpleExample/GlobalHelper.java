package simpleExample;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class GlobalHelper {
    public static HashMap<String, Agent> agents = new HashMap<String, Agent>();

    /**
     * Add all agents as receivers
     */
    public static void addAllAgentsAsReceiver(Agent sender, ACLMessage message) {
        for (Agent agent : agents.values()) {
            if (sender != agent) {
                message.addReceiver(agent.getAID());
            }
        }
    }
}
