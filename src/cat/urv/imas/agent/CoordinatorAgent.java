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
package cat.urv.imas.agent;

import static cat.urv.imas.agent.ImasAgent.OWNER;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.behaviour.coordinator.*;
import cat.urv.imas.onthology.InfoAgent;
import cat.urv.imas.onthology.InfoDiscovery;
import cat.urv.imas.onthology.InfoMapChanges;
import jade.core.*;
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;
import java.util.ArrayList;

/**
 * The main Coordinator agent. TODO: This coordinator agent should get the game
 * settings from the System agent every round and share the necessary
 * information to other coordinators.
 */
public class CoordinatorAgent extends ImasAgent {

    /**
     * Game settings in use.
     */
    public GameSettings game;
    /**
     * System agent id.
     */
    public AID systemAgent;
    /**
     * ScoutCoordinator agent id.
     */
    public AID scoutCoordinator;
    /**
     * HarvesterCoordinator agent id.
     */
    public AID harvesterCoordinator;
    /**
     * InfoAgent. It contains the following information of all agents.
     */
    public ArrayList<InfoAgent> newInfoAgent;
    /**
     * InfoDiscovery. It contains all new discoveries of a turn.
     */
    public ArrayList<InfoDiscovery> newInfoDiscoveriesList;
    /**
     * InfoMapChanges. It contains all new changes we did in this turn over the
     * map which have to be updated by SystemAgent.
     */
    public InfoMapChanges newChangesOnMap;
    /**
     * isMapUpdated. If the map has been already updated, then we can send the
     * new positions (we need it because we need to know the new map to modify
     * the previous position of our agents in the newInfoAgent variable in order
     * to delete the agents correctly).
     */
    public boolean updatedMap = false;
    public boolean sentNewPositions = false;
    /**
     * updatingMapBehaviour. It is going to be the behaviour that sends a
     * request to update the map.
     */
    public Behaviour updatingMapBehaviour;

    public void setUpdatedMap(boolean temp) {
        this.updatedMap = temp;
    }

    public boolean getUpdatedMap() {
        return this.updatedMap;
    }

    /**
     * Gets the info for new discoveries in this turn.
     *
     * @return info ALL discoveries (is a list). It has to be an ArrayList
     * because List is not serializable.
     */
    public ArrayList<InfoDiscovery> getNewInfoDiscoveriesList() {
        return this.newInfoDiscoveriesList;
    }

    /**
     * Update value of the discoveries of this turn.
     *
     * @param newInfo information about new discoveries discovered in this turn.
     */
    public void setNewInfoDiscoveriesList(ArrayList<InfoDiscovery> newInfo) {
        try {
            this.newInfoDiscoveriesList.clear();
        } catch (Exception e) {

        }
        this.newInfoDiscoveriesList = newInfo;

        try {
            boolean condition;
            condition = this.newInfoDiscoveriesList.isEmpty();
        } catch (Exception e) {

        }
    }

    /**
     * Builds the coordinator agent.
     */
    public CoordinatorAgent() {
        super(AgentType.COORDINATOR);
    }

    /**
     * Update the game settings.
     *
     * @param game current game settings.
     */
    public void setGame(GameSettings game) {
        this.game = game;
    }

    /**
     * Gets the current game settings.
     *
     * @return the current game settings.
     */
    public GameSettings getGame() {
        return this.game;
    }

    /**
     * Gets the next information for all agents.
     *
     * @return info ALL agentS (is a list). It has to be an ArrayList because
     * List is not serializable.
     */
    public ArrayList<InfoAgent> getNewInfoAgent() {
        return this.newInfoAgent;
    }

    /**
     * Update agent information.
     *
     * @param newInfo information of all agents for next simulation step.
     */
    public void setNewInfoAgent(ArrayList<InfoAgent> newInfo) {
        this.newInfoAgent = newInfo;
    }

    /**
     * Add new agent information.
     *
     * @param newInfo information of all agents for next simulation step.
     */
    public void addNewInfoAgent(ArrayList<InfoAgent> newInfo) {
        this.newInfoAgent.addAll(newInfo);
    }

