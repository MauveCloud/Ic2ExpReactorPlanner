package Ic2ExpReactorPlanner;

/**
 * Represents an advanced heat vent.
 * @author Brian McCloud
 */
public class AdvancedHeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVentDiamond.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(2, HeatVent.MATERIALS, "Diamond", 6, "Iron Bars");
    
    /**
     * Creates a new instance.
     */
    public AdvancedHeatVent() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(1000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Advanced Heat Vent";
    }
    
    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }

    @Override
    public void dissipate() {
        getParent().ventHeat(Math.min(12, getCurrentHeat()));
        adjustCurrentHeat(-Math.min(12, getCurrentHeat()));
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
}
