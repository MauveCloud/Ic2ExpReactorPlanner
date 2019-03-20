package Ic2ExpReactorPlanner;

/**
 * Represents an RSH-Condensator.
 * @author Brian McCloud
 */
public class RshCondensator extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorCondensator.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(HeatVent.MATERIALS, HeatExchanger.MATERIALS, 7, BUNDLE.getString("MaterialName.Redstone"));
    
    /**
     * Creates a new instance.
     */
    public RshCondensator() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(20000);
        setAutomationThreshold(21000);
    }
    
    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
    @Override
    public double adjustCurrentHeat(final double heat) {
        if (heat < 0.0) {
            return heat;
        }
        currentCondensatorCooling += heat;
        bestCondensatorCooling = Math.max(currentCondensatorCooling, bestCondensatorCooling);
        double acceptedHeat = Math.min(heat, getMaxHeat() - heat);
        double result = heat - acceptedHeat;
        currentHeat += acceptedHeat;
        maxReachedHeat = Math.max(maxReachedHeat, currentHeat);
        return result;
    }
    
    /**
     * Simulates having a coolant item added by a Reactor Coolant Injector.
     */
    public void injectCoolant() {
        currentHeat = 0;
    }
    
}
