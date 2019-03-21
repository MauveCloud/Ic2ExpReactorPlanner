/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner.components;

import Ic2ExpReactorPlanner.MaterialsList;

/**
 * Represents a coolant cell in a reactor.
 * @author Brian McCloud
 */
public class CoolantCell extends ReactorItem {
    
    public CoolantCell(final int id, final String baseName, final String name, final String imageFilename, final double maxDamage, final double maxHeat, final String sourceMod, final MaterialsList materials) {
        super(id, baseName, name, imageFilename, maxDamage, maxHeat, sourceMod, materials);
    }
    
    public CoolantCell(final CoolantCell other) {
        super(other);
    }
    
    @Override
    public double adjustCurrentHeat(final double heat) {
        currentCellCooling += heat;
        bestCellCooling = Math.max(currentCellCooling, bestCellCooling);
        return super.adjustCurrentHeat(heat);
    }
    
}
