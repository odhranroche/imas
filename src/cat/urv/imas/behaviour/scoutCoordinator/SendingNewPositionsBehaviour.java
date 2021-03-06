/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package cat.urv.imas.behaviour.scoutCoordinator;

import jade.core.behaviours.*;
import jade.core.AID;
import jade.domain.FIPANames;
import jade.lang.acl.ACLMessage;
import cat.urv.imas.onthology.MessageContent;
import cat.urv.imas.agent.*;
import jade.core.Agent;

public class SendingNewPositionsBehaviour extends SimpleBehaviour {

    private ACLMessage msg;
    boolean hasReply;

    public SendingNewPositionsBehaviour(Agent a) {
        super(a);

    }

    @Override
    public void action() {
        ScoutCoordinator agent = (ScoutCoordinator) this.getAgent();

        ACLMessage NewStepRequest = new ACLMessage(ACLMessage.REQUEST);
        NewStepRequest.clearAllReceiver();
        NewStepRequest.addReceiver(agent.coordinatorAgent);
        NewStepRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
        agent.log("Request message to CoordinatorAgent");
        try {
            NewStepRequest.setContentObject(agent.newInfoAgent);
            agent.log("Request message to send new positions");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.msg = NewStepRequest;

        hasReply = false;
        myAgent.send(msg);
        while (done() == false) {
            ACLMessage response = myAgent.receive();
            if (response != null) {
                switch (response.getPerformative()) {
                    case ACLMessage.AGREE:
                        agent.log("AGREE received from " + ((AID) response.getSender()).getLocalName());
                        break;
                    case ACLMessage.INFORM:
                        agent.log("INFORM (new positions received) received from " + ((AID) response.getSender()).getLocalName());
                        hasReply = true;
                        try {
                            Object content = (Object) response.getContent();
                            if (content.equals(MessageContent.NEXT_STEP)) {
                                agent.log("Coordinator Agent has received positions.");
                            } else {
                                agent.log("Coordinator Agent has NOT received positions. WHY?");
                            }
                        } catch (Exception e) {
                            agent.errorLog("Incorrect content: " + e.toString());
                        }
                        break;
                    case ACLMessage.FAILURE:
                        agent.log("The action has failed.");
                        break;
                    default:
                        agent.log("Failed to process the message");
                        break;
                }
            }
        }
    }

    @Override
    public boolean done() {
        return hasReply;
    }

    public int onEnd() {
        hasReply = false;
        return 0;
    }

}
