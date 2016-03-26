package Ic2ExpReactorPlanner;

/**
 * Represents some heat-capacity reactor plating.
 * @author Brian McCloud
 */
public class HeatCapacityReactorPlating extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorPlatingHeat.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(ReactorPlating.MATERIALS, 8, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("COPPER"));
    
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
        return java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("HEAT-CAPACITY REACTOR PLATING");
    }

    @Override
    public void addToReactor() {
        getParent().adjustMaxHeat(1700);
    }

    @Override
    public void removeFromReactor() {
        getParent().adjustMaxHeat(-1700);
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
}
