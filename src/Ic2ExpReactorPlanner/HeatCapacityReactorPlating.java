package Ic2ExpReactorPlanner;

/**
 * Represents some heat-capacity reactor plating.
 * @author Brian McCloud
 */
public class HeatCapacityReactorPlating extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorPlatingHeat.png";    
    
    /**
     * Creates a new instance.
     */
    public HeatCapacityReactorPlating() {
        setImage(TextureFactory.getImage(imageFilename));
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Heat-Capacity Reactor Plating";
    }

    @Override
    public void addToReactor() {
        getParent().adjustMaxHeat(1700);
    }

    @Override
    public void removeFromReactor() {
        getParent().adjustMaxHeat(-1700);
    }
    
}
