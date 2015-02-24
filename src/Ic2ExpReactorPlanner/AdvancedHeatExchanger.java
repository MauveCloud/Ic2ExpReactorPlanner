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
    private static final String imageFilename = "reactorHeatSwitchDiamond.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(2, HeatExchanger.MATERIALS, 2, MaterialsList.ELECTRONIC_CIRCUIT, "Copper Plate", 4, "Lapis Lazuli");
    
    private static final int switchSide = 24;
    private static final int switchReactor = 8;
    
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
        String result = "Advanced Heat Exchanger";
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
        int myHeat = 0;
        for (ReactorComponent heatableNeighbor : heatableNeighbors) {
            double mymed = getCurrentHeat() * 100.0 / getMaxHeat();
            double heatablemed = heatableNeighbor.getCurrentHeat() * 100.0 / heatableNeighbor.getMaxHeat();

            int add = (int) (heatableNeighbor.getMaxHeat() / 100.0 * (heatablemed + mymed / 2.0));
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
            double tempAdd = Math.max(-heatableNeighbor.getCurrentHeat(), add);
            myHeat -= tempAdd;
            heatableNeighbor.adjustCurrentHeat(tempAdd);
        }
        double mymed = getCurrentHeat() * 100.0 / getMaxHeat();
        double Reactormed = parentReactor.getCurrentHeat() * 100.0 / parentReactor.getMaxHeat();

        int add = (int) Math.round(parentReactor.getMaxHeat() / 100.0 * (Reactormed + mymed / 2.0));
        if (add > switchReactor) {
            add = switchReactor;
        }
        if (Reactormed + mymed / 2.0 < 1.0) {
            add = switchSide / 2;
        }
        if (Reactormed + mymed / 2.0 < 0.75) {
            add = switchSide / 4;
        }
        if (Reactormed + mymed / 2.0 < 0.5) {
            add = switchSide / 8;
        }
        if (Reactormed + mymed / 2.0 < 0.25) {
            add = 1;
        }
        if (Math.round(Reactormed * 10.0) / 10.0 > Math.round(mymed * 10.0) / 10.0) {
            add -= 2 * add;
        } else if (Math.round(Reactormed * 10.0) / 10.0 == Math.round(mymed * 10.0) / 10.0) {
            add = 0;
        }
        myHeat -= add;
        parentReactor.adjustCurrentHeat(add);
        adjustCurrentHeat(myHeat);
    }

    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
}
