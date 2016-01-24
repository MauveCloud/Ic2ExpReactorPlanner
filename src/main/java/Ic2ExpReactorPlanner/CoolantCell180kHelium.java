package Ic2ExpReactorPlanner;

import static Ic2ExpReactorPlanner.S._;

/**
 * Represents a 180k Helium Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell180kHelium extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.180k_Helium_Coolantcell.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(3, CoolantCell60kHelium.MATERIALS, 6, _("Tin"));
    
    /**
     * Creates a new instance.
     */
    public CoolantCell180kHelium() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(180000);
        automationThreshold = 170000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        String result = _("180k He Coolant Cell");
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
    public double adjustCurrentHeat(double heat) {
        currentCellCooling += heat;
        bestCellCooling = Math.max(currentCellCooling, bestCellCooling);
        return super.adjustCurrentHeat(heat);
    }
    
}
