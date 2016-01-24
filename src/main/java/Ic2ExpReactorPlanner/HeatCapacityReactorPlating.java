package Ic2ExpReactorPlanner;

import static Ic2ExpReactorPlanner.S._;

/**
 * Represents some heat-capacity reactor plating.
 * @author Brian McCloud
 */
public class HeatCapacityReactorPlating extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorPlatingHeat.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(ReactorPlating.MATERIALS, 8, _("Copper"));
    
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
        return _("Heat-Capacity Reactor Plating");
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
