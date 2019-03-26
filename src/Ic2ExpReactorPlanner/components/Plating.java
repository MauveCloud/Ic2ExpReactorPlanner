/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner.components;

import Ic2ExpReactorPlanner.MaterialsList;
import Ic2ExpReactorPlanner.Reactor;
import java.awt.Image;

/**
 * Represents some form of plating, which changes how much heat the reactor can
 * hold before causing external effects (up to and including explosion), as well
 * as somewhat reducing explosion power.
 * @author Brian McCloud
 */
public class Plating extends ReactorItem {
    
    private final int heatAdjustment;
    
    public Plating(final int id, final String baseName, final String name, final Image image, final double maxDamage, final double maxHeat, final String sourceMod, final MaterialsList materials, final int heatAdjustment, final double explosionPowerMultiplier) {
        super(id, baseName, name, image, maxDamage, maxHeat, sourceMod, materials);
        this.heatAdjustment = heatAdjustment;
        this.explosionPowerMultiplier = explosionPowerMultiplier;
    }
    
    public Plating(Plating other) {
        super(other);
        this.heatAdjustment = other.heatAdjustment;
        this.explosionPowerMultiplier = other.explosionPowerMultiplier;
    }
    
    @Override
    public void addToReactor(final Reactor parent, final int row, final int col) {
        super.addToReactor(parent, row, col);
        if (parent != null) {
            parent.adjustMaxHeat(heatAdjustment);
        }
    }

    @Override
    public void removeFromReactor() {
        if (parent != null) {
            parent.adjustMaxHeat(-heatAdjustment);
        }
        super.removeFromReactor();
    }
    
}
