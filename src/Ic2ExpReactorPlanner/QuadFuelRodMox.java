package Ic2ExpReactorPlanner;

/**
 * Represents a quad MOX fuel rod.
 * @author Brian McCloud
 */
public class QuadFuelRodMox extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorMOXQuad.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(3, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("IRON"), 2, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("COPPER"), 4, FuelRodMox.MATERIALS);
    
    /**
     * Creates a new instance.
     */
    public QuadFuelRodMox() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(10000);
        automationThreshold = 11000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("QUAD FUEL ROD (MOX)");
    }
        
    @Override
    public double generateHeat() {
        int pulses = countNeutronNeighbors() + 3;
        int heat = 8 * pulses * (pulses + 1);
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
        int pulses = countNeutronNeighbors() + 3;
        final Reactor parentReactor = getParent();
        double energy = 400 * pulses * (1 + 4.0 * parentReactor.getCurrentHeat() / parentReactor.getMaxHeat());
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
