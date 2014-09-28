package Ic2ExpReactorPlanner;

/**
 * Represents a quad MOX fuel rod.
 * @author Brian McCloud
 */
public class QuadFuelRodMox extends FuelRodUranium {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorMOXQuad.png";    
    
    /**
     * Creates a new instance.
     */
    public QuadFuelRodMox() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(10000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Quad Fuel Rod (MOX)";
    }
        
    @Override
    public void generateHeat() {
        int pulses = countNeutronNeighbors() + 3;
        int heat = 8 * pulses * (pulses + 1);
        handleHeat(heat);
        applyDamage(1.0);
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 3;
        final Reactor parentReactor = getParent();
        double energy = 400 * pulses * (1 + 4.0 * parentReactor.getCurrentHeat() / parentReactor.getMaxHeat());
        parentReactor.addEUOutput(energy);
    }

}
