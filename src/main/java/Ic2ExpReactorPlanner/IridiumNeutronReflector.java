package Ic2ExpReactorPlanner;

import static Ic2ExpReactorPlanner.S._;

/**
 * Represents a thick neutron reflector.
 * @author Brian McCloud
 */
public class IridiumNeutronReflector extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.neutronreflector.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(8, ThickNeutronReflector.MATERIALS, _("Iridium Reinforced Plate"));
    
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
        return _("Iridium Neutron Reflector");
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
