package Ic2ExpReactorPlanner;

/**
 * Represents a thick neutron reflector.
 * @author Brian McCloud
 */
public class ThickNeutronReflector extends NeutronReflector {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorReflectorThick.png";    
    
    /**
     * Creates a new instance.
     */
    public ThickNeutronReflector() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(40000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Thick Neutron Reflector";
    }
    
}
