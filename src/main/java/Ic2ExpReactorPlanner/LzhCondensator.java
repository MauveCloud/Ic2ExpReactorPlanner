package Ic2ExpReactorPlanner;

import static Ic2ExpReactorPlanner.S._;

/**
 * Represents an LZH-Condensator.
 * @author Brian McCloud
 */

public class LzhCondensator extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorCondensatorLap.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(2, RshCondensator.MATERIALS, ReactorHeatVent.MATERIALS, ReactorHeatExchanger.MATERIALS, 9, "Lapis Lazuli", 4, "Redstone");
    
    /**
     * Creates a new instance.
     */
    public LzhCondensator() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(100000);
        automationThreshold = 110000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        String result = _("LZH-Condensator");
        if (getInitialHeat() > 0) {
            result += String.format(_(" (initial heat: %,d)"), (int)getInitialHeat());
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
