package gridworld;

import java.awt.Dimension;
import java.awt.Point;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import javax.swing.SwingUtilities;

// Standard java imports

import gridworld.lib.ObsVect;
import gridworld.lib.ObsVectListener;
import gridworld.lib.Signal;

public class LogicalEnv implements ObsVectListener {
    static LogicalEnv instance = null;
    // To hold our reference to the window
    final protected Window m_window;

    // max distance of cells visible to each agent
    protected int _senserange = 10;

    // size of environment
    protected Dimension m_size = new Dimension(16, 16);

    private HashMap<String, Agent> agentmap = new HashMap<String, Agent>();

    /* ---- ALL the stuff in the environment ----- */

    // list of agents (Agent)
    protected ObsVect _agents = new ObsVect(this);
    // location of bomb traps
    protected ObsVect _traps = new ObsVect();
    // list of coordinates (Point) containing stones
    protected ObsVect _stones = new ObsVect();
    // list of coordinates (Point) containing bombs
    protected ObsVect _bombs = new ObsVect();
    // id for identifiable objects
    protected String _objType = "default";

    /* ------------------------------------------ */

    /* ---- SIGNALS ------------------------------- */
    // / emitted on collection of a bomb in the bomb trap
    public transient Signal signalBombTrapped = new Signal("env bomb trapped");

    public transient Signal signalSenseRangeChanged = new Signal("env sense range changed");

    // emitted if environment is resized
    public transient Signal signalSizeChanged = new Signal("env size changed");

    // emitted if bomb traps location changed
    public transient Signal signalTrapChanged = new Signal("env trap position changed");

    /* ------------------------------------------ */

    // The default constructor
    private LogicalEnv() {

        // Create the window
        m_window = new Window(this);
    }

    public synchronized static LogicalEnv getEnv() {
        if (instance == null) {
            instance = new LogicalEnv();
        }
        return instance;
    }

    /* Called from Jade agents */

    // Enter the agent into the world
    // Succesful returns true, else the ExternalActionFailedExeption exception is
    // thrown
    public boolean enter(String sAgent, Double x, Double y, String sColor) {

        Point position = new Point(x.intValue(), y.intValue());
        if (!isFree(position)) {
            System.out.println(sAgent + " tries to enter in " + position + " which is not empty");
            return false;
        }
        // Get the agent
        Agent agent = getAgent(sAgent);
        if (agent != null) {
            System.out.println(sAgent + " is already entered");
            writeToLog("agent already entered");
            return false;
            // throw new ExternalActionFailedExeption("Agent \""+agent.getName()+"\" has
            // already entered.");
        }
        addAgent(sAgent);
        agent = getAgent(sAgent);

        // Give a signal that we want to move
        agent.signalMove.emit();

        writeToLog("Agent entered: " + agent.getName());

        String pos = "(" + x + "," + y + ")";

        // Agent already entered

        // Update the agent his position
        agent._position = position;

        // Which color does the agent want to be
        int nColorID = getColorID(sColor);
        agent._colorID = nColorID;

        // Redraw so we can see the agent
        validatewindow();
        m_window.repaint();

        // We came so far, this means success!
        agent.signalMoveSucces.emit();
        return true;

    }

    // Move the agent north
    public boolean north(String sAgent) {
        // Get the correct agent
        Agent agent = getAgent(sAgent);

        // Get the agent his position
        Point p = (Point) agent.getPosition().clone();
        p.y = p.y - 1;

        // Set the position for the agent
        if (!setAgentPosition(agent, p)) {
            System.out.println(sAgent + " tries to go east but cannot !");
            return false;
        }

        // Redraw the window
        validatewindow();
        m_window.repaint();
        return true;
    }

    // Move the agent east
    public boolean east(String sAgent) {
        // Get the correct agent
        Agent agent = getAgent(sAgent);

        // Get the agent his position
        Point p = (Point) agent.getPosition().clone();
        p.x = p.x + 1;

        // Set the position for the agent
        if (!setAgentPosition(agent, p)) {
            System.out.println(sAgent + " tries to go east but cannot !");
            return false;
        }

        // Redraw the window
        validatewindow();
        m_window.repaint();
        return true;
    }

    // Move the agent south
    public boolean south(String sAgent) {
        // Get the correct agent
        Agent agent = getAgent(sAgent);

        // Get the agent his position
        Point p = (Point) agent.getPosition().clone();
        p.y = p.y + 1;

        // Set the position for the agent
        if (!setAgentPosition(agent, p)) {
            System.out.println(sAgent + " tries to go east but cannot !");
            return false;
        }

        // Redraw the window
        validatewindow();
        m_window.repaint();
        return true;
    }

