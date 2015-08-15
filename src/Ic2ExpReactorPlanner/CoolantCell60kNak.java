package Ic2ExpReactorPlanner;

/**
 * Represents a 60k NaK Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell60kNak extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.60k_NaK_Coolantcell.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(CoolantCell10k.MATERIALS, 2, "Sodium", 2, "Potassium", 4, "Tin");
    
    /**
     * Creates a new instance.
     */
    public CoolantCell60kNak() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(60000);
        automationThreshold = 54000;
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        String result = "60k NaK Coolant Cell";
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
