/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner.components;

import Ic2ExpReactorPlanner.MaterialsList;
import Ic2ExpReactorPlanner.Reactor;
import Ic2ExpReactorPlanner.TextureFactory;
import java.awt.Image;
import java.util.ResourceBundle;

/**
 * Represents an item (component) in an IndustrialCraft2 Experimental Nuclear Reactor.
 * @author Brian McCloud
 */
public class ReactorItem {
    // Fundamental values, set at object instantiation, should never need to be changed.
    public final int id;
    public final String baseName; // this is the non-localized version, for internal program use
    public final String name; // this is expected to be localized, for display usage.
    public final Image image;
    public final double maxDamage;
    public final double maxHeat;
    public final String sourceMod; // for potentially adjusting controls based on whether the mod is in use, will be null to indicate the item is part of base IC2.
    public final MaterialsList materials;
    
    // Simulation setting values
    private double initialHeat = 0; public double getInitialHeat() { return initialHeat; } public void setInitialheat(final double value) { if (this.isHeatAcceptor() && initialHeat >= 0 && initialHeat < this.maxHeat) { initialHeat = value; } }
    private int automationThreshold = 9000; public int getAutomationThreshold() { return automationThreshold; } public void setAutomationThreshold(final int value) { automationThreshold = value; }
    private int reactorPause = 0; public int getReactorPause() { return reactorPause; } public void setReactorPause(final int value) { reactorPause = value; }
    
    // fields below here are not to be copied by the copy constructor.
    
    // Parent reactor and position
    protected Reactor parent = null;
    protected int row = -10;
    protected int col = -10;
    
    // Special variable for holding information about this item from last simulation.
    // Usage of StringBuffer instead of StringBuilder is deliberate - this may be accessed by 
    // both the simulation worker thread and the event dispatch thread.
    public final StringBuffer info = new StringBuffer(1000);
    
    // Calculated values - readable from outside, but only writable by subclasses.
    protected double currentDamage = 0; public double getCurrentDamage() { return currentDamage; }
    protected double currentHeat = 0; public double getCurrentHeat() { return currentHeat; }
    protected double maxReachedHeat = 0; public double getMaxReachedHeat() { return maxReachedHeat; }
    
    protected double currentEUGenerated = 0; public double getCurrentEUGenerated() { return currentEUGenerated; }
    protected double minEUGenerated = Double.MAX_VALUE; public double getMinEUGenerated() { return minEUGenerated; }
    protected double maxEUGenerated = 0; public double getMaxEUGenerated() { return maxEUGenerated; }
    
    protected double currentHeatGenerated = 0; public double getCurrentHeatGenerated() { return currentHeatGenerated; }
    protected double minHeatGenerated = Double.MAX_VALUE; public double getMinHeatGenerated() { return minHeatGenerated; }
    protected double maxHeatGenerated = 0; public double getMaxHeatGenerated() { return maxHeatGenerated; }
    
    protected double currentVentCooling = 0; public double getCurrentVentCooling() { return currentVentCooling; }
    protected double bestVentCooling = 0; public double getBestVentCooling() { return bestVentCooling; }
    
    protected double currentCellCooling = 0; public double getCurrentCellCooling() { return currentCellCooling; }
    protected double bestCellCooling = 0; public double getBestCellCooling() { return bestCellCooling; }
    
    protected double currentCondensatorCooling = 0; public double getCurrentCondensatorCooling() { return currentCondensatorCooling; }
    protected double bestCondensatorCooling = 0; public double getBestCondensatorCooling() { return bestCondensatorCooling; }
    
    protected double explosionPowerMultiplier = 1;
    
    protected static final ResourceBundle BUNDLE = ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle");
    
    protected ReactorItem(final int id, final String baseName, final String name, final String imageFilename, final double maxDamage, final double maxHeat, final String sourceMod, final MaterialsList materials) {
        this.id = id;
        this.baseName = baseName;
        this.name = name;
        this.image = TextureFactory.getImage(imageFilename);
        this.maxDamage = maxDamage;
        this.maxHeat = maxHeat;
        if (maxHeat > 1) {
            automationThreshold = (int)(maxHeat * 0.9);
        } else if (maxDamage > 1) {
            automationThreshold = (int)(maxDamage * 1.1);
        }
        this.sourceMod = sourceMod;
        this.materials = materials;
    }
    
