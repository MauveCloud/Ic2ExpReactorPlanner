package Ic2ExpReactorPlanner;

/**
 * Represents a heat vent.
 * @author Brian McCloud
 */
public class HeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVent.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(4, "Iron Bars", 4, "Iron Plate", 3, "Iron Ingot", 1, "Tin Plate", 16, "Copper Cable");
    
    /**
     * Creates a new instance.
     */
    public HeatVent() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(1000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Heat Vent";
    }

    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }

    @Override
    public void dissipate() {
        adjustCurrentHeat(-Math.min(6, getCurrentHeat()));
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
}
