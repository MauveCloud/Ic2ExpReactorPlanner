package Ic2ExpReactorPlanner;

/**
 * Represents a reactor heat vent.
 * @author Brian McCloud
 */
public class ReactorHeatVent extends ReactorComponent {
    
    /**
     * The filename for the image to show for the component.
     */
    private static final String imageFilename = "reactorVentCore.png";    
    
    public static final MaterialsList MATERIALS = new MaterialsList(HeatVent.MATERIALS, 8, "Copper Plate");
    
    /**
     * Creates a new instance.
     */
    public ReactorHeatVent() {
        setImage(TextureFactory.getImage(imageFilename));
        setMaxHeat(1000);
    }
    
    /**
     * Gets the name of the component.
     * @return the name of this component.
     */
    @Override
    public String toString() {
        return "Reactor Heat Vent";
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
        parentReactor.ventHeat(Math.min(5, getCurrentHeat()));
        adjustCurrentHeat(-Math.min(5, getCurrentHeat()));
    }
    
    @Override
    public MaterialsList getMaterials() {
        return MATERIALS;
    }
    
}
