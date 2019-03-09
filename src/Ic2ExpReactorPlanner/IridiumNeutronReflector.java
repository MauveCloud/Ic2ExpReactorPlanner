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
    
    public static final MaterialsList MATERIALS = new MaterialsList(6, ThickNeutronReflector.MATERIALS, 
            18, BUNDLE.getString("MaterialName.Copper"),
            4, BUNDLE.getString("MaterialName.Iridium"),
            4, BUNDLE.getString("MaterialName.AdvancedAlloy"),
            BUNDLE.getString("MaterialName.Diamond"));
    
    /**
     * Creates a new instance.
     */
    public IridiumNeutronReflector() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(40000);
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
    @Override
    public boolean isNeutronReflector() {
        return true;
    }

    public double getExplosionPowerOffset() {
        return -1;
    }
    
}
