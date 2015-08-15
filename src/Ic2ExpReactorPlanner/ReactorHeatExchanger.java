package Ic2ExpReactorPlanner;

/**
 * Represents a reactor heat exchanger.
 * @author Brian McCloud
 */
public class ReactorHeatExchanger extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorHeatSwitchCore.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(HeatExchanger.MATERIALS, 8, "Copper");
    
    private static final int switchSide = 0;
    private static final int switchReactor = 72;
    
    /**
     * Creates a new instance.
     */
    public ReactorHeatExchanger() {
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
        String result = "Reactor Heat Exchanger";
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
        // Code adapted from decompiled IC2 code, class ItemReactorHeatSwitch, with permission from Thunderdark.
        int myHeat = 0;
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
    
}
