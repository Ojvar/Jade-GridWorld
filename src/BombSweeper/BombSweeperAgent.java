package BombSweeper;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Random;

import BombSweeper.Messages.DropBombMessage;
import BombSweeper.Messages.MessageReceiver;
import BombSweeper.Messages.SenseBombMessage;
import BombSweeper.Messages.TargetBombMessage;
import Helpers.Bomb;
import Helpers.BombsManager;
import Helpers.GlobalHelper;
import gridworld.LogicalEnv;
import jade.core.Agent;

/**
 * Bomb sweeper agent class
 */
public class BombSweeperAgent extends Agent {
    /* Thread delay vlaue */
    private final LogicalEnv env = LogicalEnv.getEnv();
    private final static int C_DELAY_VALUE = 1000;

    /* Private agent's data */
    public Date deployTime = new Date();
    private String color;
    private BombsManager bombManager = new BombsManager(this);

    /**
     * Setup agent
     */
    @Override
    protected void setup() {
        super.setup();

        /* Check input arguments */
        Object[] args = getArguments();
        if (0 == args.length) {
            System.out.println("Insufficient arguments");

            this.deleteAgent();
        } else {
            /* Setup agent */
            this.color = args[0].toString();

            /* Add to agents list */
            GlobalHelper.addAgent(this);
            System.out.printf("\n\nAgent %s has been deployed succesfully\n", color);

            /* Try to start the agent */
            this.startAgent();
        }
    }

