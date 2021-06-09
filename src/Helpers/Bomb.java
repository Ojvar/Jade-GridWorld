package Helpers;

import java.awt.Point;
import java.util.Date;

import BombSweeper.BombSweeperAgent;

/**
 * Bomb class
 */
public class Bomb {
    public Point point;
    public Date targeteDate;
    public String targetedBy;

    /* Separator */
    public static final String SEPARATOR = "\t";

    /**
     * Ctr
     * 
     * @param point
     */
    public Bomb(Point point) {
        this.point = new Point(point);
    }

    /**
     * Ctr
     * 
     * @param bomb
     */
    public Bomb(Bomb bomb) {
        this.point = new Point(bomb.point);
        this.targeteDate = bomb.targeteDate;
        this.targetedBy = bomb.targetedBy;
    }

    /**
     * Clear targetedBy valued
     * 
     * @param agent
     */
    public void untarget() {
        this.targetedBy = null;
        this.targeteDate = null;
    }

    /**
     * Set bomb as a targeted-bomb
     * 
     * @param agent
     */
    public void target(BombSweeperAgent agent) {
        target(agent.getLocalName(), new Date());
    }

    /**
     * Set bomb as a targeted-bomb
     * 
     * @param agent
     */
    public void target(String agentName, Date targetDate) {
        this.targetedBy = agentName;
        this.targeteDate = targetDate;
    }

    /**
     * To String
     */
    @Override
    public String toString() {
        /* Format: X Y TARGETED-BY TARGETED-AT */
        return String.join(SEPARATOR, "" + point.x, "" + point.y, targetedBy,
                (null != targeteDate) ? "" + targeteDate.getTime() : null);
    }

    /**
     * Create a bomb by parsing a string
     * 
     * @param bombData
     * @return
     */
    public static Bomb fromString(String bombData) {
        /* 5 is a the number os string's tokens */
        final int PARAMS_LEN = 4;
        String[] tokens = bombData.split(Bomb.SEPARATOR);

        /* Creat new bomb */
        Bomb bomb = new Bomb(new Point(0, 0));

        if (PARAMS_LEN == tokens.length) {
            bomb.point.setLocation(Integer.valueOf(tokens[0]), Integer.valueOf(tokens[1]));

            bomb.targetedBy = "null".equals(tokens[2]) ? null : tokens[2];
            bomb.targeteDate = "null".equals(tokens[3]) ? null : new Date(Long.valueOf(tokens[3]));
        }

        return bomb;
    }

    /**
     * Update bomb data
     * 
     * @param newBomb
     */
    public void updateData(Bomb newBomb) {
        this.point = new Point(newBomb.point);
        this.targeteDate = newBomb.targeteDate;
        this.targetedBy = newBomb.targetedBy;
    }
}
