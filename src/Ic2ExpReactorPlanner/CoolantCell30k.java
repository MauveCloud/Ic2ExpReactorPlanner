package Ic2ExpReactorPlanner;

/**
 * Represents a 30k Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell30k extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorCoolantTriple.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(3, CoolantCell10k.MATERIALS, 6, BUNDLE.getString("MaterialName.Tin"));
    
    /**
     * Creates a new instance.
     */
    public CoolantCell30k() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(30000);
        setAutomationThreshold(27000);
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
