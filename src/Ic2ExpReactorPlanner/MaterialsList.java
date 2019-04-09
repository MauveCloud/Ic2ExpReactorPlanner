package Ic2ExpReactorPlanner;

import static Ic2ExpReactorPlanner.BundleHelper.getI18n;
import Ic2ExpReactorPlanner.components.ReactorItem;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Represents a list of materials (such as for an IndustrialCraft2 Nuclear Reactor and components).
 * @author Brian McCloud
 */
public final class MaterialsList {

    private final SortedMap<String, Double> materials = new TreeMap<>();
    
    private static boolean useGTRecipes = false;
    private static boolean useUfcForCoolantCells = false;
    private static boolean expandAdvancedAlloy = false;
    
    // pre-load localized material names as constants to make code more readable.
    public static final String ALUMINIUM = getI18n("MaterialName.Aluminium");
    public static final String COAL = getI18n("MaterialName.Coal");
    public static final String COPPER = getI18n("MaterialName.Copper");
    public static final String DIAMOND = getI18n("MaterialName.Diamond");
    public static final String DISTILLED_WATER = getI18n("MaterialName.DistilledWater");
    // Since GT 5.09 allows different materials for making the "empty cell" (steel, tin, or PTFE), it is treated as a primitive material for GT recipes instead of a crafted item that can be further broken down.
    public static final String EMPTY_CELL = getI18n("MaterialName.EmptyCell");
    public static final String GOLD = getI18n("MaterialName.Gold");
    public static final String HELIUM = getI18n("MaterialName.Helium");
    public static final String GLASS = getI18n("MaterialName.Glass");
    public static final String GLOWSTONE = getI18n("MaterialName.GlowstoneDust");
    public static final String IRIDIUM = getI18n("MaterialName.Iridium");
    public static final String IRON = getI18n("MaterialName.Iron");
    public static final String LAPIS = getI18n("MaterialName.LapisLazuli");
    public static final String LEAD = getI18n("MaterialName.Lead");
    public static final String MOX = getI18n("MaterialName.MoxFuel");
    public static final String NAQUADAH = getI18n("MaterialName.EnrichedNaquadah");
    public static final String POTASSIUM = getI18n("MaterialName.Potassium");
    public static final String REDSTONE = getI18n("MaterialName.Redstone");
    public static final String RUBBER = getI18n("MaterialName.Rubber");
    public static final String SODIUM = getI18n("MaterialName.Sodium");
    public static final String THORIUM = getI18n("MaterialName.Thorium");
    public static final String TIN = getI18n("MaterialName.Tin");
    public static final String URANIUM = getI18n("MaterialName.UraniumFuel");

    // Special materials lists for items that may expand differently.
    public static MaterialsList alloy = new MaterialsList(getI18n("MaterialName.AdvancedAlloy"));
    public static MaterialsList coolantCell = new MaterialsList(1.0 / 3, TIN, DISTILLED_WATER, LAPIS);
    public static MaterialsList iridiumPlate = new MaterialsList(4, IRIDIUM, 4, alloy, DIAMOND);
    
    // some materials lists for crafted items that are part of reactor components without themselves being reactor components.
    public static final MaterialsList ELECTRONIC_CIRCUIT = new MaterialsList(IRON, 2, REDSTONE, 2, COPPER, 6, RUBBER);
    public static final MaterialsList ADVANCED_CIRCUIT = new MaterialsList(ELECTRONIC_CIRCUIT, 4, REDSTONE, 2, LAPIS, 2, GLOWSTONE);
    public static final MaterialsList TIN_ITEM_CASING = new MaterialsList(0.5, TIN);
    public static final MaterialsList COIL = new MaterialsList(IRON, 8.0 / 3, COPPER);
    public static final MaterialsList ELECTRIC_MOTOR = new MaterialsList(IRON, 2, COIL, 2, TIN_ITEM_CASING);
    public static final MaterialsList IRON_BARS = new MaterialsList(6.0 / 16, IRON);
    public static final MaterialsList GLASS_PANE = new MaterialsList(6.0 / 16, GLASS);
    public static final MaterialsList BRONZE = new MaterialsList(1.0 / 4, TIN, 3.0 / 4, COPPER);


    private static Map<String, MaterialsList> componentMaterialsMap = buildComponentMaterialsMap();
    
    /**
     * Creates an empty materials list.
     */
    public MaterialsList() {
        // fields are initialized when declared, so no code is needed in this constructor.
    }
    
    /**
     * Creates a materials list with the specified items in it.
     * @param materials the materials to add, which can be strings that each represent a single material or other MaterialsList objects, and either can be preceded by a number as a count.
     * @throws IllegalArgumentException if other object types are passed as arguments.
     */
    public MaterialsList(Object... materials) {
        add(materials);
    }
    
