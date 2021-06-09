package Helpers;

import java.awt.Point;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import BombSweeper.BombSweeperAgent;
import gridworld.LogicalEnv;

/**
 * Bombs manager
 */
public class BombsManager {
    private final LogicalEnv env = LogicalEnv.getEnv();

    private BombSweeperAgent agent;
    private Bomb selectedBomb;
    private HashMap<String, Bomb> bombs = new HashMap<>();

    /**
     * Class ctr
     * 
     * @param agent
     */
    public BombsManager(BombSweeperAgent agent) {
        this.agent = agent;
    }

    /**
     * Get bombs
     * 
     * @return
     */
    public HashMap<String, Bomb> getBombs() {
        return this.bombs;
    }

    /**
     * Update bombs list
     * 
     * @param sensedBombs
     */
    public ArrayList<Bomb> updateBombs() {
        ArrayList<Bomb> result = new ArrayList<Bomb>();

        /* Sense bombs */
        Set<Point> sensedBombPoints = env.senseBombs(agent.getLocalName());

        if (sensedBombPoints.size() > 0) {
            Iterator<Point> iter = sensedBombPoints.iterator();

            while (iter.hasNext()) {
                Point point = iter.next();

                /* Check for exists */
                if (!this.bombs.containsKey(point.toString())) {
                    /* Try to add bomb */
                    Bomb bomb = this.addNewBomb(point);

                    /* Add to result list */
                    result.add(bomb);
                }
            }
        }

        return result;
    }

    /**
     * Get the selectedBomb
     * 
     * @return
     */
    public Bomb selectedBomb() {
        return this.selectedBomb;
    }

    /**
     * Select nearest bomb
     */
    public Bomb selectNearestBomb() {
        Bomb bomb = null;

        if (null == this.selectedBomb) {
            bomb = GlobalHelper.nearestBomb(this.agent.getLocation(), bombs);

            if (null != bomb) {
                this.selectBomb(bomb);
            }
        }

        return bomb;
    }

    /**
     * Select bomb
     * 
     * @param bomb
     */
    public boolean selectBomb(Bomb bomb) {
        if (null != bomb.targetedBy) {
            return false;
        }

        this.selectedBomb = bomb;
        bomb.target(agent);

        return true;
    }

    /**
     * UnSelect bomb
     * 
     * @param bomb
     */
    public void unselectBomb() {
        this.selectedBomb = null;
    }

    /**
     * Add new bomb - by pint
     * 
     * @param bomb
     */
    public Bomb addNewBomb(Point point) {
        return this.addNewBomb(new Bomb(point));
    }

    /**
     * Add new bomb - by Bomb
     * 
     * @param bomb
     */
    public Bomb addNewBomb(Bomb newBomb) {
        Bomb bomb;

        /* If it's exists in the list */
        if (this.bombs.containsKey(newBomb.point.toString())) {
            bomb = this.bombs.get(newBomb.point.toString());
        } else {
            /* If it's a new bomb */
            bomb = new Bomb(newBomb);

            /* Add new bomb to bombs-list */
            this.bombs.put(bomb.point.toString(), bomb);

            // GlobalHelper.logMessage("(ADD BOMB)\t" + agent.getLocalName() + "\tNEW BOMB
            // Added\n\t\t" + bomb);
        }

        return bomb;
    }

    /**
     * Mark a bomb as targeted
     * 
     * @param bomb
     */
    public void markBombAsTargeted(Bomb targetBomb) {
        Bomb bomb = bombs.get(targetBomb.point.toString());

        if (null == bomb) {
            return;
        }

        if (this.selectedBomb != bomb) {
            /* Update a bomb other than our selected bomb */
            bomb.updateData(targetBomb);
        } else {
            /* Target bomb is already taken by the others */
            if (selectedBomb.targeteDate.getTime() > targetBomb.targeteDate.getTime()) {
                selectedBomb.updateData(targetBomb);
                unselectBomb();
            } else {
                /* Target bomb is already taken by us */
                if (selectedBomb.targeteDate.getTime() < targetBomb.targeteDate.getTime()) {
                    this.agent.sendTargetBombMessage(this.selectedBomb);
                } else {
                    BombSweeperAgent receiverAgent = GlobalHelper.agents.get(agent.getLocalName());
                    BombSweeperAgent targetAgent = GlobalHelper.agents.get(targetBomb.targetedBy);

                    if (receiverAgent.deployTime.getTime() > targetAgent.deployTime.getTime()) {
                        /* If the bomb is already taken the bomb by the target-agent before us */
                        selectedBomb.updateData(targetBomb);
                        unselectBomb();
                    } else {
                        /* If we are already taken the bomb before the target-agent */
                        this.agent.sendTargetBombMessage(this.selectedBomb);
                    }
                }
            }
        }
    }

    /**
     * Try to pick-up the bomb
     * 
     * @return
     */
    public boolean pickUpBomb() {
        boolean res = env.pickup(agent.getLocalName());

        if (res) {
            selectedBomb.isPickedUp = true;
        }

        return res;
    }

    /**
     * Remove bomb
     * 
     * @param bomb
     */
    public void removeBomb(Bomb bomb) {
        if (bombs.containsKey(bomb.point.toString())) {
            bombs.remove(bomb.point.toString());
        }

        if (null != this.selectedBomb && this.selectedBomb.point.equals(bomb.point)) {
            this.unselectBomb();
        }
    }

    /**
     * Try to drop the bomb
     * 
     * @return
     */
    public boolean dropBomb() {
        boolean res = env.drop(agent.getLocalName());

        return res;
    }
}
