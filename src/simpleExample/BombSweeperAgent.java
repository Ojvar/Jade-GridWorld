package simpleExample;

import java.awt.Point;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import gridworld.LogicalEnv;
import gridworld.TypeObject;
import jade.core.Agent;
import simpleExample.behaviors.MessageReceive;
import simpleExample.behaviors.SendBombDiscoverMessage;
import simpleExample.behaviors.SendTargetBombMessage;

/**
 * Bomb Sweeper import jade.lang.acl.ACLMessage;agent class
 */
public class BombSweeperAgent extends Agent {
    /* Thread delay vlaue */
    private static final int C_DELAY_VALUE = 500;

    private String color;
    private Bomb selectedBomb = null;
    private Map<String, Bomb> bombs = new HashMap<String, Bomb>();

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
        String name = getLocalName();
        Set<Point> sensedBombs = env.senseBombs(name);
        Iterator<Point> iterator = sensedBombs.iterator();

        while (iterator.hasNext()) {
            Point bombPoint = iterator.next();

            this.addBombToList(name, bombPoint);
        }
    }

    /**
     * Add a bomb to list
     * 
     * @param bombPoint
     */
    public void addBombToList(String sender, Point bombPoint) {
        String name = getLocalName();

        if (!bombs.containsKey(bombPoint.toString())) {
            System.out.printf("\n%s\tBOMB found at %s by %s\n\n", name, bombPoint.toString(), sender);

            /* Add to bombs list */
            Bomb newBomb = new Bomb(bombPoint);
            bombs.put(bombPoint.toString(), newBomb);

            /* Send message to all */
            if (sender.equals(name)) {
                addBehaviour(new SendBombDiscoverMessage(BombSweeperAgent.this, newBomb));
            }
        }
    }

    /**
     * Mark a bomb as targeted
     * 
     * @param bombPoint
     */
    public void markBombAsTargeted(String agent, Point bombPoint) {
        String name = getLocalName();

        if (bombs.containsKey(bombPoint.toString())) {
            System.out.printf("\n%s\tBOMB targeted at %s by %s\n\n", name, bombPoint.toString(), agent);

            /* Add to bombs list */
            Bomb bomb = bombs.get(bombPoint.toString());

            if (null == bomb.targetedBy) {
                bomb.markAsTargeted(agent);
            }
        }
    }

    /**
     * Mark a bomb as picked-up
     * 
     * @param bombPoint
     */
    public void markBombAsPickedUp(String agent, Point bombPoint) {
        if (bombs.containsKey(bombPoint.toString())) {
            System.out.printf("\nBOMB picked-up at %s by %s\n\n", bombPoint.toString(), agent);

            /* Add to bombs list */
            bombs.get(bombPoint.toString()).pickedUpBy = agent;
        }
    }

    /**
     * Move agent
     */
    private void moveAgent(LogicalEnv env) {
        String name = getLocalName();
        Point agentPoint = env.getPosition(this.getLocalName());

        /* If agent has a targeted bomb */
        if (null != this.selectedBomb) {
            /* If bomb has not been picked-up yet */
            if (null == this.selectedBomb.pickedUpBy) {
                TypeObject tObj = env.isBomb(agentPoint);

                if (null != tObj) {
                    /* Pick up the bomb */
                    env.pickup(name);

                    /* Mark bomb as picked-up */
                    markBombAsPickedUp(name, agentPoint);
                } else {
                    /* Move toward the bomb to picked it up */
                    moveToPoint(env, this.selectedBomb.point);
                }
            }
            /* If bomb is picked-up */
            else {
                System.out.println(name + "\tMoving toward the nearest trap");
                /* move to the neareset trap */
            }
        }
        /* NO any bomb selected */
        else {
            /* Random move */
            if (0 == bombs.size()) {
                this.moveToPoint(env, new Point(0, 0));
            }
            /* Move towrad the nearest bomb */
            else {
                Bomb nearestBomb = GlobalHelper.nearestBomb(agentPoint, bombs);

                if (null == nearestBomb) {
                    this.moveToPoint(env, new Point(0, 0));
                    return;
                }

                System.out.println("Selected nearest bomb is " + nearestBomb.point.toString());

                /* Select the nearest bomb */
                this.selectedBomb = nearestBomb;

                /* Mark as targeted */
                markBombAsTargeted(name, this.selectedBomb.point);

                /* Move toward the bomb */
                this.moveToPoint(env, this.selectedBomb.point);

                /* Send message to all */
                this.addBehaviour(new SendTargetBombMessage(BombSweeperAgent.this, this.selectedBomb));
            }
        }
    }

    /**
     * Move agent toward a point
     */
    private void moveToPoint(LogicalEnv env, Point targetPoint) {
        int direction = 0;
        String agentName = getLocalName();

        if ((0 == targetPoint.x) && (0 == targetPoint.y)) {
            /* Random move */
            direction = new Random().nextInt(4) + 1;
        } else {
            /* Find a way to the trap */
            Point agentPoint = env.getPosition(agentName);

            /* Move on both of 2-directions (X or Y) */
            String selectedAxis;

            /* If both axis are free to go */
            if ((agentPoint.x != targetPoint.x) && (agentPoint.y != targetPoint.y)) {
                selectedAxis = new Random().nextBoolean() ? "X" : "Y";
            } else if (agentPoint.x != targetPoint.x) {
                selectedAxis = "X";
            } else {
                selectedAxis = "Y";
            }

            if (selectedAxis.equals("X")) {
                if (agentPoint.x > targetPoint.x) {
                    direction = 4; /* West */
                } else {
                    direction = 3; /* East */
                }
            } else {
                if (agentPoint.y > targetPoint.y) {
                    direction = 1; /* North */
                } else {
                    direction = 2; /* South */
                }
            }
        }

        /* Move agent */
        switch (direction) {
            case 1:
                env.north(agentName);
                break;

            case 2:
                env.south(agentName);
                break;

            case 3:
                env.east(agentName);
                break;

            case 4:
                env.west(agentName);
                break;
        }
    }

    /**
     * Explore
     */
    private void explore() {
        String name = getLocalName();
        final LogicalEnv env = LogicalEnv.getEnv();

        /* Create new agent */;
        double xPos = new Random().nextInt(env.getWidth());
        double yPos = new Random().nextInt(env.getHeight());
        if (!env.enter(name, xPos, yPos, this.color)) {
            System.out.printf("\n\nAgent %s creation failed\n", this.color);

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
                        BombSweeperAgent.this.moveAgent(env);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
