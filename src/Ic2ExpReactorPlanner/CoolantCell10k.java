package Ic2ExpReactorPlanner;

/**
 * Represents a 10k Coolant Cell.
 * @author Brian McCloud
 */
public class CoolantCell10k extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorCoolantSimple.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(5, BUNDLE.getString("MaterialName.Tin"), BUNDLE.getString("MaterialName.DistilledWater"), BUNDLE.getString("MaterialName.LapisLazuli"));
    
    /**
     * Creates a new instance.
     */
    public CoolantCell10k() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(10000);
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
