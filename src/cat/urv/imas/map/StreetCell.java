/**
 * IMAS base code for the practical work.
 * Copyright (C) 2014 DEIM - URV
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package cat.urv.imas.map;

import cat.urv.imas.gui.CellVisualizer;
import cat.urv.imas.onthology.InfoAgent;
import jade.core.*;

/**
 * This class keeps information about a street cell in the map.
 */
public class StreetCell extends Cell {

    /**
     * Information about the agent the cell contains.
     */
    private InfoAgent agent = null;

    /**
     * Builds a cell with a given type.
     *
     * @param row row number.
     * @param col column number.
     */
    public StreetCell(int row, int col) {
        super(CellType.STREET, row, col);
    }

    /* ********************************************************************** */
    /**
     * Checks whether this cell contains an agent.
     *
     * @return
     */
    public boolean isThereAnAgent() {
        return (agent != null);
    }

    /**
     * Adds an agent to this cell.
     *
     * @param newAgent agent
     * @throws Exception
     */
    public void addAgent(InfoAgent newAgent) throws Exception {
        //System.out.println("Add an agent to " + this + "<--" + newAgent);
        if (this.isThereAnAgent()) {
            throw new Exception("Full STREET cell");
        }
        if (newAgent == null) {
            throw new Exception("No valid agent to be set (null)");
        }
        // if everything is OK, we add the new agent to the cell
        this.agent = newAgent;
    }

    public void removeAgent(InfoAgent oldInfoAgent) throws Exception {
        if (!this.isThereAnAgent()) {
            throw new Exception("There is no agent in cell");
        }
        if (oldInfoAgent == null) {
            throw new Exception("No valid agent to be remove (null).");
        }

        this.agent = null;

    }

    public void removeAgentWithAID(AID currentAgent) {
        try {
            if (this.agent != null) {
                if (currentAgent.equals(this.agent.getAID())) {
                    this.agent = null;
                }
            }
        } catch (Exception e) {
            System.err.println(e);
        }
    }

    /**
     * Get the current agent from this cell.
     *
     * @return the current agent from this cell.
     */
    public InfoAgent getAgent() {
        return this.agent;
    }

    /* ********************************************************************** */
    /**
     * Gets the string specialization for a street cell.
     *
     * @return string specialization for a street cell.
     */
    @Override
    public String toStringSpecialization() {
        if (this.isThereAnAgent()) {
            return "(agent " + agent.toString() + ")";
        } else {
            return "";
        }
    }

    /* ***************** Map visualization API ********************************/
    @Override
    public void draw(CellVisualizer visual) {
        if (agent == null) {
            visual.drawEmptyStreet(this);
        } else {
            switch (agent.getType()) {
                case SCOUT:
                    visual.drawScout(this);
                    break;
                case HARVESTER:
                    visual.drawHarvester(this);
                    break;
                default:
                // Do nothing. In fact, we'll never get here.
            }
        }
    }

    @Override
    public String getMapMessage() {
        if (agent == null) {
            return "";
        } else {
            switch (agent.getType()) {
                case HARVESTER:
                    //TODO: You here should append the recycling info.
                    return agent.getMapMessage();
                default:
                    return agent.getMapMessage();
            }
        }
    }

}
