package Ic2ExpReactorPlanner;

/**
 * Represents some reactor plating.
 * @author Brian McCloud
 */
public class ReactorPlating extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorPlating.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList("Lead Plate", "Advanced Alloy");
    
    /**
     * Creates a new instance.
     */
    public ReactorPlating() {
        setImage(TextureFactory.getImage(imageFilename));
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Reactor Plating";
    }

    @Override
    public void addToReactor() {
        getParent().adjustMaxHeat(1000);
    }

    @Override
    public void removeFromReactor() {
        getParent().adjustMaxHeat(-1000);
    }
 
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
}
