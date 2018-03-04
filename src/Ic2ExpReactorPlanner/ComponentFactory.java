/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory class to handle creating components by id or name.
 * @author Brian McCloud
 */
public class ComponentFactory {
    
    private ComponentFactory() {
        // do nothing, this class should not be instantiated.
    }
    
    private static final Object[][] components = {
        {"empty", null}, //NOI18N
        {"fuelRodUranium", new FuelRodUranium()}, //NOI18N
        {"dualFuelRodUranium", new DualFuelRodUranium()}, //NOI18N
        {"quadFuelRodUranium", new QuadFuelRodUranium()}, //NOI18N
        {"fuelRodMox", new FuelRodMox()}, //NOI18N
        {"dualFuelRodMox", new DualFuelRodMox()}, //NOI18N
        {"quadFuelRodMox", new QuadFuelRodMox()}, //NOI18N
        {"neutronReflector", new NeutronReflector()}, //NOI18N
        {"thickNeutronReflector", new ThickNeutronReflector()}, //NOI18N
        {"heatVent", new HeatVent()}, //NOI18N
        {"advancedHeatVent", new AdvancedHeatVent()}, //NOI18N
        {"reactorHeatVent", new ReactorHeatVent()}, //NOI18N
        {"componentHeatVent", new ComponentHeatVent()}, //NOI18N
        {"overclockedHeatVent", new OverclockedHeatVent()}, //NOI18N
        {"coolantCell10k", new CoolantCell10k()}, //NOI18N
        {"coolantCell30k", new CoolantCell30k()}, //NOI18N
        {"coolantCell60k", new CoolantCell60k()}, //NOI18N
        {"heatExchanger", new HeatExchanger()}, //NOI18N
        {"advancedHeatExchanger", new AdvancedHeatExchanger()}, //NOI18N
        {"coreHeatExchanger", new ReactorHeatExchanger()}, //NOI18N
        {"componentHeatExchanger", new ComponentHeatExchanger()}, //NOI18N
        {"reactorPlating", new ReactorPlating()}, //NOI18N
        {"heatCapacityReactorPlating", new HeatCapacityReactorPlating()}, //NOI18N
        {"containmentReactorPlating", new ContainmentReactorPlating()}, //NOI18N
        {"rshCondensator", new RshCondensator()}, //NOI18N
        {"lzhCondensator", new LzhCondensator()}, //NOI18N
        {"fuelRodThorium", new FuelRodThorium()}, //NOI18N
        {"dualFuelRodThorium", new DualFuelRodThorium()}, //NOI18N
        {"quadFuelRodThorium", new QuadFuelRodThorium()}, //NOI18N
        {"coolantCellHelium60k", new CoolantCell60kHelium()}, //NOI18N
        {"coolantCellHelium180k", new CoolantCell180kHelium()}, //NOI18N
        {"coolantCellHelium360k", new CoolantCell360kHelium()}, //NOI18N
        {"coolantCellNak60k", new CoolantCell60kNak()}, //NOI18N
        {"coolantCellNak180k", new CoolantCell180kNak()}, //NOI18N
        {"coolantCellNak360k", new CoolantCell360kNak()}, //NOI18N
        {"iridiumNeutronReflector", new IridiumNeutronReflector()}, //NOI18N
        {"fuelRodNaquadah", new FuelRodNaquadah()}, //NOI18N
        {"dualFuelRodNaquadah", new DualFuelRodNaquadah()}, //NOI18N
        {"quadFuelRodNaquadah", new QuadFuelRodNaquadah()}, //NOI18N
    };
    
    /**
     * Gets a default instances of the specified component (such as for drawing button images)
     * @param id the id of the component.
     * @return the component with the specified id, or null if the id is out of range.
     */
    public static ReactorComponent getDefaultComponent(int id) {
        if (id >= 0 && id < components.length && components[id][1] instanceof ReactorComponent) {
            return (ReactorComponent)components[id][1];
        }
        return null;
    }
    
    /**
     * Gets a default instances of the specified component (such as for drawing button images)
     * @param name the name of the component.
     * @return the component with the specified name, or null if the name is not found.
     */
    public static ReactorComponent getDefaultComponent(String name) {
        for (int i = 0; i < components.length; i++) {
            if (components[i][0].equals(name) && components[i][1] instanceof ReactorComponent) {
                return (ReactorComponent)components[i][1];
            }
        }
        return null;
    }
    
    /**
     * Creates a new instance of the specified component.
     * @param id the id of the component to create.
     * @return a new instance of the specified component, or null if the id is out of range.
     */
    public static ReactorComponent createComponent(int id) {
        if (id >= 0 && id < components.length && components[id][1] instanceof ReactorComponent) {
            Class<? extends ReactorComponent> componentClass = ((ReactorComponent)components[id][1]).getClass();
            try {
                return componentClass.newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(ComponentFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }
    
    /**
     * Creates a new instance of the specified component.
     * @param name the name of the component to create.
     * @return a new instance of the specified component, or null if the name is not found.
     */
    public static ReactorComponent createComponent(String name) {
        for (int i = 0; i < components.length; i++) {
            if (components[i][0].equals(name) && components[i][1] instanceof ReactorComponent) {
                Class<? extends ReactorComponent> componentClass = ((ReactorComponent) components[i][1]).getClass();
                try {
                    return componentClass.newInstance();
                } catch (InstantiationException | IllegalAccessException ex) {
                    Logger.getLogger(ComponentFactory.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }
    
    /**
     * Gets the id of the component.
     * @param component the component to identify.
     * @return the id of the passed component, 0 if the component is null, or -1 if unrecognized.
     */
    public static int getID(ReactorComponent component) {
        if (component != null) {
            for (int i = 1; i < components.length; i++) {
                if (components[i][1].getClass().equals(component.getClass())) {
                    return i;
                }
            }
            return -1;
        }
        return 0;
    }
    
    /**
     * Gets the name of the component.
     * @param component the component to identify.
     * @return the name of the passed component, "empty" for a null component, or null for an unrecognized component.
     */
    public static String getName(ReactorComponent component) {
        if (component != null) {
            for (int i = 1; i < components.length; i++) {
                if (components[i][1].getClass().equals(component.getClass())) {
                    return components[i][0].toString();
                }
            }
            return null;
        }
        return "empty"; //NO18N
    }
    
    public static String getDisplayName(ReactorComponent component) {
        if (component != null) {
            final ReactorComponent tempComponent = getDefaultComponent(getID(component));
            if (tempComponent != null) {
                return tempComponent.toString();
            }
        }
        return null;
    }
    
    /**
     * Get the number of defined components.
     * @return the number of defined components.
     */
    public static int getComponentCount() {
        return components.length;
    }
    
    /**
     * Re-creates the default instances so they'll re-load images, in case a 
     * resource pack was chosen.
     */
    public static void refresh() {
        for (int i = 0; i < components.length; i++) {
            try {
                components[i][1] = components[i][1].getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException ex) {
                Logger.getLogger(ComponentFactory.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
