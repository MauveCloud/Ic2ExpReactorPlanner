package Ic2ExpReactorPlanner;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a single uranium fuel rod.
 * @author Brian McCloud
 */
public class FuelRodUranium extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorUraniumSimple.png";    
    
    /**
     * Creates a new instance.
     */
    public FuelRodUranium() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(20000.0);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Fuel Rod (Uranium)";
    }

    @Override
    public boolean isNeutronReflector() {
        return !isBroken();
    }

    protected int countNeutronNeighbors() {
        int neutronNeighbors = 0;
        final Reactor parentReactor = getParent();
        ReactorComponent component = parentReactor.getComponentAt(getRow() + 1, getColumn());
        if (component != null && component.isNeutronReflector()) {
            neutronNeighbors++;
        }
        component = parentReactor.getComponentAt(getRow() - 1, getColumn());
        if (component != null && component.isNeutronReflector()) {
            neutronNeighbors++;
        }
        component = parentReactor.getComponentAt(getRow(), getColumn() - 1);
        if (component != null && component.isNeutronReflector()) {
            neutronNeighbors++;
        }
        component = parentReactor.getComponentAt(getRow(), getColumn() + 1);
        if (component != null && component.isNeutronReflector()) {
            neutronNeighbors++;
        }
        return neutronNeighbors;
    }
    
    @Override
    public void generateHeat() {
        int pulses = countNeutronNeighbors() + 1;
        int heat = 2 * pulses * (pulses + 1);
        handleHeat(heat);
        applyDamage(1.0);
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 1;
        final Reactor parentReactor = getParent();
        double energy = 100 * pulses;
        parentReactor.addEUOutput(energy);
    }

    protected void handleHeat(final int heat) {
        final Reactor parentReactor = getParent();
        List<ReactorComponent> heatableNeighbors = new ArrayList<>(4);
        ReactorComponent component = parentReactor.getComponentAt(getRow() + 1, getColumn());
        if (component != null && component.isHeatAcceptor()) {
            heatableNeighbors.add(component);
        }
        component = parentReactor.getComponentAt(getRow() - 1, getColumn());
        if (component != null && component.isHeatAcceptor()) {
            heatableNeighbors.add(component);
        }
        component = parentReactor.getComponentAt(getRow(), getColumn() - 1);
        if (component != null && component.isHeatAcceptor()) {
            heatableNeighbors.add(component);
        }
        component = parentReactor.getComponentAt(getRow(), getColumn() + 1);
        if (component != null && component.isHeatAcceptor()) {
            heatableNeighbors.add(component);
        }
        if (heatableNeighbors.isEmpty()) {
            parentReactor.adjustCurrentHeat(heat);
        } else {
            for (ReactorComponent heatableNeighbor : heatableNeighbors) {
                heatableNeighbor.adjustCurrentHeat(heat / heatableNeighbors.size());
            }
            int remainderHeat = heat % heatableNeighbors.size();
            heatableNeighbors.get(0).adjustCurrentHeat(remainderHeat);
        }
    }
        
}
