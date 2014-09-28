package Ic2ExpReactorPlanner;

/**
 * Represents a 10k Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell10k extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorCoolantSimple.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList("Tin Plate", "Water", 8, "Lapis Lazuli");
    
    /**
     * Creates a new instance.
     */
    public CoolantCell10k() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(10000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "10k Coolant Cell";
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
