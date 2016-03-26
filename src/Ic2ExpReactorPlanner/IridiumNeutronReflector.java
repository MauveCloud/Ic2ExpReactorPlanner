package Ic2ExpReactorPlanner;

/**
 * Represents a thick neutron reflector.
 * @author Brian McCloud
 */
public class IridiumNeutronReflector extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.neutronreflector.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(8, ThickNeutronReflector.MATERIALS, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("IRIDIUM REINFORCED PLATE"));
    
    /**
     * Creates a new instance.
     */
    public IridiumNeutronReflector() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(40000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("IRIDIUM NEUTRON REFLECTOR");
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
    @Override
    public boolean isNeutronReflector() {
        return true;
    }
}
