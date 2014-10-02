/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

/**
 * Represents an IndustrialCraft2 Nuclear Reactor.
 * @author Brian McCloud
 */
public class Reactor {
    
    private final ReactorComponent[][] grid = new ReactorComponent[6][9];
    
    private double currentEUoutput = 0.0;
    
    private double currentHeat = 0.0;
    
    private double maxHeat = 10000.0;
    
    private double ventedHeat = 0.0;
    
    public static final MaterialsList REACTOR_CHAMBER = new MaterialsList(8, "Iron Plate", 4, "Lead Plate");
    public static final MaterialsList REACTOR = new MaterialsList(3, REACTOR_CHAMBER, 36, "Lead Plate", MaterialsList.ADVANCED_CIRCUIT, MaterialsList.GENERATOR);
    
    public ReactorComponent getComponentAt(int row, int column) {
        if (row >= 0 && row < grid.length && column >= 0 && column < grid[row].length) {
            return grid[row][column];
        }
        return null;
    }
    
    public void setComponentAt(int row, int column, ReactorComponent component) {
        if (row >= 0 && row < grid.length && column >= 0 && column < grid[row].length) {
            if (grid[row][column] != null) {
                grid[row][column].removeFromReactor();
            }
            grid[row][column] = component;
            if (component != null) {
                component.setRow(row);
                component.setColumn(column);
                component.setParent(this);
                component.addToReactor();
            }
        }
    }

    public void clearGrid() {
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                grid[row][col] = null;
            }
        }
    }
    
    /**
     * @return the amount of EU output in the reactor tick just simulated.
     */
    public double getCurrentEUoutput() {
        return currentEUoutput;
    }

    /**
     * @return the current heat level of the reactor.
     */
    public double getCurrentHeat() {
        return currentHeat;
    }

    /**
     * @return the maximum heat of the reactor.
     */
    public double getMaxHeat() {
        return maxHeat;
    }
    
    /**
     * Adjust the maximum heat
     * @param adjustment the adjustment amount (negative values decrease the max heat).
     */
    public void adjustMaxHeat(double adjustment) {
        maxHeat += adjustment;
    }

    /**
     * Set the current heat of the reactor.  Mainly to be used for simulating a pre-heated reactor, or for resetting to 0 for a new simulation.
     * @param currentHeat the heat to set
     */
    public void setCurrentHeat(double currentHeat) {
        this.currentHeat = currentHeat;
    }
    
    /**
     * Adjusts the reactor's current heat by a specified amount
     * @param adjustment the adjustment amount.
     */
    public void adjustCurrentHeat(double adjustment) {
        currentHeat += adjustment;
    }
    
    /**
     * add some EU output.
     * @param amount the amount of EU to output over 1 reactor tick (20 game ticks).
     */
    public void addEUOutput(double amount) {
        currentEUoutput += amount;
    }
    
    /**
     * clears the EU output (presumably to start simulating a new reactor tick).
     */
    public void clearEUOutput() {
        currentEUoutput = 0.0;
    }
    
    /**
     * Gets a list of the materials needed to build the reactor and components.
     * @return a list of the materials needed to build the reactor and components.
     */
    public MaterialsList getMaterials() {
        MaterialsList result = new MaterialsList(REACTOR);
        int lastColumnFilled = 0;
        for (int col = 0; col < grid[0].length; col++) {
            for (int row = 0; row < grid.length; row++) {
                if (grid[row][col] != null) {
                    lastColumnFilled = Math.max(lastColumnFilled, col);
                    result.add(grid[row][col].getMaterials());
                }
            }
        }
        int chambersNeeded = Math.max(0, lastColumnFilled - 2);
        result.add(chambersNeeded, REACTOR_CHAMBER);
        return result;
    }

    /**
     * @return the amount of heat vented this reactor tick.
     */
    public double getVentedHeat() {
        return ventedHeat;
    }
    
    /**
     * Adds to the amount of heat vented this reactor tick, in case it is a new-style reactor with a pressure vessel and outputting heat to fluid instead of EU.
     * @param amount the amount to add.
     */
    public void ventHeat(double amount) {
        ventedHeat += amount;
    }
    
    /**
     * Clears the amount of vented heat, in case a new reactor tick is starting.
     */
    public void clearVentedHeat() {
        ventedHeat = 0;
    }
    
    /**
     * Get a code that represents the component set, which can be passed between forum users, etc.
     * @return a code representing some ids for the components and arrangement.  Passing the same code to setCode() should re-create an identical reactor setup, even if other changes have happened in the meantime.
     */
    public String getCode() {
        StringBuilder result = new StringBuilder(108);
        for (int row = 0; row < grid.length; row++) {
            for (int col = 0; col < grid[row].length; col++) {
                final ReactorComponent component = grid[row][col];
                final int id = ComponentFactory.getID(component);
                result.append(String.format("%02X", id));
            }
        }
        return result.toString();
    }
    
    /**
     * Sets a code to configure the entire grid all at once.  Expects the code to have originally been output by getCode().
     * @param code the code of the reactor setup to use.
     */
    public void setCode(String code) {
        int pos = 0;
        if (code.length() == 108 && code.matches("[0-9A-Fa-f]+")) {
            for (int row = 0; row < grid.length; row++) {
                for (int col = 0; col < grid[row].length; col++) {
                    int id = Integer.parseInt(code.substring(pos, pos + 2), 16);
                    setComponentAt(row, col, ComponentFactory.createComponent(id));
                    pos += 2;
                }
            }
        }
    }
    
}
