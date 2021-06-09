package BombSweeper.Messages;

import BombSweeper.BombSweeperAgent;
import Helpers.Bomb;
import Helpers.GlobalHelper;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;

public class DropBombMessage extends OneShotBehaviour {
    private BombSweeperAgent agent;
    private Bomb bomb;

    /**
     * Class ctr
     * 
     * @param agent
     * @param bomb
     */
    public DropBombMessage(BombSweeperAgent agent, Bomb bomb) {
        this.agent = agent;
        this.bomb = bomb;
    }

    /**
     * Send message
     */
    @Override
    public void action() {
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        String content = String.join(MessageReceiver.SEPARATOR, MessageReceiver.C_DROP_BOMB, agent.getLocalName(),
                bomb.toString());

        GlobalHelper.addAllAgentsAsReceiver(agent, message, false);
        message.setContent(content);

        GlobalHelper.logMessage("SEND\t" + agent.getLocalName() + " ->\t" + content);

        agent.send(message);
        done();
    }

}