    // Move the agent west
    public boolean west(String sAgent) {
        // Get the correct agent
        Agent agent = getAgent(sAgent);

        // Get the agent his position
        Point p = (Point) agent.getPosition().clone();
        p.x = p.x - 1;

        // Set the position for the agent
        if (!setAgentPosition(agent, p)) {
            System.out.println(sAgent + " tries to go west but cannot !");
            return false;
        }

        // Redraw the window
        validatewindow();
        m_window.repaint();
        return true;
    }

    // Pickup a bomb
    public boolean pickup(String sAgent) {
        // Get the agent
        Agent agent = getAgent(sAgent);

        // Let everyone know we are going to pick up a bomb
        agent.signalPickupBomb.emit();

        // see if we are not already carrying a bomb
        if (agent.atCapacity()) {
            System.out.println(sAgent + " is already holding a bomb");
            writeToLog("Pickup bomb failed");
            return false;
        }

        // we are not already carying a bomb so get this one
        TypeObject bomb = removeBomb(agent.getPosition());
        if (bomb == null) {
            System.out.println(sAgent + " is trying to pick a bomb in a place without bombs");
            writeToLog("Pickup bomb failed");
            return false;
        }

        // Yes
        agent.signalPickupBombSucces.emit();

        // there was a bomb at that position, so set token
        agent.pickBomb(bomb);

        // show what happened
        validatewindow();
        m_window.repaint();
        return true;
    }

    // Drop a bomb
    public boolean drop(String sAgent) {
        // Get the agent
        Agent agent = getAgent(sAgent);
        // we are going to drop a bomb
        agent.signalDropBomb.emit();

        TypeObject bomb = agent.senseBomb();
        // see if we are actually carrying a bomb
        if (bomb == null) {
            System.out.println(sAgent + " does not carry bomb");
            writeToLog("Drop bomb failed");
            return false;
        }

        Point pos = agent.getPosition();
        // see if we can drop that bomb here
        if (!addBomb(pos) && (isTrap(pos) == null || agent.senseBomb(isTrap(pos).getType()) == null)) {
            System.out.println(sAgent + " puts a bomb on another bomb");
            writeToLog("Drop bomb failed");
            return false;
        }

        if (isTrap(pos) != null && agent.senseBomb(isTrap(pos).getType()) != null) {
            System.out.println(sAgent + " puts the bomb in a trap");
            signalBombTrapped.emit();
            agent.dropBomb();
            return true;
        }

        // unset token
        agent.dropBomb();

        agent.signalDropBombSucces.emit();

        // Show it
        validatewindow();
        m_window.repaint();
        return true;
    }

    // what is it
    public String getDescription() {
        return "Blockworld Environment";
    }

    public String toString() {
        return "gridworld";
    }

    public void refresh() {
    }

    // Add an agent to the environment
    public void addAgent(String sAgent) {
        final Agent agent = new Agent(sAgent);
        _agents.add(agent);
        agentmap.put(sAgent, agent);

        writeToLog("agent " + agent + " added");
    }

    // Remove the agent from the environment
    public void removeAgent(String sAgent) {
        try {
            Agent a = getAgent(sAgent);
            a.reset();

            _agents.remove(sAgent);
            agentmap.remove(sAgent);

            writeToLog("Agent removed: " + sAgent);

            synchronized (this) {
                notifyAll();
            }
        } catch (Exception e) {
        }
    }

    /* END Standard functions -------------------------------------- */

    // Get the agent from its name
    public Agent getAgent(String name) {
        Agent a = null;
        a = agentmap.get(name);
        return a;
    }

    // Get the environment width
    public int getWidth() {
        return m_size.width;
    }

    // Get the environment height
    public int getHeight() {
        return m_size.height;
    }

    // Return the agents
    public Collection getAgents() {
        return _agents;
    }

    // Get senserange
    public int getSenseRange() {
        return _senserange;
    }

    // Redrawing the window is a nightmare, this does some redraw stuff
    private void validatewindow() {
        Runnable addTab = new Runnable() {
            public void run() {
                // try {Thread.sleep(500);} catch(Exception e) {}
                m_window.doLayout();
                // Changed SA: NO you may not close
                if (!m_window.isVisible()) {
                    m_window.setVisible(true);
                }
            }
        };
        SwingUtilities.invokeLater(addTab);
    }

    // Move the agent
    private boolean setAgentPosition(Agent agent, Point position) {
        agent.signalMove.emit();

        if (outOfBounds(position))
            return false;

        // suspend thread if some other agent is blocking our entrance

        // Is the position free?
        if (!isFree(position))
            return false;

        agent.signalMoveSucces.emit();

        // there may be other threads blocked because this agent was in the way,
        // notify
        // them of the changed state of environment
        synchronized (this) {
            notifyAll();
        }

        // set the agent position
        agent._position = position;
        return true;
    }

