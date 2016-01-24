package Ic2ExpReactorPlanner;

import static Ic2ExpReactorPlanner.S._;

/**
 * Represents some containment reactor plating.
 * @author Brian McCloud
 */
public class ContainmentReactorPlating extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorPlatingExplosive.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(ReactorPlating.MATERIALS, 2, "Advanced Alloy");
    
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
        return _("Containment Reactor Plating");
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
    
}
