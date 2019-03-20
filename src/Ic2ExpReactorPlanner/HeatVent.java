package Ic2ExpReactorPlanner;

/**
 * Represents a heat vent.
 * @author Brian McCloud
 */
public class HeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVent.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(8.5, BUNDLE.getString("MaterialName.Iron"), 1, BUNDLE.getString("MaterialName.Tin"), 16.0 / 3, BUNDLE.getString("MaterialName.Copper"));
    
    /**
     * Creates a new instance.
     */
    public HeatVent() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(1000);
        setAutomationThreshold(900);
    }
    
    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }

    @Override
    public void dissipate() {
        final double currentDissipation = Math.min(6, getCurrentHeat());
        currentOutput = currentDissipation;
        getParent().ventHeat(currentDissipation);
        adjustCurrentHeat(-currentDissipation);
        effectiveVentCooling = Math.max(effectiveVentCooling, currentDissipation);
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }

    @Override
    public double getVentCoolingCapacity() {
        return 6;
    }

}
