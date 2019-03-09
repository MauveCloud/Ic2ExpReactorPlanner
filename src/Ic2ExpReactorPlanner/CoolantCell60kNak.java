package Ic2ExpReactorPlanner;

/**
 * Represents a 60k NaK Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell60kNak extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.60k_NaK_Coolantcell.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(CoolantCell10k.MATERIALS, 2, BUNDLE.getString("MaterialName.Sodium"), 2, BUNDLE.getString("MaterialName.Potassium"), 4, BUNDLE.getString("MaterialName.Tin"));
    
    /**
     * Creates a new instance.
     */
    public CoolantCell60kNak() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(60000);
        automationThreshold = 54000;
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
