package Ic2ExpReactorPlanner;

import java.util.List;
import java.util.ArrayList;

import static Ic2ExpReactorPlanner.S._;

/**
 * Represents a single thorium fuel rod.
 * @author Brian McCloud
 */
public class FuelRodThorium extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final  String imageFilename = "gt.Thoriumcell.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(_("Iron"), _("Thorium"));
    
    /**
     * Creates a new instance.
     */
    public FuelRodThorium() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxDamage(50000.0);
        automationThreshold = 51000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return _("Fuel Rod (Thorium)");
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
    public double generateHeat() {
        int pulses = countNeutronNeighbors() + 1;
        int heat = pulses * (pulses + 1) / 2;
        handleHeat(heat);
        applyDamage(1.0);
        return heat;
    }

    @Override
    public void generateEnergy() {
        int pulses = countNeutronNeighbors() + 1;
        final Reactor parentReactor = getParent();
        double energy = 20 * pulses;
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
        
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }

    @Override
    public int getRodCount() {
        return 1;
    }
    
}