    /**
     * Update the NewChangesOnMap.
     *
     * @param temp current new changes we did this turn.
     */
    public void setNewChangesOnMap(InfoMapChanges temp) {
        this.newChangesOnMap = temp;
    }

    /**
     * Gets the current NewChangesOnMap.
     *
     * @return the current game settings.
     */
    public InfoMapChanges getNewChangesOnMap() {
        return this.newChangesOnMap;
    }

    /**
     * Agent setup method - called when it first come on-line. Configuration of
     * language to use, ontology and initialization of behaviours.
     */
    @Override
    protected void setup() {
        /* ** Very Important Line (VIL) ***************************************/
        this.setEnabledO2ACommunication(true, 1);
        /* ********************************************************************/

        // Register the agent to the DF
        ServiceDescription sd1 = new ServiceDescription();
        sd1.setType(AgentType.COORDINATOR.toString());
        sd1.setName(getLocalName());
        sd1.setOwnership(OWNER);

        DFAgentDescription dfd = new DFAgentDescription();
        dfd.addServices(sd1);
        dfd.setName(getAID());
        try {
            DFService.register(this, dfd);
            log("Registered to the DF");
        } catch (FIPAException e) {
            System.err.println(getLocalName() + " registration with DF unsucceeded. Reason: " + e.getMessage());
            doDelete();
        }

        // search all agents we need to talk to.
        ServiceDescription searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SYSTEM.toString());
        this.systemAgent = UtilsAgents.searchAgent(this, searchCriterion);
        searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.SCOUT_COORDINATOR.toString());
        this.scoutCoordinator = UtilsAgents.searchAgent(this, searchCriterion);
        searchCriterion = new ServiceDescription();
        searchCriterion.setType(AgentType.HARVESTER_COORDINATOR.toString());
        this.harvesterCoordinator = UtilsAgents.searchAgent(this, searchCriterion);
        // searchAgent is a blocking method, so we will obtain always a correct AID

        /* ********************************************************************/
        // we add a behaviour that sends the message and waits for an answer
        ACLMessage NewStepRequest = new ACLMessage(ACLMessage.REQUEST);
        NewStepRequest.clearAllReceiver();
        NewStepRequest.addReceiver(this.systemAgent);
        NewStepRequest.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);

        try {
            NewStepRequest.setContentObject(this.newInfoAgent);
        } catch (Exception e) {
            e.printStackTrace();
        }

        FSMBehaviour fsm = new FSMBehaviour(this) {
            public int onEnd() {
                System.out.println("(CoordinatorAgent) FSM behaviour completed.");
                myAgent.doDelete();
                return super.onEnd();
            }
        };

        fsm.registerFirstState(new AskingForMapBehaviour(this), "STATE_1");
        fsm.registerState(new SendingMapBehaviour(this), "STATE_2");
        fsm.registerState(new WaitingForNewDiscoveriesBehaviour(this), "STATE_3");
        fsm.registerState(new SendingNewDiscoveriesBehaviour(this), "STATE_4");
        fsm.registerState(new WaitingForNewPositionsBehaviour(this), "STATE_5");
        fsm.registerState(new WaitingForNewCollectedGarbageBehaviour(this), "STATE_6");
        fsm.registerState(new SendingNewChangesOnMapBehaviour(this), "STATE_7");
        fsm.registerState(new AskingForNewSimulationStepBehaviour(this), "STATE_8");
        fsm.registerState(new AskingForMapBehaviour(this), "STATE_9");

        fsm.registerDefaultTransition("STATE_1", "STATE_2");
        fsm.registerDefaultTransition("STATE_2", "STATE_3");
        fsm.registerDefaultTransition("STATE_3", "STATE_4");
        fsm.registerDefaultTransition("STATE_4", "STATE_5");
        fsm.registerDefaultTransition("STATE_5", "STATE_6");
        fsm.registerDefaultTransition("STATE_6", "STATE_7");
        fsm.registerDefaultTransition("STATE_7", "STATE_8");
        fsm.registerDefaultTransition("STATE_8", "STATE_9", new String[]{"STATE_9"});
        fsm.registerDefaultTransition("STATE_9", "STATE_2");

        this.addBehaviour(fsm);
    }

}
