package simpleExample;

import java.awt.Point;

/**
 * Mine class
 */
public class Mine {
    public Point point;
    public String pickedUpBy = null;
    public String targetedBy = null;

    /**
     * Ctr
     * 
     * @param point
     */
    public Mine(Point point) {
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
     * Set mine as a sensed mine
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
     * Set mine as a targeted-mine
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
