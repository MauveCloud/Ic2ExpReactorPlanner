package Ic2ExpReactorPlanner;

/**
 * Represents a thick neutron reflector.
 * @author Brian McCloud
 */
public class ThickNeutronReflector extends NeutronReflector {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorReflectorThick.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(4, NeutronReflector.MATERIALS, 5, BUNDLE.getString("MaterialName.Copper"));
    
    /**
     * Creates a new instance.
     */
    public ThickNeutronReflector() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(40000);
        setAutomationThreshold(41000);
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
}
