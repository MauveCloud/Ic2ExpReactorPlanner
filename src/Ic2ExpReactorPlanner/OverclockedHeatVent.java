package Ic2ExpReactorPlanner;

/**
 * Represents an overclocked heat vent.
 * @author Brian McCloud
 */
public class OverclockedHeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVentGold.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(ReactorHeatVent.MATERIALS, 4, "Gold Plate");
    
    /**
     * Creates a new instance.
     */
    public OverclockedHeatVent() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(1000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Overclocked Heat Vent";
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
        parentReactor.ventHeat(Math.min(20, getCurrentHeat()));
        adjustCurrentHeat(-Math.min(20, getCurrentHeat()));
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
}