    /**
     * Adds the specified items to this materials list.
     * @param materials the materials to add, which can be strings that each represent a single material or other MaterialsList objects, and either can be preceded by a number as a count.
     * @throws IllegalArgumentException if other object types are passed as arguments.
     */
    public void add(Object... materials) {
        double itemCount = 1;
        for (Object material : materials) {
            if (material instanceof String) {
                final String materialName = (String)material;
                if (this.materials.containsKey(materialName)) {
                    this.materials.put(materialName, this.materials.get(materialName) + itemCount);
                } else {
                    this.materials.put(materialName, itemCount);
                }
                itemCount = 1;
            } else if (material instanceof Number) {
                itemCount = ((Number)material).doubleValue();
            } else if (material instanceof MaterialsList) {
                for (Map.Entry<String, Double> entrySet : ((MaterialsList)material).materials.entrySet()) {
                    if (this.materials.containsKey(entrySet.getKey())) {
                        this.materials.put(entrySet.getKey(), this.materials.get(entrySet.getKey()) + itemCount * entrySet.getValue());
                    } else {
                        this.materials.put(entrySet.getKey(), itemCount * entrySet.getValue());
                    }
                }
                itemCount = 1;
            } else {
                throw new IllegalArgumentException("Invalid material type: " + material.getClass().getName());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(1000);
        DecimalFormat materialDecimalFormat = new DecimalFormat(getI18n("UI.MaterialDecimalFormat"));
        for (Map.Entry<String, Double> entrySet : materials.entrySet()) {
            double count = entrySet.getValue();
            String formattedNumber = materialDecimalFormat.format(count);
            result.append(String.format("%s %s\n", formattedNumber, entrySet.getKey())); //NOI18N
        }
        return result.toString();
    }
    
    public String buildComparisonString(MaterialsList rhs, boolean alwaysDiff) {
        StringBuilder result = new StringBuilder(1000);
        SortedSet<String> keys = new TreeSet<>(materials.keySet());
        keys.addAll(rhs.materials.keySet());
        DecimalFormat comparisonDecimalFormat = new DecimalFormat(getI18n("Comparison.CompareDecimalFormat"));
        DecimalFormat simpleDecimalFormat = new DecimalFormat(getI18n("Comparison.SimpleDecimalFormat"));
        for (String key : keys) {
            double left = 0;
            if (materials.containsKey(key)) {
                left = materials.get(key);
            }
            double right = 0;
            if (rhs.materials.containsKey(key)) {
                right = rhs.materials.get(key);
            }
            String color = "orange";
            if (left < right) {
                color = "green";
            } else if (left > right) {
                color = "red";
            }
            if (alwaysDiff || left != right) {
                result.append(String.format(getI18n("Comparison.MaterialsEntry"), color,
                        comparisonDecimalFormat.format(left - right), key,
                        simpleDecimalFormat.format(left),
                        simpleDecimalFormat.format(right)));
            }
        }
        return result.toString();
    }

    public static void setUseUfcForCoolantCells(boolean value) {
        useUfcForCoolantCells = value;
        if (value) {
            coolantCell = new MaterialsList(4, TIN_ITEM_CASING, GLASS_PANE, DISTILLED_WATER, LAPIS);
        } else {
            coolantCell = new MaterialsList(1.0 / 3, TIN, DISTILLED_WATER, LAPIS);
        }
        componentMaterialsMap = buildComponentMaterialsMap();
    }
    
    public static void setExpandAdvancedAlloy(boolean value) {
        expandAdvancedAlloy = value;
        if (value) {
            alloy = new MaterialsList(3.0 / 2, IRON, 3.0 / 2, BRONZE, 3.0 / 2, TIN);
        } else {
            alloy = new MaterialsList(getI18n("MaterialName.AdvancedAlloy"));
        }
        iridiumPlate = new MaterialsList(4, IRIDIUM, 4, alloy, DIAMOND);
        componentMaterialsMap = buildComponentMaterialsMap();
    }
    
    public static void setUseGTRecipes(boolean value) {
        useGTRecipes = value;
        if (value) {
            coolantCell = new MaterialsList(EMPTY_CELL, DISTILLED_WATER, LAPIS);
            alloy = new MaterialsList(getI18n("MaterialName.AdvancedAlloy"));
        } else {
            if (useUfcForCoolantCells) {
                coolantCell = new MaterialsList(4, TIN_ITEM_CASING, GLASS_PANE, DISTILLED_WATER, LAPIS);
            } else {
                coolantCell = new MaterialsList(1.0 / 3, TIN, DISTILLED_WATER, LAPIS);
            }
            if (expandAdvancedAlloy) {
                alloy = new MaterialsList(3.0 / 2, IRON, 3.0 / 2, BRONZE, 3.0 / 2, TIN);
            } else {
                alloy = new MaterialsList(getI18n("MaterialName.AdvancedAlloy"));
            }
        }
        componentMaterialsMap = buildComponentMaterialsMap();
    }
    
    public static MaterialsList getMaterialsForComponent(ReactorItem component) {
        return componentMaterialsMap.get(component.baseName);
    }
    
    private static Map<String, MaterialsList> buildComponentMaterialsMap() {
        Map<String, MaterialsList> result = new HashMap<>(50);
        result.put("fuelRodUranium", new MaterialsList(IRON, URANIUM));
        result.put("dualFuelRodUranium", new MaterialsList(IRON, 2, result.get("fuelRodUranium")));
        result.put("quadFuelRodUranium", new MaterialsList(3, IRON, 2, COPPER, 4, result.get("fuelRodUranium")));
        result.put("fuelRodMox", new MaterialsList(IRON, MOX));
        result.put("dualFuelRodMox", new MaterialsList(IRON, 2, result.get("fuelRodMox")));
        result.put("quadFuelRodMox", new MaterialsList(3, IRON, 2, COPPER, 4, result.get("fuelRodMox")));
        result.put("neutronReflector", new MaterialsList(COPPER, 4, TIN, 4, COAL));
        result.put("thickNeutronReflector", new MaterialsList(4, result.get("neutronReflector"), 5, COPPER));
        if (useGTRecipes) {
            result.put("heatVent", new MaterialsList(4, ALUMINIUM, 4, IRON_BARS));
        } else {
            result.put("heatVent", new MaterialsList(ELECTRIC_MOTOR, 4, IRON, 4, IRON_BARS));
        }
        result.put("advancedHeatVent", new MaterialsList(2, result.get("heatVent"), 6, IRON_BARS, DIAMOND));
        result.put("reactorHeatVent", new MaterialsList(result.get("heatVent"), 8, COPPER));
        result.put("componentHeatVent", new MaterialsList(result.get("heatVent"), 4, TIN, 4, IRON_BARS));
        result.put("overclockedHeatVent", new MaterialsList(result.get("reactorHeatVent"), 4, GOLD));
        result.put("coolantCell10k", new MaterialsList(coolantCell, 4, TIN));
        result.put("coolantCell30k", new MaterialsList(3, result.get("coolantCell10k"), 6, TIN));
        result.put("coolantCell60k", new MaterialsList(2, result.get("coolantCell30k"), 6, TIN, IRON));
        result.put("heatExchanger", new MaterialsList(ELECTRONIC_CIRCUIT, 3, TIN, 5, COPPER));
        result.put("advancedHeatExchanger", new MaterialsList(2, result.get("heatExchanger"), 2, ELECTRONIC_CIRCUIT, COPPER, 4, LAPIS));
        result.put("coreHeatExchanger", new MaterialsList(result.get("heatExchanger"), 8, COPPER));
        result.put("componentHeatExchanger", new MaterialsList(result.get("heatExchanger"), 4, GOLD));
        result.put("reactorPlating", new MaterialsList(LEAD, alloy));
        result.put("heatCapacityReactorPlating", new MaterialsList(result.get("reactorPlating"), 8, COPPER));
        if (useGTRecipes) {
            result.put("containmentReactorPlating", new MaterialsList(result.get("reactorPlating"), LEAD));
        } else {
            result.put("containmentReactorPlating", new MaterialsList(result.get("reactorPlating"), 2, alloy));
        }
        result.put("rshCondensator", new MaterialsList(result.get("heatVent"), result.get("heatExchanger"), 7, REDSTONE));
        result.put("lzhCondensator", new MaterialsList(2, result.get("rshCondensator"), result.get("reactorHeatVent"), result.get("coreHeatExchanger"), 9, LAPIS, 4, REDSTONE));
        result.put("fuelRodThorium", new MaterialsList(IRON, 3, THORIUM));
        result.put("dualFuelRodThorium", new MaterialsList(IRON, 2, result.get("fuelRodThorium")));
        result.put("quadFuelRodThorium", new MaterialsList(3, IRON, 2, COPPER, 4, result.get("fuelRodThorium")));
        result.put("coolantCellHelium60k", new MaterialsList(EMPTY_CELL, HELIUM, 4, TIN));
        result.put("coolantCellHelium180k", new MaterialsList(3, result.get("coolantCellHelium60k"), 6, TIN));
        result.put("coolantCellHelium360k", new MaterialsList(2, result.get("coolantCellHelium180k"), 6, TIN, 9, COPPER));
        result.put("coolantCellNak60k", new MaterialsList(result.get("coolantCell10k"), 4, TIN, 2, POTASSIUM, 2, SODIUM));
        result.put("coolantCellNak180k", new MaterialsList(3, result.get("coolantCellNak60k"), 6, TIN));
        result.put("coolantCellNak360k", new MaterialsList(2, result.get("coolantCellNak180k"), 6, TIN, 9, COPPER));
        result.put("iridiumNeutronReflector", new MaterialsList(6, result.get("thickNeutronReflector"), 18, COPPER, iridiumPlate));
        result.put("fuelRodNaquadah", new MaterialsList(IRON, 3, NAQUADAH));
        result.put("dualFuelRodNaquadah", new MaterialsList(IRON, 2, result.get("fuelRodNaquadah")));
        result.put("quadFuelRodNaquadah", new MaterialsList(3, IRON, 2, COPPER, 4, result.get("fuelRodNaquadah")));
        return result;
    }
    
}
