package simpleExample.behaviors;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import simpleExample.Bomb;
import simpleExample.GlobalHelper;

/**
 * Send bomb find message
 */
public class SendBombDiscoverMessage extends OneShotBehaviour {
	private Bomb bomb = null;
	private Agent agent = null;

	/**
	 * Ctr
	 */
	public SendBombDiscoverMessage(Agent agent, Bomb bomb) {
		this.bomb = bomb;
		this.agent = agent;
	}

	/**
	 * Action
	 */
	@Override
	public void action() {
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		String content = this.agent.getLocalName() + "\t" + this.bomb.toString();

		GlobalHelper.addAllAgentsAsReceiver(agent, message);
		message.setContent(content);

		agent.send(message);
	}
}
