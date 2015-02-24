package Ic2ExpReactorPlanner;

/**
 * Represents a 30k Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell30k extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorCoolantTriple.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(3, CoolantCell10k.MATERIALS, 6, "Tin Plate");
    
    /**
     * Creates a new instance.
     */
    public CoolantCell30k() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(30000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        String result = "30k Coolant Cell";
        if (getInitialHeat() > 0) {
            result += String.format(" (initial heat: %,d)", (int)getInitialHeat());
        }
        return result;
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
