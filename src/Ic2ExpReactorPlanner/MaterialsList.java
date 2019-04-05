package Ic2ExpReactorPlanner;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.ResourceBundle;
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
    private static final ResourceBundle BUNDLE = java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle");
    
    // pre-load localized material names as constants to make code more readable.
    public static final String ALLOY = BUNDLE.getString("MaterialName.AdvancedAlloy");
    public static final String COAL = BUNDLE.getString("MaterialName.Coal");
    public static final String COPPER = BUNDLE.getString("MaterialName.Copper");
    public static final String DIAMOND = BUNDLE.getString("MaterialName.Diamond");
    public static final String DISTILLED_WATER = BUNDLE.getString("MaterialName.DistilledWater");
    public static final String GOLD = BUNDLE.getString("MaterialName.Gold");
    // Since GT 5.09 allows different materials for making the "empty cell" (steel, tin, or PTFE), the helium cell is treated as a primitive material instead of a crafted item that can be further broken down.
    public static final String HELIUM = BUNDLE.getString("MaterialName.HeliumCell");
    public static final String GLOWSTONE = BUNDLE.getString("MaterialName.GlowstoneDust");
    public static final String IRIDIUM = BUNDLE.getString("MaterialName.Iridium");
    public static final String IRON = BUNDLE.getString("MaterialName.Iron");
    public static final String LAPIS = BUNDLE.getString("MaterialName.LapisLazuli");
    public static final String LEAD = BUNDLE.getString("MaterialName.Lead");
    public static final String MOX = BUNDLE.getString("MaterialName.MoxFuel");
    public static final String NAQUADAH = BUNDLE.getString("MaterialName.EnrichedNaquadah");
    public static final String POTASSIUM = BUNDLE.getString("MaterialName.Potassium");
    public static final String REDSTONE = BUNDLE.getString("MaterialName.Redstone");
    public static final String RUBBER = BUNDLE.getString("MaterialName.Rubber");
    public static final String SODIUM = BUNDLE.getString("MaterialName.Sodium");
    public static final String THORIUM = BUNDLE.getString("MaterialName.Thorium");
    public static final String TIN = BUNDLE.getString("MaterialName.Tin");
    public static final String URANIUM = BUNDLE.getString("MaterialName.UraniumFuel");
    
    // some materials lists for crafted items that are part of reactor components without themselves being reactor components.
    public static final MaterialsList ELECTRONIC_CIRCUIT = new MaterialsList(IRON, 2, REDSTONE, 2, COPPER, 6, RUBBER);
    public static final MaterialsList ADVANCED_CIRCUIT = new MaterialsList(ELECTRONIC_CIRCUIT, 4, REDSTONE, 2, LAPIS, 2, GLOWSTONE);
    public static final MaterialsList TIN_ITEM_CASING = new MaterialsList(0.5, TIN);
    public static final MaterialsList COIL = new MaterialsList(IRON, 8.0 / 3, COPPER);
    public static final MaterialsList ELECTRIC_MOTOR = new MaterialsList(IRON, 2, COIL, 2, TIN_ITEM_CASING);
    public static final MaterialsList IRON_BARS = new MaterialsList(6.0 / 16, IRON);
    // TODO: rework MaterialsList to allow alternate lists based on config such as Minecraft version (IC2 for MC 1.8 eliminated empty cells, so coolant cells have to be made with universal fluid cells instead)
    public static final MaterialsList COOLANT_CELL = new MaterialsList(1.0 / 3, TIN, DISTILLED_WATER, LAPIS);
    public static final MaterialsList IRIDIUM_PLATE = new MaterialsList(4, IRIDIUM, 4, ALLOY, DIAMOND);
    
    // Predefined materials lists for components based on their recipes (not pre-expanded, to reduce the risk of mistakes).
    public static final MaterialsList FUEL_ROD_URANIUM = new MaterialsList(IRON, URANIUM);
    public static final MaterialsList DUAL_FUEL_ROD_URANIUM = new MaterialsList(IRON, 2, FUEL_ROD_URANIUM);
    public static final MaterialsList QUAD_FUEL_ROD_URANIUM = new MaterialsList(3, IRON, 2, COPPER, 4, FUEL_ROD_URANIUM);
    public static final MaterialsList FUEL_ROD_MOX = new MaterialsList(IRON, MOX);
    public static final MaterialsList DUAL_FUEL_ROD_MOX = new MaterialsList(IRON, 2, FUEL_ROD_MOX);
    public static final MaterialsList QUAD_FUEL_ROD_MOX = new MaterialsList(3, IRON, 2, COPPER, 4, FUEL_ROD_MOX);
    public static final MaterialsList NEUTRON_REFLECTOR = new MaterialsList(COPPER, 4, TIN, 4, COAL);
    public static final MaterialsList THICK_NEUTRON_REFLECTOR = new MaterialsList(4, NEUTRON_REFLECTOR, 5, COPPER);
    public static final MaterialsList HEAT_VENT = new MaterialsList(ELECTRIC_MOTOR, 4, IRON, 4, IRON_BARS);
    public static final MaterialsList ADVANCED_HEAT_VENT = new MaterialsList(2, HEAT_VENT, 6, IRON_BARS, DIAMOND);
    public static final MaterialsList REACTOR_HEAT_VENT = new MaterialsList(HEAT_VENT, 8, COPPER);
    public static final MaterialsList COMPONENT_HEAT_VENT = new MaterialsList(HEAT_VENT, 4, TIN, 4, IRON_BARS);
    public static final MaterialsList OVERCLOCKED_HEAT_VENT = new MaterialsList(REACTOR_HEAT_VENT, 4, GOLD);
    public static final MaterialsList COOLANT_CELL_10K = new MaterialsList(COOLANT_CELL, 4, TIN);
    public static final MaterialsList COOLANT_CELL_30K = new MaterialsList(3, COOLANT_CELL_10K, 6, TIN);
    public static final MaterialsList COOLANT_CELL_60K = new MaterialsList(2, COOLANT_CELL_30K, 6, TIN, IRON);
    public static final MaterialsList HEAT_EXCHANGER = new MaterialsList(ELECTRONIC_CIRCUIT, 3, TIN, 5, COPPER);
    public static final MaterialsList ADVANCED_HEAT_EXCHANGER = new MaterialsList(2, HEAT_EXCHANGER, 2, ELECTRONIC_CIRCUIT, COPPER, 4, LAPIS);
    public static final MaterialsList REACTOR_HEAT_EXCHANGER = new MaterialsList(HEAT_EXCHANGER, 8, COPPER);
    public static final MaterialsList COMPONENT_HEAT_EXCHANGER = new MaterialsList(HEAT_EXCHANGER, 4, GOLD);
    public static final MaterialsList REACTOR_PLATING = new MaterialsList(LEAD, ALLOY);
    public static final MaterialsList HEAT_CAPACITY_REACTOR_PLATING = new MaterialsList(REACTOR_PLATING, 8, COPPER);
    public static final MaterialsList CONTAINMENT_REACTOR_PLATING = new MaterialsList(REACTOR_PLATING, 2, ALLOY);
    public static final MaterialsList RSH_CONDENSATOR = new MaterialsList(HEAT_VENT, HEAT_EXCHANGER, 7, REDSTONE);
    public static final MaterialsList LZH_CONDENSATOR = new MaterialsList(2, RSH_CONDENSATOR, REACTOR_HEAT_VENT, REACTOR_HEAT_EXCHANGER, 9, LAPIS, 4, REDSTONE);
    public static final MaterialsList FUEL_ROD_THORIUM = new MaterialsList(IRON, 3, THORIUM);
    public static final MaterialsList DUAL_FUEL_ROD_THORIUM = new MaterialsList(IRON, 2, FUEL_ROD_THORIUM);
    public static final MaterialsList QUAD_FUEL_ROD_THORIUM = new MaterialsList(3, IRON, 2, COPPER, 4, FUEL_ROD_THORIUM);
    public static final MaterialsList COOLANT_CELL_60K_HELIUM = new MaterialsList(HELIUM, 4, TIN);
    public static final MaterialsList COOLANT_CELL_180K_HELIUM = new MaterialsList(3, COOLANT_CELL_60K_HELIUM, 6, TIN);
    public static final MaterialsList COOLANT_CELL_360K_HELIUM = new MaterialsList(2, COOLANT_CELL_180K_HELIUM, 6, TIN, 9, COPPER);
    public static final MaterialsList COOLANT_CELL_60K_NAK = new MaterialsList(COOLANT_CELL_10K, 4, TIN, 2, POTASSIUM, 2, SODIUM);
    public static final MaterialsList COOLANT_CELL_180K_NAK = new MaterialsList(3, COOLANT_CELL_60K_NAK, 6, TIN);
    public static final MaterialsList COOLANT_CELL_360K_NAK = new MaterialsList(2, COOLANT_CELL_180K_NAK, 6, TIN, 9, COPPER);
    public static final MaterialsList IRIDIUM_NEUTRON_REFLECTOR = new MaterialsList(6, THICK_NEUTRON_REFLECTOR, 18, COPPER, IRIDIUM_PLATE);
    public static final MaterialsList FUEL_ROD_NAQUADAH = new MaterialsList(IRON, 3, NAQUADAH);
    public static final MaterialsList DUAL_FUEL_ROD_NAQUADAH = new MaterialsList(IRON, 2, FUEL_ROD_NAQUADAH);
    public static final MaterialsList QUAD_FUEL_ROD_NAQUADAH = new MaterialsList(3, IRON, 2, COPPER, 4, FUEL_ROD_NAQUADAH);
    
    /**
     * Creates an empty materials list.
     */
    public MaterialsList() {
        // fields are initialized when declared, so no code is needed in this constructor.
    }
    
    /**
     * Creates a materials list with the specified items in it.
     * @param materials the materials to add, which can be strings that each represent a single material or other MaterialsList objects, and either can be preceded by an integer as a count.
     * @throws IllegalArgumentException if other object types are passed as arguments.
     */
    public MaterialsList(Object... materials) {
        add(materials);
    }
    
    /**
     * Adds the specified items to this materials list.
     * @param materials the materials to add, which can be strings that each represent a single material or other MaterialsList objects, and either can be preceded by an integer as a count.
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
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder(1000);
        DecimalFormat materialDecimalFormat = new DecimalFormat(BUNDLE.getString("UI.MaterialDecimalFormat"));
        for (Map.Entry<String, Double> entrySet : materials.entrySet()) {
            double count = entrySet.getValue();
            String formattedNumber = materialDecimalFormat.format(count);
            result.append(String.format("%s %s\n", formattedNumber, entrySet.getKey())); //NOI18N
        }
        return result.toString();
    }
    
    public String buildComparisonString(MaterialsList rhs) {
        StringBuilder result = new StringBuilder(1000);
        SortedSet<String> keys = new TreeSet<>(materials.keySet());
        keys.addAll(rhs.materials.keySet());
        DecimalFormat comparisonDecimalFormat = new DecimalFormat(BUNDLE.getString("Comparison.CompareDecimalFormat"));
        DecimalFormat simpleDecimalFormat = new DecimalFormat(BUNDLE.getString("Comparison.SimpleDecimalFormat"));
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
            result.append(String.format(BUNDLE.getString("Comparison.MaterialsEntry"), color, 
                    comparisonDecimalFormat.format(left - right), key, 
                    simpleDecimalFormat.format(left), 
                    simpleDecimalFormat.format(right)));
        }
        return result.toString();
    }
}
