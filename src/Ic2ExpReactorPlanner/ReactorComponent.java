/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import java.awt.Image;

/**
 * Represents a component in an IndustrialCraft2 Experimental Nuclear Reactor.
 * @author Brian McCloud
 */
public class ReactorComponent {
    
    private Image image = null;
    
    private int row = -10;
    private int column = -10;
    
    private double currentHeat = 0.0;
    private double maxHeat = 1.0;
    
    private double currentDamage = 0.0;
    private double maxDamage = 1.0;
    
    private Reactor parent = null;
    
    /**
     * Get the image to show in the planner for this component.
     * @return the image.
     */
    public Image getImage() {
        return image;
    }
    
    /**
     * Set the image to show in the planner for this component.
     * @param image the image to set.
     */
    protected final void setImage(Image image) {
        this.image = image;
    }

    /**
     * @return the row
     */
    public final int getRow() {
        return row;
    }

    /**
     * @param row the row to set
     */
    public final void setRow(int row) {
        this.row = row;
    }

    /**
     * @return the column
     */
    public final int getColumn() {
        return column;
    }

    /**
     * @param column the column to set
     */
    public final void setColumn(int column) {
        this.column = column;
    }
    
    public boolean isHeatAcceptor() {
        return false;
    }
    
    public boolean isNeutronReflector() {
        return false;
    }
    
    /**
     * Generate heat if appropriate for component type, and spread to reactor or adjacent cells.
     */
    public void generateHeat() {
        // do nothing by default.
    }
    
    /**
     * Generate energy if appropriate for component type.
     */
    public void generateEnergy() {
        // do nothing by default.
    }
    
    /**
     * Dissipate heat if appropriate for component type.
     */
    public void dissipate() {
        // do nothing by default.
    }
    
    /**
     * Transfer heat between component, neighbors, and/or reactor, if appropriate for component type.
     */
    public void transfer() {
        // do nothing by default.
    }

    /**
     * Apply changes to the reactor when adding this component if appropriate, such as for reactor plating.
     */
    public void addToReactor() {
        // do nothing by default.
    }
    
    /**
     * Apply changes to the reactor when removing this component if appropriate, such as for reactor plating.
     */
    public void removeFromReactor() {
        
    }
    
    /**
     * @return the current heat level of the component.
     */
    public final double getCurrentHeat() {
        return currentHeat;
    }

    /**
     * Resets heat to 0 (used when resetting simulation).
     */
    public final void clearCurrentHeat() {
        currentHeat = 0.0;
    }
    
    /**
     * Adjusts the component heat up or down
     * @param heat the amount of heat to adjust by (positive to add heat, negative to remove heat).
     */
    public final void adjustCurrentHeat(final double heat) {
        currentHeat += heat;
        if (currentHeat < 0.0) {
            currentHeat = 0.0;
        }
    }
    
    /**
     * @return the maximum heat the component can take.
     */
    public final double getMaxHeat() {
        return maxHeat;
    }

    /**
     * @param maxHeat the maximum heat the component can take.
     */
    public final void setMaxHeat(final double maxHeat) {
        this.maxHeat = maxHeat;
    }

    /**
     * @return the damage the component has taken.
     */
    public final double getCurrentDamage() {
        return currentDamage;
    }

    /**
     * Clears the damage back to 0 (used when resetting simulation).
     */
    public final void clearDamage() {
        currentDamage = 0.0;
    }
    
    /**
     * Applies damage to the component, as opposed to heat.  Mainly used for 
     * fuel rods and neutron reflectors that lose durability as the reactor runs,
     * but can't recover it via cooling.
     * @param damage the damage to apply (only used if positive).
     */
    public final void applyDamage(final double damage) {
        if (damage > 0.0) {
            currentDamage += damage;
        }
    }
    
    /**
     * @return the the maximum damage the component can take.
     */
    public final double getMaxDamage() {
        return maxDamage;
    }

    /**
     * @param maxDamage the maximum damage the component can take.
     */
    public final void setMaxDamage(double maxDamage) {
        this.maxDamage = maxDamage;
    }

    /**
     * Gets the parent reactor.
     * @return the reactor this component is in.
     */
    protected Reactor getParent() {
        return parent;
    }

    /**
     * Sets the parent reactor.
     * @param parent the parent reactor to set
     */
    public void setParent(Reactor parent) {
        this.parent = parent;
    }
    
    /**
     * Determines if this component is broken in the current tick of the simulation
     * @return true if the component has broken either from damage (e.g. neutron reflectors, fuel rods) or from heat (e.g. heat vents, coolant cells), false otherwise.
     */
    public boolean isBroken() {
        return currentHeat > maxHeat || currentDamage > maxDamage;
    }
    
    /**
     * Gets the materials needed for this component.
     * @return the materials needed for this component.
     */
    public MaterialsList getMaterials() {
        return null;
    }
    
}
