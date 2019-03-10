package Ic2ExpReactorPlanner;

/**
 * Represents a double thorium fuel rod.
 * @author Brian McCloud
 */
public class DualFuelRodThorium extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.Double_Thoriumcell.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(BUNDLE.getString("MaterialName.Iron"), 2, FuelRodThorium.MATERIALS);
    
    /**
     * Creates a new instance.
     */
    public DualFuelRodThorium() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(50000);
        automationThreshold = 51000;
    }
    
    @Override
    public double generateHeat() {
        int pulses = countNeutronNeighbors() + 2;
        int heat = pulses * (pulses + 1);
        minHeatGenerated = Math.min(minHeatGenerated, heat);
        maxHeatGenerated = Math.max(maxHeatGenerated, heat);
        handleHeat(heat);
        applyDamage(1.0);
        return heat;
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 2;
        final Reactor parentReactor = getParent();
        double energy = 80 * pulses;
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
        return 2;
    }
    
}
