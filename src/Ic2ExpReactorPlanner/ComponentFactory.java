/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Ic2ExpReactorPlanner;

import static Ic2ExpReactorPlanner.BundleHelper.getI18n;
import Ic2ExpReactorPlanner.components.Condensator;
import Ic2ExpReactorPlanner.components.CoolantCell;
import Ic2ExpReactorPlanner.components.Exchanger;
import Ic2ExpReactorPlanner.components.FuelRod;
import Ic2ExpReactorPlanner.components.Plating;
import Ic2ExpReactorPlanner.components.ReactorItem;
import Ic2ExpReactorPlanner.components.Reflector;
import Ic2ExpReactorPlanner.components.Vent;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Factory class to handle creating components by id or name.
 * @author Brian McCloud
 */
public class ComponentFactory {
    
    private ComponentFactory() {
        // do nothing, this class should not be instantiated.
    }
    
    private static final ReactorItem[] ITEMS = {
        null, // special entry to make sure id 0 corresponds to empty space (no component)
        new FuelRod(1, "fuelRodUranium", getI18n("ComponentName.FuelRodUranium"), TextureFactory.getImage("reactorUraniumSimple.png", "uranium.png"), 20e3, 1, null, 100, 2, 1, false),
        new FuelRod(2, "dualFuelRodUranium", getI18n("ComponentName.DualFuelRodUranium"), TextureFactory.getImage("reactorUraniumDual.png", "dual_uranium.png"), 20e3, 1, null, 200, 4, 2, false),
        new FuelRod(3, "quadFuelRodUranium", getI18n("ComponentName.QuadFuelRodUranium"), TextureFactory.getImage("reactorUraniumQuad.png", "quad_uranium.png"), 20e3, 1, null, 400, 8, 4, false),
        new FuelRod(4, "fuelRodMox", getI18n("ComponentName.FuelRodMox"), TextureFactory.getImage("reactorMOXSimple.png", "mox.png"), 10e3, 1, null, 100, 2, 1, true),
        new FuelRod(5, "dualFuelRodMox", getI18n("ComponentName.DualFuelRodMox"), TextureFactory.getImage("reactorMOXDual.png", "dual_mox.png"), 10e3, 1, null, 200, 4, 2, true),
        new FuelRod(6, "quadFuelRodMox", getI18n("ComponentName.QuadFuelRodMox"), TextureFactory.getImage("reactorMOXQuad.png", "quad_mox.png"), 10e3, 1, null, 400, 8, 4, true),
        new Reflector(7, "neutronReflector", getI18n("ComponentName.NeutronReflector"), TextureFactory.getImage("reactorReflector.png", "neutron_reflector.png"), 40e3, 1, null),
        new Reflector(8, "thickNeutronReflector", getI18n("ComponentName.ThickNeutronReflector"), TextureFactory.getImage("reactorReflectorThick.png", "thick_neutron_reflector.png"), 120e3, 1, null),
        new Vent(9, "heatVent", getI18n("ComponentName.HeatVent"), TextureFactory.getImage("reactorVent.png", "heat_vent.png"), 1, 1000, null, 6, 0, 0),
        new Vent(10, "advancedHeatVent", getI18n("ComponentName.AdvancedHeatVent"), TextureFactory.getImage("reactorVentDiamond.png", "advanced_heat_vent.png"), 1, 1000, null, 12, 0, 0),
        new Vent(11, "reactorHeatVent", getI18n("ComponentName.ReactorHeatVent"), TextureFactory.getImage("reactorVentCore.png", "reactor_heat_vent.png"), 1, 1000, null, 5, 5, 0),
        new Vent(12, "componentHeatVent", getI18n("ComponentName.ComponentHeatVent"), TextureFactory.getImage("reactorVentSpread.png", "component_heat_vent.png"), 1, 1, null, 0, 0, 4),
        new Vent(13, "overclockedHeatVent", getI18n("ComponentName.OverclockedHeatVent"), TextureFactory.getImage("reactorVentGold.png", "overclocked_heat_vent.png"), 1, 1000, null, 20, 36, 0),
        new CoolantCell(14, "coolantCell10k", getI18n("ComponentName.CoolantCell10k"), TextureFactory.getImage("reactorCoolantSimple.png", "heat_storage.png"), 1, 10e3, null),
        new CoolantCell(15, "coolantCell30k", getI18n("ComponentName.CoolantCell30k"), TextureFactory.getImage("reactorCoolantTriple.png", "tri_heat_storage.png"), 1, 30e3, null),
        new CoolantCell(16, "coolantCell60k", getI18n("ComponentName.CoolantCell60k"), TextureFactory.getImage("reactorCoolantSix.png", "hex_heat_storage.png"), 1, 60e3, null),
        new Exchanger(17, "heatExchanger", getI18n("ComponentName.HeatExchanger"), TextureFactory.getImage("reactorHeatSwitch.png", "heat_exchanger.png"), 1, 2500, null, 12, 4),
        new Exchanger(18, "advancedHeatExchanger", getI18n("ComponentName.AdvancedHeatExchanger"), TextureFactory.getImage("reactorHeatSwitchDiamond.png", "advanced_heat_exchanger.png"), 1, 10e3, null, 24, 8),
        new Exchanger(19, "coreHeatExchanger", getI18n("ComponentName.ReactorHeatExchanger"), TextureFactory.getImage("reactorHeatSwitchCore.png", "reactor_heat_exchanger.png"), 1, 5000, null, 0, 72),
        new Exchanger(20, "componentHeatExchanger", getI18n("ComponentName.ComponentHeatExchanger"), TextureFactory.getImage("reactorHeatSwitchSpread.png", "component_heat_exchanger.png"), 1, 5000, null, 36, 0),
        new Plating(21, "reactorPlating", getI18n("ComponentName.ReactorPlating"), TextureFactory.getImage("reactorPlating.png", "plating.png"), 1, 1, null, 1000, 0.9025),
        new Plating(22, "heatCapacityReactorPlating", getI18n("ComponentName.HeatCapacityReactorPlating"), TextureFactory.getImage("reactorPlatingHeat.png", "heat_plating.png"), 1, 1, null, 1700, 0.9801),
        new Plating(23, "containmentReactorPlating", getI18n("ComponentName.ContainmentReactorPlating"), TextureFactory.getImage("reactorPlatingExplosive.png", "containment_plating.png"), 1, 1, null, 500, 0.81),
        new Condensator(24, "rshCondensator", getI18n("ComponentName.RshCondensator"), TextureFactory.getImage("reactorCondensator.png", "rsh_condensator.png"), 1, 20e3, null),
        new Condensator(25, "lzhCondensator", getI18n("ComponentName.LzhCondensator"), TextureFactory.getImage("reactorCondensatorLap.png", "lzh_condensator.png"), 1, 100e3, null),
        new FuelRod(26, "fuelRodThorium", getI18n("ComponentName.FuelRodThorium"), TextureFactory.getImage("gt.Thoriumcell.png"), 50e3, 1, "GT5.08", 20, 0.5, 1, false),
        new FuelRod(27, "dualFuelRodThorium", getI18n("ComponentName.DualFuelRodThorium"), TextureFactory.getImage("gt.Double_Thoriumcell.png"), 50e3, 1, "GT5.08", 40, 1, 2, false),
        new FuelRod(28, "quadFuelRodThorium", getI18n("ComponentName.QuadFuelRodThorium"), TextureFactory.getImage("gt.Quad_Thoriumcell.png"), 50e3, 1, "GT5.08", 80, 2, 4, false),
        new CoolantCell(29, "coolantCellHelium60k", getI18n("ComponentName.CoolantCell60kHelium"), TextureFactory.getImage("gt.60k_Helium_Coolantcell.png"), 1, 60e3, "GT5.08"),
        new CoolantCell(30, "coolantCellHelium180k", getI18n("ComponentName.CoolantCell180kHelium"), TextureFactory.getImage("gt.180k_Helium_Coolantcell.png"), 1, 180e3, "GT5.08"),
        new CoolantCell(31, "coolantCellHelium360k", getI18n("ComponentName.CoolantCell360kHelium"), TextureFactory.getImage("gt.360k_Helium_Coolantcell.png"), 1, 360e3, "GT5.08"),
        new CoolantCell(32, "coolantCellNak60k", getI18n("ComponentName.CoolantCell60kNak"), TextureFactory.getImage("gt.60k_NaK_Coolantcell.png"), 1, 60e3, "GT5.08"),
        new CoolantCell(33, "coolantCellNak180k", getI18n("ComponentName.CoolantCell180kNak"), TextureFactory.getImage("gt.180k_NaK_Coolantcell.png"), 1, 180e3, "GT5.08"),
        new CoolantCell(34, "coolantCellNak360k", getI18n("ComponentName.CoolantCell360kNak"), TextureFactory.getImage("gt.360k_NaK_Coolantcell.png"), 1, 360e3, "GT5.08"),
        new Reflector(35, "iridiumNeutronReflector", getI18n("ComponentName.IridiumNeutronReflector"), TextureFactory.getImage("gt.neutronreflector.png", "neutron_reflector.png"), 1, 1, null),
        new FuelRod(36, "fuelRodNaquadah", getI18n("ComponentName.FuelRodNaquadah"), TextureFactory.getImage("gt.Naquadahcell.png"), 100e3, 1, "GT5.09", 100, 2, 1, true),
        new FuelRod(37, "dualFuelRodNaquadah", getI18n("ComponentName.DualFuelRodNaquadah"), TextureFactory.getImage("gt.Double_Naquadahcell.png"), 100e3, 1, "GT5.09", 200, 4, 2, true),
        new FuelRod(38, "quadFuelRodNaquadah", getI18n("ComponentName.QuadFuelRodNaquadah"), TextureFactory.getImage("gt.Quad_Naquadahcell.png"), 100e3, 1, "GT5.09", 400, 8, 4, true),
        
    };
    
