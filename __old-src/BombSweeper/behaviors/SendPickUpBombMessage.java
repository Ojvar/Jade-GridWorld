package BombSweeper.behaviors;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import BombSweeper.Bomb;
import BombSweeper.GlobalHelper;

/**
 * Send PickUP a bomb message
 */
public class SendPickUpBombMessage extends OneShotBehaviour {
	private Bomb bomb = null;
	private Agent agent = null;

	/**
	 * Ctr
	 */
	public SendPickUpBombMessage(Agent agent, Bomb bomb) {
		this.bomb = bomb;
		this.agent = agent;
	}

	/**
	 * Action
	 */
	@Override
	public void action() {
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		String content = MessageReceive.C_PICK_BOMB + "\t" + this.agent.getLocalName() + "\t" + this.bomb.toString();

		GlobalHelper.addAllAgentsAsReceiver(agent, message);
		message.setContent(content);

		agent.send(message);
	}
}
