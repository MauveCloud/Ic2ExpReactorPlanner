package Ic2ExpReactorPlanner;

/**
 * Represents some containment reactor plating.
 * @author Brian McCloud
 */
public class ContainmentReactorPlating extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorPlatingExplosive.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(ReactorPlating.MATERIALS, 2, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("ADVANCED ALLOY"));
    
    /**
     * Creates a new instance.
     */
    public ContainmentReactorPlating() {
        setImage(TextureFactory.getImage(imageFilename));
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("CONTAINMENT REACTOR PLATING");
    }
    
    @Override
    public void addToReactor() {
        getParent().adjustMaxHeat(500);
    }

    @Override
    public void removeFromReactor() {
        getParent().adjustMaxHeat(-500);
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
    public double getExplosionPowerMultiplier() {
        return 0.81;
    }
    
}
