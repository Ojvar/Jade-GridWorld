package BombSweeper.Messages;

import BombSweeper.BombSweeperAgent;
import Helpers.Bomb;
import Helpers.GlobalHelper;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Message receiver
 */
public class MessageReceiver extends CyclicBehaviour {
    private BombSweeperAgent agent;

    /* Static fields */
    public static final String SEPARATOR = "\n";
    public static final String C_NEW_BOMB = "new-bomb";
    public static final String C_TRAGEG_BOMB = "target-bomb";
    public static final String C_DROP_BOMB = "drop-bomb";

    /**
     * Class ctr
     */
    public MessageReceiver(BombSweeperAgent agent) {
        this.agent = agent;
    }

    /**
     * Action
     */
    @Override
    public void action() {
        ACLMessage message = agent.receive();

        if (null == message) {
            block();
            return;
        }

        String content = message.getContent().toString();

        /* Parse message */
        String[] tokens = content.split(MessageReceiver.SEPARATOR);
        if (0 == tokens.length) {
            return;
        }

        GlobalHelper.logMessage("RECEIVER\t" + agent.getLocalName() + " <- \t" + content);

        /* Try to do an action */
        if (tokens[0].equals(C_NEW_BOMB)) {
            this.senseBombMessage(tokens);
        } else if (tokens[0].equals(C_TRAGEG_BOMB)) {
            this.targetBombMessage(tokens);
        } else if (tokens[0].equals(C_DROP_BOMB)) {
            this.dropBombMessage(tokens);
        }

        done();
    }

    /**
     * A Bomb has been sensed
     * 
     * @param tokens
     */
    private void senseBombMessage(String[] tokens) {
        String sender = tokens[1];
        String bombData = tokens[2];

        Bomb bomb = Bomb.fromString(bombData);

        /* Try to add new bomb */
        agent.addNewBomb(bomb);
    }

    /**
     * Target a bomb
     * 
     * @param tokens
     */
    private void targetBombMessage(String[] tokens) {
        String sender = tokens[1];
        String bombData = tokens[2];

        Bomb bomb = Bomb.fromString(bombData);

        GlobalHelper.logMessage("(RECEIVE)  Target-Bomb\t" + agent.getLocalName() + "\n\t\t" + bomb);

        /* Try to add new bomb */
        agent.markBombAsTargeted(bomb);
    }

    /**
     * Drop a bomb
     * 
     * @param tokens
     */
    private void dropBombMessage(String[] tokens) {
        String sender = tokens[1];
        String bombData = tokens[2];

        Bomb bomb = Bomb.fromString(bombData);

        GlobalHelper.logMessage("(RECEIVE)  Drop-Bomb\t" + agent.getLocalName() + "\n\t\t" + bomb);

        /* Try to add new bomb */
        agent.removeBomb(bomb);
    }
}