    // Protected copy constructor for use by subclasses.  Generalized copying should be done with a method in ComponentFactory (which can check which subclass copy constructor to use).
    protected ReactorItem(final ReactorItem other) {
        this.id = other.id;
        this.baseName = other.baseName;
        this.name = other.name;
        this.image = other.image;
        this.maxDamage = other.maxDamage;
        this.maxHeat = other.maxHeat;
        this.initialHeat = other.initialHeat;
        this.automationThreshold = other.automationThreshold;
        this.reactorPause = other.reactorPause;
        this.sourceMod = other.sourceMod;
        this.materials = other.materials;
    }

    /**
     * Gets the name of the component, and the initial heat (if applicable).
     * @return the name of this component, and potentially initial heat.
     */
    @Override
    public String toString() {
        String result = name;
        if (initialHeat > 0) {
            result += String.format(BUNDLE.getString("UI.InitialHeatDisplay"), (int)initialHeat);
        }
        return result;
    }

    /**
     * Checks if this component can accept heat. (e.g. from adjacent fuel rods, or from an exchanger)
     * @return true if this component can accept heat, false otherwise.
     */
    public boolean isHeatAcceptor() {
        // maxHeat of 1 means this component never accepts heat (though it might take damage instead)
        return maxHeat > 1 && !isBroken();
    }
    
    /**
     * Determines if this component can be cooled down, such as by a component heat vent.
     * @return true if this component can be cooled down, false otherwise.
     */
    public boolean isCoolable() {
        return isHeatAcceptor() && !(this instanceof Condensator);
    }
    
    /**
     * Checks if this component acts as a neutron reflector, and boosts performance of adjacent fuel rods,
     * either by being a "neutron reflector" item or by being a fuel rod.
     * @return true if this component reflects neutrons, false otherwise.
     */
    public boolean isNeutronReflector() {
        return false;
    }
    
    /**
     * Prepare for a new reactor tick.
     */
    public void preReactorTick() {
        currentVentCooling = 0.0;
        currentCellCooling = 0.0;
        currentCondensatorCooling = 0.0;
    }
    
    /**
     * Generate heat if appropriate for component type, and spread to reactor or adjacent cells.
     * @return the amount of heat generated by this component.
     */
    public double generateHeat() {
        return 0.0;
    }
    
    /**
     * Generate energy if appropriate for component type.
     * @return the number of EU generated by this component during the current reactor tick.
     */
    public double generateEnergy() {
        return 0.0;
    }
    
    /**
     * Dissipate (aka vent) heat if appropriate for component type.
     * @return the amount of heat successfully vented during the current reactor tick.
     */
    public double dissipate() {
        return 0.0;
    }
    
    /**
     * Transfer heat between component, neighbors, and/or reactor, if appropriate for component type.
     */
    public void transfer() {
        // do nothing by default.
    }

    /**
     * Adds this component to a new reactor, and applies changes to the reactor when adding this component if appropriate, such as for reactor plating.
     * @param parent the reactor to add this component to.
     * @param row the row this component will be in.
     * @param col the column this component will be in.
     */
    public void addToReactor(final Reactor parent, final int row, final int col) {
        // call removeFromReactor first, in case it had previously been added to a different reactor (unlikely)
        removeFromReactor();
        this.parent = parent;
        this.row = row;
        this.col = col;
    }
    
    /**
     * Removes this component from its reactor (if any), and applies changes to the reactor when removing this component if appropriate, such as for reactor plating.
     */
    public void removeFromReactor() {
        parent = null;
        this.row = -10;
        this.col = -10;
    }
    
    /**
     * Resets heat to 0 (used when resetting simulation).
     */
    public final void clearCurrentHeat() {
        currentHeat = initialHeat;
        bestVentCooling = 0.0;
        bestCondensatorCooling = 0.0;
        bestCellCooling = 0.0;
        minEUGenerated = Double.MAX_VALUE;
        maxEUGenerated = 0.0;
        minHeatGenerated = Double.MAX_VALUE;
        maxHeatGenerated = 0.0;
        maxReachedHeat = initialHeat;
    }
    
