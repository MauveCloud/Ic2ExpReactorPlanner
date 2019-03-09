package Ic2ExpReactorPlanner;

/**
 * Represents a 60k Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell60k extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorCoolantSix.png";     //NOI18N

    public static final MaterialsList MATERIALS = new MaterialsList(2, CoolantCell30k.MATERIALS, BUNDLE.getString("MaterialName.Iron"), 6, BUNDLE.getString("MaterialName.Tin"));
    
    /**
     * Creates a new instance.
     */
    public CoolantCell60k() {
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
