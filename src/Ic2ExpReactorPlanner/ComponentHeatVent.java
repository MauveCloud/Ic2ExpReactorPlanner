package Ic2ExpReactorPlanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a component heat vent.
 * @author Brian McCloud
 */
public class ComponentHeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVentSpread.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(HeatVent.MATERIALS, 4, "Tin Plate", 4, "Iron Bars");
    
    /**
     * Creates a new instance.
     */
    public ComponentHeatVent() {
        setImage(TextureFactory.getImage(imageFilename));
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Component Heat Vent";
    }

    @Override
    public void dissipate() {
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
        double dissipatedHeat = 0.0;
        for (ReactorComponent heatableNeighbor : heatableNeighbors) {
            double rejectedCooling = heatableNeighbor.adjustCurrentHeat(-4.0);
            double tempDissipatedHeat = 4 + rejectedCooling;
            parentReactor.ventHeat(tempDissipatedHeat);
            dissipatedHeat += tempDissipatedHeat;
        }
        effectiveVentCooling = Math.max(effectiveVentCooling, dissipatedHeat);
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }

    @Override
    public double getVentCoolingCapacity() {
        double result = 0.0;
        final Reactor parentReactor = getParent();
        ReactorComponent component = parentReactor.getComponentAt(getRow() + 1, getColumn());
        if (component != null && component.isHeatAcceptor() && !(component instanceof RshCondensator) && !(component instanceof LzhCondensator)) {
            result += 4.0;
        }
        component = parentReactor.getComponentAt(getRow() - 1, getColumn());
        if (component != null && component.isHeatAcceptor() && !(component instanceof RshCondensator) && !(component instanceof LzhCondensator)) {
            result += 4.0;
        }
        component = parentReactor.getComponentAt(getRow(), getColumn() - 1);
        if (component != null && component.isHeatAcceptor() && !(component instanceof RshCondensator) && !(component instanceof LzhCondensator)) {
            result += 4.0;
        }
        component = parentReactor.getComponentAt(getRow(), getColumn() + 1);
        if (component != null && component.isHeatAcceptor() && !(component instanceof RshCondensator) && !(component instanceof LzhCondensator)) {
            result += 4.0;
        }
        return result;
    }
    
}
