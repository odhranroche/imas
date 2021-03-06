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
package cat.urv.imas.behaviour.system;

import cat.urv.imas.agent.AgentType;
import jade.lang.acl.ACLMessage;
import jade.core.behaviours.*;
import cat.urv.imas.agent.SystemAgent;
import cat.urv.imas.map.BuildingCell;
import cat.urv.imas.map.Cell;
import cat.urv.imas.map.StreetCell;
import cat.urv.imas.onthology.GameSettings;
import cat.urv.imas.onthology.MessageContent;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import jade.core.Agent;
import cat.urv.imas.onthology.InfoAgent;
import java.util.ArrayList;
import cat.urv.imas.map.SettableBuildingCell;
import cat.urv.imas.onthology.GarbageType;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RequestForNewSimulationStepBehaviour extends SimpleBehaviour {

    boolean hasReply;

    public RequestForNewSimulationStepBehaviour(Agent agent) //It cannot be SystemAgent type
    {
        super(agent);
        hasReply = false;
    }

    @Override
    public void action() {
        while (done() == false) {
            ACLMessage response = myAgent.receive();

            if (response != null) {
                hasReply = true;
                SystemAgent agent = (SystemAgent) this.getAgent();
                ACLMessage reply = response.createReply();
                try {
                    // Sending an Agree..
                    agent.setNewInfoAgent((ArrayList<InfoAgent>) response.getContentObject());
                    agent.log("Request received");
                    reply.setPerformative(ACLMessage.AGREE);
                    agent.send(reply);

                    // Sending an Inform..
                    ACLMessage reply2 = response.createReply();
                    reply2.setPerformative(ACLMessage.INFORM);

                    try {
                        makeChanges();
                        agent.log("Changes done");
                        spawnGarbage();
                        agent.log("Garbage spread");
                        moveAgents();
                        agent.log("Map updated");
                        reply2.setContent(MessageContent.NEXT_STEP);
                    } catch (Exception e) {
                        reply2.setPerformative(ACLMessage.FAILURE);
                        agent.errorLog(e.toString());
                        e.printStackTrace();
                    }
                    agent.send(reply2);
                } catch (Exception e) {
                    reply.setPerformative(ACLMessage.FAILURE);
                    agent.errorLog(e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public void makeChanges() {
        try {
            SystemAgent agent = (SystemAgent) this.getAgent();
            Cell[][] mapa;
            GameSettings game;
            game = agent.getGame();
            mapa = game.getMap();
            ArrayList<Cell> newCollectedG, newFoundG;
            boolean condition;

            newCollectedG = agent.newChangesOnMap.getCollectedGarbage();
            newFoundG = agent.newChangesOnMap.getFoundGarbage();

            // We delete set the garbage as found.
            condition = newFoundG.isEmpty();
            if (!condition) {
                for (int i = 0; i < newFoundG.size(); i++) {
                    Cell c = newFoundG.get(i);
                    if (mapa[c.getRow()][c.getCol()] instanceof SettableBuildingCell) {
                        ((BuildingCell) mapa[c.getRow()][c.getCol()]).setFound(true);
                    }
                }
            }

            // We delete units of garbage if is being collected.
            condition = newCollectedG.isEmpty();
            if (!condition) {

                for (int i = 0; i < newCollectedG.size(); i++) {
                    Cell c = newCollectedG.get(i);
                    if (mapa[c.getRow()][c.getCol()] instanceof SettableBuildingCell) {
                        ((BuildingCell) mapa[c.getRow()][c.getCol()]).removeGarbage();
                    }
                }
            }
        } catch (Exception e) {

        }
    }

    public void moveAgents() {
        InfoAgent info;
        int i, j, g, ri, rj, imax, jmax;

        Cell[][] mapa;
        SystemAgent agent = (SystemAgent) this.getAgent();

        mapa = agent.getGame().getMap();
        imax = mapa.length;
        jmax = mapa[0].length;
        Map listOfAgents = agent.getGame().getAgentList();
        HashMap newListOfAgents; // We need to create another list of agents and update it.
        newListOfAgents = new HashMap<AgentType, List<Cell>>();
        List<Cell> newCellsSC; // The list of agents is a map of AgentType and list of cells.
        List<Cell> newCellsH; // The list of agents is a map of AgentType and list of cells.

        newCellsSC = new ArrayList<Cell>(); // For every agentType we change the list of cells.
        newCellsH = new ArrayList<Cell>(); // For every agentType we change the list of cells.
        int cont = 1;

        for (InfoAgent ag : agent.newInfoAgent) {
            i = ag.getPreRow();
            j = ag.getPreColumn();
            if (mapa[i][j] instanceof StreetCell) {
                ri = ag.getRow();
                rj = ag.getColumn();
                info = ((StreetCell) mapa[i][j]).getAgent();

                // If there is an Agent in the future cell we do not move the agent.
                if (((StreetCell) mapa[ri][rj]).isThereAnAgent()) {
                    // Modifying the new list of agents but with the same position.
                    if (ag.getType().equals(AgentType.SCOUT)) {
                        newCellsSC.add(mapa[i][j]);
                    } else if (ag.getType().equals(AgentType.HARVESTER)) {
                        newCellsH.add(mapa[i][j]);
                    }
                } else {
                    ((StreetCell) mapa[i][j]).removeAgentWithAID(ag.getAID());
                    try {
                        ((StreetCell) mapa[ri][rj]).addAgent(ag);
                    } catch (Exception e) {
                    }

                    if (ag.getType().equals(AgentType.SCOUT)) {
                        newCellsSC.add(mapa[ri][rj]);
                    } else if (ag.getType().equals(AgentType.HARVESTER)) {
                        newCellsH.add(mapa[ri][rj]);
                    }
                }

            }
        }

        newListOfAgents.put(AgentType.SCOUT, newCellsSC);
        newListOfAgents.put(AgentType.HARVESTER, newCellsH);
        agent.getGame().setAgentList(newListOfAgents);
    }

    public void spawnGarbage() {
        SystemAgent agent = (SystemAgent) this.getAgent();
        Cell[][] mapa;
        GameSettings game;
        game = agent.getGame();
        mapa = game.getMap();
        int ri, rj, imax, jmax, movement;
        Random randomCell = new Random();
        imax = mapa.length;
        jmax = mapa[0].length;
        boolean found = false;

        int prob = game.getNewGarbageProbability();
        int max = game.getMaxAmountOfNewGargabe();
        int limit = game.getMaxNumberBuildingWithNewGargabe();

        int rand = ThreadLocalRandom.current().nextInt(0, 101);
        // Not always we spawn garbage, it can be that we do not do it.
        if (rand > prob) {
            return;
        }

        String[] types = {"G", "P", "L"};
        int type;

        while (!found) {
            ri = Math.abs(randomCell.nextInt(imax)) + 0;
            rj = Math.abs(randomCell.nextInt(jmax)) + 0;

            if (mapa[ri][rj] instanceof SettableBuildingCell) {
                BuildingCell cell = (BuildingCell) mapa[ri][rj];
                Map<GarbageType, Integer> garbage;
                garbage = cell.detectGarbage();

                if (mapa[ri][rj] instanceof SettableBuildingCell && limit > 0) {
                    System.out.println("+++++++++++++++++++Spreading garbage...");
                    if (garbage.isEmpty()) {
                        System.out.println("+++++++++++++++++++(empty building)...");
                        rand = ThreadLocalRandom.current().nextInt(1, max + 1);
                        type = ThreadLocalRandom.current().nextInt(0, 3);
                        SettableBuildingCell temp = (SettableBuildingCell) mapa[ri][rj];
                        temp.setGarbage(GarbageType.fromShortString(types[type]), rand);
                        limit -= 1;
                    } else {
                        System.out.println("+++++++++++++++++++(NOT empty building)... " + garbage.keySet().iterator().next().getShortString() + ":" + garbage.values().iterator().next());
                    }
                }

                if (!(limit > 0)) {
                    found = true;
                }

            }
        }
        found = false;
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
