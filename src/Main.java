
import java.awt.Point;

import gridworld.LogicalEnv;
import gridworld.TypeObject;
import gridworld.lib.ObsVectListener;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;
import BombSweeper.GlobalHelper;

/***
 * Main class**
 * 
 * @author amisen
 *
 */
public class Main {
    /* Agent colors */
    private final static String[] C_AGENTS = new String[] { "blue", "red", "green", "pink", "orange", "cyna", };
    private final static int C_AGENTS_COUNT = 3;
    private final static int C_SENSE_RANGE = 2;

    /**
     * Main loop
     *
     * @throws StaleProxyException
     */
    public static void main(String[] args) {
        Main.initEnv();
    }

    /**
     * Init env
     */
    private static void initEnv() {
        /* Create runtime instance */
        Runtime rt = Runtime.instance();

        /* Create a Profile */
        Profile pf = new ProfileImpl();
        pf.setParameter(Profile.MAIN_HOST, "localhost");
        pf.setParameter(Profile.GUI, "true");
        pf.setParameter(Profile.CONTAINER_NAME, "Ojvar");

        /* Create a container controller */
        ContainerController cc = rt.createAgentContainer(pf);

        /* Setup environment */
        setupGrid();

        /* Add agents */
        addAgents(cc);
    }

    /**
     * Setup grid
     */
    private static void setupGrid() {
        final LogicalEnv env = LogicalEnv.getEnv();

        env.setSenseRange(C_SENSE_RANGE);

        /* Add trap listener */
        env.addTrapsListener(new ObsVectListener() {
            @Override
            public void onAdd(int index, Object element) {
                TypeObject tObj = (TypeObject) element;
                Point newTrap = tObj.getPosition();

                System.out.println("NEW TRAP ADDED " + element.toString());
                GlobalHelper.traps.add(newTrap);
            }

            @Override
            public void onRemove(int index, Object element) {
                TypeObject tObj = (TypeObject) element;
                Point removedTrap = tObj.getPosition();

                System.out.println("TRAP REMOVED " + element.toString());
                GlobalHelper.traps.remove(removedTrap);
            }
        });
    }

    /**
     * Add agents
     */
    private static void addAgents(ContainerController cc) {
        for (int i = 1; i <= C_AGENTS_COUNT; ++i) {
            try {
                String color = C_AGENTS[i - 1];

                /* Create and start an agent */
                AgentController ag = cc.createNewAgent(color + i, "BombSweeper.BombSweeperAgent",
                        new Object[] { color });

                ag.start();
                System.out.println("New Sweeper deployed ," + ag.getName());
            } catch (Exception ex) {
                System.out.println("\n\nError\n\n\n");
                ex.printStackTrace();
            }
        }
    }
}