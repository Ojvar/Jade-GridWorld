package simpleExample.behaviors;

import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.lang.acl.ACLMessage;
import simpleExample.GlobalHelper;
import simpleExample.Mine;

/**
 * Send mine find message
 */
public class SendMineFindMessage extends OneShotBehaviour {
	private Mine mine = null;
	private Agent agent = null;

	/**
	 * Ctr
	 */
	public SendMineFindMessage(Agent agent, Mine mine) {
		this.mine = mine;
		this.agent = agent;
	}

	/**
	 * Action
	 */
	@Override
	public void action() {
		ACLMessage message = new ACLMessage(ACLMessage.INFORM);
		String content = this.agent.getLocalName() + "\t" + this.mine.toString();

		GlobalHelper.addAllAgentsAsReceiver(agent, message);
		message.setContent(content);

		agent.send(message);
	}
}
