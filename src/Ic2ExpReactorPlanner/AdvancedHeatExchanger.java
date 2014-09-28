package Ic2ExpReactorPlanner;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an advanced heat exchanger.
 * @author Brian McCloud
 */
public class AdvancedHeatExchanger extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorHeatSwitchDiamond.png";    
    
    /**
     * Creates a new instance.
     */
    public AdvancedHeatExchanger() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(10000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Advanced Heat Exchanger";
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
            double deltaHeat = Math.min(Math.max(-24.0, Math.min(24.0, neighborTargetHeat - heatableNeighbor.getCurrentHeat())), getCurrentHeat());
            heatableNeighbor.adjustCurrentHeat(deltaHeat);
            this.adjustCurrentHeat(-deltaHeat);
        }
        double targetHeatRatio = (getCurrentHeat() + parentReactor.getCurrentHeat()) / (getMaxHeat() + parentReactor.getMaxHeat());
        double reactorTargetHeat = targetHeatRatio * parentReactor.getMaxHeat();
        double deltaHeat = Math.min(Math.max(-8.0, Math.min(8.0, reactorTargetHeat - parentReactor.getCurrentHeat())), getCurrentHeat());
        parentReactor.adjustCurrentHeat(deltaHeat);
        this.adjustCurrentHeat(-deltaHeat);
    }
    
}
