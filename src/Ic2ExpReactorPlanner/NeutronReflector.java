package Ic2ExpReactorPlanner;

/**
 * Represents a neutron reflector.
 * @author Brian McCloud
 */
public class NeutronReflector extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorReflector.png";    
    
    /**
     * Creates a new instance.
     */
    public NeutronReflector() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(10000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Neutron Reflector";
    }

    @Override
    public boolean isNeutronReflector() {
        return !isBroken();
    }

    @Override
    public void generateHeat() {
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
    
}
