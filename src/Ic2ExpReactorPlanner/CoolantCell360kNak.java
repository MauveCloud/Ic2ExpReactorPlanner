package Ic2ExpReactorPlanner;

/**
 * Represents a 360k NaK Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell360kNak extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.360k_NaK_Coolantcell.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(2, CoolantCell180kNak.MATERIALS, 6, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("TIN"), 9, java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("COPPER"));
    
    /**
     * Creates a new instance.
     */
    public CoolantCell360kNak() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(360000);
        automationThreshold = 350000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        String result = java.util.ResourceBundle.getBundle("Ic2ExpReactorPlanner/Bundle").getString("360K NAK COOLANT CELL");
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
