package Ic2ExpReactorPlanner;

/**
 * Represents a single MOX fuel rod.
 * @author Brian McCloud
 */
public class FuelRodNaquadah extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.Naquadahcell.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(BUNDLE.getString("MaterialName.Iron"), 3, BUNDLE.getString("MaterialName.EnrichedNaquadah"));
    
    /**
     * Creates a new instance.
     */
    public FuelRodNaquadah() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(100000);
        automationThreshold = 110000;
    }
    
    @Override
    public double generateHeat() {
        int pulses = countNeutronNeighbors() + 1;
        int heat = 2 * pulses * (pulses + 1);
        final Reactor parentReactor = getParent();
        if (parentReactor.isFluid() && (parentReactor.getCurrentHeat() / parentReactor.getMaxHeat()) > 0.5) {
            heat *= 2;
        }
        minHeatGenerated = Math.min(minHeatGenerated, heat);
        maxHeatGenerated = Math.max(maxHeatGenerated, heat);
        handleHeat(heat);
        applyDamage(1.0);
        return heat;
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 1;
        final Reactor parentReactor = getParent();
        double energy = 100 * pulses * (1 + 4.0 * parentReactor.getCurrentHeat() / parentReactor.getMaxHeat());
        minEUGenerated = Math.min(minEUGenerated, energy);
        maxEUGenerated = Math.max(maxEUGenerated, energy);
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
