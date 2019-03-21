/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner.components;

import Ic2ExpReactorPlanner.MaterialsList;

/**
 * Represents a neutron reflector in a reactor.
 * @author Brian McCloud
 */
public class Reflector extends ReactorItem {
    
    public Reflector(final int id, final String baseName, final String name, final String imageFilename, final double maxDamage, final double maxHeat, final String sourceMod, final MaterialsList materials) {
        super(id, baseName, name, imageFilename, maxDamage, maxHeat, sourceMod, materials);
    }
    
    public Reflector(final Reflector other) {
        super(other);
    }
    
    @Override
    public boolean isNeutronReflector() {
        return !isBroken();
    }

    @Override
    public double generateHeat() {
        ReactorItem component = parent.getComponentAt(row + 1, col);
        applyDamage(component.getRodCount());
        component = parent.getComponentAt(row, col + 1);
        applyDamage(component.getRodCount());
        component = parent.getComponentAt(row + 1, col);
        applyDamage(component.getRodCount());
        component = parent.getComponentAt(row, col - 1);
        applyDamage(component.getRodCount());
        return 0;
    }
}
