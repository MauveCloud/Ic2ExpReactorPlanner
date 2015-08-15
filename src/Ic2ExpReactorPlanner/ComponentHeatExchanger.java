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
    private static final String imageFilename = "reactorHeatSwitchSpread.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(HeatExchanger.MATERIALS, 4, "Gold");
    
    private static final int switchSide = 36;
    private static final int switchReactor = 0;
    
    /**
     * Creates a new instance.
     */
    public ComponentHeatExchanger() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(5000);
        automationThreshold = 4500;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        String result = "Component Heat Exchanger";
        if (getInitialHeat() > 0) {
            result += String.format(" (initial heat: %,d)", (int)getInitialHeat());
        }
        return result;
    }

    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }
    
    @Override
    public void transfer() {
        final Reactor parentReactor = getParent();
        List<ReactorComponent> heatableNeighbors = new ArrayList<>(4);
        ReactorComponent component = parentReactor.getComponentAt(getRow(), getColumn() - 1);
        if (component != null && component.isHeatAcceptor()) {
            heatableNeighbors.add(component);
        }
        component = parentReactor.getComponentAt(getRow(), getColumn() + 1);
        if (component != null && component.isHeatAcceptor()) {
            heatableNeighbors.add(component);
        }
        component = parentReactor.getComponentAt(getRow() - 1, getColumn());
        if (component != null && component.isHeatAcceptor()) {
            heatableNeighbors.add(component);
        }
        component = parentReactor.getComponentAt(getRow() + 1, getColumn());
        if (component != null && component.isHeatAcceptor()) {
            heatableNeighbors.add(component);
        }
        // Code adapted from decompiled IC2 code, class ItemReactorHeatSwitch, with permission from Thunderdark.
        double myHeat = 0.0;
        for (ReactorComponent heatableNeighbor : heatableNeighbors) {
            double mymed = getCurrentHeat() * 100.0 / getMaxHeat();
            double heatablemed = heatableNeighbor.getCurrentHeat() * 100.0 / heatableNeighbor.getMaxHeat();

            double add = (int) (heatableNeighbor.getMaxHeat() / 100.0 * (heatablemed + mymed / 2.0));
            if (add > switchSide) {
                add = switchSide;
            }
            if (heatablemed + mymed / 2.0 < 1.0) {
                add = switchSide / 2;
            }
            if (heatablemed + mymed / 2.0 < 0.75) {
                add = switchSide / 4;
            }
            if (heatablemed + mymed / 2.0 < 0.5) {
                add = switchSide / 8;
            }
            if (heatablemed + mymed / 2.0 < 0.25) {
                add = 1;
            }
            if (Math.round(heatablemed * 10.0) / 10.0 > Math.round(mymed * 10.0) / 10.0) {
                add -= 2 * add;
            } else if (Math.round(heatablemed * 10.0) / 10.0 == Math.round(mymed * 10.0) / 10.0) {
                add = 0;
            }
            myHeat -= add;
            add = heatableNeighbor.adjustCurrentHeat(add);
            myHeat += add;
        }
        adjustCurrentHeat(myHeat);
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
}
