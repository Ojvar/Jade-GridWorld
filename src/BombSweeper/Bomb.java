package BombSweeper;

import java.awt.Point;

/**
 * Bomb class
 */
public class Bomb {
    public Point point;
    public String pickedUpBy = null;
    public String targetedBy = null;

    /**
     * Ctr
     * 
     * @param point
     */
    public Bomb(Point point) {
        this.point = point;
    }

    /**
     * Clear
     * 
     * @param agent
     */
    public void clearSensedMark() {
        this.pickedUpBy = null;
    }

    /**
     * Set bomb as a sensed bomb
     * 
     * @param agent
     */
    public void pickedUpBy(String agent) {
        this.pickedUpBy = agent;
    }

    /**
     * Clear targetedBy valued
     * 
     * @param agent
     */
    public void clearAsTargeted(String agent) {
        this.targetedBy = null;
    }

    /**
     * Set bomb as a targeted-bomb
     * 
     * @param agent
     */
    public void markAsTargeted(String agent) {
        this.targetedBy = agent;
    }

    /**
     * To String
     */
    @Override
    public String toString() {
        return this.point.x + "," + this.point.y;
    }
}
