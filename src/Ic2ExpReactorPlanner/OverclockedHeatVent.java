package Ic2ExpReactorPlanner;

/**
 * Represents an overclocked heat vent.
 * @author Brian McCloud
 */
public class OverclockedHeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVentGold.png";     //NOI18N
    
    public static final MaterialsList MATERIALS = new MaterialsList(ReactorHeatVent.MATERIALS, 4, BUNDLE.getString("MaterialName.Gold"));
    
    /**
     * Creates a new instance.
     */
    public OverclockedHeatVent() {
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
        double deltaHeat = Math.min(36, parentReactor.getCurrentHeat());
        parentReactor.adjustCurrentHeat(-deltaHeat);
        this.adjustCurrentHeat(deltaHeat);
        final double currentDissipation = Math.min(20, getCurrentHeat());
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
        return 20;
    }

}
