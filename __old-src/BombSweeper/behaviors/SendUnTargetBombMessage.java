package BombSweeper.behaviors;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import BombSweeper.Bomb;
import BombSweeper.GlobalHelper;

/**
 * Send un-target a bomb message
 */
public class SendUnTargetBombMessage extends OneShotBehaviour {
	private Bomb bomb = null;
	private Agent agent = null;

	/**
	 * Ctr
	 */
	public SendUnTargetBombMessage(Agent agent, Bomb bomb) {
		this.bomb = bomb;
		this.agent = agent;
	}

	/**
	 * Action
	 */
	@Override
	public void action() {
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		String content = MessageReceive.C_UNTARGET_BOMB + "\t" + this.agent.getLocalName() + "\t" + this.bomb.toString();

		GlobalHelper.addAllAgentsAsReceiver(agent, message);
		message.setContent(content);

		agent.send(message);
	}
}
