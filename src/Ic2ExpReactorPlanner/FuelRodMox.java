package Ic2ExpReactorPlanner;

/**
 * Represents a single MOX fuel rod.
 * @author Brian McCloud
 */
public class FuelRodMox extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorMOXSimple.png";    
    
    /**
     * Creates a new instance.
     */
    public FuelRodMox() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(10000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Fuel Rod (MOX)";
    }
    
    @Override
    public void generateHeat() {
        int pulses = countNeutronNeighbors() + 1;
        int heat = 2 * pulses * (pulses + 1);
        handleHeat(heat);
        applyDamage(1.0);
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 1;
        final Reactor parentReactor = getParent();
        double energy = 100 * pulses * (1 + 4.0 * parentReactor.getCurrentHeat() / parentReactor.getMaxHeat());
        parentReactor.addEUOutput(energy);
    }

}
