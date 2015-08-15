package Ic2ExpReactorPlanner;

/**
 * Represents a 360k NaK Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell360kNak extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.360k_NaK_Coolantcell.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(2, CoolantCell180kNak.MATERIALS, 6, "Tin", 9, "Copper");
    
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
        String result = "360k NaK Coolant Cell";
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
