package Ic2ExpReactorPlanner;

/**
 * Represents a 180k Helium Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell180kHelium extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "gt.180k_Helium_Coolantcell.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(3, CoolantCell60kHelium.MATERIALS, 6, BUNDLE.getString("MaterialName.Tin"));
    
    /**
     * Creates a new instance.
     */
    public CoolantCell180kHelium() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(180000);
        setAutomationThreshold(170000);
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
