package Ic2ExpReactorPlanner;

/**
 * Represents some reactor plating.
 * @author Brian McCloud
 */
public class ReactorPlating extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorPlating.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(BUNDLE.getString("MaterialName.Lead"), BUNDLE.getString("MaterialName.AdvancedAlloy"));
    
    /**
     * Creates a new instance.
     */
    public ReactorPlating() {
        setImage(TextureFactory.getImage(imageFilename));
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
    
    public double getExplosionPowerMultiplier() {
        return 0.9025;
    }
    
}