    /**
     * Adjusts the component heat up or down
     * @param heat the amount of heat to adjust by (positive to add heat, negative to remove heat).
     * @return the amount of heat adjustment refused. (e.g. due to going below minimum heat, breaking due to excessive heat, or attempting to remove heat from a condensator)
     */
    public double adjustCurrentHeat(final double heat) {
        if (isHeatAcceptor()) {
            double result = 0.0;
            double tempHeat = getCurrentHeat();
            tempHeat += heat;
            if (tempHeat > maxHeat) {
                result = maxHeat - tempHeat + 1;
                tempHeat = maxHeat;
            } else if (tempHeat < 0.0) {
                result = tempHeat;
                tempHeat = 0.0;
            }
            currentHeat = tempHeat;
            maxReachedHeat = Math.max(maxReachedHeat, currentHeat);
            return result;
        }
        return heat;
    }
    
    /**
     * Clears the damage back to 0 (used when resetting simulation, or replacing the component in an automation simulation).
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
        // maxDamage of 1 is treated as meaning the component doesn't accept damage (though it might accept heat instead)
        // if someone actually writes a mod with such a flimsy component, I might have to rethink this.
        if (maxDamage > 1 && damage > 0.0) {
            currentDamage += damage;
        }
    }
    
    /**
     * Determines if this component is broken in the current tick of the simulation
     * @return true if the component has broken either from damage (e.g. neutron reflectors, fuel rods) or from heat (e.g. heat vents, coolant cells), false otherwise.
     */
    public boolean isBroken() {
        return currentHeat >= maxHeat || currentDamage > maxDamage;
    }
    
    /**
     * The number of fuel rods in this component (0 for non-fuel-rod components).
     * @return The number of fuel rods in this component, or 0 if this component has no fuel rods.
     */
    public int getRodCount() {
        return 0;
    }
    
    /**
     * Gets a value added in the formula for calculating explosion power.
     * @return the additive value for explosion power caused by this component, 
     * or 0 if this component doesn't affect the addition part of the explosion calculation.
     */
    public double getExplosionPowerOffset() {
        if (!isBroken()) {
            if (getRodCount() == 0 && isNeutronReflector()) {
                return -1;
            }
            return 2 * getRodCount(); // all known fuel rods (including those from GT) use this formula, and non-rod components return 0 for getRodCount
        }
        return 0;
    }
    
    /**
     * Gets a value multiplied in the formula for calculating explosion power.
     * @return the multiplier value for explosion power caused by this component,
     * or 1 if this component doesn't affect the multiplication part of the explosion calculation.
     */
    public double getExplosionPowerMultiplier() {
        return explosionPowerMultiplier;
    }

    /**
     * Finds the theoretical maximum venting of this component, regardless of
     * whether this venting is from itself, directly from the reactor, or from
     * adjacent components.
     * @return the capacity of this component to vent heat.
     */
    public double getVentCoolingCapacity() {
        return 0;
    }

    /**
     * Gets the current "output" of this component, presumably for writing to
     * CSV data.  What this "output" means may vary by component type or reactor type.
     * @return the output of this component for the current reactor tick.
     */
    public double getCurrentOutput() {
        return 0;
    }
    
    /**
     * Determines whether this component expects to produces some sort of output each reactor tick,
     * e.g. for purposes of tracking in a CSV file.
     * @return true if this component produces output (such as EU or vented heat), false otherwise.
     */
    public boolean producesOutput() {
        return getVentCoolingCapacity() > 0 || getRodCount() > 0;
    }
    
    /**
     * Determines if this component needs input from a Reactor Coolant Injector.
     * Simply returns false for non-condensator items.
     * @return true if this is a condensator that has absorbed enough heat to require the appropriate item added to repair it, false otherwise.
     */
    public boolean needsCoolantInjected() {
        return false;
    }
    
    /**
     * Simulates having a coolant item added by a Reactor Coolant Injector.
     */
    public void injectCoolant() {
        // do nothing by default.
    }
    
}
