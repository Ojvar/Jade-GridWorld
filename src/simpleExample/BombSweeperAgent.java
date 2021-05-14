package simpleExample;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import gridworld.LogicalEnv;
import jade.core.Agent;
import simpleExample.behaviors.MessageReceive;
import simpleExample.behaviors.SendBombDiscoverMessage;

/**
 * Bomb Sweeper import jade.lang.acl.ACLMessage;agent class
 */
public class BombSweeperAgent extends Agent {
    /* Thread delay vlaue */
    private static final int C_DELAY_VALUE = 250;

    private String color;
    private Map<Point, Bomb> bombs = new HashMap<Point, Bomb>();

    /**
     * Setup
     */
    @Override
    protected void setup() {
        super.setup();

        Object[] args = getArguments();
        if (0 == args.length) {
            System.out.println("Insufficient arguments");

            deleteAgent();
            return;
        }

        /* Add to agents list */
        GlobalHelper.agents.put(this.getLocalName(), this);

        /* Setup agent */
        this.color = args[0].toString();
        System.out.printf("\n\nAgent %s has been deployed succesfully\n", color);

        this.explore();
    }

    /**
     * Delete agent
     */
    private void deleteAgent() {
        /* Remove agent from list */
        if (GlobalHelper.agents.containsKey(BombSweeperAgent.this.getLocalName())) {
            GlobalHelper.agents.remove(BombSweeperAgent.this.getLocalName());
        }

        doDelete();
    }

    /**
     * update bombs list
     */
    private void updateBombsList(LogicalEnv env) {
        Set<Point> sensedBombs = env.senseBombs(color);
        Iterator<Point> iterator = sensedBombs.iterator();

        while (iterator.hasNext()) {
            Point bombPoint = iterator.next();

            this.addBombToList(bombPoint);
        }
    }

    /**
     * Add a bomb to list
     * 
     * @param bombPoint
     */
    public void addBombToList(Point bombPoint) {
        if (!bombs.containsKey(bombPoint)) {
            System.out.printf("\nBOMB found at %s by %s\n\n", bombPoint.toString(), color);

            /* Add to bombs list */
            Bomb newBomb = new Bomb(bombPoint);
            bombs.put(bombPoint, newBomb);

            /* Send message to all */
            addBehaviour(new SendBombDiscoverMessage(BombSweeperAgent.this, newBomb));
        }
    }

    /**
     * Move agent
     */
    private void move(LogicalEnv env) {
        /* Random move */
        int rand = new Random().nextInt(4) + 1;

        switch (rand) {
            case 1:
                env.north(color);
                break;

            case 2:
                env.south(color);
                break;

            case 3:
                env.east(color);
                break;

            case 4:
                env.west(color);
                break;
        }
    }

    /**
     * Explore
     */
    private void explore() {
        final String color = this.color;
        final LogicalEnv env = LogicalEnv.getEnv();

        /* Create new agent */;
        double xPos = new Random().nextInt(env.getWidth());
        double yPos = new Random().nextInt(env.getHeight());
        if (!env.enter(color, xPos, yPos, color)) {
            System.out.printf("\n\nAgent %s creation failed\n", color);

            this.deleteAgent();
            return;
        }

        /* Setup recieve behaviour */
        BombSweeperAgent.this.addBehaviour(new MessageReceive(BombSweeperAgent.this));

        /* Create explor thread */
        new Thread(new Runnable() {
            /**
             * Run
             */
            @Override
            public void run() {
                try {
                    while (BombSweeperAgent.this.isAlive()) {
                        Thread.sleep(C_DELAY_VALUE);

                        /* Sense all possible bombs */
                        BombSweeperAgent.this.updateBombsList(env);

                        /* Move [randomly] */
                        BombSweeperAgent.this.move(env);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
