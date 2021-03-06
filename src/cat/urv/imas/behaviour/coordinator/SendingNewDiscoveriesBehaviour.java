/**
 *  IMAS base code for the practical work.
 *  Copyright (C) 2014 DEIM - URV
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.behaviour.coordinator;

import cat.urv.imas.agent.CoordinatorAgent;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import jade.core.Agent;

/**
 * A request-responder behaviour for System agent, answering to queries from the
 * Coordinator agent. The Coordinator Agent sends a REQUEST of the whole game
 * information and the System Agent sends an AGREE and then an INFORM with the
 * city information.
 */
public class SendingNewDiscoveriesBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply;

    public SendingNewDiscoveriesBehaviour(Agent agent) {
        super(agent);
        hasReply = false;
    }

    @Override
    public void action() {
        CoordinatorAgent agent = (CoordinatorAgent) this.getAgent();
        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.clearAllReceiver();
        message.addReceiver(agent.harvesterCoordinator);

        try {
            message.setContentObject(agent.getNewInfoDiscoveriesList());
            agent.log("Sending new discoveries");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.msg = message;

        hasReply = false;
        myAgent.send(msg);
        hasReply = true;
    }

    @Override
    public boolean done() {
        return hasReply;
    }

    @Override
    public int onEnd() {
        hasReply = false;
        return 0;
    }

}
