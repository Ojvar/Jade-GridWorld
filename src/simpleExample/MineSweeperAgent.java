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
import simpleExample.behaviors.SendMineFindMessage;

/**
 * Mine Sweeper import jade.lang.acl.ACLMessage;agent class
 */
public class MineSweeperAgent extends Agent {
    private String color;
    private Map<Point, Mine> mines = new HashMap<Point, Mine>();

    /**
     * Setup
     */
    @Override
    protected void setup() {
        super.setup();

        Object[] args = getArguments();
        if (0 == args.length) {
            System.out.println("Insufficient arguments");
            doDelete();
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
     * Upad mines list
     */
    private void updateMinesList(LogicalEnv env) {
        Set<Point> sensedMines = env.senseBombs(color);
        Iterator<Point> iterator = sensedMines.iterator();

        while (iterator.hasNext()) {
            Point minePoint = iterator.next();

            this.addMineToList(minePoint);
        }
    }

    /**
     * Add a mine to list
     * 
     * @param minePoint
     */
    public void addMineToList(Point minePoint) {
        if (!mines.containsKey(minePoint)) {
            System.out.printf("\nMINE found at %s by %s\n\n", minePoint.toString(), color);

            /* Add to mines list */
            Mine newMine = new Mine(minePoint);
            mines.put(minePoint, newMine);

            /* Send message to all */
            addBehaviour(new SendMineFindMessage(MineSweeperAgent.this, newMine));
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

            /* Remove agent from list */
            GlobalHelper.agents.remove(MineSweeperAgent.this.getLocalName());
            doDelete();

            return;
        }

        /* Setup recieve behaviour */
        MineSweeperAgent.this.addBehaviour(new MessageReceive(MineSweeperAgent.this));

        /* Create explor thread */
        new Thread(new Runnable() {
            /**
             * Run
             */
            @Override
            public void run() {
                try {
                    while (MineSweeperAgent.this.isAlive()) {
                        Thread.sleep(500);

                        /* Sense all possible mines */
                        MineSweeperAgent.this.updateMinesList(env);

                        /* Move [randomly] */
                        MineSweeperAgent.this.move(env);

                        // /*
                        // * JUST FOR TEST Try to take the next around bomb
                        // */
                        // Point pos = agent.getPosition();

                        // if (env.pickup(agent.getName())) {
                        // // System.out.printf("%s Took a bomb at position [%d,%d]", agent.getName(),
                        // pos.x, pos.y);
                        // }

                        // /* Find traps */
                        // TypeObject tObj;
                        // tObj = env.isTrap(pos);
                        // if (null != tObj) {
                        // System.out.printf("\n%s", tObj.toString());
                        // }

                        /* Try to sence bombs */
                        // Set<Point> points = env.senseBombs(agent.getName());
                        // if (points.size() > 0) {
                        // for (Point point : points) {
                        // System.out.printf("\n%s sensed a bomb at %d,%d", agent.getName(), point.x,
                        // point.y);
                        // }
                        // }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}