    private static final Map<String, ReactorItem> ITEM_MAP = makeItemMap();
    
    private static Map<String, ReactorItem> makeItemMap() {
        Map<String, ReactorItem> result = new HashMap<>((int)(ITEMS.length * 1.5));
        for (ReactorItem reactorItem : ITEMS) {
            if (reactorItem != null) {
                result.put(reactorItem.baseName, reactorItem);
            }
        }
        return Collections.unmodifiableMap(result);
    }
    
    private static ReactorItem copy(ReactorItem source) {
        if (source != null) {
            Class<? extends ReactorItem> aClass = source.getClass();
            if (aClass == Condensator.class) {
                return new Condensator((Condensator) source);
            } else if (aClass == CoolantCell.class) {
                return new CoolantCell((CoolantCell) source);
            } else if (aClass == Exchanger.class) {
                return new Exchanger((Exchanger) source);
            } else if (aClass == FuelRod.class) {
                return new FuelRod((FuelRod) source);
            } else if (aClass == Plating.class) {
                return new Plating((Plating) source);
            } else if (aClass == Reflector.class) {
                return new Reflector((Reflector) source);
            } else if (aClass == Vent.class) {
                return new Vent((Vent) source);
            }
        }
        return null;
    }
    
    /**
     * Gets a default instances of the specified component (such as for drawing button images)
     * @param id the id of the component.
     * @return the component with the specified id, or null if the id is out of range.
     */
    public static ReactorItem getDefaultComponent(int id) {
        if (id >= 0 && id < ITEMS.length) {
            return ITEMS[id];
        }
        return null;
    }
    
    /**
     * Gets a default instances of the specified component (such as for drawing button images)
     * @param name the name of the component.
     * @return the component with the specified name, or null if the name is not found.
     */
    public static ReactorItem getDefaultComponent(String name) {
        if (name != null) {
            return ITEM_MAP.get(name);
        }
        return null;
    }
    
    /**
     * Creates a new instance of the specified component.
     * @param id the id of the component to create.
     * @return a new instance of the specified component, or null if the id is out of range.
     */
    public static ReactorItem createComponent(int id) {
        if (id >= 0 && id < ITEMS.length) {
            return copy(ITEMS[id]);
        }
        return null;
    }
    
    /**
     * Creates a new instance of the specified component.
     * @param name the name of the component to create.
     * @return a new instance of the specified component, or null if the name is not found.
     */
    public static ReactorItem createComponent(String name) {
        if (name != null) {
            return copy(ITEM_MAP.get(name));
        }
        return null;
    }
       
    /**
     * Get the number of defined components.
     * @return the number of defined components.
     */
    public static int getComponentCount() {
        return ITEMS.length;
    }
    
}
