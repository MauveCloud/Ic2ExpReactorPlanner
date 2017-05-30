package Ic2ExpReactorPlanner;

/**
 * Represents a single MOX fuel rod.
 * @author Brian McCloud
 */
public class FuelRodMox extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorMOXSimple.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("IRON"), java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("MOX FUEL"));
    
    /**
     * Creates a new instance.
     */
    public FuelRodMox() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(5000);
        automationThreshold = 11000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("FUEL ROD (MOX)");
    }
    
    @Override
    public double generateHeat() {
        int pulses = countNeutronNeighbors() + 1;
        int heat = 2 * pulses * (pulses + 1);
        final Reactor parentReactor = getParent();
        if (parentReactor.isFluid() && (parentReactor.getCurrentHeat() / parentReactor.getMaxHeat()) > 0.5) {
            heat *= 2;
        }
        handleHeat(heat);
        applyDamage(1.0);
        return heat;
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 1;
        final Reactor parentReactor = getParent();
        double energy = 100 * pulses * (1 + 4.0 * parentReactor.getCurrentHeat() / parentReactor.getMaxHeat());
        parentReactor.addEUOutput(energy);
    }

    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
    @Override
    public int getRodCount() {
        return 1;
    }
    
}
