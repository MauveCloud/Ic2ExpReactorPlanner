package Ic2ExpReactorPlanner;

/**
 * Represents an RSH-Condensator.
 * @author Brian McCloud
 */
public class RshCondensator extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorCondensator.png";    
    
    /**
     * Creates a new instance.
     */
    public RshCondensator() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(20000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "RSH-Condensator";
    }

    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }
    
}
