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
package cat.urv.imas.behaviour.scoutCoordinator;

import cat.urv.imas.agent.ScoutCoordinator;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import jade.core.Agent;

public class SendingNewDiscoveriesBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply;

    public SendingNewDiscoveriesBehaviour(Agent agent) {
        super(agent);
        hasReply = false;
    }

    @Override
    public void action() {
        System.out.println("(ScoutCoordinator) Starting SendingNewDiscoveriesBehaviour");
        ScoutCoordinator agent = (ScoutCoordinator) this.getAgent();

        ACLMessage message = new ACLMessage(ACLMessage.INFORM);
        message.clearAllReceiver();
        message.addReceiver(agent.coordinatorAgent);

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
