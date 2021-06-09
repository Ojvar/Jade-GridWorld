package Helpers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import BombSweeper.BombSweeperAgent;
import jade.core.Agent;
import jade.lang.acl.ACLMessage;

/**
 * Global helper
 */
public class GlobalHelper {
    public static HashMap<String, BombSweeperAgent> agents = new HashMap<String, BombSweeperAgent>();
    public static ArrayList<Point> traps = new ArrayList<Point>();

    private static ArrayList<String> logs = new ArrayList<>();
    private static Thread logThread = null;

    /**
     * Message logger
     * 
     * @param msg
     */
    public synchronized static void logMessage(String msg) {
        logs.add(msg);

        if (null != logThread) {
            return;
        }

        logThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (0 < logs.size()) {
                    System.out.println("\n/-----------------------------------------------/\n" +  logs.get(0));
                    logs.remove(0);
                }
            }
        });

        logThread.start();
        try {
            logThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logThread = null;
    }

    /**
     * Add a new agent
     */
    public static void addAgent(BombSweeperAgent agent) {
        GlobalHelper.agents.put(agent.getLocalName(), agent);
    }

    /**
     * Remove an agent
     */
    public static void removeAgent(BombSweeperAgent agent) {
        /* Remove agent from list */
        if (GlobalHelper.agents.containsKey(agent.getLocalName())) {
            GlobalHelper.agents.remove(agent.getLocalName());
        }
    }

    /**
     * Add all agents as receivers
     */
    public static void addAllAgentsAsReceiver(Agent sender, ACLMessage message, boolean excludeSender) {
        for (Agent agent : agents.values()) {
            if (!excludeSender || sender != agent) {
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

            if (null != nextBomb.targetedBy) {
                continue;
            }

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
        }

        return nearestBomb;
    }

    /**
     * Find nearest point to the agent
     * 
     * @param point
     * @param points
     * @return
     */
    public static Point nearestPoint(Point point, Set<Point> points) {
        Point nearestPoint = null;
        int nearestDist = 0;

        /* Filter un-targeted bombs */
        Iterator<Point> bombsIter = points.iterator();

        while (bombsIter.hasNext()) {
            Point nextPoint = bombsIter.next();

            if (null == nearestPoint) {
                nearestPoint = nextPoint;
                nearestDist = calcDistance(point, nextPoint);
            } else {
                int newTargetDist = calcDistance(point, nextPoint);

                if (newTargetDist < nearestDist) {
                    nearestPoint = nextPoint;
                    nearestDist = newTargetDist;
                }
            }
        }

        return nearestPoint;
    }

    /**
     * Find the nearest Trap
     * 
     * @param point
     * @return
     */
    public static Point nearestTrap(Point point) {
        Point nearestTrap = null;
        int nearestTrapDist = 0;

        Iterator<Point> trapIter = traps.iterator();

        while (trapIter.hasNext()) {
            Point nextTrap = trapIter.next();

            if (null == nearestTrap) {
                nearestTrap = nextTrap;
                nearestTrapDist = calcDistance(point, nextTrap);
            } else {
                int newTargetDist = calcDistance(point, nextTrap);

                if (newTargetDist < nearestTrapDist) {
                    nearestTrap = nextTrap;
                    nearestTrapDist = newTargetDist;
                }
            }
        }

        return nearestTrap;
    }
}
