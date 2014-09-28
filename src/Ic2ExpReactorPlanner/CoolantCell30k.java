package Ic2ExpReactorPlanner;

/**
 * Represents a 30k Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell30k extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorCoolantTriple.png";    
    
    /**
     * Creates a new instance.
     */
    public CoolantCell30k() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(30000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "30k Coolant Cell";
    }

    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }
    
}
