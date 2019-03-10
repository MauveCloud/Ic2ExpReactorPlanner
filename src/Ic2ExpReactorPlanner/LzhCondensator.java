package Ic2ExpReactorPlanner;

/**
 * Represents an LZH-Condensator.
 * @author Brian McCloud
 */
public class LzhCondensator extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorCondensatorLap.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(2, RshCondensator.MATERIALS, ReactorHeatVent.MATERIALS, ReactorHeatExchanger.MATERIALS, 
            9, BUNDLE.getString("MaterialName.LapisLazuli"), 4, BUNDLE.getString("MaterialName.Redstone"));
    
    /**
     * Creates a new instance.
     */
    public LzhCondensator() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(100000);
        automationThreshold = 110000;
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
