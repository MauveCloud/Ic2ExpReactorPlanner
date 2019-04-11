/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner.components;

import Ic2ExpReactorPlanner.MaterialsList;
import java.awt.Image;

/**
 * Represents a condensator in a reactor, either RSH or LZH.
 * @author Brian McCloud
 */
public class Condensator extends ReactorItem {
    
    public Condensator(final int id, final String baseName, final String name, final Image image, final double maxDamage, final double maxHeat, final String sourceMod) {
        super(id, baseName, name, image, maxDamage, maxHeat, sourceMod);
    }
    
    public Condensator(final Condensator other) {
        super(other);
    }
    
    @Override
    public double adjustCurrentHeat(final double heat) {
        if (heat < 0.0) {
            return heat;
        }
        currentCondensatorCooling += heat;
        bestCondensatorCooling = Math.max(currentCondensatorCooling, bestCondensatorCooling);
        double acceptedHeat = Math.min(heat, getMaxHeat() - heat);
        double result = heat - acceptedHeat;
        currentHeat += acceptedHeat;
        maxReachedHeat = Math.max(maxReachedHeat, currentHeat);
        return result;
    }

    @Override
    public boolean needsCoolantInjected() {
        return currentHeat > 0.85 * getMaxHeat();
    }
    
    @Override
    public void injectCoolant() {
        currentHeat = 0;
    }

}
