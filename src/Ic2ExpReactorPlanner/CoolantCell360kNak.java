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

    public static final MaterialsList MATERIALS = new MaterialsList(2, CoolantCell180kNak.MATERIALS, 6, BUNDLE.getString("MaterialName.Tin"), 9, BUNDLE.getString("MaterialName.Copper"));
    
    /**
     * Creates a new instance.
     */
    public CoolantCell360kNak() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(360000);
        automationThreshold = 350000;
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
