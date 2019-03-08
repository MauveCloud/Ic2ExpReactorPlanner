package Ic2ExpReactorPlanner;

/**
 * Represents a neutron reflector.
 * @author Brian McCloud
 */
public class NeutronReflector extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorReflector.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("COPPER"), 4, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("TIN"), 4, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("COAL"));
    
    /**
     * Creates a new instance.
     */
    public NeutronReflector() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(10000);
        automationThreshold = 11000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("NEUTRON REFLECTOR");
    }

    @Override
    public boolean isNeutronReflector() {
        return !isBroken();
    }

    @Override
    public double generateHeat() {
        final Reactor parentReactor = getParent();
        ReactorComponent component = parentReactor.getComponentAt(getRow() + 1, getColumn());
        if (component != null && component.isNeutronReflector()) {
            handleFuelRodDamage(component);
        }
        component = parentReactor.getComponentAt(getRow() - 1, getColumn());
        if (component != null && component.isNeutronReflector()) {
            handleFuelRodDamage(component);
        }
        component = parentReactor.getComponentAt(getRow(), getColumn() - 1);
        if (component != null && component.isNeutronReflector()) {
            handleFuelRodDamage(component);
        }
        component = parentReactor.getComponentAt(getRow(), getColumn() + 1);
        if (component != null && component.isNeutronReflector()) {
            handleFuelRodDamage(component);
        }
        return 0.0;
    }

    private void handleFuelRodDamage(ReactorComponent component) {
        if (component instanceof FuelRodUranium || component instanceof FuelRodMox) {
            applyDamage(1.0);
        } else if (component instanceof DualFuelRodUranium || component instanceof DualFuelRodMox) {
            applyDamage(2.0);
        } else if (component instanceof QuadFuelRodUranium || component instanceof QuadFuelRodMox) {
            applyDamage(4.0);
        }
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
    @Override
    public double getExplosionPowerOffset() {
        if (!isBroken()) {
            return -1;
        }
        return 0;
    }
    
}
