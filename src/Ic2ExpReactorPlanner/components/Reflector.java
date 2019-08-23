/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner.components;

import Ic2ExpReactorPlanner.MaterialsList;
import java.awt.Image;

/**
 * Represents a neutron reflector in a reactor.
 * @author Brian McCloud
 */
public class Reflector extends ReactorItem {
    
    private static String mcVersion = "1.12.2";
    
    public Reflector(final int id, final String baseName, final String name, final Image image, final double maxDamage, final double maxHeat, final String sourceMod) {
        super(id, baseName, name, image, maxDamage, maxHeat, sourceMod);
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
        ReactorItem component = parent.getComponentAt(row - 1, col);
        if (component != null) {
            applyDamage(component.getRodCount());
        }
        component = parent.getComponentAt(row, col + 1);
        if (component != null) {
            applyDamage(component.getRodCount());
        }
        component = parent.getComponentAt(row + 1, col);
        if (component != null) {
            applyDamage(component.getRodCount());
        }
        component = parent.getComponentAt(row, col - 1);
        if (component != null) {
            applyDamage(component.getRodCount());
        }
        return 0;
    }
    
    @Override
    public double getMaxDamage() {
        if (maxDamage > 1 && "1.7.10".equals(mcVersion)) {
            return maxDamage / 3;
        }
        return maxDamage;
    }
    
    public static void setMcVersion(String newVersion) {
        mcVersion = newVersion;
    }
}
