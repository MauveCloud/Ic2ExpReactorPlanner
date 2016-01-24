package Ic2ExpReactorPlanner;

import static Ic2ExpReactorPlanner.S._;

/**
 * Represents an advanced heat vent.
 * @author Brian McCloud
 */
public class AdvancedHeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVentDiamond.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(2, HeatVent.MATERIALS, "Diamond", 4.5, "Iron");
    
    /**
     * Creates a new instance.
     */
    public AdvancedHeatVent() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(1000);
        automationThreshold = 900;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        String result = _("Advanced Heat Vent");
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
    public void dissipate() {
        final double currentDissipation = Math.min(12, getCurrentHeat());
        getParent().ventHeat(currentDissipation);
        adjustCurrentHeat(-currentDissipation);
        effectiveVentCooling = Math.max(effectiveVentCooling, currentDissipation);
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }

    @Override
    public double getVentCoolingCapacity() {
        return 12;
    }

}
