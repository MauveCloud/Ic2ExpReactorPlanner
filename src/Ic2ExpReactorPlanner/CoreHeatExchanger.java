package Ic2ExpReactorPlanner;

/**
 * Represents a core heat exchanger.
 * @author Brian McCloud
 */
public class CoreHeatExchanger extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private final static String imageFilename = "reactorHeatSwitchCore.png";    
    
    /**
     * Creates a new instance.
     */
    public CoreHeatExchanger() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(5000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Core Heat Exchanger";
    }

    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }
    
    @Override
    public void transfer() {
        final Reactor parentReactor = getParent();
        double targetHeatRatio = (getCurrentHeat() + parentReactor.getCurrentHeat()) / (getMaxHeat() + parentReactor.getMaxHeat());
        double reactorTargetHeat = targetHeatRatio * parentReactor.getMaxHeat();
        double deltaHeat = Math.min(Math.max(-72.0, Math.min(72.0, reactorTargetHeat - parentReactor.getCurrentHeat())), getCurrentHeat());
        parentReactor.adjustCurrentHeat(deltaHeat);
        this.adjustCurrentHeat(-deltaHeat);
    }
    
}
