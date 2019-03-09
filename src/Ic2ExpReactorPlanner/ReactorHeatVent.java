package Ic2ExpReactorPlanner;

/**
 * Represents a reactor heat vent.
 * @author Brian McCloud
 */
public class ReactorHeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVentCore.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(HeatVent.MATERIALS, 8, BUNDLE.getString("MaterialName.Copper"));
    
    /**
     * Creates a new instance.
     */
    public ReactorHeatVent() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(1000);
        automationThreshold = 900;
    }
    
    @Override
    public boolean isHeatAcceptor() {
        return !isBroken();
    }

    @Override
    public void dissipate() {
        Reactor parentReactor = getParent();
        double deltaHeat = Math.min(5, parentReactor.getCurrentHeat());
        parentReactor.adjustCurrentHeat(-deltaHeat);
        this.adjustCurrentHeat(deltaHeat);
        final double currentDissipation = Math.min(5, getCurrentHeat());
        parentReactor.ventHeat(currentDissipation);
        adjustCurrentHeat(-currentDissipation);
        effectiveVentCooling = Math.max(effectiveVentCooling, currentDissipation);
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }

    @Override
    public double getVentCoolingCapacity() {
        return 5;
    }

}