    /**
     * Make a delay
     */
    protected void makeDelay() {
        /* Make a delay */
        try {
            Thread.sleep(BombSweeperAgent.C_DELAY_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Delete agent
     */
    private synchronized void deleteAgent() {
        GlobalHelper.removeAgent(this);
        doDelete();
    }

    /**
     * Explore the grid-world
     */
    private void startAgent() {
        if (this.enterAgent()) {
            try {
                this.addEventHandler();
                this.beginExplore();
            } catch (Exception ex) {
                ex.printStackTrace();
                this.deleteAgent();
            }
        } else {
            System.out.printf("\n\nAgent %s creation failed\n", this.color);
            this.deleteAgent();
        }
    }

    /**
     * Add event-handler
     */
    private void addEventHandler() {
        this.addBehaviour(new MessageReceiver(this));
    }

    /**
     * Try to enter an agent
     * 
     * @return {boolean}
     */
    private boolean enterAgent() {
        String name = getLocalName();

        /* Create new agent */;
        double xPos = new Random().nextInt(env.getWidth());
        double yPos = new Random().nextInt(env.getHeight());

        /* TODO: REMOVE AFTER TEST */
        if (this.color.equals("red")) {
            xPos = 1;
            yPos = 1;
        } else if (this.color.equals("blue")) {
            xPos = 3;
            yPos = 1;
        }

        return env.enter(name, xPos, yPos, this.color);
    }

    /**
     * Begin explore
     */
    private void beginExplore() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (BombSweeperAgent.this.isAlive()) {
                    /* Send agent state message -- for tracing purpose */
                    BombSweeperAgent.this.logState();

                    /* Make a delay */
                    BombSweeperAgent.this.makeDelay();

                    /* 1- Sense bombs */
                    BombSweeperAgent.this.senseBombs();

                    /* 2- Target nearest bomb */
                    BombSweeperAgent.this.targetNearestBomb();

                    /* 3 - Move agent */
                    BombSweeperAgent.this.moveAgent();
                }
            }
        }).start();
    }

    /**
     * Move agent
     */
    protected void moveAgent() {
        Bomb selectedBomb = bombManager.selectedBomb();
        Point agentLocation = getLocation();

        /* (IF) We have not any bomb in the list */
        if (this.bombManager.getBombs().size() == 0 || null == selectedBomb) {
            /* Move random */
            this.moveToPoint(new Point(-1, -1), true);
            return;
        }

        /* (IF) We target a bomb */
        if (selectedBomb.isPickedUp) {
            /* We should go toward the trap and drop the bomb */

            /* Get the nearest trap location */
            Point nearestTrap = GlobalHelper.nearestTrap(agentLocation);

            if (null == nearestTrap) {
                GlobalHelper.logMessage("No any trap found");
                return;
            }

            /* Check for location */
            if (nearestTrap.equals(agentLocation)) {
                if (!dropBomb()) {
                    GlobalHelper.logMessage("ERROR\t" + getLocalName() + "\tDrop bomb failed\n\t\t" + agentLocation);
                    return;
                }
            }

            /* Move toward that */
            moveToPoint(nearestTrap, true);
        } else {
            if (agentLocation.equals(selectedBomb.point) && !selectedBomb.isPickedUp) {
                if (!this.pickUpBomb()) {
                    GlobalHelper.logMessage("ERROR\t" + getLocalName() + "\tPickUp bomb failed\n\t\t" + agentLocation);
                    return;
                }
            }

            /* We should go toward the bomb and picked-it up */
            moveToPoint(selectedBomb.point, true);
        }
    }

    /**
     * Remove a bomb
     */
    public void removeBomb(Bomb bomb) {
        this.bombManager.removeBomb(bomb);
    }

    /**
     * Drop bomb
     */
    public boolean dropBomb() {
        Bomb selectedBomb = bombManager.selectedBomb();

        /* Try to drop bomb */
        boolean res = bombManager.dropBomb();

        if (res) {
            sendDropBombMessage(selectedBomb);

            /* Remove bomb from the list */
            bombManager.removeBomb(selectedBomb);
        }

        return res;
    }

    /**
     * Pick up the bomb
     */
    private boolean pickUpBomb() {
        return bombManager.pickUpBomb();
    }

    /**
     * Move agent toward a point
     */
    private synchronized void moveToPoint(Point point, boolean moveRandOnFailed) {
        int direction = 0;
        String agentName = getLocalName();

        if (point.equals(new Point(-1, -1))) {
            /* Random move */
            direction = new Random().nextInt(4) + 1;
        } else {
            /* Find a way to the trap */
            Point agentPoint = env.getPosition(agentName);

            /* Move on both of 2-directions (X or Y) */
            String selectedAxis;

            /* If both axis are free to go */
            if ((agentPoint.x != point.x) && (agentPoint.y != point.y)) {
                selectedAxis = new Random().nextBoolean() ? "X" : "Y";
            } else if (agentPoint.x != point.x) {
                selectedAxis = "X";
            } else {
                selectedAxis = "Y";
            }

            if (selectedAxis.equals("X")) {
                if (agentPoint.x > point.x) {
                    direction = 4; /* West */
                } else {
                    direction = 3; /* East */
                }
            } else {
                if (agentPoint.y > point.y) {
                    direction = 1; /* North */
                } else {
                    direction = 2; /* South */
                }
            }
        }

        /* Move agent */
        boolean moveResult = true;
        switch (direction) {
            case 1:
                moveResult = env.north(agentName);
                break;

            case 2:
                moveResult = env.south(agentName);
                break;

            case 3:
                moveResult = env.east(agentName);
                break;

            case 4:
                moveResult = env.west(agentName);
                break;
        }

        /*
         * Check moving result If moving has failed,[and we are allowed] We do a random
         * move
         */
        if (!moveResult && moveRandOnFailed) {
            moveToPoint(new Point(-1, -1), false);
        }
    }

    /**
     * Sense bombs
     */
    protected void senseBombs() {
        /* Update bombs list */
        ArrayList<Bomb> bombs = this.bombManager.updateBombs();
        if (0 < bombs.size()) {
            /* Send all bombs data to ther */
            Iterator<Bomb> iter = bombs.iterator();
            while (iter.hasNext()) {
                Bomb bomb = iter.next();

                this.sendSenseBombMessage(bomb);
            }
        }
    }

    /**
     * Target the nearest bomb
     */
    protected void targetNearestBomb() {
        /* Select the nearest bomb */
        Bomb bomb = this.bombManager.selectNearestBomb();

        if (null != bomb) {
            this.sendTargetBombMessage(bomb);
        }
    }

    /**
     * Mark bomb as targeted
     * 
     * @param bomb
     */
    public void markBombAsTargeted(Bomb bomb) {
        bombManager.markBombAsTargeted(bomb);
    }

    /**
     * Send target bomb message
     */
    public void sendTargetBombMessage(Bomb bomb) {
        addBehaviour(new TargetBombMessage(this, bomb));
    }

    /**
     * Send sense bomb message
     * 
     * @param bomb
     */
    public void sendSenseBombMessage(Bomb bomb) {
        addBehaviour(new SenseBombMessage(this, bomb));
    }

    /**
     * Send drop-bomb message
     */
    private void sendDropBombMessage(Bomb bomb) {
        addBehaviour(new DropBombMessage(this, bomb));
    }

    /**
     * Add new bomb
     */
    public void addNewBomb(Bomb bomb) {
        this.bombManager.addNewBomb(bomb);
    }

    /**
     * Get location
     * 
     * @return
     */
    public Point getLocation() {
        return env.getPosition(this.getLocalName());
    }

    /**
     * Log state
     */
    private void logState() {
        String result = getLocalName();

        Iterator<Bomb> iter = this.bombManager.getBombs().values().iterator();
        while (iter.hasNext()) {
            Bomb bomb = iter.next();
            result += "\n\t BOMB\t" + bomb.toString();
        }

        GlobalHelper.logMessage(result);
    }
}