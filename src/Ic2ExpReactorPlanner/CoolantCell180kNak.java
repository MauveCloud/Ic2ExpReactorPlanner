package Ic2ExpReactorPlanner;

/**
 * Represents a 180k NaK Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell180kNak extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.180k_NaK_Coolantcell.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(3, CoolantCell60kNak.MATERIALS, 6, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("TIN"));
    
    /**
     * Creates a new instance.
     */
    public CoolantCell180kNak() {
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
        String result = java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("180K NAK COOLANT CELL");
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
    public double adjustCurrentHeat(double heat) {
        currentCellCooling += heat;
        bestCellCooling = Math.max(currentCellCooling, bestCellCooling);
        return super.adjustCurrentHeat(heat);
    }
    
}