    // Sends a bom in the senserange of the agent
    public Set<Point> senseBombs(String agent) {
        // Get the agent his position
        Point position = getAgent(agent).getPosition();

        // iterate over all bombs and decide according to distance if it is in
        // vision range
        Set<Point> visible = new java.util.HashSet<Point>();
        synchronized (_bombs) {
            Iterator i = _bombs.iterator();
            while (i.hasNext()) {
                TypeObject b = (TypeObject) i.next();
                if (position.distance(b.getPosition()) <= _senserange)
                    visible.add(b.getPosition());
            }
        }

        return visible;
    }

    // check if point is within environment boundaries
    // return false is p is within bounds
    protected boolean outOfBounds(Point p) {
        if ((p.x >= m_size.getWidth()) || (p.x < 0) || (p.y >= m_size.getHeight()) || (p.y < 0)) {
            return true;
        }

        return false;
    }

    // Is the position free?
    public boolean isFree(final Point position) {
        return (isStone(position)) == null && (isAgent(position) == null);
    }

    // Check for agent at coordinate. \return Null if there is no agent at the
    // specified coordinate. Otherwise return a reference to the agent there.
    public Agent isAgent(final Point p) {
        Iterator i = _agents.iterator();
        while (i.hasNext()) {
            final Agent agent = (Agent) i.next();
            if (p.equals(agent.getPosition()))
                return agent;
        }
        return null;
    }

    // Is there a stone at this point
    public TypeObject isStone(Point p) {
        Iterator i = _stones.iterator();
        while (i.hasNext()) {
            TypeObject stones = (TypeObject) i.next();
            if (p.equals(stones.getPosition()))
                return stones;
        }
        return null;
    }

    // see if there is a trap at the specified coordinate
    public TypeObject isTrap(Point p) {
        Iterator i = _traps.iterator();
        while (i.hasNext()) {
            TypeObject trap = (TypeObject) i.next();
            if (p.equals(trap.getPosition()))
                return trap;
        }
        return null;
    }

    public TypeObject isBomb(Point p) {
        synchronized (_bombs) {
            Iterator i = _bombs.iterator();
            while (i.hasNext()) {
                TypeObject bomb = (TypeObject) i.next();
                if (p.equals(bomb.getPosition()))
                    return bomb;
            }
        }
        return null;
    }

    // Remove bomb at position TODO Jaap; why is this different then remove stone???
    public TypeObject removeBomb(Point position) {
        // find bomb in bombs list
        synchronized (_bombs) {
            Iterator i = _bombs.iterator();

            while (i.hasNext()) {
                TypeObject bomb = (TypeObject) i.next();
                if (position.equals(bomb.getPosition())) {
                    i.remove();
                    return bomb;
                }
            }
        }
        return null;
    }

    // remove stone at position
    public boolean removeStone(Point position) {
        // find stone in stones list
        Iterator i = _stones.iterator();

        while (i.hasNext())
            // if( position.equals( i.next() ) ) {
            // Changed SA:
            if (position.equals(((TypeObject) i.next()).getPosition())) {
                i.remove();

                // there may be other threads blocked because this agent was in
                // the way, notify
                // them of the changed state of environment
                synchronized (this) {
                    notifyAll();
                }

                return true;
            }

        return false;
    }

    // remove trap at position
    public boolean removeTrap(Point position) {
        // find trap in traps list
        Iterator i = _traps.iterator();

        while (i.hasNext())
            if (position.equals(((TypeObject) i.next()).getPosition())) {
                i.remove();

                // Sohan: I believe this notification is unnecessary, commented it out:
                // synchronized( this ) {
                // notifyAll();
                // }

                return true;
            }

        return false;
    }

    // Add a stone at the given position
    public boolean addStone(Point position) throws IndexOutOfBoundsException {
        // valid coordinate
        if (outOfBounds(position))
            throw new IndexOutOfBoundsException("setStone out of range: " + position + ", " + m_size);

        // is position clear of other stuff
        // Changed SA:
        if (isBomb(position) != null || isStone(position) != null || isTrap(position) != null)
            return false;

        _stones.add(new TypeObject(_objType, position));

        // notifyEvent("wallAt", position);

        return true;
    }

    // Add a bomb to the environment
    public boolean addBomb(Point position) throws IndexOutOfBoundsException {
        if (outOfBounds(position))
            throw new IndexOutOfBoundsException("addBomb outOfBounds: " + position + ", " + m_size);

        // is position clear of other stuff
        if (isBomb(position) != null || isStone(position) != null || isTrap(position) != null)
            return false;

        // all clear, accept bomb
        synchronized (_bombs) {
            _bombs.add(new TypeObject(_objType, position));
        }

        // notifyEvent("bombAt", position);

        return true;
    }

