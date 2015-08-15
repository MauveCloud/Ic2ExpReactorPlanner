package Ic2ExpReactorPlanner;

/**
 * Represents a thick neutron reflector.
 * @author Brian McCloud
 */
public class ThickNeutronReflector extends NeutronReflector {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorReflectorThick.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(4, NeutronReflector.MATERIALS, 5, "Copper");
    
    /**
     * Creates a new instance.
     */
    public ThickNeutronReflector() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(40000);
        automationThreshold = 41000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Thick Neutron Reflector";
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
}
