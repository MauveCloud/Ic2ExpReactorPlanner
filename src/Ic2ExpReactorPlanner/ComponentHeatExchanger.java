package Ic2ExpReactorPlanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a heat exchanger.
 * @author Brian McCloud
 */
public class ComponentHeatExchanger extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorHeatSwitchSpread.png";    
    
    /**
     * Creates a new instance.
     */
    public ComponentHeatExchanger() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(5000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Component Heat Exchanger";
    }

    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }
    
    @Override
    public void transfer() {
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
        for (ReactorComponent heatableNeighbor : heatableNeighbors) {
            double targetHeatRatio = (getCurrentHeat() + heatableNeighbor.getCurrentHeat()) / (getMaxHeat() + heatableNeighbor.getMaxHeat());
            double neighborTargetHeat = targetHeatRatio * heatableNeighbor.getMaxHeat();
            double deltaHeat = Math.min(Math.max(-36.0, Math.min(36.0, neighborTargetHeat - heatableNeighbor.getCurrentHeat())), getCurrentHeat());
            heatableNeighbor.adjustCurrentHeat(deltaHeat);
            this.adjustCurrentHeat(-deltaHeat);
        }
    }
    
}
