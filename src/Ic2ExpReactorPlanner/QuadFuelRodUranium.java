package Ic2ExpReactorPlanner;

/**
 * Represents a quad uranium fuel rod.
 * @author Brian McCloud
 */
public class QuadFuelRodUranium extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorUraniumQuad.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(3, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("IRON"), 2, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("COPPER"), 4, FuelRodUranium.MATERIALS);
    
    /**
     * Creates a new instance.
     */
    public QuadFuelRodUranium() {
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
        return java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("QUAD FUEL ROD (URANIUM)");
    }

    @Override
    public double generateHeat() {
        int pulses = countNeutronNeighbors() + 3;
        int heat = 8 * pulses * (pulses + 1);
        handleHeat(heat);
        applyDamage(1.0);
        return heat;
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 3;
        final Reactor parentReactor = getParent();
        double energy = 400 * pulses;
        parentReactor.addEUOutput(energy);
    }

    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
    @Override
    public int getRodCount() {
        return 4;
    }
    
}
