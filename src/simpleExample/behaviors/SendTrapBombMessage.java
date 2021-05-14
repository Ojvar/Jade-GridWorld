package simpleExample.behaviors;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import simpleExample.Bomb;
import simpleExample.GlobalHelper;

/**
 * Send Trap a bomb message
 */
public class SendTrapBombMessage extends OneShotBehaviour {
	private Bomb bomb = null;
	private Agent agent = null;

	/**
	 * Ctr
	 */
	public SendTrapBombMessage(Agent agent, Bomb bomb) {
		this.bomb = bomb;
		this.agent = agent;
	}

	/**
	 * Action
	 */
	@Override
	public void action() {
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		String content = MessageReceive.C_TRAP_BOMB + "\t" + this.agent.getLocalName() + "\t" + this.bomb.toString();

		GlobalHelper.addAllAgentsAsReceiver(agent, message);
		message.setContent(content);

		agent.send(message);
	}
}
