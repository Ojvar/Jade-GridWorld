
import gridworld.LogicalEnv;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

/***
 * Main class**
 * 
 * @author amisen
 *
 */
public class Main {
    /* Agent colors */
    private final static String[] C_AGENTS = new String[] { "blue", "red", "green", "pink", "orange", "cyna", };
    private final static int C_AGENTS_COUNT = 2;
    private final static int C_SENSE_RANGE = 1;

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
    }

    /**
     * Add agents
     */
    private static void addAgents(ContainerController cc) {
        for (int i = 1; i <= C_AGENTS_COUNT; ++i) {
            try {
                String color = C_AGENTS[i - 1];

                /* Create and start an agent */
                AgentController ag = cc.createNewAgent(color + i, "simpleExample.MineSweeperAgent",
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