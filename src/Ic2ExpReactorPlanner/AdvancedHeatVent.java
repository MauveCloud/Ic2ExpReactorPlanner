package Ic2ExpReactorPlanner;

/**
 * Represents an advanced heat vent.
 * @author Brian McCloud
 */
public class AdvancedHeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorVentDiamond.png";    
    
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
        adjustCurrentHeat(-Math.min(12, getCurrentHeat()));
    }
    
}