    // Add a trap at the given position
    public boolean addTrap(Point position) throws IndexOutOfBoundsException {
        // valid coordinate
        if (outOfBounds(position))
            throw new IndexOutOfBoundsException("setTrap out of range: " + position + ", " + m_size);

        // is position clear of other stuff
        // Changed SA:
        if (isBomb(position) != null || isStone(position) != null || isTrap(position) != null)
            return false;

        _traps.add(new TypeObject(_objType, position));

        // notifyEvent("trapAt", position);

        return true;
    }

    // Print a message to the console
    static public void writeToLog(String message) {
        System.out.println("gridworld: " + message);
    }

    // Which color does the agent want to be!
    private int getColorID(String sColor) {
        if (sColor.equals("army")) {
            return 0;
        } else if (sColor.equals("blue")) {
            return 1;
        } else if (sColor.equals("gray")) {
            return 2;
        } else if (sColor.equals("green")) {
            return 3;
        } else if (sColor.equals("orange")) {
            return 4;
        } else if (sColor.equals("pink")) {
            return 5;
        } else if (sColor.equals("purple")) {
            return 6;
        } else if (sColor.equals("red")) {
            return 7;
        } else if (sColor.equals("teal")) {
            return 8;
        } else if (sColor.equals("yellow")) {
            return 9;
        }

        // Red is the default
        return 7;
    }

    // Set the senserange
    public void setSenseRange(int senserange) {
        _senserange = senserange;
        signalSenseRangeChanged.emit();
    }

    // helper function, calls setSize(Dimension)
    public void setSize(int width, int height) {
        setSize(new Dimension(width, height));
    }

    // resize world
    public void setSize(Dimension size) {
        m_size = size;
        signalSizeChanged.emit();

        Iterator i = _bombs.iterator();
        while (i.hasNext()) {
            if (outOfBounds(((TypeObject) i.next()).getPosition()))
                i.remove();
        }
        i = _stones.iterator();
        while (i.hasNext()) {
            if (outOfBounds((Point) i.next()))
                i.remove();
        }
        i = _traps.iterator();
        while (i.hasNext()) {
            if (outOfBounds(((TypeObject) i.next()).getPosition()))
                i.remove();
        }
    }

    // what kind of object is it, bomb, stone, wall ?
    public String getObjType() {
        return _objType;
    }

    // what kind of object is it, bomb, stone, wall ?
    public void setObjType(String objType) {
        _objType = objType;
    }

    // Remove everything
    public void clear() {
        _stones.removeAllElements();
        _bombs.removeAllElements();
        _traps.removeAllElements();
    }

    // Save the environment
    public void save(OutputStream destination) throws IOException {
        ObjectOutputStream stream = new ObjectOutputStream(destination);
        stream.writeObject(m_size);

        stream.writeInt(_senserange);

        stream.writeObject((Vector) _stones);
        stream.writeObject((Vector) _bombs);
        stream.writeObject((Vector) _traps);
        stream.flush();
    }

    // Load the environment
    public void load(InputStream source) throws IOException, ClassNotFoundException {
        ObjectInputStream stream = new ObjectInputStream(source);
        Dimension size = (Dimension) stream.readObject();

        int senserange = stream.readInt();

        Vector stones = (Vector) stream.readObject();
        Vector bombs = (Vector) stream.readObject();
        Vector traps = (Vector) stream.readObject();

        // delay assignments until complete load is succesfull
        m_size = size;
        _senserange = senserange;

        signalSizeChanged.emit();
        signalTrapChanged.emit();
        signalSenseRangeChanged.emit();

        clear();

        _stones.addAll(stones);
        _bombs.addAll(bombs);
        _traps.addAll(traps);
    }

    /* END Helper functions -------------------------------------- */

    /* Listeners ------------------------------------------------ */

    // / This listener is notified upon changes regarding the Agent list.
    // / Please note that this only involves registering new agents or
    // / removing existing agents. To track agent position changes, add
    // / a listener to that specific agent.
    // / \sa Agent
    public void addAgentListener(ObsVectListener o) {
        _agents.addListener(o);
    }

    // / This listener is notified upon changes regarding the Stones list.
    public void addStonesListener(ObsVectListener o) {
        _stones.addListener(o);
    }

    // / This listener is notified upon changes regarding the Bombs list.
    public void addBombsListener(ObsVectListener o) {
        _bombs.addListener(o);
    }

    // / This listener is notified upon changes regarding the Traps list.
    public void addTrapsListener(ObsVectListener o) {
        _traps.addListener(o);
    }

    /* END Liseteners ------------------------------------------ */

    /* Overrides for ObsVector --------------------------------- */

    public void onAdd(int index, Object o) {
    }

    public void onRemove(int index, Object o) {
        ((Agent) o).deleteObservers();
    }

    public Point getPosition(String localName) {
        return getAgent(localName).getPosition();
    }

    /* END Overrides for ObsVector --------------------------------- */
}
