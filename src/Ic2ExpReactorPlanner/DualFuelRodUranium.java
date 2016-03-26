package Ic2ExpReactorPlanner;

/**
 * Represents a dual uranium fuel rod.
 * @author Brian McCloud
 */
public class DualFuelRodUranium extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorUraniumDual.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("IRON"), 2, FuelRodUranium.MATERIALS);
    
    /**
     * Creates a new instance.
     */
    public DualFuelRodUranium() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(20000);
        automationThreshold = 21000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("DUAL FUEL ROD (URANIUM)");
    }
    
    @Override
    public double generateHeat() {
        int pulses = countNeutronNeighbors() + 2;
        int heat = 4 * pulses * (pulses + 1);
        handleHeat(heat);
        applyDamage(1.0);
        return heat;
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 2;
        final Reactor parentReactor = getParent();
        double energy = 200 * pulses;
        parentReactor.addEUOutput(energy);
    }

    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
    @Override
    public int getRodCount() {
        return 2;
    }
    
}
