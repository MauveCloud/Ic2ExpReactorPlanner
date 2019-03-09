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
    
    public static final MaterialsList MATERIALS = new MaterialsList(ReactorPlating.MATERIALS, 2, BUNDLE.getString("MaterialName.AdvancedAlloy"));
    
    /**
     * Creates a new instance.
     */
    public ContainmentReactorPlating() {
        setImage(TextureFactory.getImage(imageFilename));
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
