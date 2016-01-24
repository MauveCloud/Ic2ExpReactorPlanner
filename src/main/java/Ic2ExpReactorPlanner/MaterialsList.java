package Ic2ExpReactorPlanner;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import static Ic2ExpReactorPlanner.S._;

/**
 * Represents a list of materials (such as for an IndustrialCraft2 Nuclear Reactor and components).
 * @author Brian McCloud
 */
public final class MaterialsList {
    
    private final SortedMap<String, Double> materials = new TreeMap<>();
    
    // some materials lists for common crafted items that are part of reactors and reactor components without themselves being reactor components.
    public static final MaterialsList ELECTRONIC_CIRCUIT = new MaterialsList(_("Iron"), 2, _("Redstone"), 2, _("Copper"), 6, _("Rubber"));
    public static final MaterialsList ADVANCED_CIRCUIT = new MaterialsList(ELECTRONIC_CIRCUIT, 4, _("Redstone"), 2, _("Lapis Lazuli"), 2, _("Glowstone Dust"));
    
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
        for (Map.Entry<String, Double> entrySet : materials.entrySet()) {
            double count = entrySet.getValue();
            String formattedNumber = String.format("%,.3f", count);
            if (formattedNumber.endsWith(".000")) {
                formattedNumber = String.format("%,d", (int)count);
            }
            result.append(String.format("%s * %s\n", formattedNumber, entrySet.getKey()));
        }
        return result.toString();
    }
    
}
