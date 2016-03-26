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
    
    public static final MaterialsList MATERIALS = new MaterialsList(HeatVent.MATERIALS, HeatExchanger.MATERIALS, 7, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("REDSTONE"));
    
    /**
     * Creates a new instance.
     */
    public RshCondensator() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(20000);
        automationThreshold = 21000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        String result = java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("RSH-CONDENSATOR");
        if (getInitialHeat() > 0) {
            result += String.format(java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("INITIAL_HEAT_DISPLAY"), (int)getInitialHeat());
        }
        return result;
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
        return result;
    }
    
}
