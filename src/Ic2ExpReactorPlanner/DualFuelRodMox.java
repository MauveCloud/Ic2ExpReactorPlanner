package Ic2ExpReactorPlanner;

/**
 * Represents a dual MOX fuel rod.
 * @author Brian McCloud
 */
public class DualFuelRodMox extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorMOXDual.png";    
    
    /**
     * Creates a new instance.
     */
    public DualFuelRodMox() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(10000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Dual Fuel Rod (MOX)";
    }
        
    @Override
    public void generateHeat() {
        int pulses = countNeutronNeighbors() + 2;
        int heat = 4 * pulses * (pulses + 1);
        handleHeat(heat);
        applyDamage(1.0);
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 2;
        final Reactor parentReactor = getParent();
        double energy = 200 * pulses * (1 + 4.0 * parentReactor.getCurrentHeat() / parentReactor.getMaxHeat());
        parentReactor.addEUOutput(energy);
    }

}
