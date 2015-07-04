package Ic2ExpReactorPlanner;

/**
 * Represents a quad thorium fuel rod.
 * @author Brian McCloud
 */
public class QuadFuelRodThorium extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.Quad_Thoriumcell.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(3, "Iron Plate", 2, "Copper Plate", 4, FuelRodThorium.MATERIALS);
    
    /**
     * Creates a new instance.
     */
    public QuadFuelRodThorium() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(50000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Quad Fuel Rod (Thorium)";
    }

    @Override
    public double generateHeat() {
        int pulses = countNeutronNeighbors() + 3;
        int heat = 2 * pulses * (pulses + 1);
        handleHeat(heat);
        applyDamage(1.0);
        return heat;
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 3;
        final Reactor parentReactor = getParent();
        double energy = 80 * pulses;
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
