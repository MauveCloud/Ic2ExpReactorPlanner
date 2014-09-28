package Ic2ExpReactorPlanner;

/**
 * Represents an LZH-Condensator.
 * @author Brian McCloud
 */
public class LzhCondensator extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorCondensatorLap.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(2, RshCondensator.MATERIALS, ReactorHeatVent.MATERIALS, ReactorHeatExchanger.MATERIALS, 9, "Lapis Lazuli", 4, "Redstone");
    
    /**
     * Creates a new instance.
     */
    public LzhCondensator() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(100000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "LZH-Condensator";
    }

    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
}
