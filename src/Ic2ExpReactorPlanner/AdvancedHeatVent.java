package Ic2ExpReactorPlanner;

/**
 * Represents an advanced heat vent.
 * @author Brian McCloud
 */
public class AdvancedHeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVentDiamond.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(2, HeatVent.MATERIALS, BUNDLE.getString("MaterialName.Diamond"), 2.25, BUNDLE.getString("MaterialName.Iron"));
    
    /**
     * Creates a new instance.
     */
    public AdvancedHeatVent() {
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
        final double currentDissipation = Math.min(12, getCurrentHeat());
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
        return 12;
    }

}
