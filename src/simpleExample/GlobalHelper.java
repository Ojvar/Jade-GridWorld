package simpleExample;

import java.awt.Point;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import jade.core.Agent;
import jade.lang.acl.ACLMessage;

public class GlobalHelper {
    public static HashMap<String, Agent> agents = new HashMap<String, Agent>();
    public static HashSet<Point> traps = new HashSet<Point>();

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

    /**
     * Calc distance
     * 
     * @param p1
     * @param p2
     */
    public static int calcDistance(Point p1, Point p2) {
        return (int) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
    }

    /**
     * Find nearest bomb to the agent
     * 
     * @param point
     * @param bombs
     * @return
     */
    public static Bomb nearestBomb(Point point, Map<String, Bomb> bombs) {
        Bomb nearestBomb = null;
        int nearestBombDist = 0;

        /* Filter un-targeted bombs */
        Iterator<Bomb> bombsIter = bombs.values().iterator();

        while (bombsIter.hasNext()) {
            Bomb nextBomb = bombsIter.next();

            if (null == nextBomb.targetedBy) {
                if (null == nearestBomb) {
                    nearestBomb = nextBomb;
                    nearestBombDist = calcDistance(point, nextBomb.point);
                } else {
                    int newTargetDist = calcDistance(point, nextBomb.point);

                    if (newTargetDist < nearestBombDist) {
                        nearestBomb = nextBomb;
                        nearestBombDist = newTargetDist;
                    }
                }

                System.out.println("Distance Agent and bomb " + point.toString() + " " + nearestBomb.point.toString());
            }
        }

        return nearestBomb;
    }
}
