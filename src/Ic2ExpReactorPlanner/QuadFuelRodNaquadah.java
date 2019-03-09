package Ic2ExpReactorPlanner;

/**
 * Represents a quad MOX fuel rod.
 * @author Brian McCloud
 */
public class QuadFuelRodNaquadah extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.Quad_Naquadahcell.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(3, BUNDLE.getString("MaterialName.Iron"), 2, BUNDLE.getString("MaterialName.Copper"), 4, FuelRodNaquadah.MATERIALS);
    
    /**
     * Creates a new instance.
     */
    public QuadFuelRodNaquadah() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(100000);
        automationThreshold = 110000;
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
